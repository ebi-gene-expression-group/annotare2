package uk.ac.ebi.fg.annotare2.core.magetab;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MageTabFilesTest {

    @Test
    public void testReorganizeFactorValueColumnsWithTermSourceAndAccession() {
        List<String> header = new ArrayList<>(Arrays.asList(
                "Source Name",
                "Factor Value [Age]",
                "Unit [year]",
                "Term Source REF",
                "Term Accession Number",
                "Characteristics [Organism]"
        ));
        List<String> row1 = new ArrayList<>(Arrays.asList(
                "Sample1",
                "10",
                "year",
                "UO",
                "UO:0000036",
                "Homo sapiens"
        ));

        List<List<String>> rows = new ArrayList<>();
        rows.add(header);
        rows.add(row1);

        MageTabFiles.reorganizeFactorValueColumns(rows);

        // Header should have moved Factor Value and related columns to the end
        // Original width was 6. Added Factor Value, Unit, Term Source REF, Term Accession Number (4 columns)
        // Total columns should be 10.
        // Wait, the original Factor Value [Age] and Unit [year] will be cleared in the original position but the header will still have them?
        // Actually, the current code clears the values in rows, but the header columns stay?
        // Let's check the header in the current code.

        /*
        header.addAll(newColumnHeaders);
        */

        // So the header will grow.
        // Original: Source Name (0), Factor Value [Age] (1), Unit [year] (2), Term Source REF (3), Term Accession Number (4), Characteristics [Organism] (5)
        // newColumnHeaders will contain Factor Value [Age], Unit [year]
        // Header after addAll: ... Characteristics [Organism] (5), Factor Value [Age] (6), Unit [year] (7)

        // Then it clears values at original positions in the rows.

        // If I use the CURRENT code, it will NOT move Term Source REF and Term Accession Number.

        List<String> resultHeader = rows.get(0);
        List<String> resultRow1 = rows.get(1);

        // After reorganization with current code:
        // Index 0: Source Name
        // Index 1: Factor Value [Age] (value should be "")
        // Index 2: Unit [year] (value should be "")
        // Index 3: Term Source REF (value should be "UO" - NOT CLEARED and NOT MOVED)
        // Index 4: Term Accession Number (value should be "UO:0000036" - NOT CLEARED and NOT MOVED)
        // Index 5: Characteristics [Organism]
        // Index 6: Factor Value [Age] (value should be "10")
        // Index 7: Unit [year] (value should be "year")

        // Wait, my test should verify that Term Source REF and Term Accession Number ARE moved.

        // Expected result after fix:
        // Index 0: Source Name
        // Index 1: Factor Value [Age] ("")
        // Index 2: Unit [year] ("")
        // Index 3: Term Source REF ("")
        // Index 4: Term Accession Number ("")
        // Index 5: Characteristics [Organism]
        // Index 6: Factor Value [Age] ("10")
        // Index 7: Unit [year] ("year")
        // Index 8: Term Source REF ("UO")
        // Index 9: Term Accession Number ("UO:0000036")

        assertEquals("Factor Value [Age]", resultHeader.get(6));
        assertEquals("Unit [year]", resultHeader.get(7));
        assertEquals("Term Source REF", resultHeader.get(8));
        assertEquals("Term Accession Number", resultHeader.get(9));

        assertEquals("10", resultRow1.get(6));
        assertEquals("year", resultRow1.get(7));
        assertEquals("UO", resultRow1.get(8));
        assertEquals("UO:0000036", resultRow1.get(9));

        // Original positions should be empty
        assertEquals("", resultRow1.get(1));
        assertEquals("", resultRow1.get(2));
        assertEquals("", resultRow1.get(3));
        assertEquals("", resultRow1.get(4));
    }

    @Test
    public void testMultipleFactorValues() {
        List<String> header = new ArrayList<>(Arrays.asList(
                "Source Name",
                "Factor Value [Age]",
                "Unit [year]",
                "Factor Value [Dose]",
                "Unit [mg]",
                "Term Source REF"
        ));
        List<String> row1 = new ArrayList<>(Arrays.asList(
                "Sample1",
                "10",
                "year",
                "50",
                "mg",
                "EFO"
        ));

        List<List<String>> rows = new ArrayList<>();
        rows.add(header);
        rows.add(row1);

        MageTabFiles.reorganizeFactorValueColumns(rows);

        List<String> resultRow = rows.get(1);
        // Original width 6. Added 2 (FV+Unit for Age) + 3 (FV+Unit+TSR for Dose) = 5 columns. Total 11.
        // Index 6: Factor Value [Age] (10)
        // Index 7: Unit [year] (year)
        // Index 8: Factor Value [Dose] (50)
        // Index 9: Unit [mg] (mg)
        // Index 10: Term Source REF (EFO)

        assertEquals("10", resultRow.get(6));
        assertEquals("year", resultRow.get(7));
        assertEquals("50", resultRow.get(8));
        assertEquals("mg", resultRow.get(9));
        assertEquals("EFO", resultRow.get(10));

        assertEquals("", resultRow.get(1));
        assertEquals("", resultRow.get(2));
        assertEquals("", resultRow.get(3));
        assertEquals("", resultRow.get(4));
        assertEquals("", resultRow.get(5));
    }

    @Test
    public void testTermSourceNotMovedIfFollowingCharacteristics() {
        List<String> header = new ArrayList<>(Arrays.asList(
                "Source Name",
                "Characteristics [Organism]",
                "Term Source REF",
                "Factor Value [Age]"
        ));
        List<String> row1 = new ArrayList<>(Arrays.asList(
                "Sample1",
                "Homo sapiens",
                "NCBITaxon",
                "10"
        ));

        List<List<String>> rows = new ArrayList<>();
        rows.add(header);
        rows.add(row1);

        MageTabFiles.reorganizeFactorValueColumns(rows);

        List<String> resultRow = rows.get(1);
        // Original width 4. Added 1 (FV). Total 5.
        // Index 4: Factor Value [Age] (10)

        assertEquals("NCBITaxon", resultRow.get(2)); // Still at index 2
        assertEquals("10", resultRow.get(4));
        assertEquals("", resultRow.get(3)); // Original FV position cleared
    }

    @Test
    public void testConsolidateFactorValuesAndDerivedArrayDataFiles() {
        List<String> header = new ArrayList<>(Arrays.asList(
                "Source Name",
                "Protocol REF", "Protocol REF", "Derived Array Data File", // Group 1
                "Protocol REF", "Protocol REF", "Derived Array Data File", // Group 2
                "Factor Value [disease]", "Factor Value [genotype]",       // FV Group 1
                "Factor Value [disease]", "Factor Value [genotype]"        // FV Group 2 (Duplicate names)
        ));

        // Row with scattered values
        List<String> row1 = new ArrayList<>(Arrays.asList(
                "Sample1",
                "P1", "P2", "counts.txt",
                "P1", "", "", // Scattered Protocol REF
                "HFD", "",
                "", "Genotype1" // Scattered Factor Values
        ));

        List<List<String>> rows = new ArrayList<>();
        rows.add(header);
        rows.add(row1);

        MageTabFiles.consolidateDerivedArrayDataFiles(rows);
        MageTabFiles.reorganizeFactorValueColumns(rows);

        // Check Derived Array Data File consolidation
        assertEquals("P1", rows.get(1).get(1));
        assertEquals("P2", rows.get(1).get(2));
        assertEquals("counts.txt", rows.get(1).get(3));
        assertEquals("P1", rows.get(1).get(4));
        assertEquals("", rows.get(1).get(5));
        assertEquals("", rows.get(1).get(6));

        // Check Factor Value consolidation
        assertEquals(13, rows.get(0).size());
        assertEquals("HFD", rows.get(1).get(11));
        assertEquals("Genotype1", rows.get(1).get(12));
    }
}
