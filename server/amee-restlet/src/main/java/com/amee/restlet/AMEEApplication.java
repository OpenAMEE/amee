package com.amee.restlet;

import org.restlet.Application;
import org.restlet.Context;

public class AMEEApplication extends Application {

    private Boolean allowClientCache = true;

    public AMEEApplication() {
        super();
    }

    public AMEEApplication(Context context) {
        super(context);
    }

    public boolean getAllowClientCache() {
        return allowClientCache;
    }

    public boolean isAllowClientCache() {
        return allowClientCache;
    }

    public void setAllowClientCache(boolean allowClientCache) {
            this.allowClientCache = allowClientCache;
    }

}
