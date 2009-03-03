package com.amee.admin.restlet.auth;

import com.amee.domain.auth.Permission;
import com.amee.restlet.BaseResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.Map;

public abstract class BasePermissionResource extends BaseResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private Permission permission;

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("permission", permission);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("permission", permission.getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("PermissionResource");
        element.appendChild(permission.getElement(document));
        return element;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation");
    }
}
