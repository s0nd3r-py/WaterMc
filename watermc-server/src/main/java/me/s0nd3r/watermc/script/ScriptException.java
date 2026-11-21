package me.s0nd3r.watermc.script;

/**
 * Ошибка в скрипте: при разборе, компиляции или выполнении. Всегда несёт файл и строку,
 * чтобы автор скрипта видел, где именно проблема.
 */
public class ScriptException extends RuntimeException {

    private final String file;
    private final int line;

    public ScriptException(final String file, final int line, final String message) {
        super(message);
        this.file = file;
        this.line = line;
    }

    public String file() {
        return this.file;
    }

    public int line() {
        return this.line;
    }

    /** Сообщение в виде {@code example.wsc:12: текст}. */
    public String describe() {
        return this.file + ":" + this.line + ": " + this.getMessage();
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // стек Java автору скрипта не нужен, а заполнение дорогое
    }
}
