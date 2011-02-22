package com.amee.restlet.profile.builder.v1;

import com.amee.base.utils.XMLUtils;
import com.amee.domain.IDataItemService;
import com.amee.domain.item.profile.ProfileItem;
import com.amee.domain.profile.builder.v1.ProfileItemBuilder;
import com.amee.platform.science.AmountPerUnit;
import com.amee.restlet.profile.ProfileItemResource;
import com.amee.restlet.profile.builder.IProfileItemResourceBuilder;
import com.amee.service.item.DataItemService;
import com.amee.service.item.ProfileItemService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
@Service("v1ProfileItemResourceBuilder")
public class ProfileItemResourceBuilder implements IProfileItemResourceBuilder {

    @Autowired
    private ProfileItemService profileItemService;

    @Autowired
    private DataItemService dataItemService;

    public JSONObject getJSONObject(ProfileItemResource resource) throws JSONException {
        JSONObject obj = new JSONObject();
        ProfileItem profileItem = resource.getProfileItem();
        obj.put("profileItem", new ProfileItemBuilder(profileItem, dataItemService, profileItemService).getJSONObject(true));
        obj.put("path", resource.getProfileItem().getFullPath());
        obj.put("profile", resource.getProfile().getIdentityJSONObject());
        return obj;
    }

    public Element getElement(ProfileItemResource resource, Document document) {
        ProfileItem profileItem = resource.getProfileItem();
        Element element = document.createElement("ProfileItemResource");
        element.appendChild(new ProfileItemBuilder(profileItem, dataItemService, profileItemService).getElement(document, true));
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
        if (!profileItemService.isSingleFlight(profileItem)) {
            values.put("amountPerMonth", profileItem.getAmounts().defaultValueAsAmount().convert(AmountPerUnit.MONTH).getValue());
        } else {
            values.put("amountPerMonth", profileItem.getAmounts().defaultValueAsDouble());
        }
        values.put("node", profileItem);
        return values;
    }

    public org.apache.abdera.model.Element getAtomElement(ProfileItemResource resource) {
        throw new UnsupportedOperationException();
    }
}
