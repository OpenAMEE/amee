package com.amee.admin.restlet.environment;

import com.amee.domain.APIVersion;
import com.amee.domain.auth.*;
import com.amee.domain.environment.Environment;
import com.amee.domain.site.Site;
import com.amee.domain.site.SiteApp;
import com.amee.service.BaseBrowser;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.environment.SiteService;
import com.amee.service.environment.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class EnvironmentBrowser extends BaseBrowser {

    @Autowired
    private SiteService siteService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private EnvironmentService environmentService;

    // Environments

    private String environmentUid = null;
    private Environment environment = null;

    // Sites

    private String siteUid = null;
    private Site site = null;

    // SiteApp

    private String siteAppUid = null;
    private SiteApp siteApp = null;

    // Groups

    private String groupUid = null;
    private Group group = null;

    // Users

    private String userUid = null;
    private User user = null;

    // GroupPrinciples

    private GroupPrinciple groupPrinciple = null;

    // Environments

    public String getEnvironmentUid() {
        return environmentUid;
    }

    public void setEnvironmentUid(String environmentUid) {
        this.environmentUid = environmentUid;
    }

    public Environment getEnvironment() {
        if ((environment == null) && (environmentUid != null)) {
            environment = environmentService.getEnvironmentByUid(environmentUid);
        }
        return environment;
    }

    // Sites

    public String getSiteUid() {
        return siteUid;
    }

    public void setSiteUid(String siteUid) {
        this.siteUid = siteUid;
    }

    public Site getSite() {
        if ((site == null) && (getEnvironment() != null) && (siteUid != null)) {
            site = siteService.getSiteByUid(environment, siteUid);
        }
        return site;
    }

    // SiteApps

    public String getSiteAppUid() {
        return siteAppUid;
    }

    public void setSiteAppUid(String siteAppUid) {
        this.siteAppUid = siteAppUid;
    }

    public SiteApp getSiteApp() {
        if ((siteApp == null) && (getSite() != null) && (siteAppUid != null)) {
            siteApp = siteService.getSiteAppByUid(site, siteAppUid);
        }
        return siteApp;
    }

    // Groups

    public String getGroupUid() {
        return groupUid;
    }

    public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }

    public Group getGroup() {
        if ((group == null) && (getEnvironment() != null) && (groupUid != null)) {
            group = groupService.getGroupByUid(environment, groupUid);
        }
        return group;
    }

    // Users

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public User getUser() {
        if ((user == null) && (getEnvironment() != null) && (userUid != null)) {
            user = siteService.getUserByUid(environment, userUid);
        }
        return user;
    }

    // GroupPrinciples

    public GroupPrinciple getGroupPrinciple() {
        if ((groupPrinciple == null) && (getUser() != null) && (getGroup() != null)) {
            groupPrinciple = groupService.getGroupPrinciple(group, user);
        }
        return groupPrinciple;
    }

    // APIVersion

    public List<APIVersion> getApiVersions() {
        return environmentService.getAPIVersions();
    }

    public APIVersion getApiVersion(String version) {
        return environmentService.getAPIVersion(version);
    }

}
