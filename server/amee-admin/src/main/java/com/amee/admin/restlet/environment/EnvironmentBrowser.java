package com.amee.admin.restlet.environment;

import com.amee.domain.APIVersion;
import com.amee.domain.auth.*;
import com.amee.domain.environment.Environment;
import com.amee.domain.site.Site;
import com.amee.domain.site.SiteApp;
import com.amee.service.BaseBrowser;
import com.amee.service.auth.AuthService;
import com.amee.service.auth.ResourceActions;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.environment.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class EnvironmentBrowser extends BaseBrowser {

    @Autowired
    private SiteService siteService;

    @Autowired
    private AuthService authService;

    @Autowired
    private EnvironmentService environmentService;

    // ResourceActions

    @Autowired
    @Qualifier("environmentActions")
    private ResourceActions environmentActions;

    @Autowired
    @Qualifier("siteActions")
    private ResourceActions siteActions;

    @Autowired
    @Qualifier("siteAppActions")
    private ResourceActions siteAppActions;

    @Autowired
    @Qualifier("groupActions")
    private ResourceActions groupActions;

    @Autowired
    @Qualifier("roleActions")
    private ResourceActions roleActions;

    @Autowired
    @Qualifier("userActions")
    private ResourceActions userActions;

    @Autowired
    @Qualifier("appActions")
    private ResourceActions appActions;

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

    // Roles

    private String roleUid = null;
    private Role role = null;

    // Actions

    private String actionUid = null;
    private Action action = null;

    // Users

    private String userIdOrName = null;
    private User user = null;
    private Boolean allowUserUpload = null;

    // GroupUsers

    private GroupUser groupUser = null;

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
            group = siteService.getGroupByUid(environment, groupUid);
        }
        return group;
    }

    // Roles

    public String getRoleUid() {
        return roleUid;
    }

    public void setRoleUid(String roleUid) {
        this.roleUid = roleUid;
    }

    public Role getRole() {
        if ((role == null) && (getEnvironment() != null) && (roleUid != null)) {
            role = siteService.getRoleByUid(environment, roleUid);
        }
        return role;
    }

    // Actions

    public String getActionUid() {
        return actionUid;
    }

    public void setActionUid(String actionUid) {
        this.actionUid = actionUid;
    }

    // TODO: this will be bad if many Actions are attached to Roles
    public Action getAction() {
        if ((action == null) && (getRole() != null) && (actionUid != null)) {
            for (Action a : getRole().getActions()) {
                if (a.getUid().equals(actionUid)) {
                    action = a;
                    break;
                }
            }
        }
        return action;
    }

    // Users

    public void setUserIdentifier(String identifier) {
        this.userIdOrName = identifier;
    }

    public User getUser() {
        if ((user == null) && (getEnvironment() != null) && (userIdOrName != null)) {
            user = siteService.getUserByUid(environment, userIdOrName);
            if (user == null) {
                user = siteService.getUserByUsername(environment, userIdOrName);   
            }
        }
        return user;
    }

    public boolean isAllowUserUpload() {
        if (allowUserUpload == null) {
            allowUserUpload = authService.isSuperUser();
        }
        return allowUserUpload;
    }

    // GroupUsers

    public GroupUser getGroupUser() {
        if ((groupUser == null) && (getUser() != null) && (getGroup() != null)) {
            groupUser = siteService.getGroupUser(group, user);
        }
        return groupUser;
    }

    // ResourceActions

    public ResourceActions getEnvironmentActions() {
        return environmentActions;
    }

    public ResourceActions getSiteActions() {
        return siteActions;
    }

    public ResourceActions getSiteAppActions() {
        return siteAppActions;
    }

    public ResourceActions getGroupActions() {
        return groupActions;
    }

    public ResourceActions getRoleActions() {
        return roleActions;
    }

    public ResourceActions getUserActions() {
        return userActions;
    }

    public ResourceActions getAppActions() {
        return appActions;
    }

    // APIVersion

    public List<APIVersion> getApiVersions() {
        return environmentService.getAPIVersions();
    }

    public APIVersion getApiVersion(String version) {
        return environmentService.getAPIVersion(version);
    }

}
