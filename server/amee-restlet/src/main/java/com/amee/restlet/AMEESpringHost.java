package com.amee.restlet;

import com.amee.domain.site.ISite;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.ext.spring.SpringHost;

import java.util.HashMap;
import java.util.Map;

public class AMEESpringHost extends SpringHost implements ISite {

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