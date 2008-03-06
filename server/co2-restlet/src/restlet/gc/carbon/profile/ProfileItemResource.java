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
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.data.Calculator;
import gc.carbon.data.DataService;
import gc.carbon.data.ItemValue;
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
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@Name("profileItemResource")
@Scope(ScopeType.EVENT)
public class ProfileItemResource extends BaseResource implements Serializable {

    private final static Logger log = Logger.getLogger(ProfileItemResource.class);

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
    private PathItem pathItem;

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
        JSONObject obj = new JSONObject();
        ProfileItem profileItem = profileBrowser.getProfileItem();
        obj.put("profileItem", profileItem.getJSONObject());
        obj.put("path", pathItem.getFullPath());
        obj.put("profile", profileBrowser.getProfile().getIdentityJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        ProfileItem profileItem = profileBrowser.getProfileItem();
        Element element = document.createElement("ProfileItemResource");
        element.appendChild(profileItem.getElement(document));
        element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));
        element.appendChild(profileBrowser.getProfile().getIdentityElement(document));
        return element;
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
        // update 'name' value
        Set<String> names = form.getNames();
        if (names.contains("name")) {
            profileItem.setName(form.getFirstValue("name"));
        }
        // update 'validFrom' value
        if (names.contains("validFrom")) {
            profileItem.setValidFrom(form.getFirstValue("validFrom"));
        }
        // update 'end' value
        if (names.contains("end")) {
            profileItem.setEnd(form.getFirstValue("end"));
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
}
