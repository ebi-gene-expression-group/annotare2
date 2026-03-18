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

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Olga Melnichuk
 */
public class Table implements IsSerializable {

    private List<Row> rows = new ArrayList<Row>();

    public int getHeight() {
        return rows.size();
    }

    public int getWidth() {
        int w = 0;
        for (Row r : rows) {
            int rowWidth = r.getSize();
            w = w < rowWidth ? rowWidth : w;
        }
        return w;
    }

    public int getTrimmedWidth() {
        int w = 0;
        for (Row r : rows) {
            int rowWidth = r.getTrimmedSize();
            w = w < rowWidth ? rowWidth : w;
        }
        return w;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public Row addRow(List<String> strings) {
        Row row = new Row();
        rows.add(row);
        int colIndex = 0;
        for (String s : strings) {
            if (!isNullOrEmpty(s)) {
                row.setValue(colIndex, s);
            }
            colIndex++;
        }
        return row;
    }

    public Row addRow() {
        Row row = new Row();
        rows.add(row);
        return row;
    }

    public String getValueAt(int rIndex, int cIndex) {
        checkRowIndex(rIndex);
        return rows.get(rIndex).getValue(cIndex);
    }

    public Row getRow(int rIndex) {
        checkRowIndex(rIndex);
        return rows.get(rIndex);
    }

    private void checkRowIndex(int rIndex) {
        if (rIndex < 0) {
            throw new IndexOutOfBoundsException("Row index is out of bounds [0," + rows.size() + "] : " + rIndex);
        }
        while (rIndex >= rows.size()) {
            addRow();
        }
    }

    public void cleanUp(){
        int rowWidth = getWidth();
        cleanUpFactorValueColumns(rowWidth);
        rowWidth = getWidth(); //getting updated width

        List<Integer> columnsToRemove = new ArrayList<>();

        for(int i = 0; i < rowWidth; i++){
            // Material Type column should not be deleted even if it is empty
            if ("Material Type".equalsIgnoreCase(rows.get(0).getValue(i))) {
                continue;
            }

            boolean isEmpty = true;
            for(int j = 1; j < rows.size(); j++){
                if(!isUnassignedOrEmpty(rows.get(j).getValue(i))){
                    isEmpty = false;
                    break;
                }
            }

            if(isEmpty){
                columnsToRemove.add(i);
            }
        }

        if (!columnsToRemove.isEmpty()) {
            for (Row row : rows) {
                for (int i = columnsToRemove.size() - 1; i >= 0; i--) {
                    int colIdx = columnsToRemove.get(i);
                    row.removeValue(colIdx);
                }
            }
        }
    }

    private void cleanUpFactorValueColumns(int rowWidth) {
        int noOfAddedColumns = 0;
        Map<Integer, Integer> oldToNewIdxMap = new LinkedHashMap<>();

        for (int i = 0; i < rowWidth; i++) {
            String header = rows.get(0).getValue(i);
            if (header != null && header.contains("Factor Value")) {
                noOfAddedColumns++;
                int newFvIdx = rowWidth + noOfAddedColumns - 1;
                rows.get(0).setValue(newFvIdx, header);
                oldToNewIdxMap.put(i, newFvIdx);

                int j = i + 1;
                while (j < rowWidth) {
                    String h = rows.get(0).getValue(j);
                    if (h != null && (h.contains("Unit") || h.contains("Term Source REF") || h.contains("Term Accession Number"))) {
                        noOfAddedColumns++;
                        int newRelIdx = rowWidth + noOfAddedColumns - 1;
                        rows.get(0).setValue(newRelIdx, h);
                        oldToNewIdxMap.put(j, newRelIdx);
                        j++;
                    } else {
                        break;
                    }
                }
                i = j - 1;
            }
        }

        if (oldToNewIdxMap.isEmpty()) {
            return;
        }

        for (int i = 1; i < rows.size(); i++) {
            Row row = rows.get(i);
            for (Map.Entry<Integer, Integer> entry : oldToNewIdxMap.entrySet()) {
                int oldIdx = entry.getKey();
                int newIdx = entry.getValue();
                String value = row.getValue(oldIdx);
                if (!isUnassignedOrEmpty(value)) {
                    row.setValue(newIdx, value);
                    row.setValue(oldIdx, null);
                }
            }
        }
    }

    private boolean isUnassignedOrEmpty(String value) {
        return value == null || value.isEmpty() || value.startsWith("____UNASSIGNED____");
    }
}
