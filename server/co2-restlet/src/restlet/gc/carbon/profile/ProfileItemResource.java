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

import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
import com.jellymold.utils.domain.APIUtils;
import com.jellymold.kiwi.Environment;
import gc.carbon.data.Calculator;
import gc.carbon.data.ItemValue;
import gc.carbon.data.DataService;
import gc.carbon.path.PathItem;
import gc.carbon.path.PathItemService;
import gc.carbon.profile.renderer.RendererFactory;
import gc.carbon.profile.renderer.Renderer;
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
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.List;

@Component
@Scope("prototype")
public class ProfileItemResource extends BaseProfileResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileBrowser profileBrowser;

    @Autowired
    private PathItemService pathItemService;

    @Autowired
    private ProfileSheetService profileSheetService;

    @Autowired
    private Calculator calculator;

    // TODO: Springify
    // @In
    private PathItem pathItem;

    // TODO: Springify
    // @In
    private Environment environment;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DataService dataService;

    private Renderer renderer;

    public ProfileItemResource() {
        super();
    }

    public ProfileItemResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        profileBrowser.setDataCategoryUid(request.getAttributes().get("categoryUid").toString());
        profileBrowser.setProfileItemUid(request.getAttributes().get("itemUid").toString());
        renderer = RendererFactory.createProfileItemRenderer(this);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (profileBrowser.getProfileItem() != null);
    }

    @Override
    public String getTemplatePath() {
        return ProfileConstants.VIEW_PROFILE_ITEM;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        ProfileItem profileItem = profileBrowser.getProfileItem();
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", profileBrowser);
        values.put("profile", profileItem.getProfile());
        values.put("profileItem", profileItem);
        values.put("node", profileItem);
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
        if (profileBrowser.getProfileItemActions().isAllowView()) {
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
        if (profileBrowser.getProfileItemActions().isAllowModify()) {
            Form form = getForm();
            ProfileItem profileItem = profileBrowser.getProfileItem();
            // ensure updated ProfileItem does not break rules for ProfileItems
            ProfileItem profileItemCopy = profileItem.getCopy();
            updateProfileItem(profileItemCopy, form);
            if (!profileService.isEquivilentProfileItemExists(profileItemCopy)) {
                // update persistent ProfileItem
                updateProfileItem(profileItem, form);
                // update ItemValues if supplied
                Map<String, ItemValue> itemValues = profileItem.getItemValuesMap();
                for (String name : form.getNames()) {
                    ItemValue itemValue = itemValues.get(name);
                    if (itemValue != null) {
                        itemValue.setValue(form.getFirstValue(name));
                    }
                }
                log.debug("ProfileItem updated");
                // all done, need to recalculate and clear caches
                calculator.calculate(profileItem);
                pathItemService.removePathItemGroup(profileBrowser.getProfile());
                profileSheetService.removeSheets(profileBrowser.getProfile());
                // do response
                if (isStandardWebBrowser()) {
                    success(profileBrowser.getFullPath());
                } else {
                    // return a response for API calls
                    super.handleGet();
                }
            } else {
                log.warn("ProfileItem NOT updated");
                badRequest();
            }
        } else {
            notAuthorized();
        }
    }

    protected void updateProfileItem(ProfileItem profileItem, Form form) {
        Set<String> names = form.getNames();

        // update 'name' value
        if (names.contains("name")) {
            profileItem.setName(form.getFirstValue("name"));
        }
        // update 'startDate' value
        if (names.contains("startDate")) {
            profileItem.setStartDate(new StartEndDate(form.getFirstValue("validFrom")));
        }

        // update 'end' value
        if (names.contains("end")) {
            profileItem.setEnd(Boolean.valueOf(form.getFirstValue("end")));
        }

        // update 'endDate' value
        if (names.contains("endDate")) {
            profileItem.setEndDate(new StartEndDate(form.getFirstValue("endDate")));
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void delete() {
        log.debug("delete");
        if (profileBrowser.getProfileItemActions().isAllowDelete()) {
            ProfileItem profileItem = profileBrowser.getProfileItem();
            profileService.remove(profileItem);
            pathItemService.removePathItemGroup(profileBrowser.getProfile());
            profileSheetService.removeSheets(profileBrowser.getProfile());
            success(pathItem.getParent().getFullPath());
        } else {
            notAuthorized();
        }
    }

    public List<ProfileItem> getProfileItems() {
        return null;
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
}
