package me.s0nd3r.watermc.core.config;

public enum EnumConfigCategory {
    ASYNC("async"),
    PERF("performance"),
    FIXES("fixes"),
    GAMEPLAY("gameplay-mechanisms"),
    NETWORK("network"),
    MISC("misc");

    private final String baseKeyName;
    private static final EnumConfigCategory[] VALUES = EnumConfigCategory.values();

    EnumConfigCategory(String baseKeyName) {
        this.baseKeyName = baseKeyName;
    }

    public String getBaseKeyName() {
        return this.baseKeyName;
    }

    public static EnumConfigCategory[] getCategoryValues() {
        return VALUES;
    }
}
