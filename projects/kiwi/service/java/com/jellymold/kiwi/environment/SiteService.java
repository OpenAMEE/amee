package com.jellymold.kiwi.environment;

import com.jellymold.kiwi.*;
import com.jellymold.utils.Pager;
import com.jellymold.utils.PagerSetType;
import com.jellymold.utils.ThreadBeanHolder;
import com.jellymold.utils.event.ObserveEventService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;

// TODO: SPRINGIFY - What scope?

@Service
public class SiteService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired(required = true)
    private ObserveEventService observeEventService;

    public SiteService() {
        super();
    }

    // Sites

    public Site getSiteByUid(Environment environment, String uid) {
        Site site = null;
        List<Site> sites = entityManager.createQuery(
                "SELECT s FROM Site s " +
                        "WHERE s.uid = :uid " +
                        "AND s.environment.id = :environmentId")
                .setParameter("uid", uid)
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        if (sites.size() == 1) {
            site = sites.get(0);
        }
        return site;
    }

    public Site getSiteByName(String name) {
        Site site = null;
        List<Site> sites = entityManager.createQuery(
                "SELECT s FROM Site s " +
                        "WHERE s.name = :name")
                .setParameter("name", name.trim())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        if (sites.size() == 1) {
            site = sites.get(0);
        }
        return site;
    }

    public List<Site> getSites(Environment environment, Pager pager) {
        String orderBy = "serverName";
        if (pager != null) {
            // count all sites
            long count = (Long) entityManager.createQuery(
                    "SELECT count(s) " +
                            "FROM Site s " +
                            "WHERE s.environment.id = :environmentId")
                    .setParameter("environmentId", environment.getId())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.siteService")
                    .getSingleResult();
            // tell pager how many sites there are and give it a chance to select the requested page again
            pager.setItems(count);
            pager.goRequestedPage();
        }
        // now get the sites for the current page
        Query query = entityManager.createQuery(
                "SELECT s " +
                        "FROM Site s " +
                        "WHERE s.environment.id = :environmentId " +
                        "ORDER BY " + orderBy)
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService");
        if (pager != null) {
            // pagination
            query.setMaxResults(pager.getItemsPerPage());
            query.setFirstResult((int) pager.getStart());
        }
        List<Site> sites = query.getResultList();
        if (pager != null) {
            // update the pager
            pager.setItemsFound(sites.size());
        }
        return sites;
    }

    public List<Site> getSites() {
        String orderBy = "serverName";
        List<Site> sites = entityManager.createQuery(
                "SELECT s " +
                        "FROM Site s " +
                        "ORDER BY " + orderBy)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        return sites;
    }

    public void save(Site site) {
        entityManager.persist(site);
    }

    public void remove(Site site) {
        observeEventService.raiseEvent("beforeSiteDelete", site);
        entityManager.remove(site);
    }

    public static Site getSite() {
        return (Site) ThreadBeanHolder.get("site");
    }

    // SiteApps

    public SiteApp getSiteApp(Site site, String appName) {
        SiteApp siteApp = null;
        List<SiteApp> siteApps = entityManager.createQuery(
                "SELECT sa FROM SiteApp sa, App a " +
                        "WHERE sa.site.id = :siteId " +
                        "AND sa.app.id = a.id " +
                        "AND a.name = :appName")
                .setParameter("siteId", site.getId())
                .setParameter("appName", appName)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        if (siteApps.size() == 1) {
            siteApp = siteApps.get(0);
        }
        return siteApp;
    }

    public SiteApp getSiteAppByUid(Site site, String siteAppUid) {
        SiteApp siteApp = null;
        List<SiteApp> siteApps = entityManager.createQuery(
                "SELECT sa FROM SiteApp sa " +
                        "WHERE sa.site.id = :siteId " +
                        "AND sa.uid = :siteAppUid")
                .setParameter("siteId", site.getId())
                .setParameter("siteAppUid", siteAppUid)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        if (siteApps.size() > 0) {
            siteApp = siteApps.get(0);
        }
        return siteApp;
    }

    public SiteApp getSiteAppByUid(String siteAppUid) {
        SiteApp siteApp = null;
        List<SiteApp> siteApps = entityManager.createQuery(
                "FROM SiteApp sa " +
                        "LEFT JOIN FETCH sa.app a " +
                        "WHERE sa.uid = :siteAppUid")
                .setParameter("siteAppUid", siteAppUid)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        if (siteApps.size() > 0) {
            log.debug("Found SiteApp");
            siteApp = siteApps.get(0);
        } else {
            log.warn("SiteApp NOT found");
        }
        return siteApp;
    }

    public List<SiteApp> getSiteApps(Site site, Pager pager) {
        if (pager != null) {
            // first count all objects
            long count = (Long) entityManager.createQuery(
                    "SELECT count(sa) " +
                            "FROM SiteApp sa " +
                            "WHERE sa.site.id = :siteId")
                    .setParameter("siteId", site.getId())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.siteService")
                    .getSingleResult();
            // tell pager how many objects there are and give it a chance to select the requested page again
            pager.setItems(count);
            pager.goRequestedPage();
        }
        // now get the objects for the current page
        Query query = entityManager.createQuery(
                "SELECT sa " +
                        "FROM SiteApp sa " +
                        "WHERE sa.site.id = :siteId " +
                        "ORDER BY sa.uriPattern")
                .setParameter("siteId", site.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService");
        if (pager != null) {
            query.setMaxResults(pager.getItemsPerPage());
            query.setFirstResult((int) pager.getStart());
        }
        List<SiteApp> siteApps = query.getResultList();
        if (pager != null) {
            // update the pager
            pager.setItemsFound(siteApps.size());
        }
        // all done, return results
        return siteApps;
    }

    public void remove(SiteApp siteApp) {
        entityManager.remove(siteApp);
    }

    // Groups

    public Group getGroupByUid(Environment environment, String uid) {
        Group group = null;
        List<Group> groups = entityManager.createQuery(
                "SELECT g FROM Group g " +
                        "WHERE g.environment.id = :environmentId " +
                        "AND g.uid = :uid")
                .setParameter("environmentId", environment.getId())
                .setParameter("uid", uid)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        if (groups.size() > 0) {
            group = groups.get(0);
        }
        return group;
    }

    public Group getGroupByName(Environment environment, String name) {
        Group group = null;
        List<Group> groups = entityManager.createQuery(
                "SELECT g FROM Group g " +
                        "WHERE g.environment.id = :environmentId " +
                        "AND g.name = :name")
                .setParameter("environmentId", environment.getId())
                .setParameter("name", name.trim())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        if (groups.size() > 0) {
            group = groups.get(0);
        }
        return group;
    }

    private String getPagerSetClause(String alias, Pager pager) {

        StringBuilder ret = new StringBuilder();
        if (pager.isPagerSetApplicable()) {
            if (pager.getPagerSetType().equals(PagerSetType.INCLUDE)) {
                ret.append(" AND ");
                ret.append(alias);
                ret.append(" IN (:pagerSet) ");
            } else {
                ret.append(" AND ");
                ret.append(alias);
                ret.append(" NOT IN (:pagerSet) ");
            }
        }
        return ret.toString();
    }

    public List<Group> getGroups(Environment environment) {
        List<Group> groups = entityManager.createQuery(
                "SELECT g " +
                        "FROM Group g " +
                        "WHERE g.environment.id = :environmentId " +
                        "ORDER BY g.name")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        return groups;
    }

    public List<Group> getGroups(Environment environment, Pager pager) {

        Query query;

        String pagerSetClause = getPagerSetClause("g", pager);

        // first count all objects
        query = entityManager.createQuery(
                "SELECT count(g) " +
                        "FROM Group g " +
                        "WHERE g.environment.id = :environmentId " + pagerSetClause)
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService");
        if (!"".equals(pagerSetClause)) {
            query.setParameter("pagerSet", pager.getPagerSet());
        }
        // tell pager how many objects there are and give it a chance to select the requested page again
        long count = (Long) query.getSingleResult();
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the objects for the current page
        query = entityManager.createQuery(
                "SELECT g " +
                        "FROM Group g " +
                        "WHERE g.environment.id = :environmentId " + pagerSetClause +
                        "ORDER BY g.name")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart());
        if (!"".equals(pagerSetClause)) {
            query.setParameter("pagerSet", pager.getPagerSet());
        }
        List<Group> groups = query.getResultList();
        // update the pager
        pager.setItemsFound(groups.size());
        // all done, return results
        return groups;
    }

    public void save(Group group) {
        entityManager.persist(group);
    }

    public void remove(Group group) {
        observeEventService.raiseEvent("beforeGroupDelete", group);
        entityManager.remove(group);
    }

    // GroupUsers

    public GroupUser getGroupUserByUid(Environment environment, String uid) {
        GroupUser groupUser = null;
        if ((environment != null) && (uid != null)) {
            List<GroupUser> groupUsers = entityManager.createQuery(
                    "SELECT gu FROM GroupUser gu " +
                            "WHERE gu.environment.id = :environmentId " +
                            "AND gu.uid = :uid")
                    .setParameter("environmentId", environment.getId())
                    .setParameter("uid", uid)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.siteService")
                    .getResultList();
            if (groupUsers.size() > 0) {
                groupUser = groupUsers.get(0);
            }
        }
        return groupUser;
    }

    public GroupUser getGroupUser(Group group, User user) {
        GroupUser groupUser = null;
        if ((group != null) && (user != null)) {
            List<GroupUser> groupUsers = entityManager.createQuery(
                    "SELECT gu FROM GroupUser gu " +
                            "WHERE gu.environment.id = :environmentId " +
                            "AND gu.group.id = :groupId " +
                            "AND gu.user.id = :userId")
                    .setParameter("environmentId", group.getEnvironment().getId())
                    .setParameter("groupId", group.getId())
                    .setParameter("userId", user.getId())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.siteService")
                    .getResultList();
            if (groupUsers.size() > 0) {
                groupUser = groupUsers.get(0);
            }
        }
        return groupUser;
    }

    public List<GroupUser> getGroupUsers(Group group, Pager pager) {
        // first count all objects
        long count = (Long) entityManager.createQuery(
                "SELECT count(gu) " +
                        "FROM GroupUser gu " +
                        "WHERE gu.group.id = :groupId")
                .setParameter("groupId", group.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getSingleResult();
        // tell pager how many objects there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the objects for the current page
        List<GroupUser> groupUsers = entityManager.createQuery(
                "SELECT gu " +
                        "FROM GroupUser gu " +
                        "LEFT JOIN FETCH gu.user u " +
                        "WHERE gu.group.id = :groupId " +
                        "ORDER BY u.username")
                .setParameter("groupId", group.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(groupUsers.size());
        // all done, return results
        return groupUsers;
    }

    public List<GroupUser> getGroupUsers(User user, Pager pager) {
        // first count all objects
        long count = (Long) entityManager.createQuery(
                "SELECT count(gu) " +
                        "FROM GroupUser gu " +
                        "WHERE gu.user.id = :userId")
                .setParameter("userId", user.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getSingleResult();
        // tell pager how many objects there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the objects for the current page
        List<GroupUser> groupUsers = entityManager.createQuery(
                "SELECT gu " +
                        "FROM GroupUser gu " +
                        "LEFT JOIN FETCH gu.group g " +
                        "WHERE gu.user.id = :userId " +
                        "ORDER BY g.name")
                .setParameter("userId", user.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(groupUsers.size());
        // all done, return results
        return groupUsers;
    }

    public List<GroupUser> getGroupUsers(User user) {
        List<GroupUser> groupUsers = entityManager.createQuery(
                "SELECT gu " +
                        "FROM GroupUser gu " +
                        "LEFT JOIN FETCH gu.group g " +
                        "WHERE gu.user.id = :userId " +
                        "ORDER BY g.name")
                .setParameter("userId", user.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        // all done, return results
        return groupUsers;
    }

    public List<GroupUser> getGroupUsers(Environment environment) {
        List<GroupUser> groupUsers = entityManager.createQuery(
                "SELECT gu " +
                        "FROM GroupUser gu " +
                        "WHERE gu.environment.id = :environmentId")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        return groupUsers;
    }

    public void save(GroupUser groupUser) {
        entityManager.persist(groupUser);
    }

    public void remove(GroupUser groupUser) {
        entityManager.remove(groupUser);
    }

    // Roles

    public Role getRoleByUid(Environment environment, String uid) {
        Role role = null;
        List<Role> roles = entityManager.createQuery(
                "SELECT r FROM Role r " +
                        "WHERE r.environment.id = :environmentId " +
                        "AND r.uid = :uid")
                .setParameter("environmentId", environment.getId())
                .setParameter("uid", uid)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        if (roles.size() > 0) {
            role = roles.get(0);
        }
        return role;
    }

    public Role getRoleByName(Environment environment, String name) {
        Role role = null;
        List<Role> roles = entityManager.createQuery(
                "SELECT r FROM Role r " +
                        "WHERE r.environment.id = :environmentId " +
                        "AND r.name = :name")
                .setParameter("environmentId", environment.getId())
                .setParameter("name", name.trim())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        if (roles.size() > 0) {
            role = roles.get(0);
        }
        return role;
    }

    public List<Role> getRoles(Environment environment, Pager pager) {
        Query query;
        String pagerSetClause = getPagerSetClause("r", pager);

        // first count all objects
        query = entityManager.createQuery(
                "SELECT count(r) " +
                        "FROM Role r " +
                        "WHERE r.environment.id = :environmentId " + pagerSetClause)
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService");
        if (!"".equals(pagerSetClause)) {
            query.setParameter("pagerSet", pager.getPagerSet());
        }
        // tell pager how many objects there are and give it a chance to select the requested page again
        long count = (Long) query.getSingleResult();
        pager.setItems(count);
        pager.goRequestedPage();

        query = entityManager.createQuery(
                "SELECT r " +
                        "FROM Role r " +
                        "WHERE r.environment.id = :environmentId " + pagerSetClause +
                        "ORDER BY r.name")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart());
        if (!"".equals(pagerSetClause)) {
            query.setParameter("pagerSet", pager.getPagerSet());
        }
        List<Role> roles = query.getResultList();
        // update the pager
        pager.setItemsFound(roles.size());
        // all done, return results
        return roles;
    }

    public List<Role> getRoles(Environment environment) {
        if (environment != null) {
            List<Role> roles = entityManager.createQuery(
                    "SELECT r " +
                            "FROM Role r " +
                            "WHERE r.environment.id = :environmentId " +
                            "ORDER BY r.name")
                    .setParameter("environmentId", environment.getId())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.siteService")
                    .getResultList();
            return roles;
        } else {
            return null;
        }
    }

    public void save(Role role) {
        entityManager.persist(role);
    }

    public void remove(Role role) {
        entityManager.remove(role);
    }

    // Users

    public User getUserByUid(Environment environment, String uid) {
        User user = null;
        if ((environment != null) && (uid != null)) {
            List<User> users = entityManager.createQuery(
                    "SELECT u FROM User u " +
                            "WHERE u.environment.id = :environmentId " +
                            "AND u.uid = :userUid")
                    .setParameter("environmentId", environment.getId())
                    .setParameter("userUid", uid)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.siteService")
                    .getResultList();
            if (users.size() > 0) {
                user = users.get(0);
            }
        }
        return user;
    }

    public User getUserByUsername(Environment environment, String username) {
        User user = null;
        List<User> users = entityManager.createQuery(
                "SELECT u FROM User u " +
                        "WHERE u.environment.id = :environmentId " +
                        "AND u.username = :username")
                .setParameter("environmentId", environment.getId())
                .setParameter("username", username.trim())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        if (users.size() > 0) {
            user = users.get(0);
        }
        return user;
    }

    public List<User> getUsers(Environment environment, Pager pager) {
        // first count all objects
        long count = (Long) entityManager.createQuery(
                "SELECT count(u) " +
                        "FROM User u " +
                        "WHERE u.environment.id = :environmentId ")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getSingleResult();
        // tell pager how many objects there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the objects for the current page
        List<User> users = entityManager.createQuery(
                "SELECT u " +
                        "FROM User u " +
                        "WHERE u.environment.id = :environmentId " +
                        "ORDER BY u.username")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(users.size());
        // all done, return results
        return users;
    }

    public List<User> getUsers(Environment environment) {
        if (environment != null) {
            List<User> users = entityManager.createQuery(
                    "SELECT u " +
                            "FROM User u " +
                            "WHERE u.environment.id = :environmentId " +
                            "ORDER BY u.username")
                    .setParameter("environmentId", environment.getId())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.siteService")
                    .getResultList();
            return users;
        } else {
            return null;
        }
    }

    public void save(User user) {
        entityManager.persist(user);
    }

    public void remove(User user) {
        observeEventService.raiseEvent("beforeUserDelete", user);
        //also delete group users
        entityManager.createQuery(
                "DELETE FROM GroupUser gu " +
                        "WHERE gu.user.id = :userId")
                .setParameter("userId", user.getId())
                .executeUpdate();
        entityManager.remove(user);
    }

    // Apps

    public List<App> getApps() {
        List<App> apps = entityManager.createQuery(
                "FROM App a " +
                        "ORDER BY a.name")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getResultList();
        return apps;
    }

    // SiteApps
}