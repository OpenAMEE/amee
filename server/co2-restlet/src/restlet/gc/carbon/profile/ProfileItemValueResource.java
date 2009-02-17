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

import com.jellymold.utils.domain.APIUtils;
import gc.carbon.AMEEResource;
import gc.carbon.profile.acceptor.ItemValueAcceptor;
import gc.carbon.profile.builder.v2.AtomFeed;
import gc.carbon.profile.acceptor.*;
import gc.carbon.domain.data.ItemValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Text;
import org.apache.abdera.model.IRIElement;
import org.apache.abdera.model.Category;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

@Component
@Scope("prototype")
public class ProfileItemValueResource extends AMEEResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ProfileService profileService;

    private ProfileBrowser profileBrowser;

    private Map<MediaType, ItemValueAcceptor> acceptors;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        profileBrowser = (ProfileBrowser) beanFactory.getBean("profileBrowser");
        profileBrowser.setDataCategoryUid(request.getAttributes().get("categoryUid").toString());
        profileBrowser.setProfileItemUid(request.getAttributes().get("itemUid").toString());
        profileBrowser.setProfileItemValueUid(request.getAttributes().get("valueUid").toString());
        setAcceptors();
        setAvailable(isValid());
    }

    private void setAcceptors() {
        acceptors = new HashMap<MediaType, ItemValueAcceptor>();
        acceptors.put(MediaType.APPLICATION_WWW_FORM, new ProfileItemValueFormAcceptor(this));
        acceptors.put(MediaType.APPLICATION_ATOM_XML, new ProfileItemValueAtomAcceptor(this));
    }

    public ItemValueAcceptor getAcceptor(MediaType type) {
        if (MediaType.APPLICATION_ATOM_XML.includes(type)) {
            return acceptors.get(MediaType.APPLICATION_ATOM_XML);
        } else {
            return acceptors.get(MediaType.APPLICATION_WWW_FORM);
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (profileBrowser.getProfileItemUid() != null) &&
                (profileBrowser.getProfileItemValueUid() != null);
    }

    @Override
    public String getTemplatePath() {
        return getApiVersion() + "/" + ProfileConstants.VIEW_PROFILE_ITEM_VALUE;
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
        obj.put("actions", getActions(profileBrowser.getProfileItemValueActions()));
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
    public org.apache.abdera.model.Element getAtomElement() {

        ItemValue itemValue = profileBrowser.getProfileItemValue();

        AtomFeed atomFeed = AtomFeed.getInstance();
        Entry entry = atomFeed.newEntry();

        entry.setBaseUri(getRequest().getAttributes().get("previousHierachicalPart").toString());

        Text title = atomFeed.newTitle(entry);
        title.setText(itemValue.getDisplayName());

        atomFeed.addLinks(entry, "");

        IRIElement eid = atomFeed.newID(entry);
        eid.setText("urn:itemValue:" + itemValue.getUid());

        entry.setPublished(itemValue.getCreated());
        entry.setUpdated(itemValue.getModified());

        atomFeed.addItemValue(entry, itemValue);

        StringBuilder content = new StringBuilder(itemValue.getName());
        content.append("=");
        content.append(itemValue.getValue().isEmpty() ? "N/A" : itemValue.getValue());
        if (itemValue.hasUnit())
            content.append(", unit=");
            content.append(itemValue.getUnit());
        if (itemValue.hasPerUnit())
            content.append(", perUnit=");
            content.append(itemValue.getPerUnit());
        entry.setContent(content.toString());

        Category cat = atomFeed.newItemValueCategory(entry);
        cat.setTerm(itemValue.getItemValueDefinition().getUid());
        cat.setLabel(itemValue.getItemValueDefinition().getName());

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

            getAcceptor(entity.getMediaType()).accept(entity);

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

    public ProfileBrowser getProfileBrowser() {
        return profileBrowser;
    }

    public ProfileService getProfileService() {
        return profileService;
    }
}