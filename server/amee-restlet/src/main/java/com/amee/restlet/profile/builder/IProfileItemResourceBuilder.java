package com.amee.restlet.profile.builder;

import com.amee.restlet.profile.ProfileItemResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

@Service
public interface IProfileItemResourceBuilder {

    public abstract JSONObject getJSONObject(ProfileItemResource resource) throws JSONException;

    public abstract Element getElement(ProfileItemResource resource, Document document);

    public abstract Map<String, Object> getTemplateValues(ProfileItemResource resource);

    public abstract org.apache.abdera.model.Element getAtomElement(ProfileItemResource resource);
}