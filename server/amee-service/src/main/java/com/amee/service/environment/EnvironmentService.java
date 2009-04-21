package com.amee.service.environment;

import com.amee.domain.APIVersion;
import com.amee.domain.Pager;
import com.amee.domain.environment.Environment;
import com.amee.domain.event.ObserveEventService;
import com.amee.domain.event.ObservedEvent;
import com.amee.service.ThreadBeanHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
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

    private static final String CACHE_REGION = "query.environmentService";

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

    @SuppressWarnings(value = "unchecked")
    public Environment getEnvironmentByUid(String uid) {
        Environment environment = null;
        if (!StringUtils.isBlank(uid)) {
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(Environment.class);
            criteria.add(Restrictions.naturalId().set("uid", uid));
            criteria.setCacheable(true);
            criteria.setCacheRegion(CACHE_REGION);
            List<Environment> environments = criteria.list();
            if (environments.size() == 1) {
                log.debug("getEnvironmentByUid() found: " + uid);
                environment = environments.get(0);
            } else {
                log.debug("getEnvironmentByUid() NOT found: " + uid);
            }
        }
        return environment;
    }

    public List<Environment> getEnvironments() {
        log.debug("getEnvironments()");
        List<Environment> environments = entityManager.createQuery(
                "FROM Environment e ")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        return environments;
    }

    public List<Environment> getEnvironments(Pager pager) {
        // first count all environments
        long count = (Long) entityManager.createQuery(
                "SELECT count(e) " +
                        "FROM Environment e")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
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
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
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
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        return apiVersions;
    }

    public APIVersion getAPIVersion(String version) {
        return (APIVersion) entityManager.createQuery(
                "FROM APIVersion apiv " +
                        "WHERE apiv.version = :version")
                .setParameter("version", version)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getSingleResult();
    }
}