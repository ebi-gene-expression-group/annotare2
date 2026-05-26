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

package uk.ac.ebi.fg.annotare2.web.gwt.common.client.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import uk.ac.ebi.fg.annotare2.db.model.DataFile;
import uk.ac.ebi.fg.annotare2.db.model.enums.DataFileStatus;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.rpc.ReportingAsyncCallback;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.rpc.ReportingAsyncCallback.FailureMessage;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.exepriment.DataFileRow;
import uk.ac.ebi.fg.gwt.resumable.client.ResumableFile;


import java.util.*;


public class DataFileListPanel extends SimpleLayoutPanel {

    private final CustomDataGrid<DataFileRow> grid;
    private final ListDataProvider<DataFileRow> dataProvider;
    private final MultiSelectionModel<DataFileRow> selectionModel;
    private final HTML emptyTableWidget;
    private final CheckboxHeader checkboxHeader;
    private final Map<Column<?, ?>, Comparator<DataFileRow>> sortComparators = new HashMap<Column<?, ?>, Comparator<DataFileRow>>();
    private final static int MAX_FILES = 40000;

    private long submissionId;
    private Presenter presenter;

    private Column<DataFileRow, String> statusTextColumn;
    private Object rowKeyToRestore;

    public DataFileListPanel() {
        grid = new CustomDataGrid<>(MAX_FILES, true);
        //grid = new DataGrid<>();
        grid.addStyleName("gwt-DataGrid");
        grid.setWidth("100%");
        grid.setHeight("100%");

        selectionModel =
                new MultiSelectionModel<>(new ProvidesKey<DataFileRow>() {
                    @Override
                    public Object getKey(DataFileRow item) {
                        return item.getIdentity();
                    }
                });
        grid.setSelectionModel(selectionModel, DefaultSelectionEventManager.<DataFileRow>createCheckboxManager());
        Column<DataFileRow, Boolean> checkboxColumn = new Column<DataFileRow, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(DataFileRow object) {
                return grid.getSelectionModel().isSelected(object);
            }
        };

        checkboxHeader = new CheckboxHeader();
        checkboxHeader.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                selectAllRows(event.getValue());
            }
        });

        grid.addColumn(checkboxColumn, checkboxHeader);
        grid.setColumnWidth(checkboxColumn, 40, Style.Unit.PX);

        emptyTableWidget = new HTML("<p><br/></p><p><b style=\"font-size:18px;\">Upload Files</b><br/><br/>Drag-and-drop files here to start upload<br/> or press the \"Upload Files\" button.<br/><br/></p>"+
        "<p><i class=\"fa fa-warning\"></i>File names can not contain spaces or special characters except <br/>('_', '-', '.', '#')</p>");
        emptyTableWidget.addStyleName("empty");

        final EditSuggestCell nameCell = new EditSuggestCell(null) {
            @Override
            public boolean validateInput(String value, int rowIndex) {
                if (null == value || trimValue(value).isEmpty()) {
                    NotificationPopupPanel.error("Empty file name is not permitted.", true, false);
                    return false;
                }
                if (!value.matches("^(?!\\#)[_a-zA-Z0-9\\-\\.\\#]+$")) {
                    NotificationPopupPanel.error("File names can not contain spaces or special characters (except '_', '-', '.', '#').", true, false);
                    return false;
                }
                if (isDuplicated(value, rowIndex)) {
                    NotificationPopupPanel.error("File with the name '" + value + "' already exists.", true, false);
                    return false;
                }
                return true;
            }
        };

        Column<DataFileRow, String> nameColumn = new Column<DataFileRow, String>(nameCell) {

            @Override
            public String getValue(DataFileRow row) {
                return row.getName();
            }
        };
        nameColumn.setFieldUpdater(new FieldUpdater<DataFileRow, String>() {
            @Override
            public void update(int index, DataFileRow row, String value) {
                final String oldName = row.getName();
                String newName = trimValue(value);
                rowKeyToRestore = row.getIdentity();
                presenter.renameFile(row, newName, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        rowKeyToRestore = null;
                        NotificationPopupPanel.error("Unable to rename file '" + oldName + "'", true, false);
                    }

                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
            }
        });
        setSortable(nameColumn, new Comparator<DataFileRow>() {
            @Override
            public int compare(DataFileRow left, DataFileRow right) {
                return compareStrings(left.getName(), right.getName());
            }
        });
        grid.addResizableColumn(nameColumn, "Name");
        grid.setColumnWidth(nameColumn, 30, Style.Unit.PCT);



        Column<DataFileRow, Date> dateColumn = new Column<DataFileRow, Date>(new DateCell(DateTimeFormat.getFormat("dd/MM/yy HH:mm"))) {
            @Override
            public Date getValue(DataFileRow object) {
                return object.getCreated();
            }
        };
        setSortable(dateColumn, new Comparator<DataFileRow>() {
            @Override
            public int compare(DataFileRow left, DataFileRow right) {
                return compareDates(left.getCreated(), right.getCreated());
            }
        });
        grid.addResizableColumn(dateColumn, "Date");
        grid.setColumnWidth(dateColumn, 25, Style.Unit.PCT);
        statusTextColumn = new Column<DataFileRow, String>(nameCell) {

            @Override
            public String getValue(DataFileRow row) {
                return row.getStatus().getTitle();
            }
        };
        setSortable(statusTextColumn, new Comparator<DataFileRow>() {
            @Override
            public int compare(DataFileRow left, DataFileRow right) {
                return compareStrings(getStatusTitle(left), getStatusTitle(right));
            }
        });
        statusTextColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        grid.addResizableColumn(statusTextColumn, "Status");
        grid.setColumnWidth(statusTextColumn, 15, Style.Unit.PCT);

        Column<DataFileRow, String> sizeColumn = new Column<DataFileRow, String>(nameCell) {

            @Override
            public String getValue(DataFileRow row) {

                return NumberFormat.getDecimalFormat().format(row.getFileSize());
            }
        };

        sizeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        sizeColumn.setCellStyleNames("fileSizeColumn");
        setSortable(sizeColumn, new Comparator<DataFileRow>() {
            @Override
            public int compare(DataFileRow left, DataFileRow right) {
                return compareLongs(left.getFileSize(), right.getFileSize());
            }
        });

        SafeHtmlHeader header = new SafeHtmlHeader(new SafeHtml() {

            @Override
            public String asString() {
                return "<div style=\"text-align:center;\">File Size (Bytes)</div>";
            }
        });
        grid.addColumn(sizeColumn,header);
        grid.setColumnWidth(sizeColumn, 30, Style.Unit.PCT);


        dataProvider = new ListDataProvider<>();
        dataProvider.addDataDisplay(grid);
        grid.addColumnSortHandler(new ColumnSortEvent.Handler() {
            @Override
            public void onColumnSort(ColumnSortEvent event) {
                sortRows(event);
            }
        });

        grid.setLoadingIndicator(new LoadingIndicator());
        grid.setEmptyTableWidget(emptyTableWidget);
        add(grid);
    }

    public void setDownloadCell(){
        int statusTextColumnIndex = grid.getColumnIndex(statusTextColumn);
        grid.removeColumn(statusTextColumnIndex);
        Column<DataFileRow, DataFileRow> statusText = new Column<DataFileRow, DataFileRow>(new DownloadLinkStatusCell(this)) {
            @Override
            public DataFileRow getValue(DataFileRow object) {
                return object;
            }
        };
        setSortable(statusText, new Comparator<DataFileRow>() {
            @Override
            public int compare(DataFileRow left, DataFileRow right) {
                return compareStrings(getStatusTitle(left), getStatusTitle(right));
            }
        });
        statusText.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        grid.insertResizableColumn(statusText, "Status", statusTextColumnIndex);
        grid.setColumnWidth(statusText, 15, Style.Unit.PCT);
    }

    public void addSelectionChangeHandler(SelectionChangeEvent.Handler handler) {
        selectionModel.addSelectionChangeHandler(handler);
    }

    public Set<DataFileRow> getSelectedRows() {
        return selectionModel.getSelectedSet();
    }

    public void setSubmissionId(long submissionId) {
        this.submissionId = submissionId;
    }

    public void setRows(List<DataFileRow> rows) {
        final Object restoreKey = null != rowKeyToRestore ? rowKeyToRestore : getKeyboardSelectedRowKey();
        final int restoreColumn = grid.getKeyboardSelectedColumn();
        dataProvider.setList(applyOrderingPolicy(rows));
        restoreKeyboardSelection(restoreKey, restoreColumn);
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void deleteSelectedFiles(final AsyncCallback<Void> callback) {
        final Set<DataFileRow> selection = getSelectedRows();
        if (selection.isEmpty()) {
            NotificationPopupPanel.warning("Please select files you would like to delete.", true, false);
        } else if (Window.confirm("The selected files will no longer be available to assign if you delete. Do you want to continue?")) {
            presenter.removeFiles(selection,
                    new ReportingAsyncCallback<Void>(FailureMessage.UNABLE_TO_DELETE_FILES) {
                        @Override
                        public void onSuccess(Void result) {
                            checkboxHeader.setValue(false);
                            selectionModel.clear();
                            callback.onSuccess(result);
                        }
            });
        }
    }

    private void selectAllRows(boolean selected) {
        List<DataFileRow> sublist = dataProvider.getList();
        for (DataFileRow row : sublist) {
            selectionModel.setSelected(row, selected);
        }
    }

    private boolean isDuplicated(String name, int rowIndex) {
        List<DataFileRow> rows = dataProvider.getList();
        int i = 0;
        for (DataFileRow row : rows) {
            if (i != rowIndex && name.equals(row.getName())) {
                return true;
            }
            i++;
        }
        return false;
    }

    public boolean isDuplicated(String fileName) {
        List<DataFileRow> rows = dataProvider.getList();
        for(int i=0; i < rows.size(); i++){
            if(rows.get(i).getName().equalsIgnoreCase(fileName)){
                return true;
            }
        }
        return false;
    }

    private String trimValue(String value) {
        if (null != value) {
            value = value.replaceAll("([^\\t]*)[\\t].*", "$1").trim();
        }
        return value;
    }

    private void setSortable(Column<DataFileRow, ?> column, Comparator<DataFileRow> comparator) {
        column.setSortable(true);
        sortComparators.put(column, comparator);
    }

    private void sortRows(ColumnSortEvent event) {
        Comparator<DataFileRow> comparator = sortComparators.get(event.getColumn());
        if (null == comparator) {
            return;
        }

        final Object restoreKey = getKeyboardSelectedRowKey();
        final int restoreColumn = grid.getKeyboardSelectedColumn();
        List<DataFileRow> rows = dataProvider.getList();
        if (event.isSortAscending()) {
            Collections.sort(rows, comparator);
        } else {
            Collections.sort(rows, reverse(comparator));
        }
        dataProvider.refresh();
        restoreKeyboardSelection(restoreKey, restoreColumn);
    }

    private Comparator<DataFileRow> reverse(final Comparator<DataFileRow> comparator) {
        return new Comparator<DataFileRow>() {
            @Override
            public int compare(DataFileRow left, DataFileRow right) {
                return -comparator.compare(left, right);
            }
        };
    }

    private int compareStrings(String left, String right) {
        if (left == right) {
            return 0;
        } else if (null == left) {
            return -1;
        } else if (null == right) {
            return 1;
        }

        int result = left.compareToIgnoreCase(right);
        return 0 != result ? result : left.compareTo(right);
    }

    private int compareDates(Date left, Date right) {
        if (left == right) {
            return 0;
        } else if (null == left) {
            return -1;
        } else if (null == right) {
            return 1;
        }
        return left.compareTo(right);
    }

    private int compareLongs(long left, long right) {
        return left < right ? -1 : (left == right ? 0 : 1);
    }

    private String getStatusTitle(DataFileRow row) {
        return null != row && null != row.getStatus() ? row.getStatus().getTitle() : null;
    }

    private List<DataFileRow> applyOrderingPolicy(List<DataFileRow> rows) {
        List<DataFileRow> currentRows = dataProvider.getList();
        if (currentRows.isEmpty() || containsNewRows(currentRows, rows)) {
            return new ArrayList<DataFileRow>(rows);
        }
        return rowsInCurrentOrder(currentRows, rows);
    }

    private boolean containsNewRows(List<DataFileRow> currentRows, List<DataFileRow> rows) {
        Set<Object> currentKeys = new HashSet<Object>();
        for (DataFileRow row : currentRows) {
            currentKeys.add(row.getIdentity());
        }

        for (DataFileRow row : rows) {
            if (!currentKeys.contains(row.getIdentity())) {
                return true;
            }
        }
        return false;
    }

    private List<DataFileRow> rowsInCurrentOrder(List<DataFileRow> currentRows, List<DataFileRow> rows) {
        Map<Object, DataFileRow> updatedRows = new HashMap<Object, DataFileRow>();
        for (DataFileRow row : rows) {
            updatedRows.put(row.getIdentity(), row);
        }

        List<DataFileRow> orderedRows = new ArrayList<DataFileRow>();
        for (DataFileRow currentRow : currentRows) {
            DataFileRow updatedRow = updatedRows.get(currentRow.getIdentity());
            if (null != updatedRow) {
                orderedRows.add(updatedRow);
            }
        }
        return orderedRows;
    }

    private Object getKeyboardSelectedRowKey() {
        int rowIndex = grid.getKeyboardSelectedRow() + grid.getPageStart();
        List<DataFileRow> rows = dataProvider.getList();
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            return rows.get(rowIndex).getIdentity();
        }
        return null;
    }

    private void restoreKeyboardSelection(final Object rowKey, final int columnIndex) {
        if (null == rowKey) {
            return;
        }

        final int rowIndex = findRowIndex(rowKey);
        if (rowIndex < 0) {
            clearRestoreKey(rowKey);
            return;
        }

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                int pageStart = grid.getPageStart();
                int relativeRow = rowIndex - pageStart;
                if (relativeRow >= 0 && relativeRow < grid.getPageSize()) {
                    grid.setKeyboardSelectedRow(relativeRow, 0, false);
                    if (columnIndex >= 0 && columnIndex < grid.getColumnCount()) {
                        grid.setKeyboardSelectedColumn(columnIndex, false);
                    }
                }
                clearRestoreKey(rowKey);
            }
        });
    }

    private int findRowIndex(Object rowKey) {
        List<DataFileRow> rows = dataProvider.getList();
        for (int i = 0; i < rows.size(); i++) {
            if (rowKey.equals(rows.get(i).getIdentity())) {
                return i;
            }
        }
        return -1;
    }

    private void clearRestoreKey(Object rowKey) {
        if (rowKey.equals(rowKeyToRestore)) {
            rowKeyToRestore = null;
        }
    }

    public interface Presenter {

        void renameFile(DataFileRow dataFileRow, String newFileName, AsyncCallback<Void> callback);

        void removeFiles(Set<DataFileRow> dataFileRow, AsyncCallback<Void> callback);
    }

    static class DownloadLinkStatusCell extends AbstractCell<DataFileRow> {

        interface Templates extends SafeHtmlTemplates {
            @SafeHtmlTemplates.Template(
                    "<a href=\"{1}\">{0}</a>")
            SafeHtml item(SafeHtml label, SafeUri url);
        }

        private static Templates templates = GWT.create(Templates.class);

        private final DataFileListPanel panel;
        private final String fileDownloadUrl;

        DownloadLinkStatusCell(DataFileListPanel panel) {
            this.panel = panel;
            fileDownloadUrl = GWT.getModuleBaseURL().replace("/" + GWT.getModuleName(), "") + "download";
        }

        @Override
        public void render(Context context, DataFileRow fileRow, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant("<div>");
            if (null != fileRow && fileRow.getStatus() == DataFileStatus.STORED) {
                sb.append(templates.item(
                        SafeHtmlUtils.fromString(fileRow.getStatus().getTitle()),
                        UriUtils.fromString(fileDownloadUrl + "?submissionId=" + panel.submissionId + "&fileId=" + fileRow.getId())
                ));
            } else {
                sb.append(SafeHtmlUtils.fromString(null != fileRow ? fileRow.getStatus().getTitle() : ""));
            }
            sb.appendHtmlConstant("</div>");
        }
    }
}
