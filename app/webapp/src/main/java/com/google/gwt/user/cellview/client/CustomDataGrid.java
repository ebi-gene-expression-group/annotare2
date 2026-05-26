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

package com.google.gwt.user.cellview.client;


import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CustomScrollPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.view.client.CellPreviewEvent;

import java.util.List;

import static com.google.gwt.dom.client.Style.Unit.PX;
import static java.lang.Math.max;

/**
 * DataGrid with resizable columns.
 *
 * @author Olga Melnichuk
 */
public class CustomDataGrid<T> extends DataGrid<T> {

    public interface CustomStyle extends DataGrid.Style {
        String dataGridKeyboardSelectedInactiveCell();
    }

    public interface CustomResources extends DataGrid.Resources {

        @Override
        @Source("CustomDataGrid.css")
        CustomStyle dataGridStyle();

    }

    public static CustomResources createResources() {
        return GWT.create(CustomResources.class);
    }

    private final CustomResources resources;
    private final boolean isResizable;

    public CustomDataGrid(int pageSize) {
        this(pageSize, createResources());
    }

    public CustomDataGrid(int pageSize, boolean isResizable) {
        this(pageSize, createResources(), isResizable);
    }

    public CustomDataGrid(int pageSize, CustomResources resources) {
        this(pageSize, resources, true);
    }

    public CustomDataGrid(int pageSize, CustomResources resources, boolean isResizable) {
        super(pageSize, resources);
        this.resources = resources;
        this.isResizable = isResizable;
        setKeyboardSelectionHandler(new CustomDataGridKeyboardSelectionHandler<T>(this));
    }

    @Override
    public void setRowData(int start, List<? extends T> values) {
        HeaderPanel header = (HeaderPanel) getWidget();
        final CustomScrollPanel scrollPanel = (CustomScrollPanel) header.getContentWidget();
        final int hPos = scrollPanel.getHorizontalScrollbar().getHorizontalScrollPosition();
        final int vPos = scrollPanel.getVerticalScrollPosition();

        super.setRowData(start, values);

        Scheduler.get().scheduleDeferred(new Command() {
            public void execute() {
                scrollPanel.setHorizontalScrollPosition(hPos);
                scrollPanel.setVerticalScrollPosition(vPos);
            }
        });
    }

    private int getVisibleWidth() {
        HeaderPanel header = (HeaderPanel) getWidget();
        return header.getContentWidget().getOffsetWidth();
    }

    public void addResizableColumn(Column<T, ?> column, String title) {
        insertResizableColumn(column, title, getColumnCount());
    }

    public void insertResizableColumn(Column<T, ?> column, String title, int beforeIndex, String helpText) {
        if (isResizable) {
            insertColumn(beforeIndex, column, new MyResizableHeader<T>(title, column, this, helpText), null);
        } else {
            insertColumn(beforeIndex, column, title);
        }
    }

    public void insertResizableColumn(Column<T, ?> column, String title, int beforeIndex) {
        if (isResizable) {
            insertColumn(beforeIndex, column, new MyResizableHeader<T>(title, column, this), null);
        } else {
            insertColumn(beforeIndex, column, title);
        }
    }

    @Override
    public void insertColumn(int beforeIndex, Column<T, ?> col, Header<?> header, Header<?> footer) {
        if (isResizable && getColumnCount() == 0) {
            Column<T, ?> lastColumn = new Column<T, String>(new TextCell()) {
                @Override
                public String getValue(T object) {
                    return "";
                }
            };
            super.insertColumn(0, lastColumn, (Header<?>)null/*new MyResizableHeader<T>("", lastColumn, this)*/, null);
            beforeIndex = 1;
        }
        if (isResizable && getColumnCount() > 0 && beforeIndex == getColumnCount()) {
            beforeIndex -= 1;
        }
        super.insertColumn(beforeIndex, col, header, footer);
    }

    protected int getTableBodyHeight() {
        return getTableBodyElement().getOffsetHeight();
    }

    protected void resizeColumn(int colIndex, int colWidth) {
        int residualWidth = 0;
        for (int i = 0; i < getColumnCount() - 1; i++) {
            if (i != colIndex) {
                residualWidth += getHeaderOffsetWidth(i);
            }
        }

        int lastColIndex = getColumnCount() - 1;
        int tableWidth = tableHeader.getOffsetWidth();
        int minWidth = getVisibleWidth();
        if (colIndex == lastColIndex) {
            int newTableWidth = max(residualWidth + colWidth, minWidth);
            int newColumnWidth = max(newTableWidth - residualWidth, 0);
            setTableWidth(residualWidth + newColumnWidth, PX);
            return;
        }
        int currColWidth = getHeaderOffsetWidth(colIndex);
        int lastColWidth = getHeaderOffsetWidth(lastColIndex);
        int borderWidth = tableWidth - residualWidth - currColWidth - lastColWidth;

        int newTableWidth = residualWidth + colWidth + borderWidth;
        lastColWidth = max(minWidth - newTableWidth, 0);

        setColumnWidth(colIndex, colWidth + "px");
        setTableWidth(newTableWidth + lastColWidth, PX);
    }

    protected void resizeColumn(Column<T, ?> target, int colWidth) {
        setColumnWidth(target, colWidth + "px");
        //resizeColumn(getColumnIndex(target), colWidth);
    }

    public void fix() {
        removeUnusedDataGridColumns();
        //adjustWidth();
    }

    // a workaround (for more details see: http://code.google.com/p/google-web-toolkit/issues/detail?id=6711)
    private void removeUnusedDataGridColumns() {
        int columnCount = getColumnCount();
        NodeList<Element> colGroups = getElement().getElementsByTagName("colgroup");

        for (int i = 0; i < colGroups.getLength(); i++) {
            Element colGroupEle = colGroups.getItem(i);
            NodeList<Element> colList = colGroupEle.getElementsByTagName("col");

            for (int j = colList.getLength() - 2; j >= columnCount - 1; j--) {
                colGroupEle.removeChild(colList.getItem(j));
            }
        }
    }

    private void adjustWidth() {
        int residualWidth = 0;
        int lastColIndex = getColumnCount() - 1;
        for (int i = 0; i < lastColIndex; i++) {
            residualWidth += getHeaderOffsetWidth(i);
        }

        int minWidth = getVisibleWidth();
        if (residualWidth < minWidth) {
            setTableWidth(minWidth, PX);
            return;
        }
        setTableWidth(residualWidth, PX);
    }

    protected int getHeaderOffsetWidth(int col) {
        checkColumnBounds(col);
        return tableHeader.ensureTableColElement(col).getOffsetWidth();
    }

    private void checkColumnBounds(int col) {
        if (col < 0 || col >= getColumnCount()) {
            throw new IndexOutOfBoundsException("Column index out of bound: " + col);
        }
    }

    static class MyResizableHeader<T> extends ResizableHeader<T> {
        private final CustomDataGrid<T> dataGrid;

        public MyResizableHeader(String title, Column<T, ?> column, CustomDataGrid<T> dataGrid) {
            this(title,column, dataGrid, "");
        }

        public MyResizableHeader(String title, Column<T, ?> column, CustomDataGrid<T> dataGrid, String columnHelpText) {
            super(title, dataGrid, column, columnHelpText);
            this.dataGrid = dataGrid;
        }

        @Override
        protected int getTableBodyHeight() {
            return dataGrid.getTableBodyHeight();
        }

        @Override
        protected void resizeColumn(Column<T, ?> column, int width) {
            dataGrid.resizeColumn(column, width);
        }
    }

    @Override
    protected void setKeyboardSelected(int index, boolean selected, boolean stealFocus) {
        if (KeyboardSelectionPolicy.DISABLED == getKeyboardSelectionPolicy()
                || !isRowWithinBounds(index)) {
            return;
        }

        TableRowElement tr = getSubRowElement(index + getPageStart(), getKeyboardSelectedSubRow());
        if (null != tr) {
            NodeList<TableCellElement> cells = tr.getCells();
            for (int i = 0; i < cells.getLength(); i++) {
                TableCellElement td = cells.getItem(i);
                td.removeClassName(getStyle().dataGridKeyboardSelectedInactiveCell());
            }
        }

        super.setKeyboardSelected(index, selected, stealFocus);
    }

    @Override
    protected void onFocus() {
        TableCellElement td = getKeyboardSelectedTableCellElement();
        if (td != null) {
            TableRowElement tr = td.getParentElement().cast();
            td.replaceClassName(getStyle().dataGridKeyboardSelectedInactiveCell(), getStyle().dataGridKeyboardSelectedCell());
            setRowStyleName(tr, getStyle().dataGridKeyboardSelectedRow(), getStyle().dataGridKeyboardSelectedRowCell(), true);
        }
    }

    @Override
    protected void onBlur() {
        TableCellElement td = getKeyboardSelectedTableCellElement();
        if (td != null) {
            TableRowElement tr = td.getParentElement().cast();
            td.replaceClassName(getStyle().dataGridKeyboardSelectedCell(), getStyle().dataGridKeyboardSelectedInactiveCell());
            setRowStyleName(tr, getStyle().dataGridKeyboardSelectedRow(), getStyle().dataGridKeyboardSelectedRowCell(), false);
        }
    }

    @Override
    protected void replaceAllChildren(List<T> values, SafeHtml html) {
        super.replaceAllChildren(values, html);
        this.onBlur();
    }

    private CustomStyle getStyle() {
        return resources.dataGridStyle();
    }

    private TableCellElement getKeyboardSelectedTableCellElement() {
        int colIndex = getKeyboardSelectedColumn();
        if (colIndex < 0) {
            return null;
        }

        // Do not use getRowElement() because that will flush the presenter.
        int rowIndex = getKeyboardSelectedRow();
        if (rowIndex < 0 || rowIndex >= getTableBodyElement().getRows().getLength()) {
            return null;
        }
        TableRowElement tr = getSubRowElement(rowIndex + getPageStart(), getKeyboardSelectedSubRow());
        if (tr != null) {
            int cellCount = tr.getCells().getLength();
            if (cellCount > 0) {
                int column = Math.min(colIndex, cellCount - 1);
                return tr.getCells().getItem(column);
            }
        }
        return null;
    }

    private void setRowStyleName(TableRowElement tr, String rowStyle, String cellStyle, boolean add) {
        setStyleName(tr, rowStyle, add);
        NodeList<TableCellElement> cells = tr.getCells();
        for (int i = 0; i < cells.getLength(); i++) {
            setStyleName(cells.getItem(i), cellStyle, add);
        }
    }


    public static class CustomDataGridKeyboardSelectionHandler<T> extends
            DefaultKeyboardSelectionHandler<T> {

        private AbstractCellTable<T> table;

        public CustomDataGridKeyboardSelectionHandler(AbstractCellTable<T> table) {
            super(table);
            this.table = table;
        }

        @Override
        public AbstractCellTable<T> getDisplay() {
            return table;
        }

        @Override
        public void onCellPreview(CellPreviewEvent<T> event) {
            NativeEvent nativeEvent = event.getNativeEvent();
            String eventType = event.getNativeEvent().getType();
            if (BrowserEvents.KEYDOWN.equals(eventType) && !event.isCellEditing()) {
        /*
         * Handle keyboard navigation, unless the cell is being edited. If the
         * cell is being edited, we do not want to change rows.
         *
         * Prevent default on navigation events to prevent default scrollbar
         * behavior.
         */
                int oldColumn = table.getKeyboardSelectedColumn();
                boolean isRtl = LocaleInfo.getCurrentLocale().isRTL();
                int keyCodeNext = isRtl ? KeyCodes.KEY_LEFT : KeyCodes.KEY_RIGHT;
                int keyCodePrevious = isRtl ? KeyCodes.KEY_RIGHT : KeyCodes.KEY_LEFT;
                int keyCode = nativeEvent.getKeyCode();
                int newColumn = oldColumn;
                if (keyCode == keyCodeNext) {
                    newColumn = findInteractiveColumn(oldColumn, 1);
                    handledEvent(event);
                } else if (keyCode == keyCodePrevious) {
                    newColumn = findInteractiveColumn(oldColumn, -1);
                    handledEvent(event);
                }
                if (newColumn != oldColumn) {
                    table.setKeyboardSelectedColumn(newColumn);
                }

            } else if (BrowserEvents.CLICK.equals(eventType) || BrowserEvents.FOCUS.equals(eventType)) {
        /*
         * Move keyboard focus to the clicked column, even if the cell is being
         * edited. Unlike key events, we aren't moving the currently selected
         * row, just updating it based on where the user clicked.
         *
         * Since the user clicked, allow focus to go to a non-interactive
         * column.
         */
                int col = event.getColumn();
                int relRow = event.getIndex() - getDisplay().getPageStart();
                int subrow = event.getContext().getSubIndex();
                if (((table.getKeyboardSelectedColumn() != col)
                        || (table.getKeyboardSelectedRow() != relRow)
                        || (table.getKeyboardSelectedSubRow() != subrow))
                            && isColumnInteractive(col)) {
                    boolean stealFocus = false;
                    if (BrowserEvents.CLICK.equals(eventType)) {
                        // If a natively focusable element was just clicked, then do not
                        // steal focus.
                        Element target = Element.as(event.getNativeEvent().getEventTarget());
                        stealFocus = !CellBasedWidgetImpl.get().isFocusable(target);
                    }

                    // Update the row and subrow.
                    table.setKeyboardSelectedRow(relRow, subrow, stealFocus);

                    // Update the column index.
                    table.setKeyboardSelectedColumn(col, stealFocus);
                    handledEvent(event);
                }

                // Do not cancel the event as the click may have occurred on a Cell.
                return;
            }

            // Let the parent class handle the event.
            super.onCellPreview(event);
        }

        private int findInteractiveColumn(int column, int direction) {
            int newColumn = column;
            while ((newColumn + direction) >= 0 && (newColumn + direction) < table.getColumnCount()) {
                newColumn = newColumn + direction;
                if (isColumnInteractive(newColumn)) {
                    return newColumn;
                }
            }
            return column;
        }

        private boolean isColumnInteractive(int column) {
            return (column >= 0 && column < table.getColumnCount()
                    && table.getColumn(column).getCell() instanceof AbstractEditableCell);
        }
    }
}
