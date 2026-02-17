/*
 * Copyright 2009-2016 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.fg.annotare2.core.magetab;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.IDF;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.SDRF;
import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.magetab.parser.IDFParser;
import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
import uk.ac.ebi.arrayexpress2.magetab.renderer.IDFWriter;
import uk.ac.ebi.arrayexpress2.magetab.renderer.SDRFWriter;
import uk.ac.ebi.fg.annotare2.core.components.EfoSearch;
import uk.ac.ebi.fg.annotare2.submission.model.ExperimentProfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.io.Closeables.close;
import static uk.ac.ebi.fg.annotare2.core.magetab.MageTabGenerator.restoreAllUnassignedValues;
import static uk.ac.ebi.fg.annotare2.core.magetab.MageTabGenerator.restoreOriginalNameValues;

/**
 * @author Olga Melnichuk
 */
public class MageTabFiles {

    private static final Logger log = LoggerFactory.getLogger(MageTabFiles.class);

    private final File idfFile;
    private final File sdrfFile;

    private IDF idf;
    private SDRF sdrf;

    private boolean sanitize;

    private MageTabFiles(File idfFile, File sdrfFile) {
        this(idfFile, sdrfFile, true);
    }

    private MageTabFiles(File idfFile, File sdrfFile, boolean sanitize) {
        this.idfFile = idfFile;
        this.sdrfFile = sdrfFile;
        this.sanitize = sanitize;
    }

    private MageTabFiles init(ExperimentProfile exp, EfoSearch efoSearch) throws IOException, ParseException {
        MAGETABInvestigation generated = (new MageTabGenerator(exp, efoSearch, MageTabGenerator.GenerateOption.REPLACE_NEWLINES_WITH_SPACES)).generate();

        /* Generated MAGE-TAB lacks cell locations, which are good to have during validation.
         * So we have to write files to disk and parse again */

        generated.IDF.sdrfFile.add(sdrfFile.getName());

        IDFWriter idfWriter = null;
        try {
            idfWriter = new IDFWriter(new FileWriter(idfFile));
            idfWriter.write(generated.IDF);
        } finally {
            close(idfWriter, true);
        }

        if (0 == generated.SDRFs.size()) {
            /* Limpopo MAGE-TAB parser has a bug in reading and writing empty files. We have to create an empty file and
             * an empty SDRF as workaround */
            sdrfFile.createNewFile();

            idf = new IDFParser().parse(idfFile);
            sdrf = new SDRF();
        } else {
            SDRFWriter sdrfWriter = null;
            try {
                sdrfWriter = new SDRFWriter(new FileWriter(sdrfFile));
                sdrfWriter.write(generated.SDRFs.values().iterator().next(), false, true);
            } finally {
                close(sdrfWriter, true);
            }

            MAGETABParser parser = new MAGETABParser();
            MAGETABInvestigation inv = parser.parse(idfFile);
            idf = inv.IDF;
            sdrf = inv.SDRFs.values().iterator().next();

            sanitize(sdrfFile, sanitize);

            cleanupSdrfFile(sdrfFile);
        }
        return this;
    }

    public IDF getIdf() {
        return idf;
    }

    public SDRF getSdrf() {
        return sdrf;
    }

    public File getIdfFile() {
        return idfFile;
    }

    public File getSdrfFile() {
        return sdrfFile;
    }

    public static MageTabFiles createMageTabFiles(ExperimentProfile exp, EfoSearch efoSearch, boolean sanitize) throws IOException, ParseException {
        File tmp = Files.createTempDir();
        tmp.deleteOnExit();
        return (new MageTabFiles(new File(tmp, "idf.tsv"), new File(tmp, "sdrf.tsv"), sanitize)).init(exp, efoSearch);
    }

    public static MageTabFiles createMageTabFiles(ExperimentProfile exp, EfoSearch efoSearch, File directory, String idfFileName,
                                                  String sdrfFileName) throws IOException, ParseException {
        return (new MageTabFiles(new File(directory, idfFileName), new File(directory, sdrfFileName))).init(exp, efoSearch);
    }

    /**
     * Substitutes fake values (which were required to build MAGE-TAB graph) to a required values.
     *
     * @param file file to be sanitized
     */
    private static void sanitize(File file, boolean everything) {
        try {
            String str = Files.toString(file, Charsets.UTF_8);
            str = restoreOriginalNameValues(str);

            if (everything) {
                str = restoreAllUnassignedValues(str);
            }
            Files.write(str, file, Charsets.UTF_8);
        } catch (IOException e) {
            log.error("Unable to sanitize MAGE-TAB file" + file.getAbsolutePath(), e);
        }
    }

    /**
     * Cleans up the SDRF file by:
     * 1. Reorganizing Factor Value columns to the end
     * 2. Removing empty columns (where all values after the header are unassigned or empty)
     *
     * @param file the SDRF file to clean up
     */
    private static void cleanupSdrfFile(File file) {
        try {
            String content = Files.toString(file, Charsets.UTF_8);
            String[] lines = content.split("\n");

            if (lines.length < 2) {
                return; // Nothing to clean up
            }

            // Parse the TSV content
            List<List<String>> rows = new ArrayList<>();
            for (String line : lines) {
                List<String> columns = new ArrayList<>(Arrays.asList(line.split("\t", -1)));
                rows.add(columns);
            }

            if (rows.isEmpty()) {
                return;
            }

            // Step 1: Reorganize Factor Value columns
            reorganizeFactorValueColumns(rows);

            // Step 2: Remove empty columns
            removeEmptyColumns(rows);

            // Write back to file
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < rows.size(); i++) {
                result.append(String.join("\t", rows.get(i)));
                if (i < rows.size() - 1) {
                    result.append("\n");
                }
            }

            Files.write(result.toString(), file, Charsets.UTF_8);
        } catch (IOException e) {
            log.error("Unable to cleanup SDRF file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Reorganizes Factor Value columns to the end of the table for consistency.
     */
    private static void reorganizeFactorValueColumns(List<List<String>> rows) {
        if (rows.isEmpty()) {
            return;
        }

        List<String> header = rows.get(0);
        int originalWidth = header.size();

        // Find all Factor Value column indices and their associated Unit columns
        List<Integer> factorValueIndices = new ArrayList<>();
        Map<Integer, Integer> unitColumnMap = new LinkedHashMap<>();

        for (int i = 0; i < originalWidth; i++) {
            if (header.get(i) != null && header.get(i).contains("Factor Value")) {
                factorValueIndices.add(i);
                // Check if the next column is a Unit column
                if (i + 1 < originalWidth && header.get(i + 1) != null && header.get(i + 1).contains("Unit")) {
                    unitColumnMap.put(i, i + 1);
                }
            }
        }

        if (factorValueIndices.isEmpty()) {
            return;
        }

        // Create new columns at the end for Factor Values and Units
        List<String> newColumnHeaders = new ArrayList<>();
        Map<Integer, Integer> oldToNewIdxMap = new LinkedHashMap<>();

        for (int idx : factorValueIndices) {
            oldToNewIdxMap.put(idx, header.size() + newColumnHeaders.size());
            newColumnHeaders.add(header.get(idx));
            if (unitColumnMap.containsKey(idx)) {
                int unitIdx = unitColumnMap.get(idx);
                oldToNewIdxMap.put(unitIdx, header.size() + newColumnHeaders.size());
                newColumnHeaders.add(header.get(unitIdx));
            }
        }

        // Add new column headers at the end
        header.addAll(newColumnHeaders);

        // Process each data row
        for (int rowIdx = 1; rowIdx < rows.size(); rowIdx++) {
            List<String> row = rows.get(rowIdx);

            // Ensure row has enough columns
            while (row.size() < originalWidth) {
                row.add("");
            }

            // For each Factor Value and associated Unit column, move the value to the new location
            for (int oldIdx : oldToNewIdxMap.keySet()) {
                String value = oldIdx < row.size() ? row.get(oldIdx) : "";
                int newIdx = oldToNewIdxMap.get(oldIdx);

                if (!isUnassignedOrEmpty(value)) {
                    // Add value to new position
                    while (row.size() <= newIdx) {
                        row.add("");
                    }
                    row.set(newIdx, value);
                }

                // Clear original position
                if (oldIdx < row.size()) {
                    row.set(oldIdx, "");
                }
            }
        }
    }

    /**
     * Removes columns that are completely empty (all values are unassigned or empty after the header).
     */
    private static void removeEmptyColumns(List<List<String>> rows) {
        if (rows.size() < 2) {
            return;
        }

        List<String> header = rows.get(0);
        int width = header.size();
        List<Integer> columnsToRemove = new ArrayList<>();

        // Identify columns to remove
        for (int colIdx = 0; colIdx < width; colIdx++) {
            // Material Type column should not be deleted even if it is empty
            if ("Material Type".equalsIgnoreCase(header.get(colIdx))) {
                continue;
            }

            boolean isEmpty = true;
            for (int rowIdx = 1; rowIdx < rows.size(); rowIdx++) {
                List<String> row = rows.get(rowIdx);
                String value = colIdx < row.size() ? row.get(colIdx) : "";
                if (!isUnassignedOrEmpty(value)) {
                    isEmpty = false;
                    break;
                }
            }

            if (isEmpty) {
                columnsToRemove.add(colIdx);
            }
        }

        // If there are columns to remove, do it in one pass per row
        if (!columnsToRemove.isEmpty()) {
            for (List<String> row : rows) {
                for (int i = columnsToRemove.size() - 1; i >= 0; i--) {
                    int colIdx = columnsToRemove.get(i);
                    if (colIdx < row.size()) {
                        row.remove(colIdx);
                    }
                }
            }
        }
    }

    /**
     * Checks if a value is considered unassigned or empty.
     */
    private static boolean isUnassignedOrEmpty(String value) {
        return value == null || value.isEmpty() || value.startsWith("____UNASSIGNED____");
    }

}
