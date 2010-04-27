package com.amee.domain.sheet;

import com.amee.base.utils.UidGen;
import com.amee.domain.ValueType;
import com.amee.base.utils.XMLUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Constructors purposefully verbose to ensure identity in collections after add() operations.
 * <p/>
 * TODO: convert this to use generics.
 */
public class Cell implements Serializable, Comparable {

    private Sheet sheet;
    private Column column;
    private Row row;
    private ValueType valueType = ValueType.TEXT;
    private String uid = "";
    private Object value = "";

    private Cell() {
        super();
    }

    public Cell(Column column, Row row) {
        this();
        setSheet(column.getSheet());
        setColumn(column);
        setRow(row);
        setValue("");
        setValueType();
        setUid(UidGen.INSTANCE_12.getUid());
        add();
    }

    public Cell(Column column, Row row, Object value) {
        this();
        setSheet(column.getSheet());
        setColumn(column);
        setRow(row);
        setValue(value);
        setValueType();
        setUid(UidGen.INSTANCE_12.getUid());
        add();
    }

    public Cell(Column column, Row row, Object value, ValueType valueType) {
        this();
        setSheet(column.getSheet());
        setColumn(column);
        setRow(row);
        setValue(value);
        setUid(UidGen.INSTANCE_12.getUid());
        setValueType(valueType);
        add();
    }

    public Cell(Column column, Row row, Object value, String uid) {
        this();
        setSheet(column.getSheet());
        setColumn(column);
        setRow(row);
        setValue(value);
        setValueType();
        setUid(uid);
        add();
    }

    public Cell(Column column, Row row, Object value, String uid, ValueType valueType) {
        this();
        setSheet(column.getSheet());
        setColumn(column);
        setRow(row);
        setValue(value);
        setUid(uid);
        setValueType(valueType);
        add();
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("type", getValueType());
        obj.put("name", getColumn().getName());
        obj.put("value", getValueAsString());
        return obj;
    }

    public Element getElement(Document document) {
        Element cellElement = document.createElement("Cell");
        cellElement.setAttribute("uid", getValueType().toString());
        cellElement.setAttribute("type", getValueType().toString());
        cellElement.appendChild(XMLUtils.getElement(document, "Name", getColumn().getName()));
        cellElement.appendChild(XMLUtils.getElement(document, "Value", getValueAsString()));
        return cellElement;
    }

    public void add() {
        getColumn().add(this);
        getRow().add(this);
        getSheet().add(this);
    }

    public void beforeRemove() {
        getColumn().remove(this);
        getSheet().remove(this);
    }

    public boolean equals(Object o) {
        if (super.equals(o)) return true;
        if (!(o instanceof Cell)) return false;
        Cell other = (Cell) o;
        return getUid().equalsIgnoreCase(other.getUid());
    }

    public int compareTo(Object o) {
        if (this.equals(o)) return 0;
        Cell other = (Cell) o;
        int result = 0; // default to 'greater than' to push stuff to the end of the list
        // assume other is Cell with value of same ValueType
        switch (getValueType()) {
            case INTEGER: {
                Integer a = null;
                Integer b = null;
                try {
                    a = new Integer(getValueAsString());
                } catch (NumberFormatException e) {
                    // swallow, push non integer to the end of list
                    result = 1;
                }
                try {
                    b = new Integer(other.getValueAsString());
                } catch (NumberFormatException e) {
                    // swallow, push non integer to the end of list
                    result = -1;
                }
                if ((a != null) && (b != null)) {
                    result = a.compareTo(b);
                } else if ((a == null) && (b == null)) {
                    // neither values are Integers so just compare text values instead
                    result = getValueAsString().compareToIgnoreCase(other.getValueAsString());
                }
                break;
            }
            case BOOLEAN: {
                Boolean a = Boolean.valueOf(getValueAsString());
                Boolean b = Boolean.valueOf(other.getValueAsString());
                result = a.compareTo(b);
                break;
            }
            case DECIMAL: {
                BigDecimal a = null;
                BigDecimal b = null;
                try {
                    a = new BigDecimal(getValueAsString());
                } catch (NumberFormatException e) {
                    // swallow, push non decimal to the end of list
                    result = 1;
                }
                try {
                    b = new BigDecimal(other.getValueAsString());
                } catch (NumberFormatException e) {
                    // swallow, push non decimal to the end of list
                    result = -1;
                }
                if ((a != null) && (b != null)) {
                    result = a.compareTo(b);
                } else if ((a == null) && (b == null)) {
                    // neither values are BigDecimals so just compare text values instead
                    result = getValueAsString().compareToIgnoreCase(other.getValueAsString());
                }
                break;
            }
            case DATE: {
                Date a = getValueAsDate();
                Date b = other.getValueAsDate();
                if (a == null) {
                    // push non Date to the end of list
                    result = 1;
                }
                if (b == null) {
                    // push non Date to the end of list
                    result = -1;
                }
                if ((a != null) && (b != null)) {
                    result = a.compareTo(b);
                } else if ((a == null) && (b == null)) {
                    // neither values are Dates so just compare text values instead
                    result = getValueAsString().compareToIgnoreCase(other.getValueAsString());
                }
                break;
            }
            case TEXT:
            case UNSPECIFIED:
            default: {
                result = getValueAsString().compareToIgnoreCase(other.getValueAsString());
                break;
            }
        }
        if (getColumn().getSortOrder().equals(SortOrder.DESC)) {
            return result * -1; // invert
        } else {
            return result;
        }
    }

    public int hashCode() {
        return getUid().hashCode();
    }

    public String toString() {
        return getValueAsString();
    }

    public Sheet getSheet() {
        return sheet;
    }

    protected void setSheet(Sheet sheet) {
        if (sheet != null) {
            this.sheet = sheet;
        }
    }

    public Column getColumn() {
        return column;
    }

    protected void setColumn(Column column) {
        if (column != null) {
            this.column = column;
        }
    }

    public Row getRow() {
        return row;
    }

    protected void setRow(Row row) {
        if (row != null) {
            this.row = row;
        }
    }

    public ValueType getValueType() {
        return valueType;
    }

    protected void setValueType(ValueType valueType) {
        if (valueType != null) {
            this.valueType = valueType;
        }
    }

    protected void setValueType() {
        setValueType(ValueType.getValueType(getValue()));
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

    public Object getValue() {
        return value;
    }

    public String getValueAsString() {
        return value.toString();
    }

    public Integer getValueAsInteger() {
        if (value instanceof Integer) {
            return (Integer) value;
        } else {
            try {
                return new Integer(value.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    public BigDecimal getValueAsBigDecimal() {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else {
            try {
                return new BigDecimal(value.toString());
            } catch (NumberFormatException e) {
                return new BigDecimal(0);
            }
        }
    }

    public Boolean getValueAsBoolean() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return Boolean.valueOf(value.toString());
        }
    }

    public Date getValueAsDate() {
        if (value instanceof Date) {
            return (Date) value;
        } else {
            // TODO: could try parsing string value
            return null;
        }
    }

    public void setValue(Object value) {
        if (value == null) {
            value = "";
            setValueType(ValueType.TEXT);
        }
        this.value = value;
    }
}