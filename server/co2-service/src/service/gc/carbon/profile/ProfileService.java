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
package gc.carbon.profile;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.Group;
import com.jellymold.kiwi.User;
import com.jellymold.kiwi.auth.AuthService;
import com.jellymold.kiwi.environment.EnvironmentService;
import com.jellymold.utils.Pager;
import gc.carbon.domain.data.*;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.path.PathItemService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.ext.seam.SpringController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
 * TODO: How would deletes perform when there are lots of Profiles?
 * TODO: Clear caches after entity removal.
 * TODO: Any other cache operations to put here?
 * TODO: Remove site and group injection and make method calls explicit.
 */
@Service
public class ProfileService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SpringController springController;

    @Autowired
    private PathItemService pathItemService;

    @Autowired
    private ProfileSheetService profileSheetService;

    public ProfileService() {
        super();
    }

    // Handle events

    // TODO: Springify
    // @Observer("beforeDataItemDelete")

    public void beforeDataItemDelete(DataItem dataItem) {
        log.debug("beforeDataItemDelete");
        // remove ItemValues for ProfileItems
        entityManager.createQuery(
                "DELETE FROM ItemValue iv " +
                        "WHERE iv.item IN " +
                        "(SELECT pi FROM ProfileItem pi WHERE pi.dataItem = :dataItem)")
                .setParameter("dataItem", dataItem)
                .executeUpdate();
        // remove ProfileItems
        entityManager.createQuery(
                "DELETE FROM ProfileItem pi " +
                        "WHERE pi.dataItem = :dataItem)")
                .setParameter("dataItem", dataItem)
                .executeUpdate();
    }

    // TODO: Springify
    // @Observer("beforeDataItemsDelete")
    public void beforeDataItemsDelete(ItemDefinition itemDefinition) {
        log.debug("beforeDataItemsDelete");
        // remove ItemValues for ProfileItems
        entityManager.createQuery(
                "DELETE FROM ItemValue iv " +
                        "WHERE iv.item IN " +
                        "(SELECT pi FROM ProfileItem pi WHERE pi.itemDefinition = :itemDefinition)")
                .setParameter("itemDefinition", itemDefinition)
                .executeUpdate();
        // remove ProfileItems
        entityManager.createQuery(
                "DELETE FROM ProfileItem pi " +
                        "WHERE pi.itemDefinition = :itemDefinition)")
                .setParameter("itemDefinition", itemDefinition)
                .executeUpdate();
    }

    // TODO: Springify
    // @Observer("beforeDataCategoryDelete")
    public void beforeDataCategoryDelete(DataCategory dataCategory) {
        log.debug("beforeDataCategoryDelete");
        // remove ItemValues for ProfileItems
        entityManager.createQuery(
                "DELETE FROM ItemValue iv " +
                        "WHERE iv.item IN " +
                        "(SELECT pi FROM ProfileItem pi WHERE pi.dataCategory = :dataCategory)")
                .setParameter("dataCategory", dataCategory)
                .executeUpdate();
        // remove ProfileItems
        entityManager.createQuery(
                "DELETE FROM ProfileItem pi " +
                        "WHERE pi.dataCategory = :dataCategory)")
                .setParameter("dataCategory", dataCategory)
                .executeUpdate();
    }

    // TODO: Springify
    // @Observer("beforeUserDelete")
    public void beforeUserDelete(User user) {
        log.debug("beforeUserDelete");
        List<Profile> profiles = entityManager.createQuery(
                "SELECT p " +
                        "FROM Profile p " +
                        "WHERE p.environment.id = :environmentId " +
                        "AND p.permission.user = :user")
                .setParameter("environmentId", user.getEnvironment().getId())
                .setParameter("user", user)
                .getResultList();
        for (Profile profile : profiles) {
            remove(profile);
        }

    }

    // TODO: Springify
    // @Observer("beforeGroupDelete")
    public void beforeGroupDelete(Group group) {
        log.debug("beforeGroupDelete");
        List<Profile> profiles = entityManager.createQuery(
                "SELECT p " +
                        "FROM Profile p " +
                        "WHERE p.environment.id = :environmentId " +
                        "AND p.permission.group = :group")
                .setParameter("environmentId", group.getEnvironment().getId())
                .setParameter("group", group)
                .getResultList();
        for (Profile profile : profiles) {
            remove(profile);
        }
    }

    // TODO: Springify
    // @Observer("beforeEnvironmentDelete")
    public void beforeEnvironmentDelete(Environment environment) {
        log.debug("beforeEnvironmentDelete");
        List<Profile> profiles = entityManager.createQuery(
                "SELECT p " +
                        "FROM Profile p " +
                        "WHERE p.environment.id = :environmentId")
                .setParameter("environmentId", environment.getId())
                .getResultList();
        for (Profile profile : profiles) {
            remove(profile);
        }
    }

    // Profiles

    public Profile getProfile(String path) {
        Profile profile = getProfileByPath(path);
        if (profile == null) {
            profile = getProfileByUid(path);
        }
        return profile;
    }

    public Profile getProfileByUid(String uid) {
        Profile profile = null;
        List<Profile> profiles;
        Environment environment = EnvironmentService.getEnvironment();
        profiles = entityManager.createQuery(
                "FROM Profile p " +
                        "WHERE p.uid = :uid " +
                        "AND p.environment.id = :environmentId")
                .setParameter("uid", uid.toUpperCase())
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.profileService")
                .getResultList();
        if (profiles.size() == 1) {
            log.debug("found Profile");
            profile = profiles.get(0);
        } else {
            log.debug("Profile NOT found");
        }
        return profile;
    }

    public Profile getProfileByPath(String path) {
        Profile profile = null;
        List<Profile> profiles;
        Environment environment = EnvironmentService.getEnvironment();
        profiles = entityManager.createQuery(
                "FROM Profile p " +
                        "WHERE p.path = :path " +
                        "AND p.environment.id = :environmentId")
                .setParameter("path", path)
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.profileService")
                .getResultList();
        if (profiles.size() == 1) {
            log.debug("found Profile");
            profile = profiles.get(0);
        } else {
            log.debug("Profile NOT found");
        }
        return profile;
    }

    public List<Profile> getProfiles(Pager pager) {
        Environment environment = EnvironmentService.getEnvironment();
        User user = AuthService.getUser();
        Group group = AuthService.getGroup();
        // first count all profiles
        long count = (Long) entityManager.createQuery(
                "SELECT count(p) " +
                        "FROM Profile p " +
                        "WHERE p.environment.id = :environmentId " +
                        "AND ((p.permission.otherAllowView = :otherAllowView) " +
                        "     OR (p.permission.group = :group AND p.permission.groupAllowView = :groupAllowView) " +
                        "     OR (p.permission.user = :user))")
                .setParameter("environmentId", environment.getId())
                .setParameter("group", group)
                .setParameter("user", user)
                .setParameter("otherAllowView", true)
                .setParameter("groupAllowView", true)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.profileService")
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
                        "     OR (p.permission.group = :group AND p.permission.groupAllowView = :groupAllowView) " +
                        "     OR (p.permission.user = :user)) " +
                        "ORDER BY p.created DESC")
                .setParameter("environmentId", environment.getId())
                .setParameter("group", group)
                .setParameter("user", user)
                .setParameter("otherAllowView", true)
                .setParameter("groupAllowView", true)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.profileService")
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(profiles.size());
        // all done, return results
        return profiles;
    }

    public void remove(Profile profile) {
        log.debug("remove: " + profile.getUid());
        // delete all ItemValues for ProfileItems within this Profile
        entityManager.createQuery(
                "DELETE FROM ItemValue iv " +
                        "WHERE iv.item IN " +
                        "(SELECT pi FROM ProfileItem pi WHERE pi.profile = :profile)")
                .setParameter("profile", profile)
                .executeUpdate();
        // delete all ProfileItems within this Profile
        entityManager.createQuery("DELETE FROM ProfileItem pi WHERE pi.profile = :profile")
                .setParameter("profile", profile)
                .executeUpdate();
        // delete Profile
        entityManager.remove(profile);
    }

    // ProfileItems

    public ProfileItem getProfileItem(String profileUid, String uid) {
        return getProfileItem(profileUid, null, uid);
    }

    public ProfileItem getProfileItem(String profileUid, String dataCategoryUid, String uid) {
        ProfileItem profileItem = null;
        List<ProfileItem> profileItems;
        Query query;
        String hql = "SELECT DISTINCT pi " +
                "FROM ProfileItem pi " +
                "LEFT JOIN FETCH pi.itemValues " +
                "WHERE pi.profile.uid = :profileUid " +
                "AND pi.uid = :uid";
        if (dataCategoryUid != null) {
            hql += " AND pi.dataCategory.uid = :dataCategoryUid";
        }
        query = entityManager.createQuery(hql);
        query.setParameter("profileUid", profileUid);
        query.setParameter("uid", uid.toUpperCase());
        if (dataCategoryUid != null) {
            query.setParameter("dataCategoryUid", dataCategoryUid);
        }
        profileItems = query.getResultList();
        if (profileItems.size() == 1) {
            log.debug("found ProfileItem");
            profileItem = profileItems.get(0);
            checkProfileItem(profileItem);
        } else {
            log.debug("ProfileItem NOT found");
        }
        return profileItem;
    }

    public boolean isEquivilentProfileItemExists(ProfileItem profileItem) {
        List<ProfileItem> profileItems = entityManager.createQuery(
                "SELECT DISTINCT pi " +
                        "FROM ProfileItem pi " +
                        "LEFT JOIN FETCH pi.itemValues " +
                        "WHERE pi.profile = :profile " +
                        "AND pi.uid != :uid " +
                        "AND pi.dataCategory = :dataCategory " +
                        "AND pi.dataItem = :dataItem " +
                        "AND pi.startDate = :startDate " +
                        "AND pi.name = :name")
                .setParameter("profile", profileItem.getProfile())
                .setParameter("uid", profileItem.getUid())
                .setParameter("dataCategory", profileItem.getDataCategory())
                .setParameter("dataItem", profileItem.getDataItem())
                .setParameter("startDate", profileItem.getStartDate())
                .setParameter("name", profileItem.getName())
                .getResultList();
        if (profileItems.size() > 0) {
            log.debug("found ProfileItem(s)");
            return true;
        } else {
            log.debug("ProfileItem(s) NOT found");
            return false;
        }
    }

    public List<ProfileItem> getProfileItems(Profile profile) {
        List<ProfileItem> profileItems = entityManager.createQuery(
                "SELECT DISTINCT pi " +
                        "FROM ProfileItem pi " +
                        "LEFT JOIN FETCH pi.itemValues " +
                        "WHERE pi.profile = :profile")
                .setParameter("profile", profile)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.profileService")
                .getResultList();
        return profileItems;
    }

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
                            "AND pi.dataCategory = :dataCategory " +
                            "AND pi.profile = :profile " +
                            "AND pi.startDate < :profileDate")
                    .setParameter("itemDefinitionId", dataCategory.getItemDefinition().getId())
                    .setParameter("dataCategory", dataCategory)
                    .setParameter("profile", profile)
                    .setParameter("profileDate", profileDate)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.profileService")
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

    public List<ProfileItem> getProfileItems(ProfileBrowser profileBrowser) {

        if ((profileBrowser.getDataCategory() == null) || (profileBrowser.getDataCategory().getItemDefinition() == null))
            return null;

        StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT pi FROM ProfileItem pi ");
        queryBuilder.append("LEFT JOIN FETCH pi.itemValues ");
        queryBuilder.append("WHERE pi.itemDefinition.id = :itemDefinitionId ");
        queryBuilder.append("AND pi.dataCategory = :dataCategory ");
        queryBuilder.append("AND pi.profile = :profile AND ");
        if (profileBrowser.getEndDate() == null) {
            queryBuilder.append("IFNULL(pi.endDate,:startDate) >= :startDate");
        } else {
            queryBuilder.append("pi.startDate < :endDate AND IFNULL(pi.endDate,:startDate) >= :startDate");
        }

        // now get all the Profile Items
        Query query = entityManager.createQuery(queryBuilder.toString());

        query.setParameter("itemDefinitionId", profileBrowser.getDataCategory().getItemDefinition().getId());
        query.setParameter("dataCategory", profileBrowser.getDataCategory());
        query.setParameter("profile", profileBrowser.getProfile());
        query.setParameter("startDate", profileBrowser.getStartDate().toDate());

        if (profileBrowser.getEndDate() != null)
            query.setParameter("endDate", profileBrowser.getEndDate().toDate());

        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", "query.profileService");

        return query.getResultList();

    }

    public void remove(ProfileItem profileItem) {
        entityManager.remove(profileItem);
    }

    // ItemValues

    public ItemValue getProfileItemValue(String profileItemUid, String uid) {
        ItemValue profileItemValue = null;
        List<ItemValue> profileItemValues;
        profileItemValues = entityManager.createQuery(
                "FROM ItemValue iv " +
                        "LEFT JOIN FETCH iv.item i " +
                        "WHERE i.uid = :profileItemUid " +
                        "AND iv.uid = :uid")
                .setParameter("profileItemUid", profileItemUid)
                .setParameter("uid", uid)
                .getResultList();
        if (profileItemValues.size() == 1) {
            log.debug("found ItemValue");
            profileItemValue = profileItemValues.get(0);
        } else {
            log.debug("ItemValue NOT found");
        }
        return profileItemValue;
    }

    public void remove(ItemValue profileItemValue) {
        entityManager.remove(profileItemValue);
    }

    // check Profile Item objects

    public void checkProfileItem(ProfileItem profileItem) {
        // find ItemValueDefinitions not currently implemented in this Item
        List<ItemValueDefinition> itemValueDefinitions = entityManager.createQuery(
                "FROM ItemValueDefinition ivd " +
                        "WHERE ivd NOT IN (" +
                        "   SELECT iv.itemValueDefinition " +
                        "   FROM ItemValue iv " +
                        "   WHERE iv.item = :profileItem) " +
                        "AND ivd.fromProfile = :fromProfile " +
                        "AND ivd.itemDefinition.id = :itemDefinitionId")
                .setParameter("profileItem", profileItem)
                .setParameter("itemDefinitionId", profileItem.getItemDefinition().getId())
                .setParameter("fromProfile", true)
                .getResultList();
        if (itemValueDefinitions.size() > 0) {
            // ensure transaction has been started
            springController.beginTransaction();
            // create missing ItemValues
            for (ItemValueDefinition ivd : itemValueDefinitions) {
                // start default value with value from ItemValueDefinition
                String defaultValue = ivd.getValue();
                // next give DataItem a chance to set the default value, if appropriate
                if (ivd.isFromData()) {
                    Map<String, ItemValue> dataItemValues = profileItem.getDataItem().getItemValuesMap();
                    ItemValue dataItemValue = dataItemValues.get(ivd.getPath());
                    if ((dataItemValue != null) && (dataItemValue.getValue().length() > 0)) {
                        defaultValue = dataItemValue.getValue();
                    }
                }
                // create missing ItemValue
                new ItemValue(ivd, profileItem, defaultValue);
            }
            // clear caches
            pathItemService.removePathItemGroup(profileItem.getProfile());
            profileSheetService.removeSheets(profileItem.getProfile());
        }
    }
}