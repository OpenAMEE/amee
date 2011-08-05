package com.amee.restlet.utils;

public enum APIFault {

    NONE(0, ""),
    INVALID_PARAMETERS(1, "A request was received with one or more invalid parameters."),
    INVALID_API_PARAMETERS(2, "A request was received with one or more parameters not supported by the version of the API."),
    INVALID_CONTENT(3, "A request was received with invalid content."),
    MISSING_PARAMETERS(4, "A request was received with one or more missing parameters."),
    INVALID_DATE_FORMAT(5, "A request was received with one or more datetime parameters having an invalid format."),
    INVALID_DATE_RANGE(6, "A request was received with an invalid date range."),
    INVALID_PRORATA_REQUEST(7, "A prorata request was received without a recognised bounded date range."),
    DUPLICATE_ITEM(8, "A POST or PUT request was received which would have resulted in a duplicate resource being created."),
    INVALID_UNIT(9, "A request was received with an invalid unit."),
    EMPTY_LIST(10, "An empty list was received or produced."),
    ENTITY_NOT_FOUND(11, "An entity was not found for the given identifier."),
    MAX_BATCH_SIZE_EXCEEDED(12, "Max batch size was exceeded."),
    DELETE_MUST_LEAVE_AT_LEAST_ONE_ITEM_VALUE(13, "The DELETE operation must leave at least one ITEM_VALUE per ITEM_VALUE_DEFINTION."),
    INVALID_RESOURCE_MODIFICATION(14, "A POST or PUT request was received which would have resulted in an invalid resource modification."),
    NOT_AUTHORIZED_FOR_INDIRECT_ACCESS(15, "A request was received which is not authorized to access an entity indirectly");

    private final int code;
    private final String message;

    APIFault(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return Integer.toString(code);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
