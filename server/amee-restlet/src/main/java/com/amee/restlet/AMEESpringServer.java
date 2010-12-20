package com.amee.restlet;

import org.restlet.ext.spring.SpringServer;

public class AMEESpringServer extends SpringServer {

    private boolean secure = false;

    public AMEESpringServer(String protocol) {
        super(protocol);
    }

    public AMEESpringServer(String protocol, int port) {
        super(protocol, port);
    }

    public AMEESpringServer(String protocol, String address, int port) {
        super(protocol, address, port);
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }
}
