/**
 * This file is part of AMEE.
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.service.profile;

import com.amee.domain.AMEEStatus;
import com.amee.domain.Pager;
import com.amee.domain.StartEndDate;
import com.amee.domain.auth.Group;
import com.amee.domain.auth.User;
import com.amee.domain.data.DataCategory;
import com.amee.domain.environment.Environment;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;
import com.amee.service.auth.AuthService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Encapsulates all persistence operations for Profiles and Profile Items.
 */
@Service
public class ProfileServiceDAO implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private static final String CACHE_REGION = "query.profileService";

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings(value = "unchecked")
    protected Profile getProfileByUid(String uid) {
        Profile profile = null;
        if (!StringUtils.isBlank(uid)) {
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(Profile.class);
            criteria.add(Restrictions.naturalId().set("uid", uid));
            criteria.add(Restrictions.eq("status", AMEEStatus.ACTIVE));
            criteria.setCacheable(true);
            criteria.setCacheRegion(CACHE_REGION);
            List<Profile> profiles = criteria.list();
            if (profiles.size() == 1) {
                log.debug("getProfileByUid() found: " + uid);
                profile = profiles.get(0);
            } else {
                log.debug("getProfileByUid() NOT found: " + uid);
            }
        }
        return profile;
    }

    @SuppressWarnings(value = "unchecked")
    protected Profile getProfileByPath(Environment environment, String path) {
        Profile profile = null;
        List<Profile> profiles = entityManager.createQuery(
                "FROM Profile p " +
                        "WHERE p.path = :path " +
                        "AND p.environment.id = :environmentId " +
                        "AND p.status = :active")
                .setParameter("path", path)
                .setParameter("environmentId", environment.getId())
                .setParameter("active", AMEEStatus.ACTIVE)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        if (profiles.size() == 1) {
            log.debug("getProfileByPath() found: " + path);
            profile = profiles.get(0);                                                                                                  
        } else {
            log.debug("getProfileByPath() NOT found: " + path);
        }
        return profile;
    }

    @SuppressWarnings(value = "unchecked")
    protected List<Profile> getProfiles(Environment environment, Pager pager) {
        User user = AuthService.getUser();
        Group group = AuthService.getGroup();
        // first count all profiles
        long count = (Long) entityManager.createQuery(
                "SELECT count(p) " +
                        "FROM Profile p " +
                        "WHERE p.environment.id = :environmentId " +
                        "AND ((p.permission.otherAllowView = :otherAllowView) " +
                        "     OR (p.permission.group.id = :groupId AND p.permission.groupAllowView = :groupAllowView) " +
                        "     OR (p.permission.user.id = :userId)) " +
                        "AND p.status = :active")
                .setParameter("environmentId", environment.getId())
                .setParameter("groupId", group.getId())
                .setParameter("userId", user.getId())
                .setParameter("otherAllowView", true)
                .setParameter("groupAllowView", true)
                .setParameter("active", AMEEStatus.ACTIVE)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getSingleResult();
        // tell pager how many profiles there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the profiles for the current page
        List<Profile> profiles = entityManager.createQuery(
                "SELECT p " +
                        "FROM Profile p " +
                        "WHERE p.environment.id = :environmentId " +
                        "AND ((p.permission.otherAllowView = :otherAllowView) " +
                        "     OR (p.permission.group.id = :groupId AND p.permission.groupAllowView = :groupAllowView) " +
                        "     OR (p.permission.user.id = :userId)) " +
                        "AND p.status = :active " +
                        "ORDER BY p.created DESC")
                .setParameter("environmentId", environment.getId())
                .setParameter("groupId", group.getId())
                .setParameter("userId", user.getId())
                .setParameter("otherAllowView", true)
                .setParameter("groupAllowView", true)
                .setParameter("active", AMEEStatus.ACTIVE)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(profiles.size());
        // all done, return results
        return profiles;
    }

    protected void persist(Profile profile) {
        entityManager.persist(profile);
    }

    /**
     * Removes (trashes) a Profile.
     *
     * @param profile to remove
     */
    protected void remove(Profile profile) {
        profile.setStatus(AMEEStatus.TRASH);
        profile.getPermission().setStatus(AMEEStatus.TRASH);
    }

    // ProfileItems

    @SuppressWarnings(value = "unchecked")
    protected ProfileItem getProfileItem(String uid) {
        ProfileItem profileItem = null;
        if (!StringUtils.isBlank(uid)) {
            // See http://www.hibernate.org/117.html#A12 for notes on DISTINCT_ROOT_ENTITY.
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(ProfileItem.class);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            criteria.add(Restrictions.naturalId().set("uid", uid.toUpperCase()));
            criteria.add(Restrictions.eq("status", AMEEStatus.ACTIVE));
            criteria.setFetchMode("itemValues", FetchMode.JOIN);
            criteria.setCacheable(true);
            criteria.setCacheRegion(CACHE_REGION);
            List<ProfileItem> profileItems = criteria.list();
            if (profileItems.size() == 1) {
                log.debug("getProfileItem() found: " + uid);
                profileItem = profileItems.get(0);
            } else {
                log.debug("getProfileItem() NOT found: " + uid);
            }
        }
        return profileItem;
    }

    @SuppressWarnings(value = "unchecked")
    protected List<ProfileItem> getProfileItems(Profile profile, DataCategory dataCategory, Date profileDate) {

        if ((dataCategory == null) || (dataCategory.getItemDefinition() == null)) {
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("getProfileItems() start");
        }

        // need to roll the date forward
        Calendar profileDateCal = Calendar.getInstance();
        profileDateCal.setTime(profileDate);
        profileDateCal.add(Calendar.MONTH, 1);
        profileDate = profileDateCal.getTime();

        // now get all the Profile Items
        List<ProfileItem> profileItems = entityManager.createQuery(
                "SELECT DISTINCT pi " +
                        "FROM ProfileItem pi " +
                        "LEFT JOIN FETCH pi.itemValues " +
                        "WHERE pi.itemDefinition.id = :itemDefinitionId " +
                        "AND pi.dataCategory.id = :dataCategoryId " +
                        "AND pi.profile.id = :profileId " +
                        "AND pi.startDate < :profileDate " +
                        "AND pi.status = :active " + 
                        "ORDER BY pi.name, pi.dataItem, pi.startDate DESC")
                .setParameter("itemDefinitionId", dataCategory.getItemDefinition().getId())
                .setParameter("dataCategoryId", dataCategory.getId())
                .setParameter("profileId", profile.getId())
                .setParameter("profileDate", profileDate)
                .setParameter("active", AMEEStatus.ACTIVE)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();

        if (log.isDebugEnabled()) {
            log.debug("getProfileItems() done (" + profileItems.size() + ")");
        }

        return profileItems;
    }

    @SuppressWarnings(value = "unchecked")
    protected List<ProfileItem> getProfileItems(Profile profile, DataCategory dataCategory, StartEndDate startDate, StartEndDate endDate) {

        if ((dataCategory == null) || (dataCategory.getItemDefinition() == null)) {
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("getProfileItems() start");
        }

        // Create HQL.
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT DISTINCT pi ");
        queryBuilder.append("FROM ProfileItem pi ");
        queryBuilder.append("LEFT JOIN FETCH pi.itemValues ");
        queryBuilder.append("WHERE pi.itemDefinition.id = :itemDefinitionId ");
        queryBuilder.append("AND pi.dataCategory.id = :dataCategoryId ");
        queryBuilder.append("AND pi.profile.id = :profileId AND ");
        if (endDate == null) {
            queryBuilder.append("(pi.endDate IS NULL OR pi.endDate > :startDate) ");
        } else {
            queryBuilder.append("(pi.startDate < :endDate) AND (pi.endDate IS NULL OR pi.endDate > :startDate) ");
        }
        queryBuilder.append("AND pi.status = :active ");
        queryBuilder.append("ORDER BY pi.startDate DESC");

        // Create Query.
        Query query = entityManager.createQuery(queryBuilder.toString());
        query.setParameter("itemDefinitionId", dataCategory.getItemDefinition().getId());
        query.setParameter("dataCategoryId", dataCategory.getId());
        query.setParameter("profileId", profile.getId());
        query.setParameter("startDate", startDate.toDate());
        if (endDate != null) {
            query.setParameter("endDate", endDate.toDate());
        }
        query.setParameter("active", AMEEStatus.ACTIVE);
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", CACHE_REGION);

        // Execute query.
        List<ProfileItem> profileItems = query.getResultList();

        if (log.isDebugEnabled()) {
            log.debug("getProfileItems() done (" + profileItems.size() + ")");
        }

        return profileItems;
    }

    @SuppressWarnings(value = "unchecked")
    protected boolean equivilentProfileItemExists(ProfileItem profileItem) {
        List<ProfileItem> profileItems = entityManager.createQuery(
                "SELECT DISTINCT pi " +
                        "FROM ProfileItem pi " +
                        "LEFT JOIN FETCH pi.itemValues " +
                        "WHERE pi.profile.id = :profileId " +
                        "AND pi.uid != :uid " +
                        "AND pi.dataCategory.id = :dataCategoryId " +
                        "AND pi.dataItem.id = :dataItemId " +
                        "AND pi.startDate = :startDate " +
                        "AND pi.name = :name " +
                        "AND pi.status = :active")
                .setParameter("profileId", profileItem.getProfile().getId())
                .setParameter("uid", profileItem.getUid())
                .setParameter("dataCategoryId", profileItem.getDataCategory().getId())
                .setParameter("dataItemId", profileItem.getDataItem().getId())
                .setParameter("startDate", profileItem.getStartDate())
                .setParameter("name", profileItem.getName())
                .setParameter("active", AMEEStatus.ACTIVE)
                .getResultList();
        if (profileItems.size() > 0) {
            log.debug("equivilentProfileItemExists() - found ProfileItem(s)");
            return true;
        } else {
            log.debug("equivilentProfileItemExists() - no ProfileItem(s) found");
            return false;
        }
    }

    protected void persist(ProfileItem profileItem) {
        entityManager.persist(profileItem);
    }

    protected void remove(ProfileItem profileItem) {
        profileItem.setStatus(AMEEStatus.TRASH);
    }

    // Profile DataCategories

    @SuppressWarnings(value = "unchecked")
    protected Collection<Long> getProfileDataCategoryIds(Profile profile) {

        StringBuilder sql;
        SQLQuery query;

        // check arguments
        if (profile == null) {
            throw new IllegalArgumentException("A required argument is missing.");
        }

        // create SQL
        sql = new StringBuilder();
        sql.append("SELECT DISTINCT DATA_CATEGORY_ID ID ");
        sql.append("FROM ITEM ");
        sql.append("WHERE TYPE = 'PI' ");
        sql.append("AND PROFILE_ID = :profileId ");
        sql.append("AND STATUS = :active");

        // create query
        Session session = (Session) entityManager.getDelegate();
        query = session.createSQLQuery(sql.toString());
        query.addScalar("ID", Hibernate.LONG);

        // set parameters
        query.setLong("profileId", profile.getId());
        query.setInteger("active", AMEEStatus.ACTIVE.ordinal());

        // execute SQL
        return (List<Long>) query.list();
    }
}