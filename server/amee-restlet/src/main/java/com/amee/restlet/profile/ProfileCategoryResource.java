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
package com.amee.restlet.profile;

import com.amee.base.utils.ThreadBeanHolder;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.IDataCategoryReference;
import com.amee.domain.data.DataCategory;
import com.amee.domain.profile.ProfileItem;
import com.amee.platform.science.CO2AmountUnit;
import com.amee.restlet.RequestContext;
import com.amee.restlet.profile.acceptor.ProfileCategoryAtomAcceptor;
import com.amee.restlet.profile.acceptor.ProfileCategoryFormAcceptor;
import com.amee.restlet.profile.acceptor.ProfileCategoryJSONAcceptor;
import com.amee.restlet.profile.acceptor.ProfileCategoryXMLAcceptor;
import com.amee.restlet.profile.builder.IProfileCategoryResourceBuilder;
import com.amee.restlet.profile.builder.ProfileCategoryResourceBuilderFactory;
import com.amee.restlet.utils.APIException;
import com.amee.service.profile.ProfileConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

@Component("profileCategoryResource")
@Scope("prototype")
public class ProfileCategoryResource extends BaseProfileResource {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ProfileCategoryFormAcceptor formAcceptor;

    @Autowired
    private ProfileCategoryAtomAcceptor atomAcceptor;

    @Autowired
    private ProfileCategoryXMLAcceptor xmlAcceptor;

    @Autowired
    private ProfileCategoryJSONAcceptor jsonAcceptor;

    @Autowired
    private ProfileCategoryResourceBuilderFactory builderFactory;

    private DataCategory dataCategory;
    private List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
    private IProfileCategoryResourceBuilder builder;
    private boolean recurse = false;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);

        // Obtain DataCategory.
        dataCategory = dataService.getDataCategoryByUid(request.getAttributes().get("categoryUid").toString());
        ((RequestContext) ThreadBeanHolder.get("ctx")).setDataCategory(dataCategory);

        // Media type sensitive builder.
        setBuilderStrategy();

        // Evil recursive option.
        recurse = request.getResourceRef().getQueryAsForm().getFirstValue("recurse", "false").equals("true");
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (getProfile() != null) &&
                (dataCategory != null) &&
                !dataCategory.getFullPath().startsWith("/lca/ecoinvent") &&
                !dataCategory.isTrash();
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        DataCategory dc = dataCategory;
        while (dc != null) {
            entities.add(dc);
            dc = dc.getDataCategory();
        }
        entities.add(getProfile());
        entities.add(getRootDataCategory());
        Collections.reverse(entities);
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return getAPIVersion() + "/" + ProfileConstants.VIEW_PROFILE_CATEGORY;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> templateValues = builder.getTemplateValues(this);
        templateValues.putAll(super.getTemplateValues());
        return templateValues;
    }

    @Override
    public org.apache.abdera.model.Element getAtomElement() {
        return builder.getAtomElement(this);
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        return builder.getJSONObject(this);
    }

    @Override
    public Element getElement(Document document) {
        return builder.getElement(this, document);
    }

    @Override
    public void doGet() {

        log.debug("doGet()");

        if (!validateParameters()) {
            return;
        }

        Form form = getRequest().getResourceRef().getQueryAsForm();
        if (getAPIVersion().isVersionOne()) {
            profileBrowser.setProfileDate(form.getFirstValue("profileDate"));
        } else {
            profileBrowser.setQueryStartDate(form.getFirstValue("startDate"));
            profileBrowser.setQueryEndDate(form.getFirstValue("endDate"));
            profileBrowser.setDuration(form.getFirstValue("duration"));
            profileBrowser.setSelectBy(form.getFirstValue("selectby"));
            profileBrowser.setMode(form.getFirstValue("mode"));
            String unit = form.getFirstValue("returnUnit");
            String perUnit = form.getFirstValue("returnPerUnit");
            profileBrowser.setCO2AmountUnit(new CO2AmountUnit(unit, perUnit));
        }
        super.doGet();
    }

    @Override
    public void doAcceptOrStore(Representation entity) {
        log.debug("doAcceptOrStore()");
        try {
            // get list of updated or created ProfileItems
            profileItems = doAcceptOrStoreProfileItems(entity);
            // clear caches
            profileService.clearCaches(getProfile());
            if (isPost()) {
                if (isBatchPost()) {
                    successfulBatchPost();
                } else {
                    successfulPost(getFullPath(), profileItems.get(0).getUid());
                }
            } else {
                successfulPut(getFullPath());
            }
        } catch (APIException e) {
            badRequest(e.getApiFault());
        }
    }

    public String getFullPath() {
        return "/profiles/" + getProfile().getFullPath() + dataCategory.getFullPath();
    }

    protected List<ProfileItem> doAcceptOrStoreProfileItems(Representation entity) throws APIException {
        // Accept the representation according to the MediaType
        MediaType type = entity.getMediaType();
        if (MediaType.APPLICATION_JSON.includes(type)) {
            return jsonAcceptor.accept(this, entity);
        } else if (MediaType.APPLICATION_XML.includes(type)) {
            return xmlAcceptor.accept(this, entity);
        } else if (MediaType.APPLICATION_ATOM_XML.includes(type)) {
            return atomAcceptor.accept(this, entity);
        } else {
            return formAcceptor.accept(this, getForm());
        }
    }

    @Override
    public boolean allowDelete() {
        // Only allow delete for Profiles (i.e: a request to /profiles/{profileUid}).
        return super.allowDelete() && (dataCategory.getPath().isEmpty());
    }

    @Override
    public void doRemove() {
        log.debug("doRemove()");
        profileService.clearCaches(getProfile());
        profileService.remove(getProfile());
        successfulDelete("/profiles");
    }

    public DataCategory getDataCategory() {
        return dataCategory;
    }

    public Collection<IDataCategoryReference> getDataCategories() {
        return dataService.getDataCategories(dataCategory).values();
    }

    public List<ProfileItem> getProfileItems() {
        return profileItems;
    }

    private void setBuilderStrategy() {
        builder = builderFactory.createProfileCategoryResourceBuilder(this);
    }

    public boolean isRecurse() {
        return recurse;
    }
}
