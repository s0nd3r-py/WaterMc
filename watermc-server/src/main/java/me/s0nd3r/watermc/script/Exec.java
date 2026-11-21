package me.s0nd3r.watermc.script;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Исполнение скомпилированного скрипта.
 *
 * <p>Выражения компилируются в замыкания {@link Eval} (без рефлексии и без повторного разбора текста),
 * инструкции — в плоские массивы {@link Instr}. Исполнитель хранит стек кадров явно, поэтому {@code wait}
 * в любом месте (внутри циклов и условий) просто сохраняет состояние и возвращает управление серверу;
 * через нужное число тиков выполнение продолжается с того же места.</p>
 */
final class Exec {

    static final int MAX_CALL_DEPTH = 64;

    private Exec() {
    }

    @FunctionalInterface
    interface Eval {
        Object eval(Execution ex);
    }

    @FunctionalInterface
    interface Action {
        void run(Execution ex);
    }

    // ---------- инструкции ----------

    abstract static sealed class Instr permits Run, IfI, WhileI, RepeatI, ForI, BreakI, ContinueI, StopI, ReturnI, WaitI {
        final int line;

        Instr(final int line) {
            this.line = line;
        }
    }

    static final class Run extends Instr {
        final Action action;

        Run(final int line, final Action action) {
            super(line);
            this.action = action;
        }
    }

    static final class IfI extends Instr {
        final Eval[] conditions;
        final Instr[][] blocks;
        final Instr[] elseBlock;

        IfI(final int line, final Eval[] conditions, final Instr[][] blocks, final Instr[] elseBlock) {
            super(line);
            this.conditions = conditions;
            this.blocks = blocks;
            this.elseBlock = elseBlock;
        }
    }

    static final class WhileI extends Instr {
        final Eval condition;
        final Instr[] body;

        WhileI(final int line, final Eval condition, final Instr[] body) {
            super(line);
            this.condition = condition;
            this.body = body;
        }
    }

    static final class RepeatI extends Instr {
        final Eval count;
        final Instr[] body;

        RepeatI(final int line, final Eval count, final Instr[] body) {
            super(line);
            this.count = count;
            this.body = body;
        }
    }

    static final class ForI extends Instr {
        final int slot;
        final Eval iterable;
        final Instr[] body;

        ForI(final int line, final int slot, final Eval iterable, final Instr[] body) {
            super(line);
            this.slot = slot;
            this.iterable = iterable;
            this.body = body;
        }
    }

    static final class BreakI extends Instr {
        BreakI(final int line) {
            super(line);
        }
    }

    static final class ContinueI extends Instr {
        ContinueI(final int line) {
            super(line);
        }
    }

    static final class StopI extends Instr {
        StopI(final int line) {
            super(line);
        }
    }

    static final class ReturnI extends Instr {
        final Eval value;

        ReturnI(final int line, final Eval value) {
            super(line);
            this.value = value;
        }
    }

    static final class WaitI extends Instr {
        final Eval amount;
        final long unitTicks;

        WaitI(final int line, final Eval amount, final long unitTicks) {
            super(line);
            this.amount = amount;
            this.unitTicks = unitTicks;
        }
    }

    // ---------- состояние ----------

    private static final int BLOCK = 0;
    private static final int WHILE = 1;
    private static final int REPEAT = 2;
    private static final int FOR = 3;

    private static final class Frame {
        final Instr[] body;
        final int kind;
        final int line;
        int pc;
        Eval condition;
        long remaining;
        Iterator<Object> iterator;
        int slot;

        Frame(final Instr[] body, final int kind, final int line) {
            this.body = body;
            this.kind = kind;
            this.line = line;
        }
    }

    /** Общий бюджет операций одного «отрезка» выполнения (до завершения или до {@code wait}). */
    static final class Budget {
        final long limit;
        long steps;

        Budget(final long limit) {
            this.limit = limit;
        }
    }

    static final class Execution {
        final ScriptRuntime runtime;
        final String file;
        final Object[] slots;
        final Budget budget;
        final int depth;
        final int defaultTargetSlot;
        final ArrayDeque<Frame> stack = new ArrayDeque<>();
        Runnable cancelHandle;
        boolean waited;
        Object returnValue;

        Execution(final ScriptRuntime runtime, final String file, final int slotCount, final Instr[] body,
                  final Budget budget, final int depth, final int defaultTargetSlot) {
            this.runtime = runtime;
            this.file = file;
            this.slots = new Object[slotCount];
            this.budget = budget;
            this.depth = depth;
            this.defaultTargetSlot = defaultTargetSlot;
            this.stack.push(new Frame(body, BLOCK, 0));
        }

        ScriptException error(final int line, final String message) {
            return new ScriptException(this.file, line, message);
        }

        private void step(final int line) {
            if (++this.budget.steps > this.budget.limit) {
                throw this.error(line, "превышен лимит операций (" + this.budget.limit + ") — похоже на бесконечный цикл; добавьте wait");
            }
        }

        /**
         * Выполняет до завершения или до {@code wait}.
         *
         * @return 0 — выполнение завершено; иначе — через сколько тиков продолжить
         */
        long run() {
            while (!this.stack.isEmpty()) {
                final Frame frame = this.stack.peek();
                if (frame.pc >= frame.body.length) {
                    if (!this.nextIteration(frame)) {
                        this.stack.pop();
                    }
                    continue;
                }
                final Instr instr = frame.body[frame.pc++];
                this.step(instr.line);
                try {
                    final long wait = this.execute(instr);
                    if (wait != 0) {
                        return wait;
                    }
                } catch (final ScriptException ex) {
                    throw ex;
                } catch (final IllegalArgumentException | UnsupportedOperationException | ArithmeticException | IndexOutOfBoundsException ex) {
                    throw this.error(instr.line, ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
                }
            }
            return 0;
        }

        /** @return тики ожидания или 0. -1 означает «стоп» (стек очищен). */
        private long execute(final Instr instr) {
            if (instr instanceof Run run) {
                run.action.run(this);
            } else if (instr instanceof IfI branch) {
                for (int i = 0; i < branch.conditions.length; i++) {
                    if (Values.truthy(branch.conditions[i].eval(this))) {
                        this.stack.push(new Frame(branch.blocks[i], BLOCK, branch.line));
                        return 0;
                    }
                }
                if (branch.elseBlock != null) {
                    this.stack.push(new Frame(branch.elseBlock, BLOCK, branch.line));
                }
            } else if (instr instanceof WhileI loop) {
                if (Values.truthy(loop.condition.eval(this))) {
                    final Frame frame = new Frame(loop.body, WHILE, loop.line);
                    frame.condition = loop.condition;
                    this.stack.push(frame);
                }
            } else if (instr instanceof RepeatI loop) {
                final double count = Values.number(loop.count.eval(this), "число повторений");
                if (count != Math.rint(count)) {
                    throw new IllegalArgumentException("число повторений должно быть целым");
                }
                if (count > 0) {
                    final Frame frame = new Frame(loop.body, REPEAT, loop.line);
                    frame.remaining = (long) count;
                    this.stack.push(frame);
                }
            } else if (instr instanceof ForI loop) {
                final Object iterable = loop.iterable.eval(this);
                final List<Object> items;
                if (iterable instanceof List<?> list) {
                    items = new ArrayList<>(list); // снимок: изменение списка в теле цикла безопасно
                } else {
                    throw new IllegalArgumentException("'for' перебирает список, а не " + Values.typeOf(iterable));
                }
                final Iterator<Object> iterator = items.iterator();
                if (iterator.hasNext()) {
                    this.slots[loop.slot] = iterator.next();
                    final Frame frame = new Frame(loop.body, FOR, loop.line);
                    frame.iterator = iterator;
                    frame.slot = loop.slot;
                    this.stack.push(frame);
                }
            } else if (instr instanceof BreakI) {
                while (!this.stack.isEmpty()) {
                    final Frame frame = this.stack.pop();
                    if (frame.kind != BLOCK) {
                        break;
                    }
                }
            } else if (instr instanceof ContinueI) {
                while (!this.stack.isEmpty() && this.stack.peek().kind == BLOCK) {
                    this.stack.pop();
                }
                if (!this.stack.isEmpty()) {
                    final Frame loop = this.stack.peek();
                    loop.pc = loop.body.length; // следующая итерация
                }
            } else if (instr instanceof StopI) {
                this.stack.clear();
            } else if (instr instanceof ReturnI ret) {
                this.returnValue = ret.value == null ? null : ret.value.eval(this);
                this.stack.clear();
            } else if (instr instanceof WaitI wait) {
                final double amount = Values.number(wait.amount.eval(this), "длительность ожидания");
                if (!(amount >= 0) || Double.isInfinite(amount)) {
                    throw new IllegalArgumentException("длительность ожидания должна быть неотрицательным числом");
                }
                this.waited = true;
                return Math.max(1L, Math.round(amount * wait.unitTicks));
            }
            return 0;
        }

        private boolean nextIteration(final Frame frame) {
            switch (frame.kind) {
                case WHILE -> {
                    this.step(frame.line);
                    final boolean again;
                    try {
                        again = Values.truthy(frame.condition.eval(this));
                    } catch (final IllegalArgumentException | UnsupportedOperationException ex) {
                        throw this.error(frame.line, ex.getMessage());
                    }
                    if (again) {
                        frame.pc = 0;
                        return true;
                    }
                    return false;
                }
                case REPEAT -> {
                    this.step(frame.line);
                    if (--frame.remaining > 0) {
                        frame.pc = 0;
                        return true;
                    }
                    return false;
                }
                case FOR -> {
                    this.step(frame.line);
                    if (frame.iterator.hasNext()) {
                        this.slots[frame.slot] = frame.iterator.next();
                        frame.pc = 0;
                        return true;
                    }
                    return false;
                }
                default -> {
                    return false;
                }
            }
        }

        /** Получатель {@code send} без {@code to}: отправитель команды или игрок события, иначе консоль. */
        ScriptMessageable defaultTarget() {
            if (this.defaultTargetSlot >= 0 && this.slots[this.defaultTargetSlot] instanceof ScriptMessageable target) {
                return target;
            }
            return this.runtime.host().console();
        }

        void cancelEvent(final int line) {
            if (this.waited) {
                throw this.error(line, "cancel после wait не действует: событие уже обработано сервером");
            }
            if (this.cancelHandle == null) {
                throw this.error(line, "это событие нельзя отменить");
            }
            this.cancelHandle.run();
        }
    }
}
