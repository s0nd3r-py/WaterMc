package me.s0nd3r.watermc.core.config.modules.water;

import me.s0nd3r.watermc.script.ScriptEngine;
import me.s0nd3r.watermc.script.bukkit.BukkitScripts;
import me.s0nd3r.watermc.core.config.ConfigModules;

public class ScriptsConfig extends ConfigModules {

    public String getBasePath() {
        return "scripts";
    }

    @Override
    public void onLoaded() {
        BukkitScripts.enabled = config.getBoolean(getBasePath() + ".enabled", true, """
            [WaterMc] Built-in WaterScript engine: *.wsc files in the scripts/ folder.
            Reload with /watermc script reload. See docs/scripting.md.""");
        BukkitScripts.stepLimit = Math.max(1_000L, config.getLong(getBasePath() + ".step-limit", ScriptEngine.DEFAULT_STEP_LIMIT, """
            Maximum operations a script may run before yielding with wait (protects the tick from endless loops)."""));
    }
}
