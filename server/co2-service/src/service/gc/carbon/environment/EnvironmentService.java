package gc.carbon.environment;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.ScheduledTask;
import com.jellymold.kiwi.Site;
import com.jellymold.utils.Pager;
import com.jellymold.utils.ThreadBeanHolder;
import com.jellymold.utils.event.ObservedEvent;
import com.jellymold.utils.event.ObserveEventService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

import gc.carbon.APIVersion;

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

    @ServiceActivator(inputChannel="beforeSiteDelete")
    public void beforeSiteDelete(ObservedEvent oe) {
        log.debug("beforeSiteDelete" + (Site) oe.getPayload());
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

    // ScheduledTasks

    public List<ScheduledTask> getScheduledTasks(Environment environment) {
        log.debug("getScheduledTasks()");
        List<ScheduledTask> scheduledTasks = entityManager.createQuery(
                "FROM ScheduledTask st " +
                        "WHERE st.environment.id = :environmentId")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getResultList();
        return scheduledTasks;
    }

    public ScheduledTask getScheduledTaskByUid(Environment environment, String uid) {
        ScheduledTask scheduledTask = null;
        if ((environment != null) && (uid != null)) {
            List<ScheduledTask> scheduledTasks = entityManager.createQuery(
                    "SELECT st FROM ScheduledTask st " +
                            "WHERE st.environment.id = :environmentId " +
                            "AND st.uid = :scheduledTaskUid")
                    .setParameter("environmentId", environment.getId())
                    .setParameter("scheduledTaskUid", uid)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.siteService")
                    .getResultList();
            if (scheduledTasks.size() == 1) {
                scheduledTask = scheduledTasks.get(0);
            }
        }
        return scheduledTask;
    }

    public ScheduledTask getScheduledTaskByName(Environment environment, String name) {
        ScheduledTask scheduledTask = null;
        if ((environment != null) && (name != null)) {
            List<ScheduledTask> scheduledTasks = entityManager.createQuery(
                    "SELECT st FROM ScheduledTask st " +
                            "WHERE st.environment.id = :environmentId " +
                            "AND st.name = :name")
                    .setParameter("environmentId", environment.getId())
                    .setParameter("name", name.trim())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.siteService")
                    .getResultList();
            if (scheduledTasks.size() == 1) {
                scheduledTask = scheduledTasks.get(0);
            }
        }
        return scheduledTask;
    }

    public List<ScheduledTask> getScheduledTasks(Environment environment, Pager pager) {
        // first count all objects
        long count = (Long) entityManager.createQuery(
                "SELECT count(st) " +
                        "FROM ScheduledTask st " +
                        "WHERE st.environment.id = :environmentId ")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .getSingleResult();
        // tell pager how many objects there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the objects for the current page
        List<ScheduledTask> scheduledTasks = entityManager.createQuery(
                "SELECT st " +
                        "FROM ScheduledTask st " +
                        "WHERE st.environment.id = :environmentId " +
                        "ORDER BY st.name")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.siteService")
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(scheduledTasks.size());
        // all done, return results
        return scheduledTasks;
    }


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


    public void save(ScheduledTask scheduledTask) {
        entityManager.persist(scheduledTask);
    }

    public void remove(ScheduledTask scheduledTask) {
        entityManager.remove(scheduledTask);
    }
}