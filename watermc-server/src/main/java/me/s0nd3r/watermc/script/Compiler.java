package me.s0nd3r.watermc.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import me.s0nd3r.watermc.script.Exec.Eval;
import me.s0nd3r.watermc.script.Exec.Instr;

/**
 * Компилятор AST → исполняемые замыкания. Все имена разрешаются заранее: локальные переменные
 * становятся индексами в массиве, функции — прямыми ссылками. Ошибки (опечатка в имени переменной,
 * неизвестная функция, {@code wait} внутри функции, {@code cancel} у неотменяемого события)
 * обнаруживаются при загрузке скрипта, а не во время игры.
 */
final class Compiler {

    private static final Pattern COMMAND_NAME = Pattern.compile("[a-z0-9_\\-]{1,32}");

    static final class CompiledFunction {
        final String name;
        final String file;
        final int line;
        final List<String> params;
        volatile int slotCount;
        volatile Instr[] body;

        CompiledFunction(final String name, final String file, final int line, final List<String> params) {
            this.name = name;
            this.file = file;
            this.line = line;
            this.params = params;
        }
    }

    record Handler(String file, int line, int slotCount, Instr[] body, int defaultTargetSlot) {
    }

    record CompiledCommand(String file, int line, String name, List<String> aliases, String permission, String permissionMessage,
                           String description, String usage, List<String> params, Handler handler) {
    }

    record EventHandler(String event, Handler handler) {
    }

    record Timer(long periodTicks, Handler handler) {
    }

    record Unit(String file, List<EventHandler> events, List<CompiledCommand> commands, List<Timer> timers, List<CompiledFunction> functions) {
    }

    private static final class Ctx {
        final Map<String, Integer> slots = new HashMap<>();
        boolean inFunction;
        boolean allowWait = true;
        boolean allowCancel;
        String where;
        int loopDepth;

        int declare(final String name) {
            return this.slots.computeIfAbsent(name, n -> this.slots.size());
        }
    }

    private final ScriptRuntime runtime;
    private final String file;

    Compiler(final ScriptRuntime runtime, final String file) {
        this.runtime = runtime;
        this.file = file;
    }

    private ScriptException error(final int line, final String message) {
        return new ScriptException(this.file, line, message);
    }

    /** Регистрирует объявления функций файла (до компиляции тел, чтобы работали вызовы между файлами). */
    static List<CompiledFunction> declareFunctions(final Ast.ScriptFile file, final Map<String, CompiledFunction> table) {
        final List<CompiledFunction> declared = new ArrayList<>();
        for (final Ast.Decl decl : file.decls()) {
            if (decl instanceof Ast.FunctionDecl fn) {
                if (Builtins.FUNCTIONS.containsKey(fn.name())) {
                    throw new ScriptException(file.file(), fn.line(), "функция '" + fn.name() + "' уже встроена в язык — выберите другое имя");
                }
                final CompiledFunction existing = table.get(fn.name());
                if (existing != null) {
                    throw new ScriptException(file.file(), fn.line(), "функция '" + fn.name() + "' уже объявлена в " + existing.file + ":" + existing.line);
                }
                if (new LinkedHashSet<>(fn.params()).size() != fn.params().size()) {
                    throw new ScriptException(file.file(), fn.line(), "повторяющиеся имена параметров");
                }
                final CompiledFunction compiled = new CompiledFunction(fn.name(), file.file(), fn.line(), List.copyOf(fn.params()));
                table.put(fn.name(), compiled);
                declared.add(compiled);
            }
        }
        return declared;
    }

    Unit compile(final Ast.ScriptFile script, final List<CompiledFunction> ownFunctions) {
        final List<EventHandler> events = new ArrayList<>();
        final List<CompiledCommand> commands = new ArrayList<>();
        final List<Timer> timers = new ArrayList<>();
        for (final Ast.Decl decl : script.decls()) {
            if (decl instanceof Ast.OnEvent on) {
                final EventSpec spec = EventSpec.ALL.get(on.event());
                if (spec == null) {
                    throw this.error(on.line(), "неизвестное событие '" + on.event() + "'; доступны: " + String.join(", ", new java.util.TreeSet<>(EventSpec.ALL.keySet())));
                }
                final Ctx ctx = new Ctx();
                spec.variables().forEach(ctx::declare);
                ctx.allowWait = spec.waitAllowed();
                ctx.allowCancel = spec.cancellable();
                ctx.where = "on " + spec.name();
                final int target = ctx.slots.getOrDefault("player", -1);
                events.add(new EventHandler(spec.name(), this.handler(on.line(), on.body(), ctx, target)));
            } else if (decl instanceof Ast.CommandDecl cmd) {
                if (!COMMAND_NAME.matcher(cmd.name()).matches()) {
                    throw this.error(cmd.line(), "имя команды может содержать только латиницу, цифры, '_' и '-' (до 32 символов)");
                }
                final Ctx ctx = new Ctx();
                EventSpec.COMMAND_VARIABLES.forEach(ctx::declare);
                for (final String param : cmd.params()) {
                    if (ctx.slots.containsKey(param)) {
                        throw this.error(cmd.line(), "аргумент '" + param + "' совпадает со встроенной переменной команды");
                    }
                    ctx.declare(param);
                }
                ctx.where = "command /" + cmd.name();
                @SuppressWarnings("unchecked")
                final List<String> aliases = (List<String>) cmd.meta().getOrDefault("aliases", List.of());
                for (final String alias : aliases) {
                    if (!COMMAND_NAME.matcher(alias.toLowerCase(java.util.Locale.ROOT)).matches()) {
                        throw this.error(cmd.line(), "недопустимый алиас команды '" + alias + "'");
                    }
                }
                commands.add(new CompiledCommand(this.file, cmd.line(), cmd.name(), aliases.stream().map(a -> a.toLowerCase(java.util.Locale.ROOT)).toList(),
                    (String) cmd.meta().get("permission"), (String) cmd.meta().get("permission_message"),
                    (String) cmd.meta().getOrDefault("description", "Script command"),
                    (String) cmd.meta().getOrDefault("usage", "/" + cmd.name() + (cmd.params().isEmpty() ? "" : " <" + String.join("> <", cmd.params()) + ">")),
                    List.copyOf(cmd.params()), this.handler(cmd.line(), cmd.body(), ctx, ctx.slots.get("sender"))));
            } else if (decl instanceof Ast.Every every) {
                final Ctx ctx = new Ctx();
                ctx.where = "every";
                timers.add(new Timer(every.periodTicks(), this.handler(every.line(), every.body(), ctx, -1)));
            } else if (decl instanceof Ast.FunctionDecl fn) {
                final CompiledFunction compiled = ownFunctions.stream().filter(f -> f.name.equals(fn.name())).findFirst().orElseThrow();
                final Ctx ctx = new Ctx();
                fn.params().forEach(ctx::declare);
                ctx.inFunction = true;
                ctx.allowWait = false;
                ctx.where = "function " + fn.name();
                this.collectAssigned(fn.body(), ctx);
                final Instr[] body = this.block(fn.body(), ctx);
                compiled.slotCount = ctx.slots.size();
                compiled.body = body;
            }
        }
        return new Unit(this.file, events, commands, timers, ownFunctions);
    }

    private Handler handler(final int line, final List<Ast.Stmt> body, final Ctx ctx, final int defaultTarget) {
        this.collectAssigned(body, ctx);
        final Instr[] instrs = this.block(body, ctx);
        return new Handler(this.file, line, ctx.slots.size(), instrs, defaultTarget);
    }

    private void collectAssigned(final List<Ast.Stmt> body, final Ctx ctx) {
        for (final Ast.Stmt stmt : body) {
            if (stmt instanceof Ast.Assign assign && assign.target() instanceof Ast.Local local) {
                ctx.declare(local.name());
            } else if (stmt instanceof Ast.ForEach loop) {
                ctx.declare(loop.variable());
                this.collectAssigned(loop.body(), ctx);
            } else if (stmt instanceof Ast.If branch) {
                branch.blocks().forEach(b -> this.collectAssigned(b, ctx));
                if (branch.elseBlock() != null) {
                    this.collectAssigned(branch.elseBlock(), ctx);
                }
            } else if (stmt instanceof Ast.While loop) {
                this.collectAssigned(loop.body(), ctx);
            } else if (stmt instanceof Ast.Repeat loop) {
                this.collectAssigned(loop.body(), ctx);
            }
        }
    }

    // ---------- инструкции ----------

    private Instr[] block(final List<Ast.Stmt> body, final Ctx ctx) {
        final Instr[] out = new Instr[body.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = this.statement(body.get(i), ctx);
        }
        return out;
    }

    private Instr statement(final Ast.Stmt stmt, final Ctx ctx) {
        final int line = stmt.line();
        if (stmt instanceof Ast.Assign assign) {
            return new Exec.Run(line, this.assignment(assign, ctx));
        }
        if (stmt instanceof Ast.ExprStmt es) {
            final Eval eval = this.expr(es.expr(), ctx);
            return new Exec.Run(line, eval::eval);
        }
        if (stmt instanceof Ast.Send send) {
            final Eval message = this.expr(send.message(), ctx);
            final Eval target = send.target() == null ? null : this.expr(send.target(), ctx);
            return new Exec.Run(line, ex -> {
                final String text = Values.display(message.eval(ex));
                final Object to = target == null ? ex.defaultTarget() : target.eval(ex);
                deliver(to, text);
            });
        }
        if (stmt instanceof Ast.Broadcast b) {
            final Eval message = this.expr(b.message(), ctx);
            return new Exec.Run(line, ex -> ex.runtime.host().broadcast(Values.display(message.eval(ex))));
        }
        if (stmt instanceof Ast.Console c) {
            final Eval command = this.expr(c.command(), ctx);
            return new Exec.Run(line, ex -> {
                String cmd = Values.display(command.eval(ex)).trim();
                if (cmd.startsWith("/")) {
                    cmd = cmd.substring(1);
                }
                if (cmd.isEmpty()) {
                    throw new IllegalArgumentException("пустая консольная команда");
                }
                ex.runtime.host().dispatchConsoleCommand(cmd);
            });
        }
        if (stmt instanceof Ast.Log log) {
            final Eval message = this.expr(log.message(), ctx);
            final String prefix = "[" + this.file + ":" + line + "] ";
            return new Exec.Run(line, ex -> ex.runtime.host().logInfo(prefix + Values.display(message.eval(ex))));
        }
        if (stmt instanceof Ast.Wait wait) {
            if (!ctx.allowWait) {
                throw this.error(line, ctx.inFunction
                    ? "wait нельзя использовать внутри функции (функция возвращает значение сразу)"
                    : "wait нельзя использовать в " + ctx.where);
            }
            return new Exec.WaitI(line, this.expr(wait.amount(), ctx), wait.unitTicks());
        }
        if (stmt instanceof Ast.If branch) {
            final Eval[] conditions = new Eval[branch.conditions().size()];
            final Instr[][] blocks = new Instr[conditions.length][];
            for (int i = 0; i < conditions.length; i++) {
                conditions[i] = this.expr(branch.conditions().get(i), ctx);
                blocks[i] = this.block(branch.blocks().get(i), ctx);
            }
            return new Exec.IfI(line, conditions, blocks, branch.elseBlock() == null ? null : this.block(branch.elseBlock(), ctx));
        }
        if (stmt instanceof Ast.While loop) {
            final Eval cond = this.expr(loop.condition(), ctx);
            ctx.loopDepth++;
            final Instr[] body = this.block(loop.body(), ctx);
            ctx.loopDepth--;
            return new Exec.WhileI(line, cond, body);
        }
        if (stmt instanceof Ast.Repeat loop) {
            final Eval count = this.expr(loop.count(), ctx);
            ctx.loopDepth++;
            final Instr[] body = this.block(loop.body(), ctx);
            ctx.loopDepth--;
            return new Exec.RepeatI(line, count, body);
        }
        if (stmt instanceof Ast.ForEach loop) {
            final Eval iterable = this.expr(loop.iterable(), ctx);
            final int slot = ctx.declare(loop.variable());
            ctx.loopDepth++;
            final Instr[] body = this.block(loop.body(), ctx);
            ctx.loopDepth--;
            return new Exec.ForI(line, slot, iterable, body);
        }
        if (stmt instanceof Ast.Break) {
            if (ctx.loopDepth == 0) {
                throw this.error(line, "break вне цикла");
            }
            return new Exec.BreakI(line);
        }
        if (stmt instanceof Ast.Continue) {
            if (ctx.loopDepth == 0) {
                throw this.error(line, "continue вне цикла");
            }
            return new Exec.ContinueI(line);
        }
        if (stmt instanceof Ast.Stop) {
            return new Exec.StopI(line);
        }
        if (stmt instanceof Ast.Return ret) {
            if (!ctx.inFunction && ret.value() != null) {
                throw this.error(line, "return со значением допустим только в функции; для выхода из обработчика используйте stop");
            }
            return new Exec.ReturnI(line, ret.value() == null ? null : this.expr(ret.value(), ctx));
        }
        if (stmt instanceof Ast.Cancel) {
            if (!ctx.allowCancel) {
                throw this.error(line, "cancel недоступен в " + ctx.where + (ctx.inFunction ? " (отменять событие можно только в обработчике)" : ""));
            }
            return new Exec.Run(line, ex -> ex.cancelEvent(line));
        }
        throw this.error(line, "неподдерживаемая инструкция");
    }

    /**
     * {@code x += v} / {@code x -= v} для ещё не заданной переменной: none считается пустым значением того же
     * типа, что и правая часть (0, "" или []). Для {@code *=} и {@code /=} none остаётся ошибкой.
     */
    static Object compound(final String op, final Object old, final Object value) {
        Object base = old;
        if (base == null && (op.equals("+") || op.equals("-"))) {
            if (value instanceof Double) {
                base = 0.0;
            } else if (value instanceof String) {
                base = "";
            } else if (value instanceof List<?>) {
                base = new ArrayList<>();
            }
        }
        return Values.binary(op, base, value);
    }

    static void deliver(final Object to, final String text) {
        if (to instanceof ScriptMessageable target) {
            target.sendMessage(text);
        } else if (to instanceof List<?> list) {
            for (final Object item : list) {
                deliver(item, text);
            }
        } else if (to == null) {
            throw new IllegalArgumentException("получатель сообщения — none (игрок вышел?)");
        } else {
            throw new IllegalArgumentException("нельзя отправить сообщение " + Values.typeOf(to));
        }
    }

    private Exec.Action assignment(final Ast.Assign assign, final Ctx ctx) {
        final Eval value = this.expr(assign.value(), ctx);
        final String op = assign.op().equals("=") ? null : assign.op().substring(0, 1);
        final Ast.Expr target = assign.target();
        if (target instanceof Ast.Local local) {
            final int slot = ctx.declare(local.name());
            if (op == null) {
                return ex -> ex.slots[slot] = value.eval(ex);
            }
            return ex -> ex.slots[slot] = compound(op, ex.slots[slot], value.eval(ex));
        }
        if (target instanceof Ast.Global global) {
            final String name = global.name();
            if (op == null) {
                return ex -> ex.runtime.globals().set(name, value.eval(ex));
            }
            return ex -> ex.runtime.globals().set(name, compound(op, ex.runtime.globals().get(name), value.eval(ex)));
        }
        if (target instanceof Ast.Prop prop) {
            final Eval object = this.expr(prop.target(), ctx);
            final String name = prop.name();
            return ex -> {
                final Object obj = object.eval(ex);
                if (!(obj instanceof ScriptObject so)) {
                    throw new IllegalArgumentException("нельзя изменить свойство '" + name + "' у " + Values.typeOf(obj));
                }
                final Object newValue = op == null ? value.eval(ex) : compound(op, Builtins.property(so, name), value.eval(ex));
                if (!so.set(name, newValue)) {
                    throw new IllegalArgumentException("свойство '" + name + "' у " + so.typeName() + " нельзя изменить");
                }
            };
        }
        if (target instanceof Ast.Index index) {
            final Eval list = this.expr(index.target(), ctx);
            final Eval idx = this.expr(index.index(), ctx);
            return ex -> {
                final List<Object> l = Values.list(list.eval(ex), "индексируемое значение");
                final int i = Values.index(l, idx.eval(ex));
                l.set(i, op == null ? value.eval(ex) : compound(op, l.get(i), value.eval(ex)));
            };
        }
        throw this.error(assign.line(), "этому выражению нельзя присвоить значение");
    }

    // ---------- выражения ----------

    private Eval expr(final Ast.Expr e, final Ctx ctx) {
        if (e instanceof Ast.Num n) {
            final Double value = n.value();
            return ex -> value;
        }
        if (e instanceof Ast.Str s) {
            return this.string(s, ctx);
        }
        if (e instanceof Ast.Bool b) {
            final Boolean value = b.value();
            return ex -> value;
        }
        if (e instanceof Ast.Null) {
            return ex -> null;
        }
        if (e instanceof Ast.ListLit list) {
            final Eval[] items = list.items().stream().map(i -> this.expr(i, ctx)).toArray(Eval[]::new);
            return ex -> {
                final List<Object> out = new ArrayList<>(items.length);
                for (final Eval item : items) {
                    out.add(item.eval(ex));
                }
                return out;
            };
        }
        if (e instanceof Ast.Local local) {
            final Integer slot = ctx.slots.get(local.name());
            if (slot == null) {
                throw this.error(local.line(), "неизвестная переменная '" + local.name() + "' (глобальные пишутся как $" + local.name() + ")");
            }
            final int s = slot;
            return ex -> ex.slots[s];
        }
        if (e instanceof Ast.Global global) {
            final String name = global.name();
            return ex -> ex.runtime.globals().get(name);
        }
        if (e instanceof Ast.Prop prop) {
            final Eval target = this.expr(prop.target(), ctx);
            final String name = prop.name();
            return ex -> Builtins.property(target.eval(ex), name);
        }
        if (e instanceof Ast.Index index) {
            final Eval target = this.expr(index.target(), ctx);
            final Eval idx = this.expr(index.index(), ctx);
            return ex -> {
                final Object t = target.eval(ex);
                if (t instanceof String str) {
                    final int i = Values.index(java.util.Collections.nCopies(str.length(), null), idx.eval(ex));
                    return String.valueOf(str.charAt(i));
                }
                final List<Object> list = Values.list(t, "индексируемое значение");
                return list.get(Values.index(list, idx.eval(ex)));
            };
        }
        if (e instanceof Ast.Call call) {
            return this.call(call, ctx);
        }
        if (e instanceof Ast.MethodCall mc) {
            final Eval target = this.expr(mc.target(), ctx);
            final Eval[] args = mc.args().stream().map(a -> this.expr(a, ctx)).toArray(Eval[]::new);
            final String name = mc.name();
            return ex -> {
                final Object t = target.eval(ex);
                return Builtins.method(t, name, evalArgs(args, ex));
            };
        }
        if (e instanceof Ast.Unary unary) {
            final Eval operand = this.expr(unary.operand(), ctx);
            if (unary.op().equals("not")) {
                return ex -> !Values.truthy(operand.eval(ex));
            }
            return ex -> -Values.number(operand.eval(ex), "операнд '-'");
        }
        if (e instanceof Ast.Binary binary) {
            final Eval left = this.expr(binary.left(), ctx);
            final Eval right = this.expr(binary.right(), ctx);
            final String op = binary.op();
            if (op.equals("and")) {
                return ex -> Values.truthy(left.eval(ex)) && Values.truthy(right.eval(ex));
            }
            if (op.equals("or")) {
                return ex -> Values.truthy(left.eval(ex)) || Values.truthy(right.eval(ex));
            }
            return ex -> Values.binary(op, left.eval(ex), right.eval(ex));
        }
        throw this.error(e.line(), "неподдерживаемое выражение");
    }

    private static List<Object> evalArgs(final Eval[] args, final Exec.Execution ex) {
        final List<Object> values = new ArrayList<>(args.length);
        for (final Eval arg : args) {
            values.add(arg.eval(ex));
        }
        return values;
    }

    private Eval string(final Ast.Str s, final Ctx ctx) {
        if (s.parts().stream().allMatch(p -> p instanceof String)) {
            final String constant = String.join("", s.parts().stream().map(p -> (String) p).toList());
            return ex -> constant;
        }
        final Object[] parts = new Object[s.parts().size()];
        for (int i = 0; i < parts.length; i++) {
            final Object part = s.parts().get(i);
            parts[i] = part instanceof Ast.Expr expr ? this.expr(expr, ctx) : part;
        }
        return ex -> {
            final StringBuilder sb = new StringBuilder();
            for (final Object part : parts) {
                if (part instanceof Eval eval) {
                    sb.append(Values.display(eval.eval(ex)));
                } else {
                    sb.append((String) part);
                }
                if (sb.length() > Values.MAX_STRING_LENGTH) {
                    throw new IllegalArgumentException("текст длиннее " + Values.MAX_STRING_LENGTH + " символов");
                }
            }
            return sb.toString();
        };
    }

    private Eval call(final Ast.Call call, final Ctx ctx) {
        final Eval[] args = call.args().stream().map(a -> this.expr(a, ctx)).toArray(Eval[]::new);
        final int line = call.line();
        final CompiledFunction fn = this.runtime.functions.get(call.name());
        if (fn != null) {
            if (fn.params.size() != args.length) {
                throw this.error(line, "функция '" + fn.name + "' ожидает " + fn.params.size() + " аргумент(ов), передано " + args.length);
            }
            return ex -> {
                if (ex.depth + 1 > Exec.MAX_CALL_DEPTH) {
                    throw ex.error(line, "слишком глубокая рекурсия (больше " + Exec.MAX_CALL_DEPTH + " вызовов)");
                }
                final Instr[] body = fn.body;
                if (body == null) {
                    throw ex.error(line, "функция '" + fn.name + "' не загружена из-за ошибки в " + fn.file);
                }
                final Exec.Execution sub = new Exec.Execution(ex.runtime, fn.file, fn.slotCount, body, ex.budget, ex.depth + 1, -1);
                for (int i = 0; i < args.length; i++) {
                    sub.slots[i] = args[i].eval(ex);
                }
                sub.run();
                return sub.returnValue;
            };
        }
        final Builtins.Builtin builtin = Builtins.FUNCTIONS.get(call.name());
        if (builtin == null) {
            throw this.error(line, "неизвестная функция '" + call.name() + "'");
        }
        if (args.length < builtin.minArgs() || args.length > builtin.maxArgs()) {
            throw this.error(line, "неверное число аргументов: " + builtin.signature());
        }
        return ex -> builtin.fn().apply(ex, evalArgs(args, ex));
    }

    static Set<String> keywords() {
        return Parser.KEYWORDS;
    }
}
