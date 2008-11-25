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

import com.jellymold.utils.ThreadBeanHolder;
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.data.Calculator;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.path.PathItemService;
import gc.carbon.BaseResource;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
public class ProfileItemValueResource extends BaseResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private PathItemService pathItemService;

    @Autowired
    private ProfileSheetService profileSheetService;

    @Autowired
    private Calculator calculator;

    private ProfileBrowser profileBrowser;
    private PathItem pathItem;

    public ProfileItemValueResource() {
        super();
    }

    public ProfileItemValueResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        pathItem = getPathItem();
        profileBrowser = getProfileBrowser();
        profileBrowser.setDataCategoryUid(request.getAttributes().get("categoryUid").toString());
        profileBrowser.setProfileItemUid(request.getAttributes().get("itemUid").toString());
        profileBrowser.setProfileItemValueUid(request.getAttributes().get("valueUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (profileBrowser.getProfileItemUid() != null) &&
                (profileBrowser.getProfileItemValueUid() != null);
    }

    @Override
    public String getTemplatePath() {
        return ProfileConstants.VIEW_PROFILE_ITEM_VALUE;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", profileBrowser);
        values.put("profileItemValue", profileBrowser.getProfileItemValue());
        values.put("node", profileBrowser.getProfileItemValue());
        values.put("profileItem", profileBrowser.getProfileItem());
        values.put("profile", profileBrowser.getProfile());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("itemValue", profileBrowser.getProfileItemValue().getJSONObject(true));
        obj.put("path", pathItem.getFullPath());
        obj.put("profile", profileBrowser.getProfile().getIdentityJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        ItemValue itemValue = profileBrowser.getProfileItemValue();
        Element element = document.createElement("ProfileItemValueResource");
        element.appendChild(itemValue.getElement(document));
        element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));
        element.appendChild(profileBrowser.getProfile().getIdentityElement(document));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (profileBrowser.getProfileItemValueActions().isAllowView()) {
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
        if (profileBrowser.getProfileItemValueActions().isAllowModify()) {
            Form form = getForm();
            ItemValue profileItemValue = profileBrowser.getProfileItemValue();
            ProfileItem profileItem = profileBrowser.getProfileItem();
            // are we updating this ProfileItemValue?
            if (form.getFirstValue("value") != null) {
                // update ProfileItemValue
                profileItemValue.setValue(form.getFirstValue("value"));
            }
            // should recalculate now (regardless)
            calculator.calculate(profileItem);
            // path may have changed
            pathItemService.removePathItemGroup(profileBrowser.getProfile());
            profileSheetService.removeSheets(profileBrowser.getProfile());
            // all done
            if (isStandardWebBrowser()) {
                success(profileBrowser.getFullPath());
            } else {
                // return a response for API calls
                super.handleGet();
            }
        } else {
            notAuthorized();
        }
    }
}