package org.millenaire.common.buildingplan;

import java.io.File;
import java.io.FilenameFilter;

/**
 * FilenameFilter for building files with specific endings.
 * Exact port from 1.12.2.
 */
public class BuildingFileFiler implements FilenameFilter {
    String end;

    public BuildingFileFiler(String ending) {
        this.end = ending;
    }

    @Override
    public boolean accept(File file, String name) {
        return !name.endsWith(this.end) ? false : !name.startsWith(".");
    }
}
