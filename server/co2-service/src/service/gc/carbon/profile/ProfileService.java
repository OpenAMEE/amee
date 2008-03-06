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
import com.jellymold.utils.Pager;
import gc.carbon.data.DataCategory;
import gc.carbon.data.DataItem;
import gc.carbon.data.DataService;
import gc.carbon.data.ItemDefinition;
import gc.carbon.data.ItemValue;
import gc.carbon.data.ItemValueDefinition;
import gc.carbon.path.PathItemService;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.restlet.ext.seam.SeamController;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
@Name("profileService")
@Scope(ScopeType.EVENT)
public class ProfileService implements Serializable {

    private final static Logger log = Logger.getLogger(ProfileService.class);

    @In(create = true)
    private EntityManager entityManager;

    @In(create = true)
    private DataService dataService;

    @In(create = true)
    private PathItemService pathItemService;

    @In(create = true)
    private ProfileSheetService profileSheetService;

    @In(required = false)
    private Environment environment;

    @In(required = false)
    private Group group;

    @In(required = false)
    private User user;

    public ProfileService() {
        super();
    }

    // Handle events

    @Observer("beforeDataItemDelete")
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

    @Observer("beforeDataItemsDelete")
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

    @Observer("beforeDataCategoryDelete")
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

    @Observer("beforeUserDelete")
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

    @Observer("beforeGroupDelete")
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

    @Observer("beforeEnvironmentDelete")
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

    public ProfileItem getProfileItem(String profileUid, String dataCategoryUid, String uid) {
        ProfileItem profileItem = null;
        List<ProfileItem> profileItems;
        profileItems = entityManager.createQuery(
                "SELECT DISTINCT pi " +
                        "FROM ProfileItem pi " +
                        "LEFT JOIN FETCH pi.itemValues " +
                        "WHERE pi.profile.uid = :profileUid " +
                        "AND pi.dataCategory.uid = :dataCategoryUid " +
                        "AND pi.uid = :uid")
                .setParameter("profileUid", profileUid)
                .setParameter("dataCategoryUid", dataCategoryUid)
                .setParameter("uid", uid.toUpperCase())
                .getResultList();
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
                        "AND pi.validFrom = :validFrom " +
                        "AND pi.name = :name")
                .setParameter("profile", profileItem.getProfile())
                .setParameter("uid", profileItem.getUid())
                .setParameter("dataCategory", profileItem.getDataCategory())
                .setParameter("dataItem", profileItem.getDataItem())
                .setParameter("validFrom", profileItem.getValidFrom())
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
            // TODO: this should use profileDatePrecision instead of MONTH
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
                            "AND pi.validFrom < :profileDate")
                    .setParameter("itemDefinitionId", dataCategory.getItemDefinition().getId())
                    .setParameter("dataCategory", dataCategory)
                    .setParameter("profile", profile)
                    .setParameter("profileDate", profileDate)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.profileService")
                    .getResultList();
            return profileItems;
        } else {
            return null;
        }
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
            SeamController.getInstance().beginTransaction();
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