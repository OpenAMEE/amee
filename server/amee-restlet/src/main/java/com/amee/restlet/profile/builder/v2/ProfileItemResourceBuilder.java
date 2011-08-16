package com.amee.restlet.profile.builder.v2;

import com.amee.base.utils.XMLUtils;
import com.amee.domain.item.profile.ProfileItem;
import com.amee.domain.profile.builder.v2.ProfileItemBuilder;
import com.amee.platform.science.CO2AmountUnit;
import com.amee.restlet.profile.ProfileItemResource;
import com.amee.restlet.profile.builder.IProfileItemResourceBuilder;
import com.amee.service.item.DataItemServiceImpl;
import com.amee.service.item.ProfileItemServiceImpl;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.IRIElement;
import org.apache.abdera.model.Text;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

@Service("v2ProfileItemResourceBuilder")
public class ProfileItemResourceBuilder implements IProfileItemResourceBuilder {

    @Autowired
    ProfileItemServiceImpl profileItemService;

    @Autowired
    DataItemServiceImpl dataItemService;

    public JSONObject getJSONObject(ProfileItemResource resource) throws JSONException {
        JSONObject obj = new JSONObject();
        ProfileItem profileItem = resource.getProfileItem();
        obj.put("profileItem", getProfileItemBuilder(resource, profileItem).getJSONObject(true));
        obj.put("path", resource.getProfileItem().getFullPath());
        obj.put("profile", resource.getProfile().getIdentityJSONObject());
        return obj;
    }

    public Element getElement(ProfileItemResource resource, Document document) {
        ProfileItem profileItem = resource.getProfileItem();
        Element element = document.createElement("ProfileItemResource");
        element.appendChild(getProfileItemBuilder(resource, profileItem).getElement(document, true));
        element.appendChild(XMLUtils.getElement(document, "Path", resource.getProfileItem().getFullPath()));
        element.appendChild(resource.getProfile().getIdentityElement(document));
        return element;
    }

    public Map<String, Object> getTemplateValues(ProfileItemResource resource) {
        ProfileItem profileItem = resource.getProfileItem();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getProfileBrowser());
        values.put("profile", profileItem.getProfile());
        values.put("profileItem", profileItem);
        values.put("node", profileItem);
        return values;
    }

    public org.apache.abdera.model.Element getAtomElement(ProfileItemResource resource) {

        AtomFeed atomFeed = AtomFeed.getInstance();

        ProfileItem profileItem = resource.getProfileItem();

        CO2AmountUnit returnUnit = resource.getProfileBrowser().getCo2AmountUnit();
        String amount = profileItem.getAmounts().getDefaultValue().toAmount().convert(returnUnit).toString();

        Entry entry = atomFeed.newEntry();
        entry.setBaseUri(resource.getRequest().getAttributes().get("previousHierachicalPart").toString());

        Text title = atomFeed.newTitle(entry);
        title.setText(profileItem.getDisplayName());
        Text subtitle = atomFeed.newSubtitle(entry);
        subtitle.setText(atomFeed.format(profileItem.getStartDate()) + ((profileItem.getEndDate() != null) ? " - " + atomFeed.format(profileItem.getEndDate()) : ""));

        atomFeed.addLinks(entry, "");

        IRIElement eid = atomFeed.newID(entry);
        eid.setText("urn:item:" + profileItem.getUid());

        entry.setPublished(profileItem.getStartDate());
        entry.setUpdated(profileItem.getStartDate());

        atomFeed.addDataItem(entry, profileItem.getDataItem());

        HCalendar content = new HCalendar();

        content.addSummary(amount + " " + returnUnit.toString());

        content.addStartDate(profileItem.getStartDate());
        if (profileItem.getEndDate() != null) {
            content.addEndDate(profileItem.getEndDate());
        }
        entry.setContentAsHtml(content.toString());

        atomFeed.addStartDate(entry, profileItem.getStartDate().toString());
        if (profileItem.getEndDate() != null) {
            atomFeed.addEndDate(entry, profileItem.getEndDate().toString());
        }

        atomFeed.addAmount(entry, amount, returnUnit.toString());

        atomFeed.addItemValuesWithLinks(entry, profileItemService.getItemValues(profileItem), "");

        Category cat = atomFeed.newItemCategory(entry);
        cat.setTerm(profileItem.getDataItem().getUid());
        cat.setLabel(profileItem.getDataItem().getItemDefinition().getName());

        return entry;

    }

    private ProfileItemBuilder getProfileItemBuilder(ProfileItemResource resource, ProfileItem pi) {
        if (resource.getProfileBrowser().requestedCO2InExternalUnit()) {
            return new ProfileItemBuilder(pi, dataItemService, profileItemService, resource.getProfileBrowser().getCo2AmountUnit());
        } else {
            return new ProfileItemBuilder(pi, dataItemService, profileItemService);
        }
    }
}