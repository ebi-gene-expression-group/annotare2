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
import java.util.List;

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
        List<Integer> factorValueIndices = new ArrayList<>();
        for(int i = 0; i< rowWidth; i++){
            if(rows.get(0).getValue(i).contains("Factor Value")){
                factorValueIndices.add(i);
                noOfAddedColumns++;
                //Add new columns at the end to make it consistant along all samples
                rows.get(0).setValue(rowWidth + noOfAddedColumns - 1, rows.get(0).getValue(i));

                if (i + 1 < rowWidth && rows.get(0).getValue(i + 1).contains("Unit")) {
                    noOfAddedColumns++;
                    rows.get(0).setValue(rowWidth + noOfAddedColumns - 1, rows.get(0).getValue(i + 1));
                }
            }
        }
        for(int i = 1; i<rows.size(); i++){
            int k=0;
            for(int j = 0; j< rowWidth; j++){
                if(rows.get(0).getValue(j).contains("Factor Value")){
                    String factorValueColHeader = rows.get(0).getValue(j);
                    String factorValue = rows.get(i).getValue(j);
                    if(!isUnassignedOrEmpty(factorValue)){
                        //Locate correct factor values column in newly added columns and move original column value to here
                        while(!(rows.get(0).getValue(rowWidth +k).equalsIgnoreCase(factorValueColHeader))){
                            k++;
                        }
                        rows.get(i).setValue(rowWidth +k, factorValue);
                        rows.get(i).setValue(j, null);
                    }
                    k++; // move to next added column (could be Unit or next FV)

                    // Check if it has a unit
                    if (j + 1 < rowWidth && rows.get(0).getValue(j + 1).contains("Unit")) {
                        String unitColHeader = rows.get(0).getValue(j + 1);
                        String unitValue = rows.get(i).getValue(j + 1);
                        if (!isUnassignedOrEmpty(unitValue)) {
                            while (!(rows.get(0).getValue(rowWidth + k).equalsIgnoreCase(unitColHeader))) {
                                k++;
                            }
                            rows.get(i).setValue(rowWidth + k, unitValue);
                            rows.get(i).setValue(j + 1, null);
                        }
                        k++;
                    }
                }
            }
        }
    }

    private boolean isUnassignedOrEmpty(String value) {
        return value == null || value.isEmpty() || value.startsWith("____UNASSIGNED____");
    }
}
