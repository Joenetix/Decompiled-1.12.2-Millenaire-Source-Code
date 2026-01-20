package org.millenaire.common.buildingplan;

import java.util.List;
import org.millenaire.common.culture.Culture;

/**
 * Interface for building plans (both standard and custom).
 * Exact port from 1.12.2, adapted for 1.20.1 Culture location.
 */
public interface IBuildingPlan {
    Culture getCulture();

    List<String> getFemaleResident();

    List<String> getMaleResident();

    String getNameTranslated();

    String getNativeName();

    List<String> getVisitors();
}
