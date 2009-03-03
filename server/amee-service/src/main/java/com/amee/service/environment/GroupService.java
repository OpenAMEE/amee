package com.amee.service.environment;

import com.amee.domain.auth.Group;
import com.amee.domain.site.Site;
import com.amee.service.auth.AuthService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

// TODO: SPRINGIFY - What scope?

@Service
public class GroupService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    public Group getGroup(String name) {
        Site site = SiteService.getSite();
        Group group = AuthService.getGroup();
        if (group == null) {
            List<Group> groups = entityManager.createQuery(
                    "SELECT DISTINCT gr " +
                            "FROM Group gr " +
                            "WHERE gr.environment.id = :environmentId " +
                            "AND gr.name = :name")
                    .setParameter("environmentId", site.getEnvironment().getId())
                    .setParameter("name", name)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.groupService")
                    .getResultList();
            if (groups.size() == 1) {
                log.debug("group found: " + name);
                group = groups.get(0);
            }
        }
        return group;
    }
}