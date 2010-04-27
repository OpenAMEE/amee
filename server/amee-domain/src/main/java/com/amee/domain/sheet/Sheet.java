package com.amee.domain.sheet;

import com.amee.domain.ValueType;
import com.amee.domain.Pager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.*;

public class Sheet implements Serializable {

    private List<Column> columns = new ArrayList<Column>();
    private List<Row> rows = new ArrayList<Row>();
    private Set<Cell> cells = new HashSet<Cell>();
    private Choices sortBy = new Choices("SortBy", new ArrayList<Choice>()); // columns to sort by
    private Choices displayBy = new Choices("DisplayBy", new ArrayList<Choice>()); // left-to-right column order
    private String label = "Sheet";
    private String key = "";

    public Sheet() {
        super();
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("label", getLabel());
        JSONArray rowsArr = new JSONArray();
        for (Row row : getRows()) {
            JSONObject rowObj = new JSONObject();
            rowObj.put("label", row.getLabel());
            Map<Column, Cell> cells = row.getCells();
            for (Column col : cells.keySet()) {
                Cell cell = cells.get(col);
                rowObj.put(col.getName(), cell.getValueAsString());
            }
            rowsArr.put(rowObj);
        }
        obj.put("rows", rowsArr);
        return obj;
    }

    public Element getElement(Document document, boolean genericView) {
        Element rowsElement = document.createElement(getLabel());
        for (Row row : getRows()) {
            Element rowElement = document.createElement(row.getLabel());
            Map<Column, Cell> cells = row.getCells();
            for (Column col : cells.keySet()) {
                Cell cell = cells.get(col);
                if (genericView) {
                    rowElement.appendChild(cell.getElement(document));
                } else {
                    if (col.isAttribute()) {
                        rowElement.setAttribute(col.getName(), cell.getValueAsString());
                    } else {
                        Element columnElement = document.createElement(col.getName());
                        columnElement.setTextContent(cell.getValueAsString());
                        rowElement.appendChild(columnElement);
                    }
                }
            }
            rowsElement.appendChild(rowElement);
        }
        return rowsElement;
    }

    public static Sheet getCopy(Sheet sheet) {
        return Sheet.getCopy(sheet, true);
    }

    public static Sheet getCopy(Sheet sheet, boolean createRows) {
        Sheet newSheet = getNewSheet(sheet);
        if (createRows) {
            for (Row row : sheet.getRows()) {
                getNewRow(newSheet, row);
            }
        }
        return newSheet;
    }

    public static Sheet getCopy(Sheet sheet, List<Choice> selections) {
        Sheet newSheet = Sheet.getCopy(sheet, false);
        Column column;
        Cell cell;
        boolean addRow;
        Map<String, Column> columnsMap = sheet.getColumnsMap();
        for (Row row : sheet.getRows()) {
            addRow = true;
            for (Choice selection : selections) {
                column = columnsMap.get(selection.getName().toLowerCase());
                if (column != null) {
                    cell = row.findCell(column);
                    if (!cell.getValueAsString().equalsIgnoreCase(selection.getValue())) {
                        addRow = false;
                        break;
                    }
                }
            }
            if (addRow) {
                Sheet.getNewRow(newSheet, row);
            }
        }
        return newSheet;
    }

    /**
     * See comment below
     *
     * @param sheet
     * @param filterBy
     * @return
     */
    public static Sheet getFilteredCopy(Sheet sheet, String filterBy) {
        return Sheet.getFilteredCopy(sheet, new Choices("FilterBy", Choice.parseChoices(filterBy)));
    }

    /**
     * filter rules:
     * Only show a row if :
     * cell is an integer and > 0
     * cell is a decimal and > 0
     * cell is a boolean and true
     * cell is a string with length > 0
     * cell is a date or unspecified (e.g filtering on these will make no difference)
     * (logical AND for multiple choices)
     *
     * @param sheet
     * @param filterBy
     * @return
     */
    public static Sheet getFilteredCopy(Sheet sheet, Choices filterBy) {
        Sheet newSheet = Sheet.getCopy(sheet, false);
        Column column;
        Cell cell;
        boolean addRow;
        Map<String, Column> columnsMap = sheet.getColumnsMap();
        for (Row row : sheet.getRows()) {
            addRow = true;
            for (Choice filter : filterBy.getChoices()) {
                column = columnsMap.get(filter.getName().toLowerCase());
                if (column != null) {
                    cell = row.findCell(column);
                    if (cell.getValueType().equals(ValueType.INTEGER) && Integer.parseInt(cell.getValueAsString()) == 0) {
                        addRow = false;
                        break;
                    } else if (cell.getValueType().equals(ValueType.BOOLEAN)
                            && !Boolean.parseBoolean(cell.getValueAsString())) {
                        addRow = false;
                        break;
                    } else if (cell.getValueType().equals(ValueType.TEXT)
                            && cell.getValueAsString().length() == 0) {
                        addRow = false;
                        break;
                    } else if (cell.getValueType().equals(ValueType.DECIMAL)
                            && Double.parseDouble(cell.getValueAsString()) == 0) {
                        addRow = false;
                        break;
                    } else if (cell.getValueType().equals(ValueType.DATE)) {
                        addRow = false;
                        break;
                    } else if (cell.getValueType().equals(ValueType.UNSPECIFIED)
                            || cell.getValueType().equals(ValueType.DATE)) {
                        addRow = false;
                        break;
                    }
                }
            }
            if (addRow) {
                Sheet.getNewRow(newSheet, row);
            }
        }
        return newSheet;
    }

    public static Sheet getCopy(Sheet sheet, Pager pager) {
        Sheet newSheet = Sheet.getCopy(sheet, false);
        List<Row> rows = sheet.getRows();
        pager.setItems(rows.size());
        pager.goRequestedPage();
        for (long i = pager.getStart(); (i < pager.getTo()) && (i < rows.size()); i++) {
            Sheet.getNewRow(newSheet, rows.get((int) i));
        }
        pager.setItemsFound(newSheet.getRows().size());
        return newSheet;
    }

    private static Sheet getNewSheet(Sheet sheet) {
        // create the Sheet
        Sheet newSheet = new Sheet();
        newSheet.setSortBy(Choices.getNewChoices(sheet.getSortBy()));
        newSheet.setDisplayBy(Choices.getNewChoices(sheet.getDisplayBy()));
        newSheet.setLabel(sheet.getLabel());
        newSheet.setKey(sheet.getKey());
        // add the columns
        for (Column column : sheet.getColumns()) {
            new Column(newSheet, column.getName(), column.getLabel(), column.isAttribute(), column.getSortOrder());
        }
        return newSheet;
    }

    private static Row getNewRow(Sheet sheet, Row row) {
        Map<String, Column> newColumns = sheet.getColumnsMap();
        Row newRow = new Row(sheet, row.getUid(), row.getLabel());
        Map<Column, Cell> cells = row.getCells();
        for (Column column : cells.keySet()) {
            Cell cell = cells.get(column);
            if (cell != null) {
                new Cell(
                        newColumns.get(column.getName().toLowerCase()),
                        newRow,
                        cell.getValue(),
                        cell.getUid(),
                        cell.getValueType());
            }
        }
        return newRow;
    }

    public Map<String, Column> getColumnsMap() {
        Map<String, Column> columnsMap = new HashMap<String, Column>();
        for (Column column : getColumns()) {
            columnsMap.put(column.getName().toLowerCase(), column);
        }
        return columnsMap;
    }

    public void add(Column column) {
        getColumns().add(column);
    }

    public void add(Row row) {
        getRows().add(row);
    }

    public void add(Cell cell) {
        getCells().add(cell);
    }

    public void sortColumns() {
        int index = 0;
        for (Choice choice : getDisplayBy().getChoices()) {
            sortColumn(choice.getValue(), index);
            index++;
        }
    }

    protected void sortColumn(String name, int pos) {
        if (name != null) {
            Column column;
            List<Column> columns = getColumns();
            for (int index = 0; index < columns.size(); index++) {
                column = columns.get(index);
                if (column.getName().equalsIgnoreCase(name)) {
                    columns.remove(index);
                    if (pos < columns.size()) {
                        columns.add(pos, column);
                    } else {
                        columns.add(column);
                    }
                    break;
                }
            }
        }
    }

    public void sortRows() {
        Collections.sort(rows);
    }

    public void addSortBy(String value) {
        getSortBy().getChoices().add(new Choice(value));
    }

    public void setSortBy(String values) {
        setSortBy(new Choices("SortBy", Choice.parseChoices(values)));
    }

    public void addDisplayBy(String value) {
        getDisplayBy().getChoices().add(new Choice(value));
    }

    public void setDisplayBy(String values) {
        setDisplayBy(new Choices("DisplayBy", Choice.parseChoices(values)));
    }

    public List<String> getDistinctValues(String name) {
        Column column = getColumn(name);
        if (column != null) {
            return column.getDistinctValues();
        } else {
            return new ArrayList<String>();
        }
    }

    public List<Choice> getChoices(String name) {
        List<Choice> choices = new ArrayList<Choice>();
        for (String distinctValue : getDistinctValues(name)) {
            choices.add(new Choice(distinctValue));
        }
        return choices;
    }

    public Column getColumn(String name) {
        for (Column column : getColumns()) {
            if (column.getName().equalsIgnoreCase(name)) {
                return column;
            }
        }
        return null;
    }

    public void remove(Cell cell) {
        getCells().remove(cell);
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Row> getRows() {
        return rows;
    }

    public Set<Cell> getCells() {
        return cells;
    }

    public Choices getSortBy() {
        return sortBy;
    }

    public void setSortBy(Choices sortBy) {
        if (sortBy != null) {
            this.sortBy = sortBy;
        }
    }

    public Choices getDisplayBy() {
        return displayBy;
    }

    public void setDisplayBy(Choices displayBy) {
        if (displayBy != null) {
            this.displayBy = displayBy;
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        if (label == null) {
            label = "Sheet";
        }
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
