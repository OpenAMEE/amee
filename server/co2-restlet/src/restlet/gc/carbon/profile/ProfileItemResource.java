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

import gc.carbon.ResourceBuilder;
import gc.carbon.ResourceBuilderFactory;
import gc.carbon.data.DataService;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.data.CO2AmountUnit;
import gc.carbon.profile.acceptor.ProfileAcceptor;
import gc.carbon.profile.acceptor.ProfileItemAtomAcceptor;
import gc.carbon.profile.acceptor.ProfileItemFormAcceptor;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("profileItemResource")
@Scope("prototype")
public class ProfileItemResource extends BaseProfileResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    ProfileService profileService;

    @Autowired
    DataService dataService;

    private Map<MediaType, ProfileAcceptor> acceptors;
    private ResourceBuilder builder;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setDataCategory(request.getAttributes().get("categoryUid").toString());
        setProfileItem(request.getAttributes().get("itemUid").toString());
        setAcceptors();
        setBuilderStrategy();
        setAvailable(isValid());
    }

    private void setAcceptors() {
        acceptors = new HashMap<MediaType, ProfileAcceptor>();
        acceptors.put(MediaType.APPLICATION_WWW_FORM, new ProfileItemFormAcceptor(this));
        acceptors.put(MediaType.APPLICATION_ATOM_XML, new ProfileItemAtomAcceptor(this));
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (getProfileItem() != null);
    }

    @Override
    public String getTemplatePath() {
        return getAPIVersion() + "/" + ProfileConstants.VIEW_PROFILE_ITEM;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", profileBrowser);
        values.put("profile", getProfileItem().getProfile());
        values.put("profileItem", getProfileItem());
        values.put("node", getProfileItem());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = builder.getJSONObject();
        obj.put("actions", getActions(profileBrowser.getProfileItemActions()));
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        return builder.getElement(document);
    }

    @Override
    public org.apache.abdera.model.Element getAtomElement() {
        return builder.getAtomElement();
    }

    @Override
    public void handleGet() {
        log.debug("handleGet()");
        if (profileBrowser.getProfileItemActions().isAllowView()) {
            if (getAPIVersion().isNotVersionOne()) {
                Form form = getRequest().getResourceRef().getQueryAsForm();
                String unit = form.getFirstValue("returnUnit");
                String perUnit = form.getFirstValue("returnPerUnit");
                profileBrowser.setCO2AmountUnit(new CO2AmountUnit(unit, perUnit));            }
                super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    private void setBuilderStrategy() {
        builder = ResourceBuilderFactory.createProfileItemBuilder(this);
    }

    @Override
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation()");
        if (isStoreAuthorized()) {
            List<ProfileItem> profileItems = doStore(entity);
            if (!profileItems.isEmpty()) {
                // do response
                if (isStandardWebBrowser()) {
                    success(getBrowserFullPath());
                } else {
                    // return a response for API calls
                    super.handleGet();
                }
            }
        } else {
            notAuthorized();
        }

    }

    protected boolean isStoreAuthorized() {
        return getProfileBrowser().getProfileItemActions().isAllowModify();
    }

    public List<ProfileItem> doStore(Representation entity) {
        if (getAPIVersion().isNotVersionOne()) {
            String unit = getForm().getFirstValue("returnUnit");
            String perUnit = getForm().getFirstValue("returnPerUnit");
            profileBrowser.setCO2AmountUnit(new CO2AmountUnit(unit, perUnit));
        }
        return getAcceptor(entity.getMediaType()).accept(entity);
    }

    public ProfileAcceptor getAcceptor(MediaType type) {
        if (MediaType.APPLICATION_ATOM_XML.includes(type)) {
            return acceptors.get(MediaType.APPLICATION_ATOM_XML);
        } else {
            return acceptors.get(MediaType.APPLICATION_WWW_FORM);
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void removeRepresentations() {
        log.debug("removeRepresentations()");
        if (profileBrowser.getProfileItemActions().isAllowDelete()) {
            ProfileItem profileItem = getProfileItem();
            profileService.remove(profileItem);
            profileService.clearCaches(getProfile());
            success(pathItem.getParent().getFullPath());
        } else {
            notAuthorized();
        }
    }

    public List<ProfileItem> getProfileItems() {
        return null;
    }

}
