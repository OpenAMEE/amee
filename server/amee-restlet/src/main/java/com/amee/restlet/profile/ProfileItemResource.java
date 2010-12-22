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
import com.amee.domain.data.DataCategory;
import com.amee.domain.profile.ProfileItem;
import com.amee.platform.science.CO2AmountUnit;
import com.amee.restlet.RequestContext;
import com.amee.restlet.profile.acceptor.ProfileItemAtomAcceptor;
import com.amee.restlet.profile.acceptor.ProfileItemFormAcceptor;
import com.amee.restlet.profile.builder.IProfileItemResourceBuilder;
import com.amee.restlet.profile.builder.ProfileItemResourceBuilderFactory;
import com.amee.service.profile.ProfileConstants;
import com.amee.service.profile.ProfileService;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component("profileItemResource")
@Scope("prototype")
public class ProfileItemResource extends BaseProfileResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ProfileItemAtomAcceptor atomAcceptor;

    @Autowired
    private ProfileItemFormAcceptor formAcceptor;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileItemResourceBuilderFactory builderFactory;

    private DataCategory dataCategory;
    private ProfileItem profileItem;
    private IProfileItemResourceBuilder builder;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);

        // Obtain DataCategory.
        dataCategory = dataService.getDataCategoryByUid(request.getAttributes().get("categoryUid").toString());
        (ThreadBeanHolder.get(RequestContext.class)).setDataCategory(dataCategory);

        // Obtain ProfileItem.
        profileItem = profileService.getProfileItem(request.getAttributes().get("itemUid").toString());
        (ThreadBeanHolder.get(RequestContext.class)).setProfileItem(profileItem);

        // Media type sensitive builder.
        setBuilderStrategy();
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (getProfile() != null) &&
                (dataCategory != null) &&
                (profileItem != null) &&
                !profileItem.isTrash() &&
                profileItem.getProfile().equals(getProfile()) &&
                profileItem.getDataCategory().equals(dataCategory) &&
                !profileItem.getDataCategory().getFullPath().startsWith("/lca/ecoinvent");
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(profileItem);
        DataCategory dc = profileItem.getDataCategory();
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
        return getAPIVersion() + "/" + ProfileConstants.VIEW_PROFILE_ITEM;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.putAll(builder.getTemplateValues(this));
        return values;
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
    public org.apache.abdera.model.Element getAtomElement() {
        return builder.getAtomElement(this);
    }

    @Override
    public void doGet() {
        log.debug("doGet()");
        if (getAPIVersion().isNotVersionOne()) {
            Form form = getRequest().getResourceRef().getQueryAsForm();
            String unit = form.getFirstValue("returnUnit");
            String perUnit = form.getFirstValue("returnPerUnit");
            profileBrowser.setCO2AmountUnit(new CO2AmountUnit(unit, perUnit));
        }
        super.doGet();
    }

    private void setBuilderStrategy() {
        builder = builderFactory.createProfileItemResourceBuilder(this);
    }

    @Override
    public void doStore(Representation entity) {
        log.debug("doStore()");
        List<ProfileItem> profileItems = doStoreProfileItems(entity);
        if (!profileItems.isEmpty()) {
            successfulPut(getFullPath());
        }
    }

    public String getFullPath() {
        return "/profiles/" + profileItem.getProfile().getDisplayPath() + profileItem.getFullPath();
    }

    protected List<ProfileItem> doStoreProfileItems(Representation entity) {
        // units are only supported beyond version one
        if (getAPIVersion().isNotVersionOne()) {
            String unit = getForm().getFirstValue("returnUnit");
            String perUnit = getForm().getFirstValue("returnPerUnit");
            profileBrowser.setCO2AmountUnit(new CO2AmountUnit(unit, perUnit));
        }
        // handle atom and standard forms differently
        MediaType type = entity.getMediaType();
        if (MediaType.APPLICATION_ATOM_XML.includes(type)) {
            return atomAcceptor.accept(this, entity);
        } else {
            return formAcceptor.accept(this, getForm());
        }
    }

    @Override
    public void doRemove() {
        log.debug("doRemove()");
        profileService.remove(profileItem);
        profileService.clearCaches(getProfile());
        successfulDelete("/profiles/" + profileItem.getProfile().getFullPath() + "/" + dataCategory.getFullPath());
    }

    // TODO: What is this used by? Templates?
    public List<ProfileItem> getProfileItems() {
        return null;
    }

    public ProfileItem getProfileItem() {
        return profileItem;
    }
}
