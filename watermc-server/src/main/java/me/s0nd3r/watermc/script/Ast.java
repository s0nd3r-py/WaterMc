package me.s0nd3r.watermc.script;

import java.util.List;
import java.util.Map;

/** Синтаксическое дерево WaterScript. */
final class Ast {

    private Ast() {
    }

    // ---------- выражения ----------

    sealed interface Expr permits Num, Str, Bool, Null, ListLit, Local, Global, Prop, Index, Call, MethodCall, Unary, Binary {
        int line();
    }

    record Num(int line, double value) implements Expr {
    }

    /** Части: {@link String} или {@link Expr} (интерполяция). */
    record Str(int line, List<Object> parts) implements Expr {
    }

    record Bool(int line, boolean value) implements Expr {
    }

    record Null(int line) implements Expr {
    }

    record ListLit(int line, List<Expr> items) implements Expr {
    }

    record Local(int line, String name) implements Expr {
    }

    record Global(int line, String name) implements Expr {
    }

    record Prop(int line, Expr target, String name) implements Expr {
    }

    record Index(int line, Expr target, Expr index) implements Expr {
    }

    record Call(int line, String name, List<Expr> args) implements Expr {
    }

    record MethodCall(int line, Expr target, String name, List<Expr> args) implements Expr {
    }

    record Unary(int line, String op, Expr operand) implements Expr {
    }

    record Binary(int line, String op, Expr left, Expr right) implements Expr {
    }

    // ---------- инструкции ----------

    sealed interface Stmt permits Assign, ExprStmt, Send, Broadcast, Console, Log, Wait, If, While, Repeat, ForEach, Break, Continue, Stop, Return, Cancel {
        int line();
    }

    /** {@code op}: "=", "+=", "-=", "*=", "/=". */
    record Assign(int line, Expr target, String op, Expr value) implements Stmt {
    }

    record ExprStmt(int line, Expr expr) implements Stmt {
    }

    record Send(int line, Expr message, Expr target) implements Stmt {
    }

    record Broadcast(int line, Expr message) implements Stmt {
    }

    record Console(int line, Expr command) implements Stmt {
    }

    record Log(int line, Expr message) implements Stmt {
    }

    record Wait(int line, Expr amount, long unitTicks) implements Stmt {
    }

    record If(int line, List<Expr> conditions, List<List<Stmt>> blocks, List<Stmt> elseBlock) implements Stmt {
    }

    record While(int line, Expr condition, List<Stmt> body) implements Stmt {
    }

    record Repeat(int line, Expr count, List<Stmt> body) implements Stmt {
    }

    record ForEach(int line, String variable, Expr iterable, List<Stmt> body) implements Stmt {
    }

    record Break(int line) implements Stmt {
    }

    record Continue(int line) implements Stmt {
    }

    record Stop(int line) implements Stmt {
    }

    record Return(int line, Expr value) implements Stmt {
    }

    record Cancel(int line) implements Stmt {
    }

    // ---------- объявления верхнего уровня ----------

    sealed interface Decl permits OnEvent, CommandDecl, Every, FunctionDecl {
        int line();
    }

    record OnEvent(int line, String event, List<Stmt> body) implements Decl {
    }

    record CommandDecl(int line, String name, List<String> params, Map<String, Object> meta, List<Stmt> body) implements Decl {
    }

    record Every(int line, long periodTicks, List<Stmt> body) implements Decl {
    }

    record FunctionDecl(int line, String name, List<String> params, List<Stmt> body) implements Decl {
    }

    record ScriptFile(String file, List<Decl> decls) {
    }
}
