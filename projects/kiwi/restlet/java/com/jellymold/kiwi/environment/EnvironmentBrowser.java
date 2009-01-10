package com.jellymold.kiwi.environment;

import com.jellymold.kiwi.*;
import com.jellymold.kiwi.app.AppConstants;
import com.jellymold.kiwi.auth.AuthService;
import com.jellymold.utils.BaseBrowser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import gc.carbon.APIVersion;

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
    
    // Environments
    private String environmentUid = null;
    private Environment environment = null;

    // Sites
    private String siteUid = null;
    private Site site = null;

    // SiteAlaises
    private String siteAliasUid = null;
    private SiteAlias siteAlias = null;

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
    private String userUid = null;
    private User user = null;
    private Boolean allowUserUpload = null;

    // GroupUsers
    private GroupUser groupUser = null;

    // ScheduledTasks
    private String scheduledTaskUid = null;
    private ScheduledTask scheduledTask = null;

    // ResourceActions
    private ResourceActions environmentActions = new ResourceActions(EnvironmentConstants.ACTION_ENVIRONMENT_PREFIX);
    private ResourceActions siteActions = new ResourceActions(EnvironmentConstants.ACTION_SITE_PREFIX);
    private ResourceActions siteAliasActions = new ResourceActions(EnvironmentConstants.ACTION_SITE_ALIAS_PREFIX);
    private ResourceActions siteAppActions = new ResourceActions(EnvironmentConstants.ACTION_SITE_APP_PREFIX);
    private ResourceActions groupActions = new ResourceActions(EnvironmentConstants.ACTION_GROUP_PREFIX);
    private ResourceActions roleActions = new ResourceActions(EnvironmentConstants.ACTION_ROLE_PREFIX);
    private ResourceActions userActions = new ResourceActions(EnvironmentConstants.ACTION_USER_PREFIX);
    private ResourceActions appActions = new ResourceActions(AppConstants.ACTION_APP_PREFIX);
    private ResourceActions scheduledTaskActions = new ResourceActions(EnvironmentConstants.ACTION_SCHEDULED_TASK_PREFIX);

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

    // SiteAliass

    public String getSiteAliasUid() {
        return siteAliasUid;
    }

    public void setSiteAliasUid(String siteAliasUid) {
        this.siteAliasUid = siteAliasUid;
    }

    public SiteAlias getSiteAlias() {
        if ((siteAlias == null) && (getSite() != null) && (siteAliasUid != null)) {
            siteAlias = siteService.getSiteAliasByUid(site, siteAliasUid);
        }
        return siteAlias;
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

    // ScheduledTasks

    public String getScheduledTaskUid() {
        return scheduledTaskUid;
    }

    public void setScheduledTaskUid(String scheduledTaskUid) {
        this.scheduledTaskUid = scheduledTaskUid;
    }

    public ScheduledTask getScheduledTask() {
        if ((scheduledTask == null) && (getEnvironment() != null) && (scheduledTaskUid != null)) {
            scheduledTask = environmentService.getScheduledTaskByUid(environment, scheduledTaskUid);
        }
        return scheduledTask;
    }

    // ResourceActions

    public ResourceActions getEnvironmentActions() {
        return environmentActions;
    }

    public ResourceActions getSiteActions() {
        return siteActions;
    }

    public ResourceActions getSiteAliasActions() {
        return siteAliasActions;
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

    public ResourceActions getScheduledTaskActions() {
        return scheduledTaskActions;
    }

    public List<APIVersion> getApiVersions() {
        return environmentService.getAPIVersions(getEnvironment());
    }

    public APIVersion getApiVersion(String version) {
        return environmentService.getAPIVersion(version, getEnvironment());
    }

    public APIVersion getApiVersion(String version, Environment environment) {
        return environmentService.getAPIVersion(version, environment);
    }

}
