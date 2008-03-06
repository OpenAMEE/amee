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
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.EngineUtils;
import gc.carbon.data.Calculator;
import gc.carbon.data.DataCategory;
import gc.carbon.data.DataItem;
import gc.carbon.data.DataService;
import gc.carbon.data.ItemValue;
import gc.carbon.path.PathItem;
import gc.carbon.path.PathItemService;
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
import java.util.Map;

@Name("profileCategoryResource")
@Scope(ScopeType.EVENT)
public class ProfileCategoryResource extends BaseResource implements Serializable {

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

    private ProfileItem newProfileItem;

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
        profileBrowser.setProfileDate(request.getResourceRef().getQueryAsForm());
        setPage(request);
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
        // Map<String, Sheet> sheets = profileSheetService.getSheets(profile, pathItem.findChildUidsByType("DC"));
        Sheet sheet = profileSheetService.getSheet(profile, dataCategory, profileBrowser.getProfileDate());
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
        JSONObject obj = new JSONObject();

        Profile profile = profileBrowser.getProfile();
        DataCategory dataCategory = profileBrowser.getDataCategory();

        // add objects
        obj.put("path", pathItem.getFullPath());
        obj.put("profileDate", EngineUtils.getMonthlyDate(profileBrowser.getProfileDate()));

        // add relevant Profile info depending on whether we are at root
        if (pathItem.getParent() == null) {
            obj.put("profile", profile.getJSONObject());
        } else {
            obj.put("profile", profile.getIdentityJSONObject());
        }

        // add Data Category
        obj.put("dataCategory", dataCategory.getIdentityJSONObject());

        if (isGet()) {

            // create children JSON
            JSONObject children = new JSONObject();

            // add Data Categories via pathItem to children
            JSONArray dataCategories = new JSONArray();
            for (PathItem pi : pathItem.getChildrenByType("DC")) {
                dataCategories.put(pi.getJSONObject());
            }
            children.put("dataCategories", dataCategories);

            // add Sheet containing Profile Items & totalAmountPerMonth
            Sheet sheet = profileSheetService.getSheet(profile, dataCategory, profileBrowser.getProfileDate());
            if (sheet != null) {
                Pager pager = getPager(environment.getItemsPerPage());
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(getPage());
                children.put("profileItems", sheet.getJSONObject());
                children.put("pager", pager.getJSONObject());
                obj.put("totalAmountPerMonth", profileSheetService.getTotalAmountPerMonth(sheet));
            } else {
                children.put("profileItems", new JSONObject());
                children.put("pager", new JSONObject());
                obj.put("totalAmountPerMonth", "0");
            }

            // add chilren
            obj.put("children", children);

        } else if (isPost()) {
            if (newProfileItem != null) {
                obj.put("profileItem", newProfileItem.getJSONObject());
            }
        }
        return obj;
    }

    @Override
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        return getJSONObject();
    }

    @Override
    public Element getElement(Document document, boolean detailed) {
        Profile profile = profileBrowser.getProfile();
        DataCategory dataCategory = profileBrowser.getDataCategory();

        // create element
        Element element = document.createElement("ProfileCategoryResource");

        // add objects
        element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));
        // add profile date
        element.appendChild(APIUtils.getElement(document, "ProfileDate",
                EngineUtils.getMonthlyDate(profileBrowser.getProfileDate())));

        // add relevant Profile info depending on whether we are at root
        if (pathItem.getParent() == null) {
            element.appendChild(profile.getElement(document));
        } else {
            element.appendChild(profile.getIdentityElement(document));
        }

        // add DataCategory and Profile elements
        element.appendChild(dataCategory.getIdentityElement(document));

        if (isGet()) {

            // list child Profile Categories and child Profile Items
            Element childrenElement = document.createElement("Children");
            element.appendChild(childrenElement);

            // add Profile Categories via pathItem
            Element dataCategoriesElement = document.createElement("ProfileCategories");
            for (PathItem pi : pathItem.getChildrenByType("DC")) {
                dataCategoriesElement.appendChild(pi.getElement(document));
            }
            childrenElement.appendChild(dataCategoriesElement);

            // get Sheet containing Profile Items
            Sheet sheet = profileSheetService.getSheet(profile, dataCategory, profileBrowser.getProfileDate());
            if (sheet != null) {
                Pager pager = getPager(environment.getItemsPerPage());
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(getPage());
                // list child Profile Items via sheet
                childrenElement.appendChild(sheet.getElement(document, false));
                childrenElement.appendChild(pager.getElement(document));
                // add CO2 amount
                element.appendChild(APIUtils.getElement(document, "TotalAmountPerMonth",
                        profileSheetService.getTotalAmountPerMonth(sheet).toString()));
            }

        } else if (isPost()) {
            if (newProfileItem != null) {
                element.appendChild(newProfileItem.getElement(document, detailed));
            }
        }

        return element;
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
    public void post(Representation entity) {
        log.debug("post");
        if (profileBrowser.getProfileItemActions().isAllowCreate()) {
            Form form = getForm();
            // are we creating a new ProfileItem?
            String dateItemUid = form.getFirstValue("dataItemUid");
            if (dateItemUid != null) {
                // find the DataItem
                DataCategory dataCategory = profileBrowser.getDataCategory();
                DataItem dataItem =
                        dataService.getDataItem(dataCategory, dateItemUid);
                if (dataItem != null) {
                    // create new ProfileItem
                    newProfileItem =
                            new ProfileItem(profileBrowser.getProfile(), dataCategory, dataItem);
                    // determine name for new ProfileItem
                    newProfileItem.setName(form.getFirstValue("name"));
                    // determine date for new ProfileItem
                    newProfileItem.setValidFrom(form.getFirstValue("validFrom"));
                    // determine if new ProfileItem is an end marker
                    newProfileItem.setEnd(form.getFirstValue("end"));
                    // see if ProfileItem already exists
                    if (!profileService.isEquivilentProfileItemExists(newProfileItem)) {
                        // save newProfileItem and do calculations
                        entityManager.persist(newProfileItem);
                        profileService.checkProfileItem(newProfileItem);
                        // update item values if supplied
                        Map<String, ItemValue> itemValues = newProfileItem.getItemValuesMap();
                        for (String name : form.getNames()) {
                            ItemValue itemValue = itemValues.get(name);
                            if (itemValue != null) {
                                itemValue.setValue(form.getFirstValue(name));
                            }
                        }
                        calculator.calculate(newProfileItem);
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
                        log.warn("ProfileItem already exists");
                        // TODO: return nicer error code
                        badRequest();
                    }
                } else {
                    log.warn("DataItem not found");
                    // TODO: return nicer error code
                    badRequest();
                }
            } else {
                log.warn("dataItemUid not supplied");
                // TODO: return nicer error code
                badRequest();
            }
        } else {
            notAuthorized();
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
}