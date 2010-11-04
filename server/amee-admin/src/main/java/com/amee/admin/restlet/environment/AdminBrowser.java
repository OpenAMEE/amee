package com.amee.admin.restlet.environment;

import com.amee.domain.APIVersion;
import com.amee.domain.auth.Group;
import com.amee.domain.auth.GroupPrincipal;
import com.amee.domain.auth.User;
import com.amee.service.BaseBrowser;
import com.amee.service.auth.GroupService;
import com.amee.service.data.DataService;
import com.amee.service.environment.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class AdminBrowser extends BaseBrowser {

    @Autowired
    private DataService dataService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private GroupService groupService;

    // Groups

    private String groupUid = null;
    private Group group = null;

    // Users

    private String userIdOrName = null;
    private User user = null;

    // GroupPrincipals

    private GroupPrincipal groupPrincipal = null;

    // Groups

    public String getGroupUid() {
        return groupUid;
    }

    public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }

    public Group getGroup() {
        if ((group == null) && (groupUid != null)) {
            group = groupService.getGroupByUid(groupUid);
        }
        return group;
    }

    // Users

    public void setUserIdentifier(String identifier) {
        this.userIdOrName = identifier;
    }

    public User getUser() {
        if ((user == null) && (userIdOrName != null)) {
            user = siteService.getUserByUid(userIdOrName);
            if (user == null) {
                user = siteService.getUserByUsername(userIdOrName);
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
        return dataService.getAPIVersions();
    }

    public APIVersion getApiVersion(String version) {
        return dataService.getAPIVersion(version);
    }

}
