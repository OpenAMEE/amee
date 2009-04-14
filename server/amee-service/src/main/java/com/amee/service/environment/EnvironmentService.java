package com.amee.service.environment;

import com.amee.domain.APIVersion;
import com.amee.domain.Pager;
import com.amee.domain.environment.Environment;
import com.amee.domain.event.ObserveEventService;
import com.amee.domain.event.ObservedEvent;
import com.amee.service.ThreadBeanHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

@Service
public class EnvironmentService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired(required = true)
    private ObserveEventService observeEventService;

    public EnvironmentService() {
        super();
    }

    @ServiceActivator(inputChannel = "beforeSiteDelete")
    public void beforeSiteDelete(ObservedEvent oe) {
        log.debug("beforeSiteDelete" + oe.getPayload());
    }

    // Environments

    public Environment getEnvironmentByUid(String uid) {
        Environment environment = null;
        if (uid != null) {
            List<Environment> environments = entityManager.createQuery(
                    "FROM Environment e " +
                            "WHERE e.uid = :uid")
                    .setParameter("uid", uid)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.environmentService")
                    .getResultList();
            if (environments.size() > 0) {
                log.debug("found Environment");
                environment = environments.get(0);
            } else {
                log.debug("Environment NOT found");
            }
        }
        return environment;
    }

    public Environment getEnvironmentByName(String name) {
        Environment environment = null;
        if (name != null) {
            List<Environment> environments = entityManager.createQuery(
                    "FROM Environment e " +
                            "WHERE e.name = :name")
                    .setParameter("name", name.trim())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.environmentService")
                    .getResultList();
            if (environments.size() > 0) {
                log.debug("found Environment");
                environment = environments.get(0);
            } else {
                log.debug("Environment NOT found");
            }
        }
        return environment;
    }

    public List<Environment> getEnvironments() {
        log.debug("getEnvironments()");
        List<Environment> environments = entityManager.createQuery(
                "FROM Environment e ")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getResultList();
        return environments;
    }

    public List<Environment> getEnvironments(Pager pager) {
        // first count all environments
        long count = (Long) entityManager.createQuery(
                "SELECT count(e) " +
                        "FROM Environment e")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getSingleResult();
        // tell pager how many environments there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the environments for the current page
        List<Environment> environments = entityManager.createQuery(
                "SELECT e " +
                        "FROM Environment e " +
                        "ORDER BY e.name")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(environments.size());
        // all done, return results
        return environments;
    }

    public void save(Environment environment) {
        entityManager.persist(environment);
    }

    public void remove(Environment environment) {
        observeEventService.raiseEvent("beforeEnvironmentDelete", environment);
        entityManager.remove(environment);
    }

    public static Environment getEnvironment() {
        return (Environment) ThreadBeanHolder.get("environment");
    }

    // API Versions

    public List<APIVersion> getAPIVersions() {
        List<APIVersion> apiVersions = entityManager.createQuery(
                "FROM APIVersion apiv " +
                        "ORDER BY apiv.version")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getResultList();
        return apiVersions;
    }

    public APIVersion getAPIVersion(String version) {
        return (APIVersion) entityManager.createQuery(
                "FROM APIVersion apiv " +
                        "WHERE apiv.version = :version")
                .setParameter("version", version)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getSingleResult();
    }
}