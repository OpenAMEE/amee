package com.amee.restlet.profile.builder;

import com.amee.restlet.profile.ProfileItemValueResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

@Service
public interface IProfileItemValueResourceBuilder {

    public abstract JSONObject getJSONObject(ProfileItemValueResource resource) throws JSONException;

    public abstract Element getElement(ProfileItemValueResource resource, Document document);

    public abstract Map<String, Object> getTemplateValues(ProfileItemValueResource resource);

    public abstract org.apache.abdera.model.Element getAtomElement(ProfileItemValueResource resource);
}