package com.jellymold.plum.admin;

import com.jellymold.plum.Skin;
import com.jellymold.plum.SkinBeans;
import com.jellymold.plum.SkinService;
import com.jellymold.utils.BaseResource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class SkinsResource extends BaseResource {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private SkinService skinService;

    @Autowired
    private SkinAdmin skinAdmin;

    private List<Skin> skins;

    public SkinsResource() {
        super();
    }

    public SkinsResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public String getTemplatePath() {
        return SkinBeans.VIEW_SKINS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("skinAdmin", skinAdmin);
        values.put("skins", getSkins());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        JSONArray skins = new JSONArray();
        for (Skin skin : getSkins()) {
            skins.put(skin.getJSONObject(true));
        }
        obj.put("skins", skins);
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("SkinsResource");
        Element skins = document.createElement("Skins");
        for (Skin skin : getSkins()) {
            skins.appendChild(skin.getElement(document));
        }
        element.appendChild(skins);
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (skinAdmin.getSkinActions().isAllowView()) {
            // validation for this resource
            if (getSkins() != null) {
                super.handleGet();
            } else {
                log.warn("skins not found");
                notFound();
            }
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void post(Representation entity) {
        log.debug("post");
        if (skinAdmin.getSkinActions().isAllowCreate()) {
            Form form = getForm();
            // TODO: prevent duplicate paths
            if (form.getFirstValue("name") != null) {
                log.debug("creating new Skin");
                Skin skin = new Skin();
                skin.setName(form.getFirstValue("name"));
                skin.setPath(form.getFirstValue("path"));
                skinService.save(skin);
            }
            success();
        } else {
            notAuthorized();
        }
    }

    protected List<Skin> getSkins() {
        if (skins == null) {
            skins = skinService.getSkins();
        }
        return skins;
    }
}