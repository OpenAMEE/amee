package com.amee.restlet;

import com.amee.domain.auth.User;
import com.amee.domain.site.ISite;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Request;

public class BaseFilter extends Filter {

    public BaseFilter() {
        super();
    }

    public BaseFilter(Context context) {
        super(context);
    }

    public BaseFilter(Context context, Restlet next) {
        super(context, next);
    }

    public ISite getActiveSite() {
        return (ISite) Request.getCurrent().getAttributes().get("activeSite");
    }

    public User getActiveUser() {
        return (User) Request.getCurrent().getAttributes().get("activeUser");
    }
}
