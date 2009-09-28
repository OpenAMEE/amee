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
package com.amee.restlet.permissions;

import com.amee.domain.AMEEEntity;
import com.amee.domain.AMEEEntityReference;
import com.amee.domain.auth.Permission;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.auth.PermissionService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")

public class PermissionsResource extends AuthorizeResource {

    @Autowired
    private PermissionService permissionService;

    private AMEEEntityReference entityReference;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        setRequireSuperUser(true);
        entityReference = getEntityReference(request);
    }

    @Override
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(getActiveEnvironment());
        return entities;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (entityReference != null);
    }

    @Override
    public String getTemplatePath() {
        throw new UnsupportedOperationException("PermissionsResource does not have or need a template.");
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        List<Permission> permissions =
                permissionService.getPermissionsForEntity(entityReference);
        JSONObject obj = new JSONObject();
        JSONArray permissionsArr = new JSONArray();
        for (Permission permission : permissions) {
            permissionsArr.put(permission.getJSONObject());
        }
        obj.put("permissions", permissionsArr);
        return obj;
    }

    @Override
    public void doAccept(Representation entity) {
    }

    public AMEEEntityReference getEntityReference(Request request) {
        AMEEEntityReference entityReference = null;
        Form form = request.getResourceRef().getQueryAsForm();
        if (form.getNames().contains("entityUid") && form.getNames().contains("entityType")) {
            entityReference = new AMEEEntityReference(
                    form.getFirstValue("entityType"),
                    form.getFirstValue("entityUid"));
        }
        return entityReference;
    }
}
