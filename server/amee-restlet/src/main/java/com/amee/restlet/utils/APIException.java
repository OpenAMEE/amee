package com.amee.restlet.utils;

public class APIException extends Exception {

    private APIFault apiFault;

    public APIException() {
        super();
    }

    public APIException(String message) {
        super(message);
    }

    public APIException(String message, Throwable cause) {
        super(message, cause);
    }

    public APIException(Throwable cause) {
        super(cause);
    }

    public APIException(APIFault apiFault) {
        super();
        this.apiFault = apiFault;
    }

    public APIFault getApiFault() {
        return apiFault;
    }
}
