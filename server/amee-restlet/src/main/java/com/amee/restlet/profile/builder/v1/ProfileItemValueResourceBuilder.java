/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
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
package com.amee.restlet.profile.builder.v1;

import com.amee.core.APIUtils;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.builder.v1.ItemValueBuilder;
import com.amee.restlet.profile.ProfileItemValueResource;
import com.amee.restlet.profile.builder.IProfileItemValueResourceBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

@Service("v1ProfileItemValueResourceBuilder")
public class ProfileItemValueResourceBuilder implements IProfileItemValueResourceBuilder {

    @Override
    public Element getElement(ProfileItemValueResource resource, Document document) {
        ItemValue itemValue = resource.getProfileItemValue();
        Element element = document.createElement("ProfileItemValueResource");
        itemValue.setBuilder(new ItemValueBuilder(itemValue));
        element.appendChild(itemValue.getElement(document));
        element.appendChild(APIUtils.getElement(document, "Path", resource.getPathItem().getFullPath()));
        element.appendChild(resource.getProfile().getIdentityElement(document));
        return element;
    }

    @Override
    public Map<String, Object> getTemplateValues(ProfileItemValueResource resource) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getProfileBrowser());
        values.put("profileItemValue", resource.getProfileItemValue());
        values.put("node", resource.getProfileItemValue());
        values.put("profileItem", resource.getProfileItem());
        values.put("profile", resource.getProfile());
        return values;
    }

    @Override
    public org.apache.abdera.model.Element getAtomElement(ProfileItemValueResource resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject getJSONObject(ProfileItemValueResource resource) throws JSONException {
        JSONObject obj = new JSONObject();
        ItemValue itemValue = resource.getProfileItemValue();
        itemValue.setBuilder(new ItemValueBuilder(itemValue));
        obj.put("itemValue", itemValue.getJSONObject(true));
        obj.put("path", resource.getPathItem().getFullPath());
        obj.put("profile", resource.getProfile().getIdentityJSONObject());
        return obj;
    }
}
