package com.splendor.model;

import java.util.Map;

public final class GemCollection {

    private final Map<GemColor, Integer> gems;

    private GemCollection(Map<GemColor, Integer> gems) {}

    public static GemCollection empty() { return null; }
    public static GemCollection of(GemColor color, int amount) { return null; }

    public int get(GemColor color) { return 0; }
    public int total() { return 0; }
    public boolean isEmpty() { return false; }
    public Map<GemColor, Integer> asMap() { return null; }

    public GemCollection add(GemCollection other) { return null; }
    public GemCollection subtract(GemCollection other) { return null; }
    public boolean canSubtract(GemCollection other) { return false; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {

        public Builder() {}
        public Builder(GemCollection base) {}

        public Builder set(GemColor color, int amount) { return null; }
        public Builder add(GemColor color, int amount) { return null; }
        public GemCollection build() { return null; }
    }
}
