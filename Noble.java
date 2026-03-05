package com.splendor.model;

public class Noble {

    public int prestigePoints = 3;
    private int id;
    private GemCollection requirements;

    public Noble(int id, GemCollection requirements) {
        this.id = id;
        this.requirements = requirements;
    }

    public int getId() {
        return id;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public GemCollection getRequirements() {
        return requirements;
    }

    public boolean isSatisfiedBy(GemCollection playerBonuses) {
        for (GemColor color: GemColor.values()) {
            if (playerBonuses.get(color) < requirements.get(color)) {
                return false;
            }
        }
        return true;
    }
}
