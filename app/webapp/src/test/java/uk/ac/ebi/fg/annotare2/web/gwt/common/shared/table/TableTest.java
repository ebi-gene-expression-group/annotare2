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

package uk.ac.ebi.fg.annotare2.web.gwt.common.shared.table;

import com.google.common.base.Function;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Collections2.transform;
import static org.junit.Assert.*;

/**
 * @author Olga Melnichuk
 */
public class TableTest {

    @Test
    public void testTableCreation() {

        createAndTest(
                new ArrayList<String>(),
                new ArrayList<String>());

        createAndTest(
                new ArrayList<String>(),
                asList("0"),
                asList("0", "1"),
                asList("0", "1", "2"));

        createAndTest(
                new ArrayList<String>(),
                asList("0"),
                asList("0", "1"),
                asList("0", "1", "2"),
                new ArrayList<String>(),
                asList("0", "1"));

        createAndTest(
                asList("", "", "", null)
        );
    }

    private void createAndTest(List<String>... rows) {
        Table table = new Table();
        for (List<String> row : rows) {
            table.addRow(row);
        }
        assertTableEqualsTo(table, asList(rows));
    }

    private void assertTableEqualsTo(Table table, List<List<String>> rows) {
        if (rows.isEmpty()) {
            assertEquals("An empty table should correspond to the empty set of input rows", 0, table.getHeight());
            return;
        }

        int trimmedWidth = Collections.max(
                transform(rows, new Function<List<String>, Integer>() {
                    public Integer apply(@Nullable List<String> input) {
                        int size = 0;
                        for (String s : input) {
                            if (!isNullOrEmpty(s)) {
                                size++;
                            }
                        }
                        return size;
                    }
                }));

        assertEquals("The height of the table should be equal to the number of input rows",
                rows.size(), table.getHeight());
        assertEquals("The trimmed width of the table should be equal to the max trimmed width of the input rows",
                trimmedWidth, table.getTrimmedWidth());

        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            int rowSize = 0;
            for (int j = 0; j < row.size(); j++) {
                String value = table.getValueAt(i, j);
                if (!isNullOrEmpty(row.get(j))) {
                    assertNotNull("Not empty values should be added to the table", value);
                    assertEquals("Not empty values should be equal to the corresponding values in the input",
                            row.get(j), value);
                    rowSize++;
                } else {
                    assertNull("Empty or null values should be ignored in the table (until you set it intentionally)", value);
                }
            }

            assertEquals("Untrimmed width of a row should be exactly equal to the size of corresponding input row",
                    row.size(), table.getRow(i).getSize());

            assertEquals("Table should be able to trim out empty values",
                    rowSize, table.getRow(i).getTrimmedSize());
        }
    }

    @Test
    public void testTableCleanUpWithTermSourceAndAccession() {
        Table table = new Table();
        table.addRow(asList("Sample Name", "Factor Value [Age]", "Unit", "Term Source REF", "Term Accession Number"));
        table.addRow(asList("Sample 1", "10", "year", "UO", "http://purl.obolibrary.org/obo/UO_0000036"));

        table.cleanUp();

        assertEquals(5, table.getWidth());
        assertEquals("Sample Name", table.getValueAt(0, 0));
        assertEquals("Factor Value [Age]", table.getValueAt(0, 1));
        assertEquals("Unit", table.getValueAt(0, 2));
        assertEquals("Term Source REF", table.getValueAt(0, 3));
        assertEquals("Term Accession Number", table.getValueAt(0, 4));

        assertEquals("Sample 1", table.getValueAt(1, 0));
        assertEquals("10", table.getValueAt(1, 1));
        assertEquals("year", table.getValueAt(1, 2));
        assertEquals("UO", table.getValueAt(1, 3));
        assertEquals("http://purl.obolibrary.org/obo/UO_0000036", table.getValueAt(1, 4));
    }

    @Test
    public void testTableCleanUpWithCharacteristics() {
        Table table = new Table();
        table.addRow(asList("Sample Name", "Characteristics [Organism]", "Factor Value [Age]", "Unit", "Term Source REF"));
        table.addRow(asList("Sample 1", "Homo sapiens", "10", "year", "UO"));

        table.cleanUp();

        assertEquals(5, table.getWidth());
        assertEquals("Sample Name", table.getValueAt(0, 0));
        assertEquals("Characteristics [Organism]", table.getValueAt(0, 1));
        assertEquals("Factor Value [Age]", table.getValueAt(0, 2));
        assertEquals("Unit", table.getValueAt(0, 3));
        assertEquals("Term Source REF", table.getValueAt(0, 4));

        assertEquals("Sample 1", table.getValueAt(1, 0));
        assertEquals("Homo sapiens", table.getValueAt(1, 1));
        assertEquals("10", table.getValueAt(1, 2));
        assertEquals("year", table.getValueAt(1, 3));
        assertEquals("UO", table.getValueAt(1, 4));
    }

    private static <T> List<T> asList(T... array) {
        List<T> list = new ArrayList<T>();
        Collections.addAll(list, array);
        return list;
    }
}
