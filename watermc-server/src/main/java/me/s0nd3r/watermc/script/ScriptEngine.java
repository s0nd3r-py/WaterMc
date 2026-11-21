package me.s0nd3r.watermc.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jspecify.annotations.Nullable;

/**
 * Встроенный скриптовый движок WaterMc.
 *
 * <p>Скрипты ({@code scripts/*.wsc}) разбираются и компилируются целиком при загрузке; ошибка в одном
 * файле не мешает остальным. Каждый запуск обработчика изолирован: ошибка или превышение лимита
 * операций прерывает только его и выводится в консоль с файлом и строкой (не чаще раза в 10 секунд
 * на одно место). Ожидания ({@code wait}) не блокируют сервер.</p>
 */
public final class ScriptEngine {

    public static final long DEFAULT_STEP_LIMIT = 200_000;
    private static final long ERROR_COOLDOWN_NANOS = 10_000_000_000L;
    private static final long GLOBALS_FLUSH_TICKS = 20L * 60;

    /** Команда, которую должен зарегистрировать сервер. */
    public record CommandBinding(String name, List<String> aliases, @Nullable String permission, @Nullable String permissionMessage,
                                 String description, String usage, String file, List<String> params) {
    }

    public record LoadResult(int filesLoaded, int filesFailed, List<String> errors, List<CommandBinding> commands, Set<String> events) {
    }

    private final ScriptHost host;
    private final GlobalStore globals;
    private final long stepLimit;
    private final Map<String, Long> lastErrors = new ConcurrentHashMap<>();
    private final Set<ScriptHost.Task> pending = ConcurrentHashMap.newKeySet();
    private final List<ScriptHost.Task> timers = new CopyOnWriteArrayList<>();

    private volatile ScriptRuntime runtime;
    private volatile Map<String, List<Compiler.Handler>> handlers = Map.of();
    private volatile Map<String, Compiler.CompiledCommand> commands = Map.of();
    private volatile int generation;

    public ScriptEngine(final ScriptHost host, final GlobalStore globals, final long stepLimit) {
        this.host = host;
        this.globals = globals;
        this.stepLimit = stepLimit;
        this.runtime = new ScriptRuntime(host, globals, stepLimit);
    }

    public GlobalStore globals() {
        return this.globals;
    }

    /**
     * Компилирует и запускает скрипты. Ключ — имя файла (для сообщений об ошибках), значение — исходный текст.
     * Предыдущий набор должен быть выгружен {@link #unload()}.
     */
    public synchronized LoadResult load(final Map<String, String> sources) {
        final ScriptRuntime rt = new ScriptRuntime(this.host, this.globals, this.stepLimit);
        final List<String> errors = new ArrayList<>();
        final Map<String, Ast.ScriptFile> parsed = new TreeMap<>();
        final Map<String, List<Compiler.CompiledFunction>> functionsByFile = new HashMap<>();

        for (final Map.Entry<String, String> source : new TreeMap<>(sources).entrySet()) {
            try {
                final Ast.ScriptFile file = Parser.parse(source.getKey(), source.getValue());
                functionsByFile.put(source.getKey(), Compiler.declareFunctions(file, rt.functions));
                parsed.put(source.getKey(), file);
            } catch (final ScriptException ex) {
                errors.add(ex.describe());
            }
        }

        final Map<String, List<Compiler.Handler>> newHandlers = new HashMap<>();
        final Map<String, Compiler.CompiledCommand> newCommands = new HashMap<>();
        final Map<String, Compiler.CompiledCommand> claimedNames = new HashMap<>(); // имена и алиасы
        final List<Compiler.Timer> newTimers = new ArrayList<>();
        int loaded = 0;
        for (final Map.Entry<String, Ast.ScriptFile> entry : parsed.entrySet()) {
            final List<Compiler.CompiledFunction> own = functionsByFile.get(entry.getKey());
            try {
                final Compiler.Unit unit = new Compiler(rt, entry.getKey()).compile(entry.getValue(), own);
                for (final Compiler.CompiledCommand command : unit.commands()) {
                    for (final String name : concat(command.name(), command.aliases())) {
                        final Compiler.CompiledCommand clash = claimedNames.get(name);
                        if (clash != null) {
                            throw new ScriptException(command.file(), command.line(), "команда /" + name + " уже объявлена в " + clash.file() + ":" + clash.line());
                        }
                    }
                }
                for (final Compiler.CompiledCommand command : unit.commands()) {
                    newCommands.put(command.name(), command);
                    for (final String name : concat(command.name(), command.aliases())) {
                        claimedNames.put(name, command);
                    }
                }
                for (final Compiler.EventHandler handler : unit.events()) {
                    newHandlers.computeIfAbsent(handler.event(), e -> new ArrayList<>()).add(handler.handler());
                }
                newTimers.addAll(unit.timers());
                loaded++;
            } catch (final ScriptException ex) {
                errors.add(ex.describe());
                // функции файла с ошибкой остаются объявленными, но без тела: вызов сообщит об ошибке
                own.forEach(f -> f.body = null);
            }
        }

        this.runtime = rt;
        this.handlers = Collections.unmodifiableMap(newHandlers);
        this.commands = Collections.unmodifiableMap(newCommands);
        final int gen = ++this.generation;

        for (final Compiler.Timer timer : newTimers) {
            this.timers.add(this.host.scheduleRepeating(timer.periodTicks(), () -> {
                if (this.generation == gen) {
                    this.start(timer.handler(), null, null);
                }
            }));
        }
        this.timers.add(this.host.scheduleRepeating(GLOBALS_FLUSH_TICKS, this::flushGlobals));
        this.dispatch("load", new Object[0], null);

        final List<CommandBinding> bindings = new ArrayList<>();
        for (final Compiler.CompiledCommand c : newCommands.values()) {
            bindings.add(new CommandBinding(c.name(), c.aliases(), c.permission(), c.permissionMessage(), c.description(), c.usage(), c.file(), c.params()));
        }
        bindings.sort((a, b) -> a.name().compareTo(b.name()));
        return new LoadResult(loaded, sources.size() - loaded, errors, bindings, newHandlers.keySet());
    }

    private static List<String> concat(final String first, final List<String> rest) {
        final List<String> out = new ArrayList<>(rest.size() + 1);
        out.add(first);
        out.addAll(rest);
        return out;
    }

    /** Выгружает скрипты: {@code on unload}, отмена таймеров и ожиданий, сохранение $-переменных. */
    public synchronized void unload() {
        this.dispatch("unload", new Object[0], null);
        this.generation++;
        this.timers.forEach(ScriptHost.Task::cancel);
        this.timers.clear();
        this.pending.forEach(ScriptHost.Task::cancel);
        this.pending.clear();
        this.handlers = Map.of();
        this.commands = Map.of();
        this.flushGlobals();
    }

    public void flushGlobals() {
        try {
            this.globals.flush();
        } catch (final RuntimeException ex) {
            this.host.logError("[scripts] не удалось сохранить $-переменные: " + ex);
        }
    }

    public boolean hasHandlers(final String event) {
        return this.handlers.containsKey(event);
    }

    /**
     * Запускает обработчики события.
     *
     * @param values значения переменных события в порядке {@link EventSpec#variables()}
     * @param cancel отмена события или {@code null}, если событие отменить нельзя
     */
    public void dispatch(final String event, final Object[] values, final @Nullable Runnable cancel) {
        final List<Compiler.Handler> list = this.handlers.get(event);
        if (list == null) {
            return;
        }
        for (final Compiler.Handler handler : list) {
            this.start(handler, values, cancel);
        }
    }

    /** Выполняет команду скрипта. {@code player} — {@code null}, если отправитель — консоль. */
    public boolean runCommand(final String name, final Object sender, final @Nullable Object player, final List<String> args) {
        final Compiler.CompiledCommand command = this.commands.get(name);
        if (command == null) {
            return false;
        }
        final Object[] values = new Object[3 + command.params().size()];
        values[0] = sender;
        values[1] = player;
        values[2] = new ArrayList<Object>(args);
        for (int i = 0; i < command.params().size(); i++) {
            values[3 + i] = i < args.size() ? args.get(i) : null;
        }
        this.start(command.handler(), values, null);
        return true;
    }

    private void start(final Compiler.Handler handler, final Object @Nullable [] values, final @Nullable Runnable cancel) {
        final Exec.Execution ex = new Exec.Execution(this.runtime, handler.file(), handler.slotCount(), handler.body(),
            new Exec.Budget(this.stepLimit), 0, handler.defaultTargetSlot());
        if (values != null) {
            System.arraycopy(values, 0, ex.slots, 0, Math.min(values.length, ex.slots.length));
        }
        ex.cancelHandle = cancel;
        this.resume(ex, this.generation);
    }

    private void resume(final Exec.Execution ex, final int gen) {
        if (gen != this.generation) {
            return; // скрипты перезагружены, пока выполнение ждало
        }
        final long waitTicks;
        try {
            ex.budget.steps = 0;
            waitTicks = ex.run();
        } catch (final ScriptException error) {
            this.report(error);
            return;
        } catch (final RuntimeException unexpected) {
            this.report(new ScriptException(ex.file, 0, "внутренняя ошибка: " + unexpected));
            return;
        } catch (final StackOverflowError overflow) {
            this.report(new ScriptException(ex.file, 0, "переполнение стека (слишком глубокая рекурсия)"));
            return;
        }
        if (waitTicks > 0) {
            final ScriptHost.Task[] holder = new ScriptHost.Task[1];
            holder[0] = this.host.schedule(waitTicks, () -> {
                if (holder[0] != null) {
                    this.pending.remove(holder[0]);
                }
                this.resume(ex, gen);
            });
            this.pending.add(holder[0]);
        }
    }

    private void report(final ScriptException error) {
        final String key = error.file() + ":" + error.line() + ":" + error.getMessage();
        final long now = System.nanoTime();
        final Long last = this.lastErrors.get(key);
        if (last != null && now - last < ERROR_COOLDOWN_NANOS) {
            return;
        }
        this.lastErrors.put(key, now);
        if (this.lastErrors.size() > 1000) {
            this.lastErrors.clear();
        }
        this.host.logError("[scripts] " + error.describe());
    }

    /** Число ожидающих выполнений (для /watermc script status и тестов). */
    public int pendingWaits() {
        return this.pending.size();
    }

    /** Объявленные функции: имя → файл. */
    public Map<String, String> functions() {
        final Map<String, String> out = new TreeMap<>();
        this.runtime.functions.forEach((name, fn) -> out.put(name, fn.file));
        return out;
    }
}
