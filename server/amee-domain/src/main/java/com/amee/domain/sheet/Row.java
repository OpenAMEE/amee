package com.amee.domain.sheet;

import com.amee.base.utils.UidGen;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Row implements Serializable, Comparable {

    private Sheet sheet;
    private Map<Column, Cell> cells = new HashMap<Column, Cell>();
    private String uid = "";
    private String label = "Row";

    private Row() {
        super();
    }

    public Row(Sheet sheet) {
        this();
        setSheet(sheet);
        setUid(UidGen.INSTANCE_12.getUid());
        add();
    }

    public Row(Sheet sheet, String uid) {
        this();
        setSheet(sheet);
        setUid(uid);
        add();
    }

    public Row(Sheet sheet, String uid, String label) {
        this();
        setSheet(sheet);
        setUid(uid);
        setLabel(label);
        add();
    }

    public void add() {
        getSheet().add(this);
    }

    public void add(Cell cell) {
        getCells().put(cell.getColumn(), cell);
    }

    public Cell findCell(Column column) {
        if (column != null) {
            return getCells().get(column);
        } else {
            return null;
        }
    }

    public Cell findCell(String columnName) {
        return findCell(getSheet().getColumn(columnName));
    }

    public void beforeRemove() {
        for (Cell cell : getCells().values()) {
            cell.beforeRemove();
        }
    }

    public boolean equals(Object o) {
        if (super.equals(o)) return true;
        if (!(o instanceof Row)) return false;
        Row other = (Row) o;
        return getUid().equalsIgnoreCase(other.getUid());
    }

    public int compareTo(Object o) {
        if (this.equals(o)) return 0;
        Column column;
        Row other = (Row) o;
        int result;
        // compare cells in columns mentioned in Sheet.sortBy
        for (Choice choice : getSheet().getSortBy().getChoices()) {
            column = sheet.getColumn(choice.getValue());
            if (column != null) {
                result = findCell(column).compareTo(other.findCell(column));
                if (result != 0) {
                    return result;
                }
            }
        }
        return 0; // assume equal
    }

    public int hashCode() {
        return getUid().hashCode();
    }

    public String toString() {
        Cell cell;
        String toString = "";
        for (Choice choice : getSheet().getDisplayBy().getChoices()) {
            cell = findCell(choice.getValue().trim());
            if ((cell != null) && (cell.getValueAsString().length() > 0)) {
                if (toString.length() > 0) {
                    toString = toString.concat(", ");
                }
                toString = toString.concat(cell.getValueAsString());
            }
        }
        if (toString.length() == 0) {
            toString = getUid();
        }
        return toString.trim();
    }

    public Sheet getSheet() {
        return sheet;
    }

    protected void setSheet(Sheet sheet) {
        if (sheet != null) {
            this.sheet = sheet;
        }
    }

    public Map<Column, Cell> getCells() {
        return cells;
    }

    public String getUid() {
        return uid;
    }

    protected void setUid(String uid) {
        if (uid == null) {
            uid = "";
        }
        this.uid = uid;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        if (label == null) {
            label = "Row";
        }
        this.label = label;
    }
}