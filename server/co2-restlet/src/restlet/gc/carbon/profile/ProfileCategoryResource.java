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

import com.jellymold.kiwi.Environment;
import com.jellymold.sheet.Sheet;
import com.jellymold.utils.Pager;
import gc.carbon.profile.renderer.Renderer;
import gc.carbon.profile.renderer.RendererFactory;
import gc.carbon.profile.acceptor.ProfileCategoryJSONAcceptor;
import gc.carbon.profile.acceptor.ProfileCategoryFormAcceptor;
import gc.carbon.profile.acceptor.Acceptor;
import gc.carbon.profile.acceptor.ProfileCategoryXMLAcceptor;
import gc.carbon.data.*;
import gc.carbon.path.PathItem;
import gc.carbon.path.PathItemService;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Name("profileCategoryResource")
@Scope(ScopeType.EVENT)
public class ProfileCategoryResource extends BaseProfileResource implements Serializable {

    private final static Logger log = Logger.getLogger(ProfileCategoryResource.class);

    @In(create = true)
    private EntityManager entityManager;

    @In(create = true)
    private DataService dataService;

    @In(create = true)
    private ProfileService profileService;

    @In(create = true)
    private ProfileBrowser profileBrowser;

    @In(create = true)
    private PathItemService pathItemService;

    @In(create = true)
    private ProfileSheetService profileSheetService;

    @In(create = true)
    private Calculator calculator;

    @In
    private Environment environment;

    @In
    private PathItem pathItem;

    private List<ProfileItem> profileItems = new ArrayList<ProfileItem>();

    private Renderer renderer;

    private Map<MediaType, Acceptor> acceptors;

    private DateTimeBrowser dateTimeBrowser;

    public ProfileCategoryResource() {
        super();
    }

    public ProfileCategoryResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        profileBrowser.setDataCategoryUid(request.getAttributes().get("categoryUid").toString());
        dateTimeBrowser = new DateTimeBrowser(request);
        setPage(request);
        setAcceptors();
        setRenderStrategy();
    }

    private void setRenderStrategy() {
        renderer = RendererFactory.createProfileCategoryRenderer(this);
    }

    private void setAcceptors() {
        acceptors = new HashMap<MediaType, Acceptor>();
        acceptors.put(MediaType.APPLICATION_XML, new ProfileCategoryXMLAcceptor(this));
        acceptors.put(MediaType.APPLICATION_JSON, new ProfileCategoryJSONAcceptor(this));
        acceptors.put(MediaType.APPLICATION_WWW_FORM, new ProfileCategoryFormAcceptor(this));
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (profileBrowser.getDataCategory() != null);
    }

    @Override
    public String getTemplatePath() {
        return ProfileConstants.VIEW_PROFILE_CATEGORY;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Profile profile = profileBrowser.getProfile();
        DataCategory dataCategory = profileBrowser.getDataCategory();
        Sheet sheet = profileSheetService.getSheet(profile, dataCategory, dateTimeBrowser.getStartDate().toDate());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", profileBrowser);
        values.put("profile", profile);
        values.put("dataCategory", dataCategory);
        values.put("node", dataCategory);
        values.put("sheet", sheet);
        if (sheet != null) {
            values.put("totalAmountPerMonth", profileSheetService.getTotalAmountPerMonth(sheet));
        }
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        return renderer.getJSONObject();
    }

    @Override
    public Element getElement(Document document) {
        return renderer.getElement(document);
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (profileBrowser.getEnvironmentActions().isAllowView()) {
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
    public boolean allowPut() {
        return true;
    }

    @Override
    public void post(org.restlet.resource.Representation entity) {
        log.debug("post");
        postOrPut(entity);
    }

    @Override
    public void put(org.restlet.resource.Representation entity) {
        log.debug("put");
        postOrPut(entity);
    }

    protected void postOrPut(org.restlet.resource.Representation entity) {
        log.debug("postOrPut");

        if (isPostOrPutAuthorized()) {

            profileItems = doPostOrPut(entity, getForm());

            if (!profileItems.isEmpty()) {
                // clear caches
                pathItemService.removePathItemGroup(profileBrowser.getProfile());
                profileSheetService.removeSheets(profileBrowser.getProfile());
                if (isStandardWebBrowser()) {
                    success(profileBrowser.getFullPath());
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

    private boolean isPostOrPutAuthorized() {
         return (getRequest().getMethod().equals(Method.POST) && (profileBrowser.getProfileItemActions().isAllowCreate())) ||
                 (getRequest().getMethod().equals(Method.PUT) && (profileBrowser.getProfileItemActions().isAllowModify()));
     }


    public List<ProfileItem> doPostOrPut(org.restlet.resource.Representation entity, Form form) {
        return lookupAcceptor(entity.getMediaType()).accept(entity, form);
    }


    private Acceptor lookupAcceptor(MediaType type) {
        if (MediaType.APPLICATION_JSON.includes(type)) {
            return acceptors.get(MediaType.APPLICATION_JSON);
        } else if (MediaType.APPLICATION_XML.includes(type)) {
            return acceptors.get(MediaType.APPLICATION_XML);
        } else {
           return acceptors.get(MediaType.APPLICATION_WWW_FORM);
        }
    }

     @Override
    public boolean allowDelete() {
        // only allow delete for profile (a request to /profiles/{profileUid})
        return (pathItem.getPath().length() == 0);
    }

    @Override
    public void delete() {
        log.debug("delete");
        if (profileBrowser.getProfileActions().isAllowDelete()) {
            Profile profile = profileBrowser.getProfile();
            pathItemService.removePathItemGroup(profile);
            profileSheetService.removeSheets(profile);
            profileService.remove(profile);
            success("/profiles");
        } else {
            notAuthorized();
        }
    }

    public List<ProfileItem> getProfileItems() {
        return profileItems;
    }

    public ProfileSheetService getProfileSheetService() {
        return profileSheetService;
    }

    public ProfileBrowser getProfileBrowser() {
        return profileBrowser;
    }

    public PathItem getPathItem() {
        return pathItem;
    }

    public Pager getPager() {
        return getPager(profileBrowser.getItemsPerPage(getRequest()));
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    public DataService getDataService() {
        return dataService;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Calculator getCalculator() {
        return calculator;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public DateTimeBrowser getDateTimeBrowser() {
        return dateTimeBrowser;
    }
}