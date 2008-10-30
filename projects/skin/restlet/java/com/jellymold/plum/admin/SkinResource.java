package com.jellymold.plum.admin;

import com.jellymold.plum.Skin;
import com.jellymold.plum.SkinBeans;
import com.jellymold.plum.SkinService;
import com.jellymold.utils.BaseResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class SkinResource extends BaseResource {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private SkinService skinService;

    @Autowired
    private SkinAdmin skinAdmin;

    public SkinResource() {
        super();
    }

    public SkinResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        skinAdmin.setSkinUid(request.getAttributes().get("skinUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (skinAdmin.getSkin() != null);
    }

    @Override
    public String getTemplatePath() {
        return SkinBeans.VIEW_SKIN;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("skinAdmin", skinAdmin);
        values.put("skin", skinAdmin.getSkin());
        values.put("skins", skinService.getSkins());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("skin", skinAdmin.getSkin().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("SkinResource");
        element.appendChild(skinAdmin.getSkin().getElement(document));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (skinAdmin.getSkinActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void put(Representation entity) {
        log.debug("put");
        if (skinAdmin.getSkinActions().isAllowModify()) {
            Form form = getForm();
            Skin skin = skinAdmin.getSkin();
            if (form.getNames().contains("name")) {
                log.debug("updating Skin");
                // update core properties
                // TODO: prevent duplicate paths
                skin.setName(form.getFirstValue("name"));
                skin.setPath(form.getFirstValue("path"));
                skin.setSvnUrl(form.getFirstValue("svnUrl"));
                skin.setSvnUsername(form.getFirstValue("svnUsername"));
                skin.setSvnPassword(form.getFirstValue("svnPassword"));
            }
            if (form.getNames().contains("parent")) {
                // update parent
                String parent = form.getFirstValue("parent");
                if (parent != null) {
                    if (!parent.equalsIgnoreCase("root") && !parent.equalsIgnoreCase(skin.getUid())) {
                        skin.setParent(skinService.getSkinByUID(parent));
                    } else if (parent.equalsIgnoreCase("root")) {
                        skin.setParent(null);
                    }
                } else {
                    skin.setParent(null);
                }
            }
            if (form.getNames().contains("skin_imports")) {
                // update imported skins
                Set<Skin> importedSkins = new HashSet<Skin>();
                if (null != form.getValues("skin_imports")) {
                    for (String imp : form.getValues("skin_imports").split(",")) {
                        importedSkins.add(skinService.getSkinByUID(imp));
                    }
                    skin.setImportedSkins(importedSkins);
                }
            }
            success();
        } else {
            notAuthorized();
        }
    }
}