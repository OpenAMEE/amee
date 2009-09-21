package com.amee.admin.restlet.environment;

import com.amee.domain.APIVersion;
import com.amee.domain.auth.Group;
import com.amee.domain.auth.GroupPrincipal;
import com.amee.domain.auth.User;
import com.amee.domain.environment.Environment;
import com.amee.service.BaseBrowser;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.environment.GroupService;
import com.amee.service.environment.SiteService;
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

    // Groups

    private String groupUid = null;
    private Group group = null;

    // Users

    private String userIdOrName = null;
    private User user = null;

    // GroupPrincipals

    private GroupPrincipal groupPrincipal = null;

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

    // GroupPrincipals

    public GroupPrincipal getGroupPrincipal() {
        if ((groupPrincipal == null) && (getUser() != null) && (getGroup() != null)) {
            groupPrincipal = groupService.getGroupPrincipal(group, user);
        }
        return groupPrincipal;
    }

    // APIVersion

    public List<APIVersion> getApiVersions() {
        return environmentService.getAPIVersions();
    }

    public APIVersion getApiVersion(String version) {
        return environmentService.getAPIVersion(version);
    }

}
