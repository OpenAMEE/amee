package com.amee.restlet;

import com.amee.domain.auth.User;
import com.amee.domain.data.DataCategory;
import com.amee.domain.item.BaseItemValue;
import com.amee.domain.item.data.DataItem;
import com.amee.domain.item.profile.ProfileItem;
import com.amee.domain.profile.Profile;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Status;

import java.util.Iterator;

/**
 * A simple bean for holding contextual information about the request.
 * <p/>
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
    private int status = 200;

    public RequestContext() {
        this.start = System.currentTimeMillis();
    }

    public void setUser(User user) {
        if (user != null) {
            this.username = user.getUsername();
            this.apiVersion = user.getAPIVersion().toString();
        } else {
            this.username = "";
            this.apiVersion = "";
        }
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
        for (; ;) {
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

    public void setDataCategory(DataCategory category) {
        if (category != null) {
            this.categoryUid = category.getUid();
            this.label = category.getDisplayName();
            this.type = category.getObjectType().getName();
        }
    }

    public void setDataItem(DataItem dataItem) {
        if (dataItem != null) {
            this.categoryUid = dataItem.getUid();
            this.label = dataItem.getDisplayPath();
            this.type = dataItem.getObjectType().getName();
        }
    }

    public void setProfileItem(ProfileItem profileItem) {
        if (profileItem != null) {
            this.categoryUid = profileItem.getUid();
            this.label = profileItem.getDisplayName();
            this.type = profileItem.getObjectType().getName();
        }
    }

    public void setItemValue(BaseItemValue itemValue) {
        if (itemValue != null) {
            this.categoryUid = itemValue.getUid();
            this.label = itemValue.getPath();
            this.type = itemValue.getObjectType().getName();
        }
    }

    public void setDrillDown(DataCategory dataCategory) {
        if (dataCategory != null) {
            this.categoryUid = dataCategory.getUid();
            this.label = dataCategory.getDisplayName();
            this.type = "DD";
        }
    }

    public void setError(String error) {
        if (StringUtils.isNotBlank(error))
            this.error = error;
    }

    public void setStatus(Status status) {
        this.status = status.getCode();
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
        sb.append(requestParameters.replace("=", "__") + "|");
        sb.append(form + "|");
        sb.append(error + "|");
        sb.append(method + "|");
        sb.append(status + "|");
        sb.append(System.currentTimeMillis() - start);
        return sb.toString();
    }
}
