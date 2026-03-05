package com.splendor.model;

import java.util.Map;

public class GemCost {
    // for hash maps you must use a reference datatype --> Integer not int 
    private final Map<GemColour, Integer> cost;

    private GemCollection(Map<GemColor, Integer> gems) {
        // Initialise map
    }

    public static GemCollection empty() { 
        // code
    }

    public static GemCollection of(GemColor color, int amount) { 
        // code
    }

    public int get(GemColor color) { 
        // code 
    }

    public int total() { 
        // code
    }

    public boolean isEmpty() { 
        // code
    }

    public Map<GemColor, Integer> asMap() { 
        // code
    }

    public GemCollection add(GemCollection other) { 
        // code
    }

    public GemCollection subtract(GemCollection other) { 
        // code
    }
    public boolean canSubtract(GemCollection other) { 
        // code
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {

        public Builder() {}
        public Builder(GemCollection base) {}

        public Builder set(GemColor color, int amount) { return null; }
        public Builder add(GemColor color, int amount) { return null; }
        public GemCollection build() { return null; }
    }
}