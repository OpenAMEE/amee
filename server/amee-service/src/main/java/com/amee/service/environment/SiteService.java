package com.amee.service.environment;

import com.amee.domain.AMEEStatus;
import com.amee.domain.Pager;
import com.amee.domain.auth.User;
import com.amee.domain.environment.Environment;
import com.amee.service.profile.ProfileService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

@Service
public class SiteService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private static final String CACHE_REGION = "query.siteService";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProfileService profileService;

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
        user.setStatus(AMEEStatus.TRASH);
    }
}