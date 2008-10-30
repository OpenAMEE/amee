package com.jellymold.kiwi.legacy;

import com.jellymold.kiwi.Group;
import com.jellymold.kiwi.site.SiteService;
import com.jellymold.kiwi.User;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.RequestParameter;
import org.jboss.seam.annotations.Scope;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

// TODO: need to sanitize the params

@Stateful
@Name("browseAccountService")
@Scope(ScopeType.EVENT)
public class BrowseAccountServiceBean implements Serializable, BrowseAccountService {

    private final static Logger log = Logger.getLogger(BrowseAccountServiceBean.class);

    @In(create = true)
    private transient EntityManager entityManager;

    @In(create = true)
    private transient SiteService siteService;

    @In(create = true)
    private transient Auth authBean;

    @Out(scope = ScopeType.EVENT, required = false)
    private List<User> allUsers;

    @In(required = false)
    @Out(scope = ScopeType.EVENT, required = false)
    private User browseUser;

    @Out(scope = ScopeType.EVENT, required = false)
    private List<Group> browseUserAccounts;

    @Out(scope = ScopeType.EVENT, required = false)
    private List<Group> allAccounts;

    @In(required = false)
    @Out(scope = ScopeType.EVENT, required = false)
    private Group browseAccount;

    @Out(scope = ScopeType.EVENT, required = false)
    private List<User> browseAccountUsers;

    @RequestParameter
    private String username;

    @RequestParameter
    private String name;

    @Factory("allUsers")
    public void allUsersFactory() {
        log.debug("allUsersFactory()");
        allUsers = entityManager.createQuery("from User where site = :site")
                .setParameter("site", siteService.getSite())
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
    }

    @Factory("browseUser")
    public void browseUserFactory() {
        log.debug("browseUserFactory()");
        if ((username != null) && (browseUser == null)) {
            log.debug("browseUserFactory() " + username);
            List<User> users = entityManager.createQuery("from User where lower(username) = :username and site = :site")
                    .setParameter("username", username.toLowerCase())
                    .setParameter("site", siteService.getSite())
                    .setHint("org.hibernate.cacheable", true)
                    .getResultList();
            if (users.size() == 1) {
                log.debug("browseUserFactory() found user");
                browseUser = users.get(0);
            }
        }
    }

    @Factory("browseUserAccounts")
    public void browseUserAccountsFactory() {
        log.debug("browseUserAccountsFactory()");
        browseUserFactory();
        if (browseUser != null) {
            log.debug("browseUserAccountsFactory()");
            browseUserAccounts = entityManager.createQuery(
                    "SELECT DISTINCT a FROM Account a JOIN a.accountUsers au " +
                            "WHERE au.user = :user AND a.site = :site")
                    .setParameter("user", browseUser)
                    .setParameter("site", siteService.getSite())
                    .setHint("org.hibernate.cacheable", true)
                    .getResultList();
        }
    }

    @Factory("allAccounts")
    public void allAccountsFactory() {
        log.debug("allAccountsFactory()");
        allAccounts = entityManager.createQuery("from Account where site = :site")
                .setParameter("site", siteService.getSite())
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
    }

    @Factory("browseAccount")
    public void browseAccountFactory() {
        log.debug("browseAccountFactory()");
        if ((name != null) && (browseAccount == null)) {
            log.debug("browseAccountFactory() " + name);
            List<Group> accounts = entityManager.createQuery("from Account where lower(name) = :name and site = :site")
                    .setParameter("name", name.toLowerCase())
                    .setParameter("site", siteService.getSite())
                    .setHint("org.hibernate.cacheable", true)
                    .getResultList();
            if (accounts.size() == 1) {
                log.debug("browseAccountFactory() found account");
                browseAccount = accounts.get(0);
                authBean.loadGroupMember(browseAccount);
            }
        }
    }

    @Factory("browseAccountUsers")
    public void browseAccountUsersFactory() {
        log.debug("browseAccountUsersFactory()");
        browseAccountFactory();
        if (browseAccount != null) {
            log.debug("browseAccountUsersFactory()");
            browseAccountUsers = entityManager.createQuery(
                    "SELECT DISTINCT u FROM User u JOIN u.accountUsers au " +
                            "WHERE au.account = :account AND u.site = :site")
                    .setParameter("account", browseAccount)
                    .setParameter("site", siteService.getSite())
                    .setHint("org.hibernate.cacheable", true)
                    .getResultList();
        }
    }

    @Destroy
    @Remove
    public void destroy() {
        log.debug("destroy()");
    }
}