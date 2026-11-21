package me.s0nd3r.watermc.script;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Лексер WaterScript. Блоки задаются отступами (как в Python): строка, оканчивающаяся на {@code :},
 * открывает блок, следующие строки с бо́льшим отступом — его тело. Внутри скобок переводы строк
 * игнорируются, поэтому длинные выражения можно переносить.
 */
final class Lexer {

    enum Type { NAME, GLOBAL, NUMBER, STRING, OP, NEWLINE, INDENT, DEDENT, EOF }

    /** Часть строки: либо литерал, либо исходный текст интерполяции {@code {expr}}. */
    record Interpolation(String source, int line) {
    }

    record Token(Type type, String text, int line, double number, List<Object> parts) {
        boolean is(final Type t, final String s) {
            return this.type == t && this.text.equals(s);
        }

        boolean isOp(final String s) {
            return this.type == Type.OP && this.text.equals(s);
        }

        boolean isName(final String s) {
            return this.type == Type.NAME && this.text.equals(s);
        }

        @Override
        public String toString() {
            return switch (this.type) {
                case NEWLINE -> "конец строки";
                case INDENT -> "отступ";
                case DEDENT -> "конец блока";
                case EOF -> "конец файла";
                case STRING -> "строка";
                default -> "'" + this.text + "'";
            };
        }
    }

    private static final String[] OPERATORS = {
        "==", "!=", "<=", ">=", "+=", "-=", "*=", "/=", "<", ">", "=", "+", "-", "*", "/", "%",
        "(", ")", "[", "]", ",", ".", ":", "@"
    };

    private final String file;
    private final String src;
    private int pos;
    private int line = 1;
    private int depth; // глубина скобок
    private final List<Token> out = new ArrayList<>();
    private final Deque<Integer> indents = new ArrayDeque<>();

    Lexer(final String file, final String source) {
        this.file = file;
        this.src = source.replace("\r\n", "\n").replace('\r', '\n');
        this.indents.push(0);
    }

    static List<Token> tokenize(final String file, final String source) {
        return new Lexer(file, source).run();
    }

    private ScriptException error(final String message) {
        return new ScriptException(this.file, this.line, message);
    }

    private List<Token> run() {
        boolean lineStart = true;
        while (this.pos < this.src.length()) {
            if (lineStart && this.depth == 0) {
                if (this.handleIndent()) {
                    continue; // пустая строка или комментарий: следующая строка снова начинается с отступа
                }
                lineStart = false;
            }
            final char c = this.src.charAt(this.pos);
            if (c == '\n') {
                this.pos++;
                if (this.depth == 0) {
                    this.emitNewline();
                    lineStart = true;
                }
                this.line++;
                continue;
            }
            if (c == ' ' || c == '\t') {
                this.pos++;
                continue;
            }
            if (c == '#') {
                while (this.pos < this.src.length() && this.src.charAt(this.pos) != '\n') {
                    this.pos++;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                this.string(c);
                continue;
            }
            if (Character.isDigit(c)) {
                this.number();
                continue;
            }
            if (c == '$') {
                this.pos++;
                final String name = this.identifier();
                if (name.isEmpty()) {
                    throw this.error("после '$' ожидается имя глобальной переменной");
                }
                this.out.add(new Token(Type.GLOBAL, name, this.line, 0, null));
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                this.out.add(new Token(Type.NAME, this.identifier(), this.line, 0, null));
                continue;
            }
            if (c == '/' && this.lastIsName("command")) {
                this.pos++; // "command /heal": слеш перед именем команды необязателен
                continue;
            }
            boolean matched = false;
            for (final String op : OPERATORS) {
                if (this.src.startsWith(op, this.pos)) {
                    if (op.equals("(") || op.equals("[")) {
                        this.depth++;
                    } else if (op.equals(")") || op.equals("]")) {
                        if (this.depth == 0) {
                            throw this.error("лишняя закрывающая скобка '" + op + "'");
                        }
                        this.depth--;
                    }
                    this.out.add(new Token(Type.OP, op, this.line, 0, null));
                    this.pos += op.length();
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                throw this.error("неожиданный символ '" + c + "'");
            }
        }
        if (this.depth != 0) {
            throw this.error("не закрыта скобка");
        }
        this.emitNewline();
        while (this.indents.peek() > 0) {
            this.indents.pop();
            this.out.add(new Token(Type.DEDENT, "", this.line, 0, null));
        }
        this.out.add(new Token(Type.EOF, "", this.line, 0, null));
        return this.out;
    }

    private boolean lastIsName(final String name) {
        return !this.out.isEmpty() && this.out.get(this.out.size() - 1).isName(name);
    }

    private void emitNewline() {
        if (!this.out.isEmpty()) {
            final Type last = this.out.get(this.out.size() - 1).type;
            if (last != Type.NEWLINE && last != Type.INDENT && last != Type.DEDENT) {
                this.out.add(new Token(Type.NEWLINE, "", this.line, 0, null));
            }
        }
    }

    /** Обрабатывает отступ в начале строки. Возвращает true, если строка пустая или комментарий. */
    private boolean handleIndent() {
        int width = 0;
        int p = this.pos;
        boolean tabs = false;
        boolean spaces = false;
        while (p < this.src.length()) {
            final char c = this.src.charAt(p);
            if (c == ' ') {
                width++;
                spaces = true;
            } else if (c == '\t') {
                width += 4;
                tabs = true;
            } else {
                break;
            }
            p++;
        }
        if (p >= this.src.length() || this.src.charAt(p) == '\n' || this.src.charAt(p) == '#') {
            // пустая строка/комментарий: отступ не учитываем
            while (p < this.src.length() && this.src.charAt(p) != '\n') {
                p++;
            }
            this.pos = p;
            if (p < this.src.length()) {
                this.pos++;
                this.line++;
            }
            return true;
        }
        if (tabs && spaces) {
            throw this.error("смешаны табы и пробелы в отступе");
        }
        this.pos = p;
        final int current = this.indents.peek();
        if (width > current) {
            this.indents.push(width);
            this.out.add(new Token(Type.INDENT, "", this.line, 0, null));
        } else {
            while (width < this.indents.peek()) {
                this.indents.pop();
                this.out.add(new Token(Type.DEDENT, "", this.line, 0, null));
            }
            if (width != this.indents.peek()) {
                throw this.error("отступ не совпадает ни с одним внешним блоком");
            }
        }
        return false;
    }

    private String identifier() {
        final int start = this.pos;
        while (this.pos < this.src.length()) {
            final char c = this.src.charAt(this.pos);
            if (Character.isLetterOrDigit(c) || c == '_') {
                this.pos++;
            } else {
                break;
            }
        }
        return this.src.substring(start, this.pos);
    }

    private void number() {
        final int start = this.pos;
        while (this.pos < this.src.length() && (Character.isDigit(this.src.charAt(this.pos)) || this.src.charAt(this.pos) == '_')) {
            this.pos++;
        }
        if (this.pos + 1 < this.src.length() && this.src.charAt(this.pos) == '.' && Character.isDigit(this.src.charAt(this.pos + 1))) {
            this.pos++;
            while (this.pos < this.src.length() && Character.isDigit(this.src.charAt(this.pos))) {
                this.pos++;
            }
        }
        final String text = this.src.substring(start, this.pos);
        this.out.add(new Token(Type.NUMBER, text, this.line, Double.parseDouble(text.replace("_", "")), null));
    }

    private void string(final char quote) {
        final int startLine = this.line;
        this.pos++;
        final List<Object> parts = new ArrayList<>();
        final StringBuilder literal = new StringBuilder();
        while (true) {
            if (this.pos >= this.src.length() || this.src.charAt(this.pos) == '\n') {
                throw new ScriptException(this.file, startLine, "строка не закрыта кавычкой");
            }
            final char c = this.src.charAt(this.pos);
            if (c == quote) {
                this.pos++;
                break;
            }
            if (c == '\\') {
                if (this.pos + 1 >= this.src.length()) {
                    throw this.error("незавершённая escape-последовательность");
                }
                final char e = this.src.charAt(this.pos + 1);
                switch (e) {
                    case 'n' -> literal.append('\n');
                    case 't' -> literal.append('\t');
                    case '\\', '"', '\'', '{', '}' -> literal.append(e);
                    default -> throw this.error("неизвестная escape-последовательность '\\" + e + "'");
                }
                this.pos += 2;
                continue;
            }
            if (c == '{') {
                final int close = this.findClosingBrace(this.pos + 1);
                if (literal.length() > 0) {
                    parts.add(literal.toString());
                    literal.setLength(0);
                }
                final String expr = this.src.substring(this.pos + 1, close).trim();
                if (expr.isEmpty()) {
                    throw this.error("пустая интерполяция {} в строке (для фигурной скобки используйте \\{)");
                }
                parts.add(new Interpolation(expr, this.line));
                this.pos = close + 1;
                continue;
            }
            literal.append(c);
            this.pos++;
        }
        if (literal.length() > 0 || parts.isEmpty()) {
            parts.add(literal.toString());
        }
        this.out.add(new Token(Type.STRING, "", startLine, 0, parts));
    }

    private int findClosingBrace(final int from) {
        int nested = 0;
        for (int p = from; p < this.src.length(); p++) {
            final char c = this.src.charAt(p);
            if (c == '\n') {
                break;
            }
            if (c == '"' || c == '\'') {
                // пропустить вложенную строку
                final char q = c;
                p++;
                while (p < this.src.length() && this.src.charAt(p) != q && this.src.charAt(p) != '\n') {
                    if (this.src.charAt(p) == '\\') {
                        p++;
                    }
                    p++;
                }
                continue;
            }
            if (c == '{') {
                nested++;
            } else if (c == '}') {
                if (nested == 0) {
                    return p;
                }
                nested--;
            }
        }
        throw this.error("не закрыта интерполяция '{' в строке");
    }
}
