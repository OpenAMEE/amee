package com.amee.admin.restlet.environment.group;

import com.amee.admin.restlet.environment.AdminBrowser;
import com.amee.domain.AMEEEntity;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.auth.Group;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.environment.AdminConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class GroupResource extends AuthorizeResource {

    @Autowired
    private AdminBrowser adminBrowser;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        adminBrowser.setGroupUid(request.getAttributes().get("groupUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (adminBrowser.getGroup() != null);
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(getRootDataCategory());
        entities.add(adminBrowser.getGroup());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return AdminConstants.VIEW_GROUP;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", adminBrowser);
        values.put("group", adminBrowser.getGroup());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("group", adminBrowser.getGroup().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("GroupResource");
        element.appendChild(adminBrowser.getGroup().getElement(document));
        return element;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    // TODO: prevent duplicates
    @Override
    public void doStore(Representation entity) {
        log.debug("put");
        Form form = getForm();
        Group group = adminBrowser.getGroup();
        // update values
        if (form.getNames().contains("name")) {
            group.setName(form.getFirstValue("name"));
        }
        if (form.getNames().contains("description")) {
            group.setDescription(form.getFirstValue("description"));
        }
        success();
    }

    @Override
    public boolean allowDelete() {
        return false;
    }
}