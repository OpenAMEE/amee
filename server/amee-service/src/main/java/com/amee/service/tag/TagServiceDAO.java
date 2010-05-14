package com.amee.service.tag;

import com.amee.domain.AMEEStatus;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.tag.EntityTag;
import com.amee.domain.tag.Tag;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

@Repository
public class TagServiceDAO implements Serializable {

    private static final String CACHE_REGION = "query.tagService";

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings(value = "unchecked")
    protected List<Tag> getTags() {
        Session session = (Session) entityManager.getDelegate();
        Criteria criteria = session.createCriteria(Tag.class);
        criteria.add(Restrictions.ne("status", AMEEStatus.TRASH));
        criteria.setCacheable(true);
        criteria.setCacheRegion(CACHE_REGION);
        return criteria.list();
    }

    @SuppressWarnings(value = "unchecked")
    public List<EntityTag> getEntityTags(IAMEEEntityReference entity) {
        Session session = (Session) entityManager.getDelegate();
        Criteria criteria = session.createCriteria(EntityTag.class);
        criteria.add(Restrictions.eq("entityReference.entityUid", entity.getEntityUid()));
        criteria.add(Restrictions.eq("entityReference.entityType", entity.getObjectType().getName()));
        criteria.add(Restrictions.ne("status", AMEEStatus.TRASH));
        criteria.setCacheable(true);
        criteria.setCacheRegion(CACHE_REGION);
        return criteria.list();
    }

    public void persist(Tag tag) {
        entityManager.persist(tag);
    }

    public void remove(Tag tag) {
        tag.setStatus(AMEEStatus.TRASH);
    }

    public void persist(EntityTag entityTag) {
        entityManager.persist(entityTag);
    }

    public void remove(EntityTag entityTag) {
        entityTag.setStatus(AMEEStatus.TRASH);
    }
}