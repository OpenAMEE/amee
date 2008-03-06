/**
* This file is part of AMEE.
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
package gc.carbon.profile;

import com.jellymold.kiwi.Group;
import com.jellymold.kiwi.Permission;
import com.jellymold.kiwi.Site;
import com.jellymold.kiwi.User;
import com.jellymold.kiwi.Environment;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
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

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Name("profilesResource")
@Scope(ScopeType.EVENT)
public class ProfilesResource extends BaseResource implements Serializable {

    private final static Logger log = Logger.getLogger(ProfilesResource.class);

    @In(create = true)
    private EntityManager entityManager;

    @In(create = true)
    private ProfileService profileService;

    @In(create = true)
    private ProfileBrowser profileBrowser;

    @In
    private Environment environment;

    @In
    private Site site;

    @In
    private User user;

    @In(required = false)
    private Group group;

    private Profile newProfile = null;

    public ProfilesResource() {
        super();
    }

    public ProfilesResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setPage(request);
    }

    @Override
    public String getTemplatePath() {
        return ProfileConstants.VIEW_PROFILES;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(environment.getItemsPerPage());
        List<Profile> profiles = profileService.getProfiles(pager);
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
            Pager pager = getPager(environment.getItemsPerPage());
            List<Profile> profiles = profileService.getProfiles(pager);
            pager.setCurrentPage(getPage());
            JSONArray profilesJSONArray = new JSONArray();
            for (Profile profile : profiles) {
                profilesJSONArray.put(profile.getJSONObject());
            }
            obj.put("profiles", profilesJSONArray);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("profile", newProfile.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("ProfilesResource");
        if (isGet()) {
            Pager pager = getPager(environment.getItemsPerPage());
            List<Profile> profiles = profileService.getProfiles(pager);
            pager.setCurrentPage(getPage());
            Element profilesElement = document.createElement("Profiles");
            for (Profile profile : profiles) {
                profilesElement.appendChild(profile.getElement(document));
            }
            element.appendChild(profilesElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newProfile.getElement(document));
        }
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (profileBrowser.getProfileActions().isAllowList()) {
            super.handleGet();
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
        if (profileBrowser.getProfileActions().isAllowCreate()) {
            Form form = getForm();
            // are we creating a new Profile?
            if ((form.getFirstValue("profile") != null) && (group != null)) {
                // create Permission for Profile
                Permission permission = new Permission(group, user);
                // create new Profile
                newProfile = new Profile(environment, permission);
                entityManager.persist(newProfile);
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
        } else {
            notAuthorized();
        }
    }
}
