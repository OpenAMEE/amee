package com.amee.restlet.utils;

public enum APIFault {

    NONE,
    INVALID_PARAMETERS,
    INVALID_API_PARAMETERS,
    INVALID_CONTENT,
    MISSING_PARAMETERS,
    INVALID_DATE_FORMAT,
    INVALID_DATE_RANGE,
    INVALID_PRORATA_REQUEST,
    DUPLICATE_ITEM,
    INVALID_UNIT,
    EMPTY_LIST,
    ENTITY_NOT_FOUND,
    MAX_BATCH_SIZE_EXCEEDED,
    DELETE_MUST_LEAVE_AT_LEAST_ONE_ITEM_VALUE,
    INVALID_RESOURCE_MODIFICATION,
    NOT_AUTHORIZED_FOR_INDIRECT_ACCESS;

    private String[] strings = {
            "", // NONE
            "A request was received with one or more invalid parameters.", // INVALID_PARAMETERS
            "A request was received with one or more parameters not supported by the version of the API.", // INVALID_API_PARAMETERS
            "A request was received with invalid content.", // INVALID_CONTENT
            "A request was received with one or more missing parameters.", // MISSING_PARAMETERS
            "A request was received with one or more datetime parameters having an invalid format.", // INVALID_DATE_FORMAT
            "A request was received with an invalid date range.", // INVALID_DATE_RANGE
            "A prorata request was received without a recognised bounded date range.", // INVALID_PRORATA_REQUEST
            "A POST or PUT request was received which would have resulted in a duplicate resource being created.", // DUPLICATE_ITEM
            "A request was received with an invalid unit.", // INVALID_UNIT
            "An empty list was received or produced.", // EMPTY_LIST
            "An entity was not found for the given identifier.", // ENTITY_NOT_FOUND
            "Max batch size was exceeded.", // MAX_BATCH_SIZE_EXCEEDED
            "The DELETE operation must leave at least one ITEM_VALUE per ITEM_VALUE_DEFINTION.", // DELETE_MUST_LEAVE_AT_LEAST_ONE_ITEM_VALUE
            "A POST or PUT request was received which would have resulted in an invalid resource modification.", // INVALID_RESOURCE_MODIFICATION
            "A request was received which is not authorized to access an entity indirectly", // NOT_AUTHORIZED_FOR_INDIRECT_ACCESS
    };

    public String getString() {
        return strings[this.ordinal()];
    }

    public String getCode() {
        return Integer.toString(this.ordinal());
    }

    public String toString() {
        return getString();
    }
}
