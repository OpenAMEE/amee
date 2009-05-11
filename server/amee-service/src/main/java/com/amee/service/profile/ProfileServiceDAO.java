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
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValue;
import com.amee.domain.environment.Environment;
import com.amee.domain.event.ObservedEvent;
import com.amee.domain.event.ObserveEventService;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;
import com.amee.service.auth.AuthService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.*;

/**
 * Encapsulates all persistence operations for Profiles and Profile Items.
 * Some business logic also included.
 * <p/>
 * Most removes are either cascaded from collections or
 * handled explicity here. 'beforeItemValueDefinitionDelete' is handled in DataService.
 * <p/>
 */
@Service
class ProfileServiceDAO implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private static final String CACHE_REGION = "query.profileService";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired(required = true)
    private ObserveEventService observeEventService;

    // Handle events

    @SuppressWarnings(value = "unchecked")
    @ServiceActivator(inputChannel = "beforeProfileDelete")
    public void beforeProfileDelete(ObservedEvent oe) {
        Profile profile = (Profile) oe.getPayload();
        // trash all ItemValues for ProfileItems within this Profile
        Session session = (Session) entityManager.getDelegate();
        SQLQuery query = session.createSQLQuery(
                new StringBuilder()
                        .append("UPDATE ITEM_VALUE iv, ITEM i ")
                        .append("SET iv.STATUS = :status ")
                        .append("WHERE iv.ITEM_ID = i.ID ")
                        .append("AND i.TYPE = 'PI' ")
                        .append("AND i.PROFILE_ID = :profileId").toString());
        query.setInteger("status", AMEEStatus.TRASH.ordinal());
        query.setLong("profileId", profile.getId());
        query.addSynchronizedEntityClass(ItemValue.class);
        query.executeUpdate();
        // trash all ProfileItems within this Profile
        entityManager.createQuery(
                new StringBuilder()
                        .append("UPDATE ProfileItem ")
                        .append("SET status = :status ")
                        .append("WHERE profile.id = :profileId").toString())
                .setParameter("status", AMEEStatus.TRASH)
                .setParameter("profileId", profile.getId())
                .executeUpdate();
    }

    @SuppressWarnings(value = "unchecked")
    @ServiceActivator(inputChannel = "beforeProfileItemDelete")
    public void beforeProfileItemDelete(ObservedEvent oe) {
        ProfileItem profileItem = (ProfileItem) oe.getPayload();
        log.debug("beforeDataItemDelete");
        // trash ItemValues for ProfileItem
        entityManager.createQuery(
                "UPDATE ItemValue iv " +
                        "SET status = :status " +
                        "WHERE iv.item.id = :profileItemId")
                .setParameter("status", AMEEStatus.TRASH)
                .setParameter("profileItemId", profileItem.getId())
                .executeUpdate();
    }

    @SuppressWarnings(value = "unchecked")
    @ServiceActivator(inputChannel = "beforeDataItemDelete")
    public void beforeDataItemDelete(ObservedEvent oe) {
        DataItem dataItem = (DataItem) oe.getPayload();
        log.debug("beforeDataItemDelete");
        // trash ItemValues for ProfileItems
        entityManager.createQuery(
                "UPDATE ItemValue iv " +
                        "SET status = :status " +
                        "WHERE iv.item.id IN " +
                        "(SELECT pi.id FROM ProfileItem pi WHERE pi.dataItem.id = :dataItemId)")
                .setParameter("status", AMEEStatus.TRASH)
                .setParameter("dataItemId", dataItem.getId())
                .executeUpdate();
        // trash ProfileItems
        entityManager.createQuery(
                "UPDATE ProfileItem " +
                        "SET status = :status " +
                        "WHERE dataItem.id = :dataItemId")
                .setParameter("status", AMEEStatus.TRASH)
                .setParameter("dataItemId", dataItem.getId())
                .executeUpdate();
    }

    @SuppressWarnings(value = "unchecked")
    @ServiceActivator(inputChannel = "beforeDataItemsDelete")
    public void beforeDataItemsDelete(ObservedEvent oe) {
        ItemDefinition itemDefinition = (ItemDefinition) oe.getPayload();
        log.debug("beforeDataItemsDelete");
        // trash ItemValues for ProfileItems
        entityManager.createQuery(
                "UPDATE ItemValue " +
                        "SET status = :status " +
                        "WHERE item.id IN " +
                        "(SELECT pi.id FROM ProfileItem pi WHERE pi.itemDefinition.id = :itemDefinitionId)")
                .setParameter("status", AMEEStatus.TRASH)
                .setParameter("itemDefinitionId", itemDefinition.getId())
                .executeUpdate();
        // trash ProfileItems
        entityManager.createQuery(
                "UPDATE ProfileItem " +
                        "SET status = :status " +
                        "WHERE itemDefinition.id = :itemDefinitionId")
                .setParameter("status", AMEEStatus.TRASH)
                .setParameter("itemDefinitionId", itemDefinition.getId())
                .executeUpdate();
    }

    @SuppressWarnings(value = "unchecked")
    @ServiceActivator(inputChannel = "beforeDataCategoryDelete")
    public void beforeDataCategoryDelete(ObservedEvent oe) {
        DataCategory dataCategory = (DataCategory) oe.getPayload();
        log.debug("beforeDataCategoryDelete");
        // trash ItemValues for ProfileItems
        entityManager.createQuery(
                "UPDATE ItemValue " +
                        "SET status = :status " +
                        "WHERE item.id IN " +
                        "(SELECT pi.id FROM ProfileItem pi WHERE pi.dataCategory.id = :dataCategoryId)")
                .setParameter("status", AMEEStatus.TRASH)
                .setParameter("dataCategoryId", dataCategory.getId())
                .executeUpdate();
        // trash ProfileItems
        entityManager.createQuery(
                "UPDATE ProfileItem " +
                        "SET status = :status " +
                        "WHERE dataCategory.id = :dataCategoryId")
                .setParameter("status", AMEEStatus.TRASH)
                .setParameter("dataCategoryId", dataCategory.getId())
                .executeUpdate();
    }

    @SuppressWarnings(value = "unchecked")
    @ServiceActivator(inputChannel = "beforeUserDelete")
    public void beforeUserDelete(ObservedEvent oe) {
        User user = (User) oe.getPayload();
        log.debug("beforeUserDelete");
        List<Profile> profiles = entityManager.createQuery(
                "SELECT p " +
                        "FROM Profile p " +
                        "WHERE p.environment.id = :environmentId " +
                        "AND p.permission.user.id = :userId")
                .setParameter("environmentId", user.getEnvironment().getId())
                .setParameter("userId", user.getId())
                .getResultList();
        for (Profile profile : profiles) {
            remove(profile);
        }
    }

    @SuppressWarnings(value = "unchecked")
    @ServiceActivator(inputChannel = "beforeGroupDelete")
    public void beforeGroupDelete(ObservedEvent oe) {
        Group group = (Group) oe.getPayload();
        log.debug("beforeGroupDelete");
        List<Profile> profiles = entityManager.createQuery(
                "SELECT p " +
                        "FROM Profile p " +
                        "WHERE p.environment.id = :environmentId " +
                        "AND p.permission.group.id = :groupId " +
                        "AND p.status = :status")
                .setParameter("environmentId", group.getEnvironment().getId())
                .setParameter("groupId", group.getId())
                .setParameter("status", AMEEStatus.ACTIVE)
                .getResultList();
        for (Profile profile : profiles) {
            remove(profile);
        }
    }

    @SuppressWarnings(value = "unchecked")
    @ServiceActivator(inputChannel = "beforeEnvironmentDelete")
    public void beforeEnvironmentDelete(ObservedEvent oe) {
        Environment environment = (Environment) oe.getPayload();
        log.debug("beforeEnvironmentDelete");
        List<Profile> profiles = entityManager.createQuery(
                "SELECT p " +
                        "FROM Profile p " +
                        "WHERE p.environment.id = :environmentId " +
                        "AND p.status = :status")
                .setParameter("environmentId", environment.getId())
                .setParameter("status", AMEEStatus.ACTIVE)
                .getResultList();
        for (Profile profile : profiles) {
            remove(profile);
        }
    }

    // Profiles

    @SuppressWarnings(value = "unchecked")
    public Profile getProfileByUid(String uid) {
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
    public Profile getProfileByPath(Environment environment, String path) {
        Profile profile = null;
        List<Profile> profiles = entityManager.createQuery(
                "FROM Profile p " +
                        "WHERE p.path = :path " +
                        "AND p.environment.id = :environmentId " +
                        "AND p.status = :status")
                .setParameter("path", path)
                .setParameter("environmentId", environment.getId())
                .setParameter("status", AMEEStatus.ACTIVE)
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
    public List<Profile> getProfiles(Environment environment, Pager pager) {
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
                        "AND p.status = :status")
                .setParameter("environmentId", environment.getId())
                .setParameter("groupId", group.getId())
                .setParameter("userId", user.getId())
                .setParameter("otherAllowView", true)
                .setParameter("groupAllowView", true)
                .setParameter("status", AMEEStatus.ACTIVE)
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
                        "AND p.status = :status " +
                        "ORDER BY p.created DESC")
                .setParameter("environmentId", environment.getId())
                .setParameter("groupId", group.getId())
                .setParameter("userId", user.getId())
                .setParameter("otherAllowView", true)
                .setParameter("groupAllowView", true)
                .setParameter("status", AMEEStatus.ACTIVE)
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

    public void persist(Profile profile) {
        entityManager.persist(profile);
    }

    /**
     * Removes (trashes) a Profile.
     *
     * @param profile to remove
     */
    public void remove(Profile profile) {
        observeEventService.raiseEvent("beforeProfileDelete", profile);
        profile.setStatus(AMEEStatus.TRASH);
    }

    // ProfileItems

    @SuppressWarnings(value = "unchecked")
    public ProfileItem getProfileItem(String uid) {
        ProfileItem profileItem = null;
        if (!StringUtils.isBlank(uid)) {
            // See http://www.hibernate.org/117.html#A12 for notes on DISTINCT_ROOT_ENTITY.
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(ProfileItem.class);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            criteria.add(Restrictions.naturalId().set("uid", uid.toUpperCase()));
            criteria.add(Restrictions.eq("status", AMEEStatus.ACTIVE));
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
    public List<ProfileItem> getProfileItems(Profile profile) {
        return (List<ProfileItem>) entityManager.createQuery(
                "SELECT DISTINCT pi " +
                        "FROM ProfileItem pi " +
                        "LEFT JOIN FETCH pi.itemValues " +
                        "WHERE pi.profile.id = :profileId " +
                        "AND pi.status = :status")
                .setParameter("profileId", profile.getId())
                .setParameter("status", AMEEStatus.ACTIVE)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<ProfileItem> getProfileItems(Profile profile, DataCategory dataCategory, Date profileDate) {
        if ((dataCategory != null) && (dataCategory.getItemDefinition() != null)) {

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
                            "AND pi.status = :status")
                    .setParameter("itemDefinitionId", dataCategory.getItemDefinition().getId())
                    .setParameter("dataCategoryId", dataCategory.getId())
                    .setParameter("profileId", profile.getId())
                    .setParameter("profileDate", profileDate)
                    .setParameter("status", AMEEStatus.ACTIVE)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                    .getResultList();

            // only include most recent ProfileItem per ProfileItem name per DataItem
            Iterator<ProfileItem> iterator = profileItems.iterator();
            while (iterator.hasNext()) {
                ProfileItem outerProfileItem = iterator.next();
                for (ProfileItem innerProfileItem : profileItems) {
                    if (outerProfileItem.getDataItem().equals(innerProfileItem.getDataItem()) &&
                            outerProfileItem.getName().equalsIgnoreCase(innerProfileItem.getName()) &&
                            outerProfileItem.getStartDate().before(innerProfileItem.getStartDate())) {
                        iterator.remove();
                        break;
                    }
                }
            }

            return profileItems;
        } else {
            return null;
        }
    }

    @SuppressWarnings(value = "unchecked")
    public List<ProfileItem> getProfileItems(Profile profile, DataCategory dataCategory, StartEndDate startDate, StartEndDate endDate) {

        if ((dataCategory == null) || (dataCategory.getItemDefinition() == null))
            return null;

        // Create HQL.
        StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT pi FROM ProfileItem pi ");
        queryBuilder.append("LEFT JOIN FETCH pi.itemValues ");
        queryBuilder.append("WHERE pi.itemDefinition.id = :itemDefinitionId ");
        queryBuilder.append("AND pi.dataCategory.id = :dataCategoryId ");
        queryBuilder.append("AND pi.profile.id = :profileId AND ");
        if (endDate == null) {
            queryBuilder.append("(pi.endDate > :startDate OR pi.endDate IS NULL)");
        } else {
            queryBuilder.append("pi.startDate < :endDate AND (pi.endDate > :startDate OR pi.endDate IS NULL)");
        }
        queryBuilder.append("AND pi.status = :status");

        // Create Query.
        Query query = entityManager.createQuery(queryBuilder.toString());
        query.setParameter("itemDefinitionId", dataCategory.getItemDefinition().getId());
        query.setParameter("dataCategoryId", dataCategory.getId());
        query.setParameter("profileId", profile.getId());
        query.setParameter("startDate", startDate.toDate());
        if (endDate != null) {
            query.setParameter("endDate", endDate.toDate());
        }
        query.setParameter("status", AMEEStatus.ACTIVE);
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", CACHE_REGION);

        return query.getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public boolean equivilentProfileItemExists(ProfileItem profileItem) {
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
                        "AND pi.status = :status")
                .setParameter("profileId", profileItem.getProfile().getId())
                .setParameter("uid", profileItem.getUid())
                .setParameter("dataCategoryId", profileItem.getDataCategory().getId())
                .setParameter("dataItemId", profileItem.getDataItem().getId())
                .setParameter("startDate", profileItem.getStartDate())
                .setParameter("name", profileItem.getName())
                .setParameter("status", AMEEStatus.ACTIVE)
                .getResultList();
        if (profileItems.size() > 0) {
            log.debug("equivilentProfileItemExists() - found ProfileItem(s)");
            return true;
        } else {
            log.debug("equivilentProfileItemExists() - no ProfileItem(s) found");
            return false;
        }
    }

    public void persist(ProfileItem profileItem) {
        entityManager.persist(profileItem);
    }

    public void remove(ProfileItem profileItem) {
        observeEventService.raiseEvent("beforeProfileItemDelete", profileItem);
        profileItem.setStatus(AMEEStatus.TRASH);
    }

    // Profile DataCategories

    @SuppressWarnings(value = "unchecked")
    public Collection<Long> getProfileDataCategoryIds(Profile profile) {

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
        sql.append("AND STATUS = :status");

        // create query
        Session session = (Session) entityManager.getDelegate();
        query = session.createSQLQuery(sql.toString());
        query.addScalar("ID", Hibernate.LONG);

        // set parameters
        query.setLong("profileId", profile.getId());
        query.setInteger("status", AMEEStatus.ACTIVE.ordinal());

        // execute SQL
        return (List<Long>) query.list();
    }
}