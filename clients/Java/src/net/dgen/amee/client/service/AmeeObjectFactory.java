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
/**
 * This file is part of AMEE Java Client Library.
 *
 * AMEE Java Client Library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE Java Client Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.dgen.amee.client.service;

import net.dgen.amee.client.AmeeException;
import net.dgen.amee.client.cache.AmeeObjectCache;
import net.dgen.amee.client.cache.AmeeObjectCacheDummyImpl;
import net.dgen.amee.client.cache.AmeeObjectCacheEntry;
import net.dgen.amee.client.cache.AmeeObjectCacheImpl;
import net.dgen.amee.client.model.base.AmeeCategory;
import net.dgen.amee.client.model.base.AmeeItem;
import net.dgen.amee.client.model.base.AmeeObject;
import net.dgen.amee.client.model.base.AmeeObjectReference;
import net.dgen.amee.client.model.base.AmeeObjectType;
import net.dgen.amee.client.model.base.AmeeValue;
import net.dgen.amee.client.model.data.AmeeDataCategory;
import net.dgen.amee.client.model.data.AmeeDataItem;
import net.dgen.amee.client.model.data.AmeeDrillDown;
import net.dgen.amee.client.model.profile.AmeeProfile;
import net.dgen.amee.client.model.profile.AmeeProfileCategory;
import net.dgen.amee.client.model.profile.AmeeProfileItem;
import net.dgen.amee.client.util.Choice;
import net.dgen.amee.client.util.Pager;
import net.dgen.amee.client.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AmeeObjectFactory implements Serializable {

    public final static String[] ITEM_EXCLUDES_ARR = {
            "uid",
            "path",
            "label",
            "created",
            "modified",
            "dataItemUid",
            "validFrom",
            "dataItemLabel",
            "end",
            "name",
            "amountPerMonth"};
    public final static List<String> ITEM_EXCLUDES = new ArrayList<String>(Arrays.asList(ITEM_EXCLUDES_ARR));

    // TODO: inject
    private AmeeObjectCache cache = new AmeeObjectCacheImpl();

    private static AmeeObjectFactory instance = new AmeeObjectFactory();

    public static AmeeObjectFactory getInstance() {
        return instance;
    }

    private AmeeObjectFactory() {
        super();
    }

    // factory methods

    public AmeeDataCategory getDataCategoryRoot() throws AmeeException {
        return (AmeeDataCategory) getObject("data", AmeeObjectType.DATA_CATEGORY);
    }

    public AmeeDataCategory getDataCategory(String path) throws AmeeException {
        return (AmeeDataCategory) getObject("data/" + path, AmeeObjectType.DATA_CATEGORY);
    }

    public AmeeDrillDown getDrillDown(String path) throws AmeeException {
        return (AmeeDrillDown) getObject("data/" + path, AmeeObjectType.DRILL_DOWN);
    }

    public AmeeDrillDown getDrillDown(AmeeDataCategory dataCategory) throws AmeeException {
        return (AmeeDrillDown) getObject(dataCategory.getUri() + "/drill", AmeeObjectType.DRILL_DOWN);
    }

    public AmeeProfile getProfile(String uid) throws AmeeException {
        return (AmeeProfile) getObject("profiles/" + uid, AmeeObjectType.PROFILE);
    }

    public AmeeProfile getProfile() throws AmeeException {
        String response;
        AmeeProfile profile;
        JSONObject profileObj;
        List<Choice> parameters = new ArrayList<Choice>();
        parameters.add(new Choice("profile", "true"));
        response = AmeeInterface.getInstance().getAmeeResourceByPost("/profiles", parameters);
        try {
            profileObj = new JSONObject(response);
            profileObj = profileObj.getJSONObject("profile");
            profile = new AmeeProfile("/profiles/" + profileObj.getString("path"), AmeeObjectType.PROFILE);
            profile.setUid(profileObj.getString("uid"));
            profile.setName(profileObj.getString("name"));
            addObjectToCache(profile, true);
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
        return profile;
    }

    public AmeeProfileItem getProfileItem(AmeeProfileCategory profileCategory, String dataItemUid, List<Choice> values) throws AmeeException {
        String response;
        AmeeProfileItem profileItem;
        JSONObject jsonObj;
        List<Choice> parameters = new ArrayList<Choice>();
        parameters.add(new Choice("dataItemUid", dataItemUid));
        if (values != null) {
            parameters.addAll(values);
        }
        response = AmeeInterface.getInstance().getAmeeResourceByPost(profileCategory.getUri(), parameters);
        try {
            // create ProfileItem and add to cache
            jsonObj = new JSONObject(response);
            profileItem = new AmeeProfileItem(
                    profileCategory.getObjectReference().getPath() + "/" + jsonObj.getJSONObject("profileItem").getString("uid"), AmeeObjectType.PROFILE_ITEM);
            parse(profileItem, jsonObj);
            addObjectToCache(profileItem, true);
            // invalidate ProfileCategory
            getCache().remove(profileCategory.getUri());
            profileCategory.setFetched(false);
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
        return profileItem;
    }

    public void setItemValues(AmeeItem item, List<Choice> values) throws AmeeException {
        String response;
        JSONObject jsonObj;
        List<Choice> parameters = new ArrayList<Choice>();
        if (values != null) {
            parameters.addAll(values);
        }
        response = AmeeInterface.getInstance().getAmeeResourceByPut(item.getUri(), parameters);
        try {
            // invalidate item and parent
            invalidate(item);
            getCache().remove(item.getParentUri());
            // update ProfileItem and replace in cache
            jsonObj = new JSONObject(response);
            parse(item, jsonObj);
            addObjectToCache(item, true);
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
    }

    public AmeeProfileCategory getProfileCategory(AmeeProfile profile, String path) throws AmeeException {
        return (AmeeProfileCategory) getObject(profile.getUri() + "/" + path, AmeeObjectType.PROFILE_CATEGORY);
    }

    public AmeeObject getObject(String uri, AmeeObjectType objectType) throws AmeeException {
        return getObject(new AmeeObjectReference(uri, objectType));
    }

    public AmeeObject getObject(AmeeObjectReference ref) throws AmeeException {
        return getObject(ref, true);
    }

    protected AmeeObject getObject(AmeeObjectReference ref, boolean create) throws AmeeException {
        AmeeObject ameeObject = null;
        AmeeObjectCacheEntry ameeObjectCacheEntry = getObjectCacheEntry(ref, create);
        if (ameeObjectCacheEntry != null) {
            ameeObject = ameeObjectCacheEntry.getObject();
            if (ameeObject != null) {
                return ameeObject.getCopy();
            }
        }
        return ameeObject;
    }

    protected AmeeObjectCacheEntry getObjectCacheEntry(AmeeObjectReference ref) throws AmeeException {
        return getObjectCacheEntry(ref, true);
    }

    protected AmeeObjectCacheEntry getObjectCacheEntry(AmeeObjectReference ref, boolean create) throws AmeeException {
        String response;
        AmeeObject ameeObject;
        AmeeObjectCacheEntry ameeObjectCacheEntry = getCache().get(ref.getUri());
        if (create &&
                ((ameeObjectCacheEntry == null) ||
                        !ameeObjectCacheEntry.getObjectReference().
                                getObjectType().equals(ref.getObjectType()))) {
            ameeObject = getNewObject(ref);
            response = AmeeInterface.getInstance().getAmeeResource(ameeObject.getUri());
            parse(ameeObject, response);
            ameeObjectCacheEntry = addObjectToCache(ameeObject);
        }
        return ameeObjectCacheEntry;
    }

    public void fetch(AmeeObject object) throws AmeeException {
        String response = AmeeInterface.getInstance().getAmeeResource(object.getUri());
        parse(object, response);
        addObjectToCache(object, true);
    }

    public void save(AmeeObject object) throws AmeeException {
        switch (object.getObjectType()) {
            case DATA_CATEGORY:
                break;
            case PROFILE_CATEGORY:
                break;
            case DATA_ITEM:
                break;
            case DRILL_DOWN:
                break;
            case PROFILE:
                break;
            case PROFILE_ITEM:
                break;
            case VALUE:
                saveValue((AmeeValue) object);
                return;
            default:
                break;
        }
        throw new AmeeException("Save not supported for this object.");
    }

    public void delete(AmeeObject object) throws AmeeException {
        switch (object.getObjectType()) {
            case DATA_CATEGORY:
                break;
            case PROFILE_CATEGORY:
                break;
            case DATA_ITEM:
                break;
            case DRILL_DOWN:
                break;
            case PROFILE:
                deleteProfile((AmeeProfile) object);
                return;
            case PROFILE_ITEM:
                deleteProfileItem((AmeeProfileItem) object);
                return;
            case VALUE:
                break;
            default:
                break;
        }
        throw new AmeeException("Delete not supported for this object.");
    }

    protected AmeeObjectCacheEntry getObjectCacheEntry(AmeeObject object) throws AmeeException {
        AmeeObjectCacheEntry ameeObjectCacheEntry;
        String response = AmeeInterface.getInstance().getAmeeResource(object.getUri());
        parse(object, response);
        ameeObjectCacheEntry = addObjectToCache(object);
        return ameeObjectCacheEntry;
    }

    protected AmeeObject getNewObject(AmeeObjectReference ref) throws AmeeException {
        switch (ref.getObjectType()) {
            case DATA_CATEGORY:
                return new AmeeDataCategory(ref);
            case PROFILE_CATEGORY:
                return new AmeeProfileCategory(ref);
            case DATA_ITEM:
                return new AmeeDataItem(ref);
            case DRILL_DOWN:
                return new AmeeDrillDown(ref);
            case PROFILE:
                return new AmeeProfile(ref);
            case PROFILE_ITEM:
                return new AmeeProfileItem(ref);
            case VALUE:
                return new AmeeValue(ref);
        }
        throw new AmeeException("Object Type not recognised.");
    }

    protected AmeeObjectCacheEntry addObjectToCache(AmeeObject object) {
        return addObjectToCache(object, false);
    }

    protected AmeeObjectCacheEntry addObjectToCache(AmeeObject object, boolean copy) {
        if (copy) {
            object = object.getCopy();
        }
        AmeeObjectCacheEntry ameeObjectCacheEntry =
                new AmeeObjectCacheEntry(object.getObjectReference(), object);
        getCache().put(ameeObjectCacheEntry);
        return ameeObjectCacheEntry;
    }

    // AmeeObject parsing

    protected void parse(AmeeObject ameeObject, String s) throws AmeeException {
        try {
            parse(ameeObject, new JSONObject(s));
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
    }

    protected void parse(AmeeObject object, JSONObject jsonObj) throws AmeeException {
        switch (object.getObjectType()) {
            case DATA_CATEGORY:
                parseDataCategory((AmeeDataCategory) object, jsonObj);
                break;
            case PROFILE_CATEGORY:
                parseProfileCategory((AmeeProfileCategory) object, jsonObj);
                break;
            case DATA_ITEM:
            case PROFILE_ITEM:
                parseItem((AmeeItem) object, jsonObj);
                break;
            case DRILL_DOWN:
                parseDrillDown((AmeeDrillDown) object, jsonObj);
                break;
            case PROFILE:
                parseProfile((AmeeProfile) object, jsonObj);
                break;
            case VALUE:
                parseValue((AmeeValue) object, jsonObj);
                break;
            case UNKNOWN:
            default:
                break;
        }
    }

    // AmeeCategory parsing

    protected void parseDataCategory(AmeeDataCategory dataCategory, JSONObject json) throws AmeeException {
        JSONObject dataCategoryJson;
        try {
            dataCategoryJson = json.getJSONObject("dataCategory");
            dataCategory.setUid(dataCategoryJson.getString("uid"));
            dataCategory.setName(dataCategoryJson.getString("name"));
            parseCategoryChildren(dataCategory, json.getJSONObject("children"));
            dataCategory.setFetched(true);
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
    }

    protected void parseProfileCategory(AmeeProfileCategory profileCategory, JSONObject json) throws AmeeException {
        JSONObject dataCategoryJson;
        try {
            dataCategoryJson = json.getJSONObject("dataCategory");
            profileCategory.setUid(""); // no UID for Profile Categories
            profileCategory.setName(dataCategoryJson.getString("name"));
            profileCategory.setAmountPerMonth(json.getString("totalAmountPerMonth"));
            parseCategoryChildren(profileCategory, json.getJSONObject("children"));
            profileCategory.setFetched(true);
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
    }

    protected void parseCategoryChildren(AmeeCategory category, JSONObject json) throws AmeeException {
        JSONArray categoriesArr;
        JSONObject pagerObj;
        JSONObject itemsObj;
        JSONArray itemsArr;
        JSONObject childJson;
        AmeeObjectReference ameeObjectReference;
        AmeeCategory childCategory;
        AmeeItem childItem;
        try {
            if (json.has("dataCategories")) {
                category.clearCategoryRefs();
                categoriesArr = json.getJSONArray("dataCategories");
                for (int i = 0; i < categoriesArr.length(); i++) {
                    childJson = categoriesArr.getJSONObject(i);
                    // create reference and add to parent
                    ameeObjectReference =
                            new AmeeObjectReference(
                                    category.getObjectReference().getPath() + "/" + childJson.getString("path"),
                                    category.getChildCategoryObjectType());
                    category.addCategoryRef(ameeObjectReference);
                    // look for object in cache
                    childCategory = (AmeeCategory) getObject(ameeObjectReference, false);
                    if (childCategory == null) {
                        // create object, add to cache and update
                        childCategory = category.getNewChildCategory(ameeObjectReference);
                        if (childCategory instanceof AmeeDataCategory) {
                            childCategory.setUid(childJson.getString("uid"));
                        } else if (childCategory instanceof AmeeDataCategory) {
                            childCategory.setUid(""); // no UID for Profile Categories
                        }
                        childCategory.setName(childJson.getString("name"));
                        addObjectToCache(childCategory);
                    }
                }
            }
            String s = category.getChildItemObjectType().equals(AmeeObjectType.DATA_ITEM) ? "dataItems" : "profileItems";
            if (json.has(s) && (json.get(s) instanceof JSONObject)) {
                category.clearItemRefs();
                itemsObj = json.getJSONObject(s);
                if (itemsObj.has("rows")) {
                    itemsArr = itemsObj.getJSONArray("rows");
                    for (int i = 0; i < itemsArr.length(); i++) {
                        childJson = itemsArr.getJSONObject(i);
                        // create reference and add to parent
                        ameeObjectReference =
                                new AmeeObjectReference(
                                        category.getObjectReference().getPath() + "/" + childJson.getString("path"),
                                        category.getChildItemObjectType());
                        category.addItemRef(ameeObjectReference);
                        // look for object in cache
                        childItem = (AmeeItem) getObject(ameeObjectReference, false);
                        if (childItem == null) {
                            // create object, add to cache and update
                            childItem = category.getNewChildItem(ameeObjectReference);
                            parseCategoryItemProperties(childItem, childJson);
                            addObjectToCache(childItem);
                        }
                    }
                }
            }
            if (json.has("pager")) {
                pagerObj = json.getJSONObject("pager");
                if (pagerObj.has("items") && pagerObj.has("itemsPerPage") && pagerObj.has("currentPage")) {
                    category.setPage(pagerObj.getInt("currentPage"));
                    category.setItemsPager(new Pager(pagerObj.getInt("items"), pagerObj.getInt("itemsPerPage"), pagerObj.getInt("currentPage")));
                } else {
                    category.setPage(1);
                    category.setItemsPager(new Pager(0, 10));
                }
            }
        } catch (ClassCastException e) {
            throw new AmeeException("Caught ClassCastException: " + e.getMessage());
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
    }

    protected void parseCategoryItemProperties(AmeeItem item, JSONObject json) throws AmeeException {
        AmeeObjectReference ameeObjectReference;
        AmeeValue ameeValue;
        String dataItemUri;
        AmeeProfileItem profileItem;
        AmeeDataItem dataItem;
        try {

            // set common values
            item.setUid(json.getString("uid"));

            // set implementation specific values
            if (item instanceof AmeeDataItem) {
                dataItem = (AmeeDataItem) item;
                dataItem.setLabel(json.getString("label"));
            } else if (item instanceof AmeeProfileItem) {
                profileItem = (AmeeProfileItem) item;
                profileItem.setName(json.getString("name"));
                profileItem.setLabel(json.getString("dataItemLabel"));
                profileItem.setAmountPerMonth(json.getString("amountPerMonth"));
                profileItem.setEnd(json.getString("end"));
                dataItemUri = "data/" + UriUtils.getUriExceptFirstTwoParts(profileItem.getParentRef().getPath()) + "/" + json.getString("dataItemUid");
                profileItem.setDataItemRef(
                        new AmeeObjectReference(
                                dataItemUri,
                                AmeeObjectType.DATA_ITEM));
            }
            // TODO: some values in excludes list need to be set
            // parse values
            // iterate over all keys but only work with keys not in excludes list
            item.clearValueRefs();
            Iterator i = json.keys();
            while (i.hasNext()) {
                String path = (String) i.next();
                if (!ITEM_EXCLUDES.contains(path)) {
                    // create reference and add to parent
                    ameeObjectReference =
                            new AmeeObjectReference(
                                    item.getUri() + "/" + path,
                                    AmeeObjectType.VALUE);
                    item.addValueRef(ameeObjectReference);
                    // look for object in cache
                    ameeValue = (AmeeValue) getObject(ameeObjectReference, false);
                    if (ameeValue == null) {
                        // create object and cache
                        ameeValue = new AmeeValue(ameeObjectReference);
                        addObjectToCache(ameeValue);
                    }
                    // update object
                    ameeValue.setName(path);
                    ameeValue.setValue(json.getString(path));
                }
            }
        } catch (ClassCastException e) {
            throw new AmeeException("Caught ClassCastException: " + e.getMessage());
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
    }

    // AmeeItem

    protected void parseItem(AmeeItem item, JSONObject json) throws AmeeException {
        JSONObject itemObj;
        AmeeProfileItem profileItem;
        AmeeDataItem dataItem;
        try {
            if (item instanceof AmeeProfileItem) {
                itemObj = json.getJSONObject("profileItem");
                profileItem = (AmeeProfileItem) item;
                profileItem.setEnd(itemObj.getString("end"));
                profileItem.setAmountPerMonth(itemObj.getString("amountPerMonth"));
                profileItem.setValidFrom(itemObj.getString("validFrom"));
                profileItem.setLabel(""); // TODO: label is missing from API
            } else {
                itemObj = json.getJSONObject("dataItem");
                dataItem = (AmeeDataItem) item;
                dataItem.setLabel(itemObj.getString("label"));
            }
            parseItemValues(item, itemObj.getJSONArray("itemValues"));
            item.setUid(itemObj.getString("uid"));
            item.setName(itemObj.getString("name"));
            item.setFetched(true);
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
    }

    protected void parseItemValues(AmeeItem item, JSONArray valuesArr) throws AmeeException {
        JSONObject valueObj;
        AmeeObjectReference ameeObjectReference;
        AmeeValue value;
        try {
            item.clearValueRefs();
            for (int i = 0; i < valuesArr.length(); i++) {
                valueObj = valuesArr.getJSONObject(i);
                // create reference and add to parent
                ameeObjectReference =
                        new AmeeObjectReference(
                                item.getUri() + "/" + valueObj.getString("path"),
                                AmeeObjectType.VALUE);
                item.addValueRef(ameeObjectReference);
                // look for object in cache
                value = (AmeeValue) getObject(ameeObjectReference, false);
                if (value == null) {
                    // create object and cache
                    value = new AmeeValue(ameeObjectReference);
                    addObjectToCache(value);
                }
                // update object
                value.setUid(valueObj.getString("uid"));
                value.setName(valueObj.getString("name"));
                value.setValue(valueObj.getString("value"));
                value.setItemRef(item.getObjectReference());
                value.setFetched(true);
            }
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
    }

    protected void deleteProfileItem(AmeeProfileItem profileItem) throws AmeeException {
        // delete the resource
        AmeeInterface.getInstance().deleteAmeeResource(profileItem.getUri());
        // invalidate this
        invalidate(profileItem);
        // invalidate parent (if it exists)
        getCache().remove(profileItem.getParentUri());
        // mark as not fetched
        profileItem.setFetched(false);
    }

    // AmeeProfile

    protected void parseProfile(AmeeProfile profile, JSONObject json) throws AmeeException {
        JSONObject profileJson;
        try {
            profileJson = json.getJSONObject("profile");
            profile.setUid(profileJson.getString("uid"));
            profile.setName(profileJson.getString("name"));
            // TODO: AmountPerMonth at some point
            parseCategoryChildren(profile, json.getJSONObject("children"));
            profile.setFetched(true);
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
    }

    protected void deleteProfile(AmeeProfile profile) throws AmeeException {
        // delete the resource
        AmeeInterface.getInstance().deleteAmeeResource(profile.getUri());
        // invalidate this
        invalidate(profile);
        // mark as not fetched
        profile.setFetched(false);
    }

    // AmeeValue

    protected void parseValue(AmeeValue value, JSONObject json) throws AmeeException {
        JSONObject valueObj;
        try {
            valueObj = json.getJSONObject("itemValue");
            value.setUid(valueObj.getString("uid"));
            value.setName(valueObj.getString("name"));
            value.setValue(valueObj.getString("value"));
            if (json.has("profile")) {
                value.setItemRef(new AmeeObjectReference(value.getParentUri(), AmeeObjectType.PROFILE_ITEM));
            } else {
                value.setItemRef(new AmeeObjectReference(value.getParentUri(), AmeeObjectType.DATA_ITEM));
            }
            value.setFetched(true);
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
    }

    protected void saveValue(AmeeValue value) throws AmeeException {
        try {
            // setup and make request
            List<Choice> parameters = new ArrayList<Choice>();
            parameters.add(new Choice("value", value.getValue()));
            String response = AmeeInterface.getInstance().getAmeeResourceByPut(value.getUri(), parameters);
            // parse response into AmeeValue and update cache entry
            parseValue(value, new JSONObject(response));
            addObjectToCache(value, true);
            // invalidate parent Profile Item, if it exists
            getCache().remove(value.getItemRef().getUri());
            // invalidate parent Profile Catagory, if it exists
            getCache().remove(value.getItemRef().getParentUri());
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
    }

    // AmeeDrillDown parsing

    public void parseDrillDown(AmeeDrillDown drillDown, JSONObject object) throws AmeeException {
        try {
            // load choice info
            parseDrillDownChoiceInfo(drillDown, object);
            // load selection info
            parseDrillDownSelectionInfo(drillDown, object);
        } catch (JSONException e) {
            throw new AmeeException("Caught JSONException: " + e.getMessage());
        }
        drillDown.setFetched(true);
    }

    private void parseDrillDownChoiceInfo(AmeeDrillDown drillDown, JSONObject object) throws JSONException {
        JSONObject choice;
        JSONObject choicesWrapper = object.getJSONObject("choices");
        drillDown.setChoiceName(choicesWrapper.getString("name"));
        JSONArray choices = choicesWrapper.getJSONArray("choices");
        drillDown.clearChoices();
        for (int i = 0; i < choices.length(); i++) {
            choice = choices.getJSONObject(i);
            drillDown.addChoice(choice.getString("name"), choice.getString("value"));
        }
    }

    private void parseDrillDownSelectionInfo(AmeeDrillDown drillDown, JSONObject object) throws JSONException {
        JSONArray selections = object.getJSONArray("selections");
        drillDown.clearSelections();
        for (int i = 0; i < selections.length(); i++) {
            JSONObject selectionObj = selections.getJSONObject(i);
            drillDown.addSelection(selectionObj.getString("name"), selectionObj.getString("value"));
        }
    }

    // util

    protected void invalidate(AmeeCategory category) throws AmeeException {
        AmeeObjectCacheEntry aoce;
        getCache().remove(category.getUri());
        for (AmeeObjectReference ref : category.getCategoryRefs(false)) {
            aoce = getCache().get(ref.getUri());
            if (aoce != null) {
                invalidate((AmeeCategory) aoce.getObject());
            }
        }
        for (AmeeObjectReference ref : category.getItemRefs(false)) {
            aoce = getCache().get(ref.getUri());
            if (aoce != null) {
                invalidate((AmeeItem) aoce.getObject());
            }
        }
    }

    protected void invalidate(AmeeItem item) throws AmeeException {
        getCache().remove(item.getUri());
        for (AmeeObjectReference ref : item.getValueRefs(false)) {
            getCache().remove(ref.getUri());
        }
    }

    // local properties

    public AmeeObjectCache getCache() {
        return cache;
    }

    /**
     * Set the AmeeObjectCache implementation.
     * Supply null to use the dummy no-op implementation (AmeeObjectCacheDummyImpl).
     *
     * @param cache an AmeeObjectCache implementation
     */
    public void setCache(AmeeObjectCache cache) {
        if (cache == null) {
            cache = new AmeeObjectCacheDummyImpl();
        }
        this.cache = cache;
    }
}