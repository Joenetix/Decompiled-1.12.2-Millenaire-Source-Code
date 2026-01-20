package org.millenaire.common.commands;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for Millénaire commands.
 * Ported from 1.12.2 to 1.20.1.
 */
public class CommandUtilities {

    /**
     * Normalize a string for matching (lowercase, remove accents, replace spaces).
     */
    public static String normalizeString(String string) {
        if (string == null)
            return "";
        string = string.replaceAll(" ", "_").toLowerCase();
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        return string.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /**
     * Get villages matching a partial name.
     * TODO: Implement when MillWorldData and Building are fully ported.
     */
    public static List<String> getMatchingVillageNames(String partialName) {
        // Stub - will be implemented when village system is ported
        return new ArrayList<>();
    }
}
