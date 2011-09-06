package com.amee.restlet.profile;

import com.amee.base.utils.ThreadBeanHolder;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.data.DataCategory;
import com.amee.domain.item.BaseItemValue;
import com.amee.domain.item.profile.ProfileItem;
import com.amee.restlet.RequestContext;
import com.amee.restlet.profile.acceptor.IItemValueFormAcceptor;
import com.amee.restlet.profile.acceptor.IItemValueRepresentationAcceptor;
import com.amee.restlet.profile.builder.IProfileItemValueResourceBuilder;
import com.amee.restlet.profile.builder.ProfileItemValueResourceBuilderFactory;
import com.amee.service.item.ProfileItemServiceImpl;
import com.amee.service.profile.ProfileBrowser;
import com.amee.service.profile.ProfileConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
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

@Component
@Scope("prototype")
public class ProfileItemValueResource extends BaseProfileResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private IItemValueFormAcceptor formAcceptor;

    @Autowired
    private IItemValueRepresentationAcceptor atomAcceptor;

    @Autowired
    private ProfileItemValueResourceBuilderFactory builderFactory;

    @Autowired
    private ProfileItemServiceImpl profileItemService;

    private DataCategory dataCategory;
    private ProfileItem profileItem;
    private BaseItemValue itemValue;
    private IProfileItemValueResourceBuilder builder;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);

        // Obtain DataCategory.
        dataCategory = dataService.getDataCategoryByUid(request.getAttributes().get("categoryUid").toString());
        (ThreadBeanHolder.get(RequestContext.class)).setDataCategory(dataCategory);

        // Obtain ProfileItem.
        profileItem = profileItemService.getItemByUid(request.getAttributes().get("itemUid").toString());
        (ThreadBeanHolder.get(RequestContext.class)).setProfileItem(profileItem);

        // Obtain ItemValue.
        setProfileItemValue(request.getAttributes().get("valuePath").toString());
        (ThreadBeanHolder.get(RequestContext.class)).setItemValue(getProfileItemValue());

        // Media type sensitive builder.
        setBuilderStrategy();
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (getProfile() != null) &&
                (getProfileItemValue() != null) &&
                !getProfileItemValue().isTrash() &&
                (profileItem != null) &&
                (dataCategory != null) &&
                profileItem.getProfile().equals(getProfile()) &&
                profileItem.getDataCategory().equals(dataCategory) &&
                getProfileItemValue().getItem().equals(profileItem);
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(getProfileItemValue());
        entities.add(profileItem);
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
        return getAPIVersion() + "/" + ProfileConstants.VIEW_PROFILE_ITEM_VALUE;
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
    public void doStore(Representation entity) {
        log.debug("doStore()");
        if (MediaType.APPLICATION_ATOM_XML.includes(entity.getMediaType())) {
            atomAcceptor.accept(this, entity);
        } else {
            formAcceptor.accept(this, getForm());
        }
        successfulPut(getFullPath());
    }

    public String getFullPath() {
        return "/profiles/" + profileItem.getProfile().getDisplayPath() + profileItem.getFullPath() + "/" + itemValue.getDisplayPath();
    }

    public ProfileBrowser getProfileBrowser() {
        return profileBrowser;
    }

    public ProfileItem getProfileItem() {
        return profileItem;
    }

    public BaseItemValue getProfileItemValue() {
        return itemValue;
    }

    private void setProfileItemValue(String itemValuePath) {
        if (itemValuePath.isEmpty() || profileItem == null)
            return;
        this.itemValue = profileItemService.getItemValue(profileItem, itemValuePath);
    }

    private void setBuilderStrategy() {
        builder = builderFactory.createProfileItemValueResourceBuilder(this);
    }
}