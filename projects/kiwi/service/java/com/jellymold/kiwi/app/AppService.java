package com.jellymold.kiwi.app;

import com.jellymold.kiwi.Action;
import com.jellymold.kiwi.App;
import com.jellymold.utils.Pager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;

@Service
public class AppService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    public AppService() {
        super();
    }

    // Apps

    public App getAppByUid(String uid) {
        App app = null;
        if (uid != null) {
            List<App> apps = entityManager.createQuery(
                    "SELECT a FROM App a " +
                            "WHERE a.uid = :uid")
                    .setParameter("uid", uid)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.appService")
                    .getResultList();
            if (apps.size() > 0) {
                app = apps.get(0);
            }
        }
        return app;
    }

    public App getAppByName(String name) {
        App app = null;
        if (name != null) {
            List<App> apps = entityManager.createQuery(
                    "SELECT a FROM App a " +
                            "WHERE a.name = :name")
                    .setParameter("name", name.trim())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.appService")
                    .getResultList();
            if (apps.size() > 0) {
                app = apps.get(0);
            }
        }
        return app;
    }

    public List<App> getApps(Pager pager) {
        // first count all apps
        long count = (Long) entityManager.createQuery(
                "SELECT count(a) " +
                        "FROM App a")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.appService")
                .getSingleResult();
        // tell pager how many apps there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the apps for the current page
        List<App> apps = entityManager.createQuery(
                "SELECT a " +
                        "FROM App a " +
                        "ORDER BY a.name")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.appService")
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(apps.size());
        // all done, return results
        return apps;
    }

    public List<App> getApps() {
        List<App> apps = entityManager.createQuery(
                "FROM App a " +
                        "ORDER BY a.name")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.appService")
                .getResultList();
        return apps;
    }

    public void save(App app) {
        entityManager.persist(app);
    }

    public void remove(App app) {
        entityManager.remove(app);
    }

    // Actions

    public Action getActionByUid(App app, String uid) {
        Action action = null;
        List<Action> actions = entityManager.createQuery(
                "SELECT a FROM Action a " +
                        "WHERE a.app.id = :appId " +
                        "AND a.uid = :uid")
                .setParameter("appId", app.getId())
                .setParameter("uid", uid)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.appService")
                .getResultList();
        if (actions.size() > 0) {
            action = actions.get(0);
        }
        return action;
    }

    public Action getActionByUid(String uid) {
        Action action = null;
        if (uid != null) {
            List<Action> actions = entityManager.createQuery(
                    "SELECT a FROM Action a " +
                            "WHERE a.uid = :uid")
                    .setParameter("uid", uid)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.appService")
                    .getResultList();
            if (actions.size() > 0) {
                action = actions.get(0);
            }
        }
        return action;
    }

    public Action getActionByKey(String key) {
        Action action = null;
        if (key != null) {
            List<Action> actions = entityManager.createQuery(
                    "SELECT a FROM Action a " +
                            "WHERE a.key = :key")
                    .setParameter("key", key)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.appService")
                    .getResultList();
            if (actions.size() > 0) {
                action = actions.get(0);
            }
        }
        return action;
    }

    public List<Action> getActions(App app) {
        return getActions(app, null);
    }

    public List<Action> getActions(App app, Pager pager) {
        if (pager != null) {
            // count all objects
            long count = (Long) entityManager.createQuery(
                    "SELECT count(a) " +
                            "FROM Action a " +
                            "WHERE a.app.id = :appId")
                    .setParameter("appId", app.getId())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.appService")
                    .getSingleResult();
            // tell pager how many objects there are and give it a chance to select the requested page again
            pager.setItems(count);
            pager.goRequestedPage();
        }
        // now get the objects
        Query query = entityManager.createQuery(
                "SELECT a " +
                        "FROM Action a " +
                        "WHERE a.app.id = :appId " +
                        "ORDER BY a.key")
                .setParameter("appId", app.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.appService");
        if (pager != null) {
            query.setMaxResults(pager.getItemsPerPage());
            query.setFirstResult((int) pager.getStart());
        }
        List<Action> actions = query.getResultList();
        if (pager != null) {
            // update the pager
            pager.setItemsFound(actions.size());
        }
        // all done, return results
        return actions;
    }

    public List<Action> getActions(Pager pager) {
        // first count all objects
        long count = (Long) entityManager.createQuery(
                "SELECT count(a) " +
                        "FROM Action a")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.appService")
                .getSingleResult();
        // tell pager how many objects there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the objects for the current page
        List<Action> actions = entityManager.createQuery(
                "SELECT a " +
                        "FROM Action a " +
                        "ORDER BY a.app.name, a.name")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.appService")
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(actions.size());
        // all done, return results
        return actions;
    }

    public void save(Action action) {
        entityManager.persist(action);
    }

    public void remove(Action action) {
        entityManager.remove(action);
    }
}
