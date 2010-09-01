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
import com.amee.domain.auth.Permission;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.auth.PermissionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class PermissionResource extends AuthorizeResource implements Serializable {

    @Autowired
    private PermissionService permissionService;

    private Permission permission;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        setRequireSuperUser(true);
        permission = permissionService.getPermissionByUid(request.getAttributes().get("permissionUid").toString());
    }

    @Override
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(getRootDataCategory());
        entities.add(permission);
        return entities;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (permission != null);
    }

    @Override
    public String getTemplatePath() {
        throw new UnsupportedOperationException("PermissionResource does not have or need a template.");
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        return obj;
    }

    @Override
    public void doStore(Representation entity) {
        log.debug("doStore");
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void doRemove() {
        permissionService.remove(permission);
        success();
    }
}