package com.amee.restlet.profile.builder;

import com.amee.restlet.profile.ProfileCategoryResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
@Service
public interface IProfileCategoryResourceBuilder {

    public abstract JSONObject getJSONObject(ProfileCategoryResource resource)  throws JSONException;

    public abstract Element getElement(ProfileCategoryResource resource, Document document);

    public abstract Map<String, Object> getTemplateValues(ProfileCategoryResource resource);

    public abstract org.apache.abdera.model.Element getAtomElement(ProfileCategoryResource resource);
}
