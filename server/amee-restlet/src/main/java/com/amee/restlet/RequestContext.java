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
package com.amee.restlet;

import com.amee.domain.auth.User;
import com.amee.domain.profile.Profile;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.restlet.data.Form;
import org.restlet.data.Request;

/**
 * A simple bean for holding contextual information about the request.
 *
 * Its intended use is in debug statements etc.
 */
public class RequestContext {

    private String username;
    private String profileUid;
    private String apiVersion;
    private String requestPath;
    private String method;
    private String requestParameters;
    private String error;
    private String form;

    public void setUser(User user) {
        if (user == null)
            return;
        this.username = user.getUsername();
        this.apiVersion = user.getAPIVersion().toString();
    }

    public void setProfile(Profile profile) {
        this.profileUid = profile.getUid();
    }

    public void setRequest(Request request) {
        this.requestPath = request.getResourceRef().getPath();
        this.method = request.getMethod().toString();
        this.requestParameters = request.getResourceRef().getQuery();
    }

    public void setError(String error) {
        this.error = error;    
    }

    public void setForm(Form form) {
        this.form = form.toString();
    }

    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        sb.append("username", username);
        sb.append("apiVersion", apiVersion);
        if (profileUid != null)
            sb.append("profile", profileUid);
        sb.append("path", requestPath);
        sb.append("method", method);
        sb.append("parameters", requestParameters);
        sb.append("form", form);
        sb.append("error", error);
        return sb.toString();
    }
}
