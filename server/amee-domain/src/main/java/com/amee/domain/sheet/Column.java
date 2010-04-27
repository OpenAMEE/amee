package com.amee.domain.sheet;

import com.amee.base.utils.XMLUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.*;

public class Column implements Serializable, Comparable {

    private Sheet sheet;
    private String name = "";
    private String label = "";
    private boolean attribute = false;
    private SortOrder sortOrder = SortOrder.ASC;
    private Set<Cell> cells = new HashSet<Cell>();

    private Column() {
        super();
    }

    private Column(Sheet sheet) {
        this();
        setSheet(sheet);
    }

    public Column(Sheet sheet, String name) {
        this(sheet);
        setName(name);
        setLabel(name);
        add();
    }

    public Column(Sheet sheet, String name, boolean attribute) {
        this(sheet);
        setName(name);
        setLabel(name);
        setAttribute(attribute);
        add();
    }

    public Column(Sheet sheet, String name, String label) {
        this(sheet);
        setName(name);
        setLabel(label);
        add();
    }

    public Column(Sheet sheet, String name, String label, boolean attribute) {
        this(sheet);
        setName(name);
        setLabel(label);
        setAttribute(attribute);
        add();
    }

    public Column(Sheet sheet, String name, String label, boolean attribute, SortOrder sortOrder) {
        this(sheet);
        setName(name);
        setLabel(label);
        setAttribute(attribute);
        setSortOrder(sortOrder);
        add();
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("name", getName());
        obj.put("label", getLabel());
        obj.put("atribute", Boolean.toString(isAttribute()));
        obj.put("sortOrder", getSortOrder().toString());
        return obj;
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        return getJSONObject();
    }

    public Element getElement(Document document) {
        return getElement(document, true);
    }

    public Element getElement(Document document, boolean detailed) {
        Element columnElement = document.createElement("Column");
        columnElement.appendChild(XMLUtils.getElement(document, "Name", getName()));
        columnElement.appendChild(XMLUtils.getElement(document, "Label", getLabel()));
        columnElement.appendChild(XMLUtils.getElement(document, "Atribute", Boolean.toString(isAttribute())));
        columnElement.appendChild(XMLUtils.getElement(document, "SortOrder", getSortOrder().toString()));
        return columnElement;
    }

    public void add() {
        getSheet().add(this);
    }

    public void add(Cell cell) {
        getCells().add(cell);
    }

    public List<String> getDistinctValues() {
        List<String> distinctValues = new ArrayList<String>();
        List<Cell> cells = new ArrayList<Cell>();
        cells.addAll(getCells());
        Collections.sort(cells);
        for (Cell cell : cells) {
            if (!distinctValues.contains(cell.getValueAsString())) {
                distinctValues.add(cell.getValueAsString());
            }
        }
        return distinctValues;
    }

    public void remove(Cell cell) {
        getCells().remove(cell);
    }

    public boolean equals(Object o) {
        if (super.equals(o)) return true;
        if (!(o instanceof Column)) return false;
        Column other = (Column) o;
        return getName().equalsIgnoreCase(other.getName());
    }

    public int compareTo(Object o) {
        if (this.equals(o)) return 0;
        Column other = (Column) o;
        return getName().compareToIgnoreCase(other.getName());
    }

    public int hashCode() {
        return getName().toLowerCase().hashCode();
    }

    public String toString() {
        return getName();
    }

    public Sheet getSheet() {
        return sheet;
    }

    protected void setSheet(Sheet sheet) {
        if (sheet != null) {
            this.sheet = sheet;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        if (label == null) {
            label = "";
        }
        this.label = label;
    }

    public boolean isAttribute() {
        return attribute;
    }

    public void setAttribute(boolean attribute) {
        this.attribute = attribute;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public SortOrder getNewSortOrder() {
        return sortOrder == SortOrder.ASC ? SortOrder.DESC : SortOrder.ASC;
    }

    public void setSortOrder(SortOrder sortOrder) {
        if (sortOrder == null) {
            sortOrder = SortOrder.ASC;
        }
        this.sortOrder = sortOrder;
    }

    public Set<Cell> getCells() {
        return cells;
    }
}