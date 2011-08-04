package com.amee.restlet.profile;

import com.amee.domain.AMEEStatistics;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.Pager;
import com.amee.domain.auth.AccessSpecification;
import com.amee.domain.auth.PermissionEntry;
import com.amee.domain.profile.Profile;
import com.amee.restlet.AMEEResource;
import com.amee.service.profile.ProfileBrowser;
import com.amee.service.profile.ProfileConstants;
import com.amee.service.profile.ProfileService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("profilesResource")
@Scope("prototype")
public class ProfilesResource extends AMEEResource implements Serializable {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileBrowser profileBrowser;

    @Autowired
    private AMEEStatistics ameeStatistics;

    private Profile newProfile = null;

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(getRootDataCategory());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return getAPIVersion() + "/" + ProfileConstants.VIEW_PROFILES;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager();
        List<Profile> profiles = profileService.getProfiles(getActiveUser(), pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", profileBrowser);
        values.put("profiles", profiles);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager();
            List<Profile> profiles = profileService.getProfiles(getActiveUser(), pager);
            pager.setCurrentPage(getPage());
            JSONArray profilesJSONArray = new JSONArray();
            for (Profile profile : profiles) {
                profilesJSONArray.put(profile.getJSONObject());
            }
            obj.put("profiles", profilesJSONArray);
            obj.put("pager", pager.getJSONObject());
        } else if (getRequest().getMethod().equals(Method.POST)) {
            obj.put("profile", newProfile.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("ProfilesResource");
        if (isGet()) {
            Pager pager = getPager();
            List<Profile> profiles = profileService.getProfiles(getActiveUser(), pager);
            pager.setCurrentPage(getPage());
            Element profilesElement = document.createElement("Profiles");
            for (Profile profile : profiles) {
                profilesElement.appendChild(profile.getElement(document));
            }
            element.appendChild(profilesElement);
            element.appendChild(pager.getElement(document));
        } else if (getRequest().getMethod().equals(Method.POST)) {
            element.appendChild(newProfile.getElement(document));
        }
        return element;
    }

    @Override
    public List<AccessSpecification> getAcceptAccessSpecifications() {
        return updateLastAccessSpecificationWithPermissionEntry(getGetAccessSpecifications(), PermissionEntry.CREATE_PROFILE);
    }

    @Override
    public void doAccept(Representation entity) {
        log.debug("doAccept()");
        Form form = getForm();
        // are we creating a new Profile?
        if ((form.getFirstValue("profile") != null)) {
            // create new Profile, update statistics
            newProfile = new Profile(getActiveUser());
            profileService.persist(newProfile);
            ameeStatistics.createProfile();
        }
        if (newProfile != null) {
            if (isStandardWebBrowser()) {
                success();
            } else {
                // return a response for API calls
                super.handleGet();
            }
        } else {
            badRequest();
        }
    }
}