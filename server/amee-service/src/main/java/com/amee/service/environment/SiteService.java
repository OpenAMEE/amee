package com.amee.service.environment;

import com.amee.core.ThreadBeanHolder;
import com.amee.domain.AMEEStatus;
import com.amee.domain.Pager;
import com.amee.domain.auth.User;
import com.amee.domain.environment.Environment;
import com.amee.domain.profile.Profile;
import com.amee.domain.site.App;
import com.amee.domain.site.Site;
import com.amee.domain.site.SiteApp;
import com.amee.service.profile.ProfileService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;

@Service
public class SiteService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private static final String CACHE_REGION = "query.siteService";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private ProfileService profileService;

    // Events

    // TODO: Other entities to cascade dependencies from?

    @SuppressWarnings(value = "unchecked")
    public void beforeUserDelete(User user) {
        log.debug("beforeUserDelete");
        List<Profile> profiles = entityManager.createQuery(
                "SELECT p " +
                        "FROM Profile p " +
                        "WHERE p.environment.id = :environmentId " +
                        "AND p.user.id = :userId " +
                        "AND p.status != :trash")
                .setParameter("environmentId", user.getEnvironment().getId())
                .setParameter("userId", user.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .getResultList();
        for (Profile profile : profiles) {
            profileService.remove(profile);
        }
    }

    public void beforeSiteDelete(Site site) {
        log.debug("beforeSiteDelete");
        environmentService.beforeSiteDelete(site);
        // TODO: More cascade dependencies?
    }

    public void beforeSiteAppDelete(SiteApp siteApp) {
        log.debug("beforeSiteAppDelete");
        // TODO: More cascade dependencies?
    }

    // Sites

    public Site getSiteByUid(Environment environment, String uid) {
        Site site = null;
        List<Site> sites = entityManager.createQuery(
                "SELECT s FROM Site s " +
                        "WHERE s.uid = :uid " +
                        "AND s.environment.id = :environmentId " +
                        "AND s.status != :trash")
                .setParameter("uid", uid)
                .setParameter("environmentId", environment.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
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
                        "WHERE s.name = :name " +
                        "AND s.status != :trash")
                .setParameter("name", name.trim())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        if (sites.size() == 1) {
            site = sites.get(0);
        }
        return site;
    }

    public List<Site> getSites(Environment environment, Pager pager) {
        String orderBy = "name";
        if (pager != null) {
            // count all sites
            long count = (Long) entityManager.createQuery(
                    "SELECT count(s) " +
                            "FROM Site s " +
                            "WHERE s.environment.id = :environmentId " +
                            "AND s.status != :trash")
                    .setParameter("environmentId", environment.getId())
                    .setParameter("trash", AMEEStatus.TRASH)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", CACHE_REGION)
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
                        "AND s.status != :trash " +
                        "ORDER BY " + orderBy)
                .setParameter("environmentId", environment.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION);
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
        String orderBy = "name";
        List<Site> sites = entityManager.createQuery(
                "SELECT s " +
                        "FROM Site s " +
                        "WHERE s.status != :trash " +
                        "ORDER BY " + orderBy)
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        return sites;
    }

    public void save(Site site) {
        entityManager.persist(site);
    }

    public void remove(Site site) {
        beforeSiteDelete(site);
        site.setStatus(AMEEStatus.TRASH);
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
                        "AND sa.status != :trash " +
                        "AND a.name = :appName")
                .setParameter("siteId", site.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .setParameter("appName", appName)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
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
                        "AND sa.uid = :siteAppUid " +
                        "AND sa.status != :trash")
                .setParameter("siteId", site.getId())
                .setParameter("siteAppUid", siteAppUid)
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        if (siteApps.size() > 0) {
            siteApp = siteApps.get(0);
        }
        return siteApp;
    }

    public List<SiteApp> getSiteApps(Site site, Pager pager) {
        if (pager != null) {
            // first count all objects
            long count = (Long) entityManager.createQuery(
                    "SELECT count(sa) " +
                            "FROM SiteApp sa " +
                            "WHERE sa.site.id = :siteId " +
                            "AND sa.status != :trash")
                    .setParameter("siteId", site.getId())
                    .setParameter("trash", AMEEStatus.TRASH)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", CACHE_REGION)
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
                        "AND sa.status != :trash")
                .setParameter("siteId", site.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION);
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
        beforeSiteAppDelete(siteApp);
        siteApp.setStatus(AMEEStatus.TRASH);
    }

    // Users

    public User getUserByUid(Environment environment, String uid) {
        User user = null;
        if ((environment != null) && (uid != null)) {
            List<User> users = entityManager.createQuery(
                    "SELECT u FROM User u " +
                            "WHERE u.environment.id = :environmentId " +
                            "AND u.uid = :userUid " +
                            "AND u.status != :trash")
                    .setParameter("environmentId", environment.getId())
                    .setParameter("userUid", uid)
                    .setParameter("trash", AMEEStatus.TRASH)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", CACHE_REGION)
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
                        "AND u.username = :username " +
                        "AND u.status != :trash")
                .setParameter("environmentId", environment.getId())
                .setParameter("username", username.trim())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
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
                        "WHERE u.environment.id = :environmentId " +
                        "AND u.status != :trash")
                .setParameter("environmentId", environment.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getSingleResult();
        // tell pager how many objects there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the objects for the current page
        List<User> users = entityManager.createQuery(
                "SELECT u " +
                        "FROM User u " +
                        "WHERE u.environment.id = :environmentId " +
                        "AND u.status != :trash " +
                        "ORDER BY u.username")
                .setParameter("environmentId", environment.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
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
                            "AND u.status != :trash " +
                            "ORDER BY u.username")
                    .setParameter("environmentId", environment.getId())
                    .setParameter("trash", AMEEStatus.TRASH)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", CACHE_REGION)
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
        beforeUserDelete(user);
        user.setStatus(AMEEStatus.TRASH);
    }

    // Apps

    public List<App> getApps() {
        List<App> apps = entityManager.createQuery(
                "FROM App a " +
                        "ORDER BY a.name " +
                        "AND a.status != :trash ")
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        return apps;
    }
}