package com.amee.restlet.profile.builder;

import com.amee.restlet.profile.ProfileCategoryResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

@Service
public interface IProfileCategoryResourceBuilder {

    public abstract JSONObject getJSONObject(ProfileCategoryResource resource)  throws JSONException;

    public abstract Element getElement(ProfileCategoryResource resource, Document document);

    public abstract Map<String, Object> getTemplateValues(ProfileCategoryResource resource);

    public abstract org.apache.abdera.model.Element getAtomElement(ProfileCategoryResource resource);
}
