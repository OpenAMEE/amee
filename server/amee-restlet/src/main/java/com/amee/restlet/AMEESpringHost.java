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

import com.amee.domain.site.ISite;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.ext.spring.SpringHost;

import java.util.HashMap;
import java.util.Map;

public class AMEESpringHost extends SpringHost implements ISite {

    private String environmentName = "";
    private boolean secureAvailable = false;
    private boolean checkRemoteAddress = false;
    private String authCookieDomain = "";
    private Long maxAuthDuration = -1L;
    private Long maxAuthIdle = -1L;
    private Map<Application, String> skinPaths = new HashMap<Application, String>();

    public AMEESpringHost(Component component) {
        super(component);
    }

    public AMEESpringHost(Context context) {
        super(context);
    }

    public String getActiveSkinPath() {
        return getSkinPaths().get(Application.getCurrent());
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        if (environmentName == null) {
            environmentName = "";
        }
        this.environmentName = environmentName;
    }

    public boolean isSecureAvailable() {
        return secureAvailable;
    }

    public void setSecureAvailable(boolean secureAvailable) {
        this.secureAvailable = secureAvailable;
    }

    public void setSecureAvailable(String secureAvailable) {
        setSecureAvailable(Boolean.parseBoolean(secureAvailable));
    }

    public boolean isCheckRemoteAddress() {
        return checkRemoteAddress;
    }

    public void setCheckRemoteAddress(boolean checkRemoteAddress) {
        this.checkRemoteAddress = checkRemoteAddress;
    }

    public void setCheckRemoteAddress(String checkRemoteAddress) {
        setCheckRemoteAddress(Boolean.parseBoolean(checkRemoteAddress));
    }

    public String getAuthCookieDomain() {
        return authCookieDomain;
    }

    public void setAuthCookieDomain(String authCookieDomain) {
        if (authCookieDomain == null) {
            authCookieDomain = "";
        }
        this.authCookieDomain = authCookieDomain;
    }

    public Long getMaxAuthDuration() {
        return maxAuthDuration;
    }

    public void setMaxAuthDuration(Long maxAuthDuration) {
        if ((maxAuthDuration == null) || (maxAuthDuration < 0)) {
            maxAuthDuration = -1L;
        }
        this.maxAuthDuration = maxAuthDuration;
    }

    public void setMaxAuthDuration(String maxAuthDuration) {
        try {
            setMaxAuthDuration(Long.parseLong(maxAuthDuration));
        } catch (NumberFormatException e) {
            // swallow
        }
    }

    public Long getMaxAuthIdle() {
        return maxAuthIdle;
    }

    public void setMaxAuthIdle(Long maxAuthIdle) {
        if ((maxAuthIdle == null) || (maxAuthIdle < 0)) {
            maxAuthIdle = -1L;
        }
        this.maxAuthIdle = maxAuthIdle;
    }

    public void setMaxAuthIdle(String maxAuthIdle) {
        try {
            setMaxAuthIdle(Long.parseLong(maxAuthIdle));
        } catch (NumberFormatException e) {
            // swallow
        }
    }

    public Map<Application, String> getSkinPaths() {
        return skinPaths;
    }

    public void setSkinPaths(Map<Application, String> skinPaths) {
        this.skinPaths = skinPaths;
    }
}