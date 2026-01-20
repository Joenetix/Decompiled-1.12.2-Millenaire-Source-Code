package org.millenaire.utilities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Village utility methods.
 * Ported from original 1.12.2 VillageUtilities.java
 */
public class VillageUtilities {

    /**
     * Get the relation name based on relation value.
     * Used for displaying reputation/standing with villages.
     */
    public static String getRelationName(int relation) {
        if (relation >= 90) {
            return "relation.excellent";
        } else if (relation >= 70) {
            return "relation.verygood";
        } else if (relation >= 50) {
            return "relation.good";
        } else if (relation >= 30) {
            return "relation.decent";
        } else if (relation >= 10) {
            return "relation.fair";
        } else if (relation <= -90) {
            return "relation.openconflict";
        } else if (relation <= -70) {
            return "relation.atrocious";
        } else if (relation <= -50) {
            return "relation.verybad";
        } else if (relation <= -30) {
            return "relation.bad";
        } else if (relation <= -10) {
            return "relation.chilly";
        } else {
            return "relation.neutral";
        }
    }

    /**
     * Get relation tier (0-5 positive, -1 to -5 negative, 0 = neutral).
     */
    public static int getRelationTier(int relation) {
        if (relation >= 90)
            return 5;
        if (relation >= 70)
            return 4;
        if (relation >= 50)
            return 3;
        if (relation >= 30)
            return 2;
        if (relation >= 10)
            return 1;
        if (relation <= -90)
            return -5;
        if (relation <= -70)
            return -4;
        if (relation <= -50)
            return -3;
        if (relation <= -30)
            return -2;
        if (relation <= -10)
            return -1;
        return 0;
    }

    /**
     * Get all server players in a level.
     */
    public static List<ServerPlayer> getServerPlayers(ServerLevel level) {
        return new ArrayList<>(level.players());
    }

    /**
     * Calculate distance between two points (horizontal only).
     */
    public static double horizontalDistance(double x1, double z1, double x2, double z2) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Calculate squared distance between two points (horizontal only).
     * More efficient when you only need comparisons.
     */
    public static double horizontalDistanceSquared(double x1, double z1, double x2, double z2) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        return dx * dx + dz * dz;
    }

    /**
     * Calculate full 3D distance between two points.
     */
    public static double distance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Clamp a value between min and max.
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamp a value between min and max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Linear interpolation between two values.
     */
    public static double lerp(double start, double end, double t) {
        return start + t * (end - start);
    }

    /**
     * Check if a value is in range (inclusive).
     */
    public static boolean inRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Get a weighted random choice.
     * 
     * @param weights Array of weights
     * @param random  Random source (0-1)
     * @return Index of chosen weight
     */
    public static int weightedChoice(int[] weights, double random) {
        int total = 0;
        for (int w : weights) {
            total += w;
        }

        int target = (int) (random * total);
        int cumulative = 0;

        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (target < cumulative) {
                return i;
            }
        }

        return weights.length - 1;
    }
}
