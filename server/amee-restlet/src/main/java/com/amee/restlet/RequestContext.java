/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.restlet;

import com.amee.domain.auth.User;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemValue;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Request;

import java.util.Iterator;

/**
 * A simple bean for holding contextual information about the request.
 *
 * Its intended use is in debug statements etc.
 */
public class RequestContext {

    private final Log log = LogFactory.getLog(getClass());
    private final Log transactions = LogFactory.getLog("transactions");

    private String username = "";
    private String profileUid = "";
    private String apiVersion = "";
    private String requestPath = "";
    private String method = "";
    private String requestParameters = "";
    private String error = "";
    private String form = "";
    private String categoryUid = "";
    private String label = "";
    private String type = "";
    private long start = 0L;

    public RequestContext() {
        this.start = System.currentTimeMillis();
    }

    public void setUser(User user) {
        if (user == null)
            return;
        this.username = user.getUsername();
        this.apiVersion = user.getAPIVersion().toString();
    }

    public void setProfile(Profile profile) {
        if (profile != null)
            this.profileUid = profile.getUid();
    }

    public void setRequest(Request request) {
        this.requestPath = request.getResourceRef().getPath();
        this.method = request.getMethod().toString();
        this.requestParameters = getParameters(request.getResourceRef().getQueryAsForm());
    }

    private String getParameters(Form parmameters) {
        Iterator<Parameter> i = parmameters.iterator();
        if (!i.hasNext())
            return "";

        StringBuilder sb = new StringBuilder();
        for (;;) {
            Parameter p = i.next();
            sb.append(p.getName());
            sb.append("__");
            if (!p.getName().equals("password")) {
                sb.append(p.getValue());
            } else {
                sb.append("XXXXXX");        
            }
            if (i.hasNext()) {
                sb.append(", ");
            } else {
                return sb.toString();
            }
        }

    }

    public void setCategory(DataCategory category) {
        this.categoryUid = category.getUid();
        this.label = category.getDisplayName();
        this.type = category.getObjectType().getName();
    }

    public void setDataItem(DataItem item) {
        this.categoryUid = item.getUid();
        this.label = item.getLabel();
        this.type = item.getObjectType().getName();
    }

    public void setProfileItem(ProfileItem item) {
        this.categoryUid = item.getUid();
        this.label = item.getDisplayName();
        this.type = item.getObjectType().getName();
    }

    public void setItemValue(ItemValue value) {
        this.categoryUid = value.getUid();
        this.label = value.getPath();
        this.type = value.getObjectType().getName();
    }

    public void setDrillDown(DataCategory dataCategory) {
        this.categoryUid = dataCategory.getUid();
        this.label = dataCategory.getDisplayName();
        this.type = "DD";
    }

    public void setError(String error) {
        if (StringUtils.isNotBlank(error))
            this.error = error;    
    }

    public void setForm(Form form) {
        this.form = getParameters(form);
    }

    public void error() {
        log.error(toString());
    }

    public void record() {
        if (categoryUid != null) {
            transactions.info(toString());
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
            sb.append(username + "|");
            sb.append(apiVersion + "|");
            sb.append(profileUid + "|");
            sb.append(categoryUid + "|");
            sb.append(requestPath + "|");
            sb.append(type + "|");
            sb.append(label + "|");
            sb.append(requestParameters.replace("=","__") + "|");
            sb.append(form + "|");
            sb.append(error + "|");
            sb.append(method + "|");
            sb.append(System.currentTimeMillis() - start);
        return sb.toString();
    }
}
