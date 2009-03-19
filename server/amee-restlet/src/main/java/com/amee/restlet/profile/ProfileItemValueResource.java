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

import com.amee.domain.APIUtils;
import com.amee.domain.data.ItemValue;
import com.amee.restlet.profile.acceptor.IItemValueFormAcceptor;
import com.amee.restlet.profile.acceptor.IItemValueRepresentationAcceptor;
import com.amee.restlet.profile.builder.v2.AtomFeed;
import com.amee.service.profile.ProfileBrowser;
import com.amee.service.profile.ProfileConstants;
import com.amee.service.profile.ProfileService;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.IRIElement;
import org.apache.abdera.model.Text;
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
import java.util.Map;

@Component
@Scope("prototype")
public class ProfileItemValueResource extends BaseProfileResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ProfileService profileService;

    private ItemValue itemValue;

    @Autowired
    private IItemValueFormAcceptor formAcceptor;

    @Autowired
    private IItemValueRepresentationAcceptor atomAcceptor;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setDataCategory(request.getAttributes().get("categoryUid").toString());
        setProfileItem(request.getAttributes().get("itemUid").toString());
        setProfileItemValue(request.getAttributes().get("valuePath").toString());
        setAvailable(isValid());
    }

    private void setProfileItemValue(String itemValuePath) {
        if (itemValuePath.isEmpty()) return;
        if (getProfileItem() == null) return;
        this.itemValue = getProfileItem().getItemValuesMap().get(itemValuePath);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (getProfileItem() != null) && (getProfileItemValue() != null);
    }

    @Override
    public String getTemplatePath() {
        return getAPIVersion() + "/" + ProfileConstants.VIEW_PROFILE_ITEM_VALUE;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", profileBrowser);
        values.put("profileItemValue", getProfileItemValue());
        values.put("node", getProfileItemValue());
        values.put("profileItem", getProfileItem());
        values.put("profile", getProfile());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("itemValue", getProfileItemValue().getJSONObject(true));
        obj.put("path", getPathItem().getFullPath());
        obj.put("profile", getProfile().getIdentityJSONObject());
        obj.put("actions", getActions(profileBrowser.getProfileItemValueActions()));
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        ItemValue itemValue = getProfileItemValue();
        Element element = document.createElement("ProfileItemValueResource");
        element.appendChild(itemValue.getElement(document));
        element.appendChild(APIUtils.getElement(document, "Path", getPathItem().getFullPath()));
        element.appendChild(getProfile().getIdentityElement(document));
        return element;
    }

    @Override
    public org.apache.abdera.model.Element getAtomElement() {

        AtomFeed atomFeed = AtomFeed.getInstance();
        Entry entry = atomFeed.newEntry();

        entry.setBaseUri(getRequest().getAttributes().get("previousHierachicalPart").toString());

        Text title = atomFeed.newTitle(entry);
        title.setText(getProfileItemValue().getDisplayName());

        atomFeed.addLinks(entry, "");

        IRIElement eid = atomFeed.newID(entry);
        eid.setText("urn:itemValue:" + getProfileItemValue().getUid());

        entry.setPublished(getProfileItemValue().getCreated());
        entry.setUpdated(getProfileItemValue().getModified());

        atomFeed.addItemValue(entry, getProfileItemValue());

        StringBuilder content = new StringBuilder(getProfileItemValue().getName());
        content.append("=");
        content.append(getProfileItemValue().getValue().isEmpty() ? "N/A" : getProfileItemValue().getValue());
        if (getProfileItemValue().hasUnit())
            content.append(", unit=");
            content.append(getProfileItemValue().getUnit());
        if (getProfileItemValue().hasPerUnit())
            content.append(", v=");
            content.append(getProfileItemValue().getPerUnit());
        entry.setContent(content.toString());

        Category cat = atomFeed.newItemValueCategory(entry);
        cat.setTerm(getProfileItemValue().getItemValueDefinition().getUid());
        cat.setLabel(getProfileItemValue().getItemValueDefinition().getName());

        return entry;

    }

    @Override
    public void handleGet() {
        log.debug("handleGet()");
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
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation()");
        if (profileBrowser.getProfileItemValueActions().isAllowModify()) {

            if (MediaType.APPLICATION_ATOM_XML.includes(entity.getMediaType())) {
                atomAcceptor.accept(this, entity);
            } else {
                formAcceptor.accept(this, getForm());
            }
            successfulPut(getFullPath());
        } else {
            notAuthorized();
        }
    }

    public ProfileBrowser getProfileBrowser() {
        return profileBrowser;
    }

    public ItemValue getProfileItemValue() {
        return itemValue;
    }
}