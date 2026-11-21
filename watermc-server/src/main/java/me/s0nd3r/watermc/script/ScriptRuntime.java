package me.s0nd3r.watermc.script;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Общие службы для всех скриптов: сервер, глобальные переменные, функции, лимиты. */
final class ScriptRuntime {

    private final ScriptHost host;
    private final GlobalStore globals;
    private final long stepLimit;
    final Map<String, Compiler.CompiledFunction> functions = new ConcurrentHashMap<>();

    ScriptRuntime(final ScriptHost host, final GlobalStore globals, final long stepLimit) {
        this.host = host;
        this.globals = globals;
        this.stepLimit = stepLimit;
    }

    ScriptHost host() {
        return this.host;
    }

    GlobalStore globals() {
        return this.globals;
    }

    long stepLimit() {
        return this.stepLimit;
    }
}
