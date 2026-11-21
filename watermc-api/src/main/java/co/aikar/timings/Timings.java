/*
 * This file is licensed under the MIT License (MIT).
 *
 * Copyright (c) 2014 Daniel Ennis <http://aikar.co>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package co.aikar.timings;

// Leaf start - Remove Timings
/**
 * @deprecated Timings will be removed in the future
 * Keep this class for plugin compatibility (detect usage)
 */
@Deprecated(forRemoval = true)
public final class Timings {

    private Timings() {}

    public static boolean isTimingsEnabled() {
        return false;
    }
    public static void setTimingsEnabled(boolean enabled) {
    }
    public static net.kyori.adventure.text.Component deprecationMessage() {
        return net.kyori.adventure.text.Component.empty();
    }
    public static boolean isVerboseTimingsEnabled() {
        return false;
    }
    public static void setVerboseTimingsEnabled(boolean enabled) {
    }
    public static int getHistoryInterval() {
        return 0;
    }
    public static void setHistoryInterval(int interval) {
    }
    public static int getHistoryLength() {
        return 0;
    }
    public static void setHistoryLength(int length) {
    }
    public static void reset() {
    }
    public static void generateReport(@org.jetbrains.annotations.Nullable org.bukkit.command.CommandSender sender) {
    }
    // Leaf end - Remove Timings
}
