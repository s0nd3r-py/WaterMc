package me.s0nd3r.watermc.script;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.s0nd3r.watermc.script.Lexer.Token;
import me.s0nd3r.watermc.script.Lexer.Type;

/** Парсер WaterScript (рекурсивный спуск). */
final class Parser {

    static final Set<String> KEYWORDS = Set.of(
        "on", "command", "every", "function", "if", "else", "while", "repeat", "times", "for", "in",
        "set", "to", "send", "broadcast", "console", "log", "wait", "break", "continue", "stop",
        "return", "cancel", "and", "or", "not", "true", "false", "null", "none"
    );

    private final String file;
    private final List<Token> tokens;
    private int pos;

    private Parser(final String file, final List<Token> tokens) {
        this.file = file;
        this.tokens = tokens;
    }

    static Ast.ScriptFile parse(final String file, final String source) {
        final Parser parser = new Parser(file, Lexer.tokenize(file, source));
        return parser.parseFile();
    }

    // ---------- утилиты ----------

    private Token peek() {
        return this.tokens.get(this.pos);
    }

    private Token peek(final int ahead) {
        return this.tokens.get(Math.min(this.pos + ahead, this.tokens.size() - 1));
    }

    private Token next() {
        final Token t = this.tokens.get(this.pos);
        if (t.type() != Type.EOF) {
            this.pos++;
        }
        return t;
    }

    private ScriptException error(final Token at, final String message) {
        return new ScriptException(this.file, at.line(), message);
    }

    private boolean acceptOp(final String op) {
        if (this.peek().isOp(op)) {
            this.pos++;
            return true;
        }
        return false;
    }

    private boolean acceptName(final String name) {
        if (this.peek().isName(name)) {
            this.pos++;
            return true;
        }
        return false;
    }

    private void expectOp(final String op) {
        final Token t = this.next();
        if (!t.isOp(op)) {
            throw this.error(t, "ожидалось '" + op + "', найдено " + t);
        }
    }

    private void expectName(final String name) {
        final Token t = this.next();
        if (!t.isName(name)) {
            throw this.error(t, "ожидалось '" + name + "', найдено " + t);
        }
    }

    private String identifier(final String what) {
        final Token t = this.next();
        if (t.type() != Type.NAME) {
            throw this.error(t, "ожидалось " + what + ", найдено " + t);
        }
        if (KEYWORDS.contains(t.text())) {
            throw this.error(t, "'" + t.text() + "' — ключевое слово, его нельзя использовать как " + what);
        }
        return t.text();
    }

    private void endOfStatement() {
        final Token t = this.next();
        if (t.type() != Type.NEWLINE && t.type() != Type.EOF) {
            throw this.error(t, "ожидался конец строки, найдено " + t);
        }
    }

    // ---------- файл и объявления ----------

    private Ast.ScriptFile parseFile() {
        final List<Ast.Decl> decls = new ArrayList<>();
        while (this.peek().type() != Type.EOF) {
            if (this.peek().type() == Type.NEWLINE) {
                this.pos++;
                continue;
            }
            decls.add(this.declaration());
        }
        return new Ast.ScriptFile(this.file, decls);
    }

    private Ast.Decl declaration() {
        final Token t = this.next();
        if (t.isName("on")) {
            final Token eventTok = this.next(); // имя события может совпадать с ключевым словом (on break)
            if (eventTok.type() != Type.NAME) {
                throw this.error(eventTok, "ожидалось имя события, найдено " + eventTok);
            }
            final String event = eventTok.text();
            this.expectOp(":");
            return new Ast.OnEvent(t.line(), event, this.block());
        }
        if (t.isName("command")) {
            final String name = this.commandName();
            final List<String> params = new ArrayList<>();
            while (this.peek().type() == Type.NAME) {
                params.add(this.identifier("имя аргумента команды"));
            }
            this.expectOp(":");
            final Map<String, Object> meta = new LinkedHashMap<>();
            final List<Ast.Stmt> body = this.block(meta);
            return new Ast.CommandDecl(t.line(), name, params, meta, body);
        }
        if (t.isName("every")) {
            final Token amount = this.next();
            if (amount.type() != Type.NUMBER || amount.number() <= 0) {
                throw this.error(amount, "после 'every' ожидается положительное число");
            }
            final long unit = this.unit();
            final double ticks = amount.number() * unit;
            if (ticks < 1 || ticks > Integer.MAX_VALUE) {
                throw this.error(amount, "интервал должен быть от 1 тика");
            }
            this.expectOp(":");
            return new Ast.Every(t.line(), Math.round(ticks), this.block());
        }
        if (t.isName("function")) {
            final String name = this.identifier("имя функции");
            this.expectOp("(");
            final List<String> params = new ArrayList<>();
            if (!this.acceptOp(")")) {
                do {
                    params.add(this.identifier("имя параметра"));
                } while (this.acceptOp(","));
                this.expectOp(")");
            }
            this.expectOp(":");
            return new Ast.FunctionDecl(t.line(), name, params, this.block());
        }
        throw this.error(t, "ожидалось 'on', 'command', 'every' или 'function', найдено " + t);
    }

    private String commandName() {
        final Token t = this.next();
        if (t.type() != Type.NAME) {
            throw this.error(t, "ожидалось имя команды, найдено " + t);
        }
        final StringBuilder name = new StringBuilder(t.text());
        while (this.peek().isOp("-") && this.peek(1).type() == Type.NAME) {
            this.pos++;
            name.append('-').append(this.next().text());
        }
        return name.toString().toLowerCase(java.util.Locale.ROOT);
    }

    private long unit() {
        final Token t = this.next();
        if (t.type() == Type.NAME) {
            switch (t.text()) {
                case "tick", "ticks", "t" -> {
                    return 1;
                }
                case "second", "seconds", "sec", "s" -> {
                    return 20;
                }
                case "minute", "minutes", "min", "m" -> {
                    return 1200;
                }
                case "hour", "hours", "h" -> {
                    return 72_000;
                }
                default -> {
                }
            }
        }
        throw this.error(t, "ожидалась единица времени (ticks, seconds, minutes, hours), найдено " + t);
    }

    private List<Ast.Stmt> block() {
        return this.block(null);
    }

    private List<Ast.Stmt> block(final Map<String, Object> meta) {
        final Token nl = this.next();
        if (nl.type() != Type.NEWLINE) {
            throw this.error(nl, "после ':' тело блока пишется с новой строки");
        }
        final Token indent = this.next();
        if (indent.type() != Type.INDENT) {
            throw this.error(indent, "ожидался блок с отступом");
        }
        if (meta != null) {
            while (this.peek().isOp("@")) {
                final Token at = this.next();
                final String key = this.identifier("имя параметра команды");
                final List<String> values = new ArrayList<>();
                do {
                    final Token value = this.next();
                    if (value.type() != Type.STRING || value.parts().size() != 1 || !(value.parts().get(0) instanceof String s)) {
                        throw this.error(value, "@" + key + " ожидает строку без интерполяции");
                    }
                    values.add(s);
                } while (this.acceptOp(","));
                this.endOfStatement();
                if (!Set.of("permission", "description", "usage", "aliases", "permission_message").contains(key)) {
                    throw this.error(at, "неизвестный параметр команды @" + key);
                }
                meta.put(key, key.equals("aliases") ? List.copyOf(values) : values.get(0));
            }
        }
        final List<Ast.Stmt> body = new ArrayList<>();
        while (this.peek().type() != Type.DEDENT && this.peek().type() != Type.EOF) {
            if (this.peek().type() == Type.NEWLINE) {
                this.pos++;
                continue;
            }
            body.add(this.statement());
        }
        if (this.peek().type() == Type.DEDENT) {
            this.pos++;
        }
        if (body.isEmpty()) {
            throw this.error(indent, "пустой блок");
        }
        return body;
    }

    // ---------- инструкции ----------

    private Ast.Stmt statement() {
        final Token t = this.peek();
        if (t.type() == Type.NAME) {
            switch (t.text()) {
                case "if" -> {
                    return this.ifStatement();
                }
                case "while" -> {
                    this.pos++;
                    final Ast.Expr cond = this.expression();
                    this.expectOp(":");
                    return new Ast.While(t.line(), cond, this.block());
                }
                case "repeat" -> {
                    this.pos++;
                    final Ast.Expr count = this.expression();
                    this.expectName("times");
                    this.expectOp(":");
                    return new Ast.Repeat(t.line(), count, this.block());
                }
                case "for" -> {
                    this.pos++;
                    final String var = this.identifier("имя переменной цикла");
                    this.expectName("in");
                    final Ast.Expr iterable = this.expression();
                    this.expectOp(":");
                    return new Ast.ForEach(t.line(), var, iterable, this.block());
                }
                case "set" -> {
                    this.pos++;
                    final Ast.Expr target = this.assignable(this.postfix());
                    this.expectName("to");
                    final Ast.Expr value = this.expression();
                    this.endOfStatement();
                    return new Ast.Assign(t.line(), target, "=", value);
                }
                case "send" -> {
                    this.pos++;
                    final Ast.Expr msg = this.expression();
                    Ast.Expr target = null;
                    if (this.acceptName("to")) {
                        target = this.expression();
                    }
                    this.endOfStatement();
                    return new Ast.Send(t.line(), msg, target);
                }
                case "broadcast" -> {
                    this.pos++;
                    final Ast.Expr msg = this.expression();
                    this.endOfStatement();
                    return new Ast.Broadcast(t.line(), msg);
                }
                case "console" -> {
                    this.pos++;
                    final Ast.Expr cmd = this.expression();
                    this.endOfStatement();
                    return new Ast.Console(t.line(), cmd);
                }
                case "log" -> {
                    this.pos++;
                    final Ast.Expr msg = this.expression();
                    this.endOfStatement();
                    return new Ast.Log(t.line(), msg);
                }
                case "wait" -> {
                    this.pos++;
                    final Ast.Expr amount = this.expression();
                    final long unit = this.unit();
                    this.endOfStatement();
                    return new Ast.Wait(t.line(), amount, unit);
                }
                case "break" -> {
                    this.pos++;
                    this.endOfStatement();
                    return new Ast.Break(t.line());
                }
                case "continue" -> {
                    this.pos++;
                    this.endOfStatement();
                    return new Ast.Continue(t.line());
                }
                case "stop" -> {
                    this.pos++;
                    this.endOfStatement();
                    return new Ast.Stop(t.line());
                }
                case "cancel" -> {
                    this.pos++;
                    this.endOfStatement();
                    return new Ast.Cancel(t.line());
                }
                case "return" -> {
                    this.pos++;
                    Ast.Expr value = null;
                    if (this.peek().type() != Type.NEWLINE && this.peek().type() != Type.EOF) {
                        value = this.expression();
                    }
                    this.endOfStatement();
                    return new Ast.Return(t.line(), value);
                }
                case "else" -> throw this.error(t, "'else' без соответствующего 'if'");
                default -> {
                }
            }
        }
        final Ast.Expr expr = this.expression();
        final Token op = this.peek();
        if (op.type() == Type.OP && (op.text().equals("=") || op.text().equals("+=") || op.text().equals("-=") || op.text().equals("*=") || op.text().equals("/="))) {
            this.pos++;
            final Ast.Expr value = this.expression();
            this.endOfStatement();
            return new Ast.Assign(t.line(), this.assignable(expr), op.text(), value);
        }
        this.endOfStatement();
        if (!(expr instanceof Ast.Call) && !(expr instanceof Ast.MethodCall)) {
            throw this.error(t, "выражение ничего не делает: ожидался вызов функции, присваивание или команда");
        }
        return new Ast.ExprStmt(t.line(), expr);
    }

    private Ast.Expr assignable(final Ast.Expr expr) {
        if (expr instanceof Ast.Local || expr instanceof Ast.Global || expr instanceof Ast.Prop || expr instanceof Ast.Index) {
            return expr;
        }
        throw new ScriptException(this.file, expr.line(), "этому выражению нельзя присвоить значение");
    }

    private Ast.Stmt ifStatement() {
        final Token t = this.next(); // if
        final List<Ast.Expr> conditions = new ArrayList<>();
        final List<List<Ast.Stmt>> blocks = new ArrayList<>();
        conditions.add(this.expression());
        this.expectOp(":");
        blocks.add(this.block());
        List<Ast.Stmt> elseBlock = null;
        while (this.peek().isName("else")) {
            this.pos++;
            if (this.acceptName("if")) {
                conditions.add(this.expression());
                this.expectOp(":");
                blocks.add(this.block());
            } else {
                this.expectOp(":");
                elseBlock = this.block();
                break;
            }
        }
        return new Ast.If(t.line(), conditions, blocks, elseBlock);
    }

    // ---------- выражения ----------

    Ast.Expr expression() {
        return this.or();
    }

    private Ast.Expr or() {
        Ast.Expr left = this.and();
        while (this.peek().isName("or")) {
            final int line = this.next().line();
            left = new Ast.Binary(line, "or", left, this.and());
        }
        return left;
    }

    private Ast.Expr and() {
        Ast.Expr left = this.not();
        while (this.peek().isName("and")) {
            final int line = this.next().line();
            left = new Ast.Binary(line, "and", left, this.not());
        }
        return left;
    }

    private Ast.Expr not() {
        if (this.peek().isName("not")) {
            final int line = this.next().line();
            return new Ast.Unary(line, "not", this.not());
        }
        return this.comparison();
    }

    private Ast.Expr comparison() {
        final Ast.Expr left = this.additive();
        final Token t = this.peek();
        final String op;
        if (t.type() == Type.OP && Set.of("==", "!=", "<", ">", "<=", ">=").contains(t.text())) {
            op = t.text();
        } else if (t.isName("in")) {
            op = "in";
        } else if (t.isName("not") && this.peek(1).isName("in")) {
            this.pos++;
            op = "not in";
        } else {
            return left;
        }
        this.pos++;
        return new Ast.Binary(t.line(), op, left, this.additive());
    }

    private Ast.Expr additive() {
        Ast.Expr left = this.multiplicative();
        while (this.peek().isOp("+") || this.peek().isOp("-")) {
            final Token op = this.next();
            left = new Ast.Binary(op.line(), op.text(), left, this.multiplicative());
        }
        return left;
    }

    private Ast.Expr multiplicative() {
        Ast.Expr left = this.unary();
        while (this.peek().isOp("*") || this.peek().isOp("/") || this.peek().isOp("%")) {
            final Token op = this.next();
            left = new Ast.Binary(op.line(), op.text(), left, this.unary());
        }
        return left;
    }

    private Ast.Expr unary() {
        if (this.peek().isOp("-")) {
            final int line = this.next().line();
            return new Ast.Unary(line, "-", this.unary());
        }
        return this.postfix();
    }

    private Ast.Expr postfix() {
        Ast.Expr expr = this.primary();
        while (true) {
            if (this.acceptOp(".")) {
                final Token nameTok = this.next();
                if (nameTok.type() != Type.NAME) {
                    throw this.error(nameTok, "после '.' ожидалось имя свойства, найдено " + nameTok);
                }
                if (this.acceptOp("(")) {
                    expr = new Ast.MethodCall(nameTok.line(), expr, nameTok.text(), this.arguments());
                } else {
                    expr = new Ast.Prop(nameTok.line(), expr, nameTok.text());
                }
            } else if (this.peek().isOp("[")) {
                final int line = this.next().line();
                final Ast.Expr index = this.expression();
                this.expectOp("]");
                expr = new Ast.Index(line, expr, index);
            } else {
                return expr;
            }
        }
    }

    private List<Ast.Expr> arguments() {
        final List<Ast.Expr> args = new ArrayList<>();
        if (this.acceptOp(")")) {
            return args;
        }
        do {
            args.add(this.expression());
        } while (this.acceptOp(","));
        this.expectOp(")");
        return args;
    }

    private Ast.Expr primary() {
        final Token t = this.next();
        switch (t.type()) {
            case NUMBER -> {
                return new Ast.Num(t.line(), t.number());
            }
            case STRING -> {
                return this.string(t);
            }
            case GLOBAL -> {
                return new Ast.Global(t.line(), t.text());
            }
            case NAME -> {
                switch (t.text()) {
                    case "true" -> {
                        return new Ast.Bool(t.line(), true);
                    }
                    case "false" -> {
                        return new Ast.Bool(t.line(), false);
                    }
                    case "null", "none" -> {
                        return new Ast.Null(t.line());
                    }
                    default -> {
                    }
                }
                if (KEYWORDS.contains(t.text())) {
                    throw this.error(t, "неожиданное ключевое слово '" + t.text() + "'");
                }
                if (this.acceptOp("(")) {
                    return new Ast.Call(t.line(), t.text(), this.arguments());
                }
                return new Ast.Local(t.line(), t.text());
            }
            case OP -> {
                if (t.text().equals("(")) {
                    final Ast.Expr inner = this.expression();
                    this.expectOp(")");
                    return inner;
                }
                if (t.text().equals("[")) {
                    final List<Ast.Expr> items = new ArrayList<>();
                    if (!this.acceptOp("]")) {
                        do {
                            items.add(this.expression());
                        } while (this.acceptOp(","));
                        this.expectOp("]");
                    }
                    return new Ast.ListLit(t.line(), items);
                }
                throw this.error(t, "неожиданный символ " + t);
            }
            default -> throw this.error(t, "ожидалось значение, найдено " + t);
        }
    }

    private Ast.Expr string(final Token t) {
        final List<Object> parts = new ArrayList<>(t.parts().size());
        for (final Object part : t.parts()) {
            if (part instanceof Lexer.Interpolation interpolation) {
                parts.add(this.parseInterpolation(interpolation));
            } else {
                parts.add(part);
            }
        }
        return new Ast.Str(t.line(), parts);
    }

    private Ast.Expr parseInterpolation(final Lexer.Interpolation interpolation) {
        final List<Token> raw = Lexer.tokenize(this.file, interpolation.source());
        final List<Token> shifted = new ArrayList<>(raw.size());
        for (final Token token : raw) {
            shifted.add(new Token(token.type(), token.text(), interpolation.line(), token.number(), token.parts()));
        }
        final Parser sub = new Parser(this.file, shifted);
        final Ast.Expr expr = sub.expression();
        while (sub.peek().type() == Type.NEWLINE) {
            sub.pos++;
        }
        if (sub.peek().type() != Type.EOF) {
            throw sub.error(sub.peek(), "лишнее в интерполяции: " + sub.peek());
        }
        return expr;
    }
}
