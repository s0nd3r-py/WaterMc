package dev.imanity.antixray.sdk;

import org.jspecify.annotations.Nullable;

public class AntiXraySDK {

    private static @Nullable AntiXrayAdapter ADAPTER;

    public static @Nullable AntiXrayAdapter getAdapter() {
        return ADAPTER;
    }

    public static void setAdapter(AntiXrayAdapter adapter) {
        ADAPTER = adapter;
    }
}
