package com.jellymold.kiwi.legacy;

import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import java.util.List;

import com.jellymold.kiwi.site.SiteService;
import com.jellymold.kiwi.User;
import com.jellymold.kiwi.GroupMember;
import com.jellymold.kiwi.Role;
import com.jellymold.kiwi.Group;
import com.jellymold.kiwi.auth.LoggedIn;

@Stateful
@Scope(ScopeType.EVENT)
@Name("authBean")
public class AuthBean implements Auth {

    private final static Logger log = Logger.getLogger(AuthBean.class);

    @In(create = true)
    private transient EntityManager entityManager;

    @In(create = true)
    private SiteService siteService;

    @In(required = false)
    private User user;

    @In(required = false)
    @Out(required = false)
    private GroupMember groupMember;

    private Role ownerRole;

    private Role adminRole;

    private Role userRole;

    @Create
    public void create() {
        log.debug("create()");
        assert entityManager != null : "entityManager is null";
        getRoles();
    }

    // TODO: this should be a factory method that depends on 'browseAccount'
    public void loadGroupMember(Group group) {

        log.debug("loadGroupMember()");

        assert entityManager != null : "entityManager is null";

        // only continue if group is supplied
        if (group == null) {
            log.debug("loadGroupMember() group not available");
            clearGroupMember();
            return;
        }

        // only continue if user is available and logged in
        if (!isLoggedIn()) {
            log.debug("loadGroupMember() user not available");
            clearGroupMember();
            return;
        }

        // TODO: cache something here

        // get all GroupMember objects for this user
        List<GroupMember> groupMembers = entityManager.createQuery(
                "from GroupMember " +
                        "where user = :user " +
                        "and site = :site " +
                        "and group = :group")
                .setParameter("user", user)
                .setParameter("site", siteService.getSite())
                .setParameter("group", group)
                .setHint("org.hibernate.cacheable", true)
                .getResultList();

        if (groupMembers.size() == 1) {
            log.debug("loadGroupMember() GroupMember found");
            groupMember = groupMembers.get(0);
        } else {
            log.debug("loadGroupMember() GroupMember not found");
            clearGroupMember();
        }
    }

    public void clearGroupMember() {
        log.debug("clearGroupMember()");
        groupMember = null;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private void getRoles() {

        log.debug("getRoles()");

/*        Set<Role> roles = siteService.getSite().getRoles();

        if (roles != null) {
            for (Role r : roles) {
                if (r.getName().equals("owner")) {
                    ownerRole = r;
                } else if (r.getName().equals("admin")) {
                    adminRole = r;
                } else if (r.getName().equals("user")) {
                    userRole = r;
                }
            }
        } */
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean isActions(String actions) {
        log.debug("isActions() " + actions);
        return true;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean isLoggedIn() {
        return ((user != null) &&
                (Contexts.getSessionContext().get(LoggedIn.LOGIN_KEY) != null) &&
                Contexts.getSessionContext().get(LoggedIn.LOGIN_KEY).equals(true));
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean isOwner() {
        return ((groupMember != null) &&
                (groupMember.hasRole("owner")));

    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean isAdmin() {
        return ((groupMember != null) &&
                (groupMember.hasRole("admin")));

    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean isUser() {
        return ((groupMember != null) &&
                (groupMember.hasRole("user")));

    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Role getOwnerRole() {
        return ownerRole;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Role getAdminRole() {
        return adminRole;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Role getUserRole() {
        return userRole;
    }

    @Destroy
    @Remove
    public void destroy() {
        log.debug("destroy()");
    }
}