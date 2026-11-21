package me.s0nd3r.watermc.script;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ScriptEngineTest {

    // ---------- тестовый сервер ----------

    static final class FakeHost implements ScriptHost {
        final List<String> broadcasts = new ArrayList<>();
        final List<String> console = new ArrayList<>();
        final List<String> commands = new ArrayList<>();
        final List<String> info = new ArrayList<>();
        final List<String> errors = new ArrayList<>();
        final List<Object> players = new ArrayList<>();
        long tick;
        private long seq;
        private final PriorityQueue<Scheduled> queue = new PriorityQueue<>((a, b) -> a.due != b.due ? Long.compare(a.due, b.due) : Long.compare(a.seq, b.seq));

        final class Scheduled implements Task {
            long due;
            final long period;
            final long seq = FakeHost.this.seq++;
            final Runnable task;
            boolean cancelled;

            Scheduled(final long due, final long period, final Runnable task) {
                this.due = due;
                this.period = period;
                this.task = task;
            }

            @Override
            public void cancel() {
                this.cancelled = true;
            }
        }

        void advance(final long ticks) {
            final long end = this.tick + ticks;
            while (!this.queue.isEmpty() && this.queue.peek().due <= end) {
                final Scheduled next = this.queue.poll();
                this.tick = next.due;
                if (next.cancelled) {
                    continue;
                }
                next.task.run();
                if (next.period > 0 && !next.cancelled) {
                    next.due += next.period;
                    this.queue.add(next);
                }
            }
            this.tick = end;
        }

        int scheduled() {
            return (int) this.queue.stream().filter(s -> !s.cancelled).count();
        }

        @Override
        public void broadcast(final String message) {
            this.broadcasts.add(message);
        }

        @Override
        public void dispatchConsoleCommand(final String command) {
            this.commands.add(command);
        }

        @Override
        public void logInfo(final String message) {
            this.info.add(message);
        }

        @Override
        public void logError(final String message) {
            this.errors.add(message);
        }

        @Override
        public Task schedule(final long delayTicks, final Runnable task) {
            final Scheduled s = new Scheduled(this.tick + delayTicks, 0, task);
            this.queue.add(s);
            return s;
        }

        @Override
        public Task scheduleRepeating(final long periodTicks, final Runnable task) {
            final Scheduled s = new Scheduled(this.tick + periodTicks, periodTicks, task);
            this.queue.add(s);
            return s;
        }

        @Override
        public List<Object> onlinePlayers() {
            return this.players;
        }

        @Override
        public Object playerByName(final String name) {
            return this.players.stream().filter(p -> ((FakePlayer) p).name.equalsIgnoreCase(name)).findFirst().orElse(null);
        }

        @Override
        public Object worldByName(final String name) {
            return null;
        }

        @Override
        public ScriptMessageable console() {
            return this.console::add;
        }
    }

    static final class FakePlayer implements ScriptObject, ScriptMessageable {
        final String name;
        double health = 10;
        final List<String> messages = new ArrayList<>();
        final List<String> permissions = new ArrayList<>();

        FakePlayer(final String name) {
            this.name = name;
        }

        @Override
        public String typeName() {
            return "player";
        }

        @Override
        public Object get(final String property) {
            return switch (property) {
                case "name" -> this.name;
                case "health" -> this.health;
                default -> MISSING;
            };
        }

        @Override
        public boolean set(final String property, final Object value) {
            if (property.equals("health")) {
                this.health = Math.max(0, Math.min(20, (Double) value));
                return true;
            }
            return false;
        }

        @Override
        public Object call(final String method, final List<Object> args) {
            if (method.equals("has_permission")) {
                return this.permissions.contains((String) args.get(0));
            }
            throw new UnsupportedOperationException(method);
        }

        @Override
        public void sendMessage(final String message) {
            this.messages.add(message);
        }

        @Override
        public String display() {
            return this.name;
        }
    }

    private final FakeHost host = new FakeHost();

    private ScriptEngine engine(final String source) {
        final ScriptEngine engine = new ScriptEngine(this.host, GlobalStore.inMemory(), 10_000);
        final ScriptEngine.LoadResult result = engine.load(Map.of("test.wsc", source));
        assertTrue(result.errors().isEmpty(), () -> "unexpected errors: " + result.errors());
        return engine;
    }

    private List<String> compileErrors(final String source) {
        final ScriptEngine engine = new ScriptEngine(this.host, GlobalStore.inMemory(), 10_000);
        return engine.load(Map.of("bad.wsc", source)).errors();
    }

    private List<String> run(final String body) {
        final FakePlayer player = new FakePlayer("Steve");
        this.engine("on join:\n" + body.indent(4)).dispatch("join", new Object[]{player}, null);
        return player.messages;
    }

    // ---------- язык ----------

    @Test
    void expressionsPrecedenceAndFormatting() {
        assertEquals(List.of("7", "9", "2.5", "-3", "1", "true", "false", "true", "abc3"), this.run("""
            send str(1 + 2 * 3)
            send str((1 + 2) * 3)
            send str(5 / 2)
            send str(-3)
            send str(7 % 3)
            send str(1 < 2 and not (2 < 1))
            send str(1 == 2 or 3 != 3)
            send str("b" in "abc" and 2 in [1, 2])
            send "abc" + 3
            """));
    }

    @Test
    void stringsInterpolationAndMembers() {
        assertEquals(List.of("Hi Steve, hp=10!", "STEVE", "true", "a-b-c", "3", "{literal}", "x'y"), this.run("""
            send "Hi {player.name}, hp={player.health}!"
            send upper(player.name)
            send str("hello".starts_with("he"))
            send join(split("a,b,c", ","), "-")
            send str(len([1, 2, 3]))
            send "\\{literal\\}"
            send 'x\\'y'
            """));
    }

    @Test
    void listsIndexingAndMutation() {
        assertEquals(List.of("[1, 2, 3, 4]", "4", "1", "[1, 9, 3, 4]", "3", "true"), this.run("""
            set xs to [1, 2, 3]
            xs.add(4)
            send str(xs)
            send str(xs[-1])
            send str(xs.first)
            xs[1] = 9
            send str(xs)
            send str(xs.size - 1)
            send str(9 in xs)
            """));
    }

    @Test
    void controlFlowLoopsBreakContinue() {
        assertEquals(List.of("odd 1", "odd 3", "sum 6", "big", "r3"), this.run("""
            for i in range(0, 10):
                if i % 2 == 0:
                    continue
                if i > 3:
                    break
                send "odd {i}"
            set total to 0
            set n to 0
            while n < 3:
                n += 1
                total += n
            send "sum {total}"
            if total > 100:
                send "huge"
            else if total > 5:
                send "big"
            else:
                send "small"
            set r to 0
            repeat 3 times:
                r += 1
            send "r{r}"
            """));
    }

    @Test
    void userFunctionsAcrossFilesAndRecursion() {
        final ScriptEngine engine = new ScriptEngine(this.host, GlobalStore.inMemory(), 10_000);
        final ScriptEngine.LoadResult result = engine.load(Map.of(
            "a.wsc", """
                function fact(n):
                    if n <= 1:
                        return 1
                    return n * fact(n - 1)
                """,
            "b.wsc", """
                on join:
                    send "10! = {fact(10)}"
                """));
        assertTrue(result.errors().isEmpty(), result.errors()::toString);
        final FakePlayer p = new FakePlayer("Alex");
        engine.dispatch("join", new Object[]{p}, null);
        assertEquals(List.of("10! = 3628800"), p.messages);
    }

    @Test
    void propertiesAreWhitelisted() {
        final FakePlayer p = new FakePlayer("Steve");
        final ScriptEngine engine = this.engine("""
            on join:
                player.health += 15
                send "hp {player.health}"
                send "{player.secret}"
            """);
        engine.dispatch("join", new Object[]{p}, null);
        assertEquals(List.of("hp 20"), p.messages, "health is clamped by the object, unknown property aborts");
        assertEquals(1, this.host.errors.size());
        assertTrue(this.host.errors.get(0).contains("test.wsc:4"), this.host.errors.toString());
        assertTrue(this.host.errors.get(0).contains("secret"), this.host.errors.toString());
    }

    // ---------- ошибки компиляции с номером строки ----------

    @Test
    void compileErrorsPointToTheLine() {
        assertError("on join:\n    send x\n", "bad.wsc:2", "неизвестная переменная 'x'");
        assertError("on join:\n    sendd(\"a\")\n", "bad.wsc:2", "неизвестная функция 'sendd'");
        assertError("on jump:\n    stop\n", "bad.wsc:1", "неизвестное событие 'jump'");
        assertError("function f():\n    wait 1 second\n    return 1\n", "bad.wsc:2", "wait нельзя использовать внутри функции");
        assertError("on join:\n    cancel\n", "bad.wsc:2", "cancel недоступен");
        assertError("on join:\n    break\n", "bad.wsc:2", "break вне цикла");
        assertError("on join\n    stop\n", "bad.wsc:1", "ожидалось ':'");
        assertError("on join:\n    if true:\n      send \"a\"\n     send \"b\"\n", "bad.wsc:4", "отступ");
        assertError("on join:\n    send \"abc\n", "bad.wsc:2", "строка не закрыта");
        assertError("on join:\n    send str(1, 2)\n", "bad.wsc:2", "str(x)");
        assertError("function random():\n    return 1\n", "bad.wsc:1", "уже встроена");
        assertError("on join:\n    player + 1\n", "bad.wsc:2", "выражение ничего не делает");
        assertError("on unload:\n    wait 1 tick\n", "bad.wsc:2", "wait нельзя использовать в on unload");
    }

    private void assertError(final String source, final String location, final String fragment) {
        final List<String> errors = this.compileErrors(source);
        assertEquals(1, errors.size(), () -> source + " -> " + errors);
        assertTrue(errors.get(0).startsWith(location + ":"), errors.get(0));
        assertTrue(errors.get(0).contains(fragment), errors.get(0));
    }

    @Test
    void oneBrokenFileDoesNotStopOthers() {
        final ScriptEngine engine = new ScriptEngine(this.host, GlobalStore.inMemory(), 10_000);
        final ScriptEngine.LoadResult result = engine.load(Map.of("good.wsc", "on join:\n    send \"ok\"\n", "broken.wsc", "on join:\n    send (\n"));
        assertEquals(1, result.filesLoaded());
        assertEquals(1, result.filesFailed());
        final FakePlayer p = new FakePlayer("A");
        engine.dispatch("join", new Object[]{p}, null);
        assertEquals(List.of("ok"), p.messages);
    }

    // ---------- песочница и лимиты ----------

    @Test
    void endlessLoopIsStoppedWithoutHurtingTheServer() {
        final ScriptEngine engine = this.engine("""
            on join:
                set i to 0
                while true:
                    i += 1
            """);
        engine.dispatch("join", new Object[]{new FakePlayer("A")}, null);
        assertEquals(1, this.host.errors.size());
        assertTrue(this.host.errors.get(0).contains("лимит операций"), this.host.errors.toString());
        engine.dispatch("join", new Object[]{new FakePlayer("B")}, null);
        assertEquals(1, this.host.errors.size(), "the same error is rate-limited");
    }

    @Test
    void deepRecursionIsReportedNotCrashing() {
        final ScriptEngine engine = this.engine("""
            function down(n):
                return down(n + 1)
            on join:
                send str(down(0))
            """);
        engine.dispatch("join", new Object[]{new FakePlayer("A")}, null);
        assertEquals(1, this.host.errors.size());
        assertTrue(this.host.errors.get(0).contains("рекурсия"), this.host.errors.toString());
    }

    @Test
    void hugeStringsAndListsAreRejected() {
        final ScriptEngine engine = this.engine("""
            on join:
                set s to "x" * 2000000
            """);
        engine.dispatch("join", new Object[]{new FakePlayer("A")}, null);
        assertTrue(this.host.errors.get(0).contains("длиннее"), this.host.errors.toString());
    }

    // ---------- wait, таймеры, выгрузка ----------

    @Test
    void waitSuspendsInsideLoopsAndResumesOnTime() {
        final FakePlayer p = new FakePlayer("Steve");
        final ScriptEngine engine = this.engine("""
            on join:
                for i in [1, 2, 3]:
                    send "tick {i}"
                    wait 1 second
                send "done"
            """);
        engine.dispatch("join", new Object[]{p}, null);
        assertEquals(List.of("tick 1"), p.messages);
        this.host.advance(19);
        assertEquals(List.of("tick 1"), p.messages);
        this.host.advance(1);
        assertEquals(List.of("tick 1", "tick 2"), p.messages);
        this.host.advance(40);
        assertEquals(List.of("tick 1", "tick 2", "tick 3", "done"), p.messages);
        assertEquals(0, engine.pendingWaits());
    }

    @Test
    void unloadCancelsWaitsAndTimers() {
        final FakePlayer p = new FakePlayer("Steve");
        final ScriptEngine engine = this.engine("""
            on join:
                wait 5 ticks
                send "late"
            every 10 ticks:
                broadcast "tick"
            on unload:
                broadcast "bye"
            """);
        engine.dispatch("join", new Object[]{p}, null);
        this.host.advance(20);
        assertEquals(List.of("late"), p.messages);
        assertEquals(List.of("tick", "tick"), this.host.broadcasts);
        engine.dispatch("join", new Object[]{p}, null);
        engine.unload();
        this.host.advance(100);
        assertEquals(List.of("late"), p.messages, "pending wait was cancelled by unload");
        assertEquals(List.of("tick", "tick", "bye"), this.host.broadcasts);
        assertEquals(0, this.host.scheduled());
    }

    @Test
    void cancelWorksOnlyBeforeWait() {
        final ScriptEngine engine = this.engine("""
            on break:
                if block == "protected":
                    cancel
                    stop
                wait 1 tick
                cancel
            """);
        final AtomicBoolean cancelled = new AtomicBoolean();
        engine.dispatch("break", new Object[]{new FakePlayer("A"), "protected"}, () -> cancelled.set(true));
        assertTrue(cancelled.get());
        cancelled.set(false);
        engine.dispatch("break", new Object[]{new FakePlayer("A"), "dirt"}, () -> cancelled.set(true));
        this.host.advance(1);
        assertFalse(cancelled.get());
        assertTrue(this.host.errors.get(0).contains("cancel после wait"), this.host.errors.toString());
    }

    @Test
    void newEventsBindVariablesAndCancel() {
        final ScriptEngine engine = this.engine("""
            on chat:
                if message.contains("spam"):
                    cancel
            on player_command:
                if line.starts_with("op "):
                    cancel
            on damage:
                if cause == "fall" and damage < 3:
                    cancel
            on interact:
                if block == none:
                    broadcast "air " + action
            on kill:
                broadcast player.name + " killed " + entity
            on world_change:
                broadcast player.name + " left " + from
            """);
        assertTrue(this.host.errors.isEmpty(), this.host.errors.toString());
        final AtomicBoolean cancelled = new AtomicBoolean();
        engine.dispatch("chat", new Object[]{new FakePlayer("A"), "buy spam now"}, () -> cancelled.set(true));
        assertTrue(cancelled.getAndSet(false), "chat is cancellable");
        engine.dispatch("chat", new Object[]{new FakePlayer("A"), "hello"}, () -> cancelled.set(true));
        assertFalse(cancelled.get());
        engine.dispatch("player_command", new Object[]{new FakePlayer("A"), "op Steve"}, () -> cancelled.set(true));
        assertTrue(cancelled.getAndSet(false));
        engine.dispatch("player_command", new Object[]{new FakePlayer("A"), "spawn"}, () -> cancelled.set(true));
        assertFalse(cancelled.get());
        engine.dispatch("damage", new Object[]{new FakePlayer("A"), "fall", 2.0}, () -> cancelled.set(true));
        assertTrue(cancelled.getAndSet(false));
        engine.dispatch("damage", new Object[]{new FakePlayer("A"), "fall", 7.0}, () -> cancelled.set(true));
        assertFalse(cancelled.get());
        engine.dispatch("interact", new Object[]{new FakePlayer("A"), null, "right_click_air"}, () -> cancelled.set(true));
        engine.dispatch("kill", new Object[]{new FakePlayer("A"), "zombie"}, null);
        engine.dispatch("world_change", new Object[]{new FakePlayer("A"), "world"}, null);
        assertEquals(List.of("air right_click_air", "A killed zombie", "A left world"), this.host.broadcasts);
    }

    @Test
    void cancelIsRejectedAtLoadForNonCancellableNewEvents() {
        assertError("on kill:\n    cancel\n", "bad.wsc:2", "cancel недоступен");
        assertError("on world_change:\n    cancel\n", "bad.wsc:2", "cancel недоступен");
    }

    // ---------- команды и глобальные переменные ----------

    @Test
    void commandsBindArgumentsAndReplyToSender() {
        final ScriptEngine engine = this.engine("""
            command /greet who count:
                @permission "test.greet"
                @aliases "hi"
                if who == none:
                    send "usage"
                    stop
                repeat num(count) times:
                    send "hello {who}"
                console "say greeted {who}"
            """);
        final FakePlayer sender = new FakePlayer("Op");
        assertTrue(engine.runCommand("greet", sender, sender, List.of("Bob", "2")));
        assertEquals(List.of("hello Bob", "hello Bob"), sender.messages);
        assertEquals(List.of("say greeted Bob"), this.host.commands);
        sender.messages.clear();
        engine.runCommand("greet", sender, sender, List.of());
        assertEquals(List.of("usage"), sender.messages);
        assertFalse(engine.runCommand("missing", sender, sender, List.of()));
    }

    @Test
    void commandMetadataAndClashes() {
        final ScriptEngine engine = new ScriptEngine(this.host, GlobalStore.inMemory(), 10_000);
        final ScriptEngine.LoadResult result = engine.load(Map.of(
            "a.wsc", "command /spawn:\n    @permission \"x.spawn\"\n    @aliases \"s\", \"home\"\n    stop\n",
            "b.wsc", "command /home:\n    stop\n"));
        assertEquals(1, result.errors().size(), result.errors()::toString);
        assertTrue(result.errors().get(0).contains("/home"));
        assertEquals(1, result.commands().size());
        final ScriptEngine.CommandBinding spawn = result.commands().get(0);
        assertEquals("spawn", spawn.name());
        assertEquals(List.of("s", "home"), spawn.aliases());
        assertEquals("x.spawn", spawn.permission());
    }

    @Test
    void globalsPersistAcrossRestarts(@TempDir final Path dir) throws Exception {
        final Path file = dir.resolve("script-variables.wdb");
        GlobalStore store = GlobalStore.open(file);
        ScriptEngine engine = new ScriptEngine(this.host, store, 10_000);
        engine.load(Map.of("v.wsc", """
            on load:
                if $visits == none:
                    $visits = 0
                $visits += 1
                $names = ["a", "b"]
                $flag = true
            on join:
                $who = player
            """));
        engine.dispatch("join", new Object[]{new FakePlayer("A")}, null);
        assertTrue(this.host.errors.get(0).contains("можно хранить только"), this.host.errors.toString());
        engine.unload();
        store.close();

        store = GlobalStore.open(file);
        assertEquals(1.0, store.get("visits"));
        assertEquals(List.of("a", "b"), store.get("names"));
        assertEquals(Boolean.TRUE, store.get("flag"));
        engine = new ScriptEngine(this.host, store, 10_000);
        engine.load(Map.of("v.wsc", "on load:\n    $visits += 1\n"));
        engine.unload();
        store.close();
        final GlobalStore reopened = GlobalStore.open(file);
        assertEquals(2.0, reopened.get("visits"));
        reopened.close();
    }

    @Test
    void bundledExampleCompiles() throws Exception {
        final String example;
        try (InputStream in = ScriptEngineTest.class.getResourceAsStream("/watermc/scripts/_example.wsc")) {
            assertNotNull(in, "example script is packaged");
            example = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        final ScriptEngine engine = new ScriptEngine(this.host, GlobalStore.inMemory(), 10_000);
        final ScriptEngine.LoadResult result = engine.load(Map.of("example.wsc", example));
        assertTrue(result.errors().isEmpty(), result.errors()::toString);
        assertEquals(List.of("countdown", "heal", "top"), result.commands().stream().map(ScriptEngine.CommandBinding::name).toList());
        assertTrue(this.host.info.get(0).contains("пример загружен"), this.host.info.toString());

        final FakePlayer p = new FakePlayer("Steve");
        engine.dispatch("join", new Object[]{p}, null);
        assertEquals(List.of("&aПривет, &eSteve&a! Ты 1-й гость сервера."), p.messages);

        engine.runCommand("countdown", p, p, List.of());
        this.host.advance(100);
        assertEquals(List.of("&e3...", "&e2...", "&e1...", "&cСтарт!"), this.host.broadcasts);
    }

    @Test
    void compoundAssignmentTreatsNoneAsEmpty() {
        assertEquals(List.of("1", "ab", "[x]"), this.run("""
            $counter += 1
            send str($counter)
            set text to none
            text += "ab"
            send text
            set items to none
            items += ["x"]
            send str(items)
            """));
        final ScriptEngine engine = this.engine("on join:\n    set n to none\n    n *= 2\n");
        engine.dispatch("join", new Object[]{new FakePlayer("A")}, null);
        assertTrue(this.host.errors.get(0).contains("числом"), this.host.errors.toString());
    }

    @Test
    void sampleSourcesAreIndependentOfMapOrder() {
        final Map<String, String> sources = new HashMap<>();
        sources.put("z.wsc", "on join:\n    send helper()\n");
        sources.put("a.wsc", "function helper():\n    return \"from a\"\n");
        final ScriptEngine engine = new ScriptEngine(this.host, GlobalStore.inMemory(), 10_000);
        assertTrue(engine.load(sources).errors().isEmpty());
        final FakePlayer p = new FakePlayer("A");
        engine.dispatch("join", new Object[]{p}, null);
        assertEquals(List.of("from a"), p.messages);
    }
}
