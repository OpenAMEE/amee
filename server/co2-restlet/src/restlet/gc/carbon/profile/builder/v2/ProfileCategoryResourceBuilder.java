package gc.carbon.profile.builder.v2;

import com.jellymold.utils.Pager;
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.builder.ResourceBuilder;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.ItemDefinition;
import gc.carbon.domain.data.Decimal;
import gc.carbon.domain.data.CO2AmountUnit;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.profile.builder.v2.ProfileItemBuilder;
import gc.carbon.profile.*;
import org.apache.abdera.ext.history.FeedPagingHelper;
import org.apache.abdera.ext.opensearch.OpenSearchExtensionFactory;
import org.apache.abdera.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.util.*;

/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */

public class ProfileCategoryResourceBuilder implements ResourceBuilder {

    private final Log log = LogFactory.getLog(getClass());

    private ProfileService profileService;

    ProfileCategoryResource resource;

    public ProfileCategoryResourceBuilder(ProfileCategoryResource resource) {
        this.resource = resource;
        this.profileService = resource.getProfileService();
    }

    public JSONObject getJSONObject() throws JSONException {

        JSONObject obj = new JSONObject();

        // add objects
        obj.put("path", resource.getFullPath());

        // add relevant Profile info depending on whether we are at root
        if (resource.hasParent()) {
            obj.put("profile", resource.getProfile().getIdentityJSONObject());
        } else {
            obj.put("profile", resource.getProfile().getJSONObject());
        }

        // add environment
        obj.put("environment", resource.getEnvironment().getJSONObject(true));

        // add Data Category
        obj.put("dataCategory", resource.getDataCategory().getJSONObject(true));

        // add Data Categories via pathItem to children
        JSONArray dataCategories = new JSONArray();
        for (PathItem pi : resource.getChildrenByType("DC")) {
            dataCategories.put(pi.getJSONObject());
        }
        obj.put("profileCategories", dataCategories);

        // profile items
        List<ProfileItem> profileItems;
        Pager pager = null;

        if (resource.isGet()) {
            // get profile items
            profileItems = getProfileItems();

            // set-up pager
            pager = resource.getPager();
            profileItems = pageResults(profileItems, pager);
        } else {
            profileItems = resource.getProfileItems();
        }

        if (!profileItems.isEmpty()) {
            JSONArray jsonProfileItems = new JSONArray();
            obj.put("profileItems", jsonProfileItems);
            for (ProfileItem pi : profileItems) {
                setBuilder(pi);
                jsonProfileItems.put(pi.getJSONObject(false));
            }

            // pager
            if (pager != null) {
                obj.put("pager", pager.getJSONObject());
            }

            // add CO2 amount
            JSONObject totalAmount = new JSONObject();
            totalAmount.put("value", getTotalAmount(profileItems).toString());
            totalAmount.put("unit", resource.getProfileBrowser().getCo2AmountUnit());
            obj.put("totalAmount", totalAmount);

        } else if (resource.isPost() || resource.isPut()) {

            if (!profileItems.isEmpty()) {
                JSONArray profileItemsJSonn = new JSONArray();
                obj.put("profileItems", profileItems);
                for (ProfileItem pi : profileItems) {
                    setBuilder(pi);
                    profileItemsJSonn.put(pi.getJSONObject(false));
                }
            }

        } else {
            obj.put("profileItems", new JSONObject());
            obj.put("pager", new JSONObject());
            obj.put("totalAmount", "0");
        }

        return obj;
    }

    public Element getElement(Document document) {

        // create element
        Element element = document.createElement("ProfileCategoryResource");

        element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));

        // add relevant Profile info depending on whether we are at root
        if (resource.hasParent()) {
            element.appendChild(resource.getProfile().getIdentityElement(document));
        } else {
            element.appendChild(resource.getProfile().getElement(document));
        }

        // add environment
        element.appendChild(resource.getEnvironment().getElement(document, true));
        
        // add DataCategory
        element.appendChild(resource.getDataCategory().getIdentityElement(document));

        // add Data Categories via pathItem to children
        Element dataCategoriesElement = document.createElement("ProfileCategories");
        for (PathItem dc : resource.getChildrenByType("DC")) {
            dataCategoriesElement.appendChild(dc.getElement(document));
        }
        element.appendChild(dataCategoriesElement);

        // profile items
        List<ProfileItem> profileItems;
        Pager pager = null;

        if (resource.isGet()) {
            // get profile items
            profileItems = getProfileItems();

            // set-up pager
            pager = resource.getPager();
            profileItems = pageResults(profileItems, pager);
        } else {
            profileItems = resource.getProfileItems();
        }

        if (!profileItems.isEmpty()) {

            Element profileItemsElement = document.createElement("ProfileItems");
            element.appendChild(profileItemsElement);
            for (ProfileItem pi : profileItems) {
                setBuilder(pi);
                profileItemsElement.appendChild(pi.getElement(document, false));
            }

            // pager
            if (pager != null) {
                element.appendChild(pager.getElement(document));
            }

            // add CO2 amount
            Element totalAmount = APIUtils.getElement(document,
                    "TotalAmount",
                    getTotalAmount(profileItems).toString());
            totalAmount.setAttribute("unit", resource.getProfileBrowser().getCo2AmountUnit().toString());
            element.appendChild(totalAmount);

        }
        return element;
    }

    private List<ProfileItem> pageResults(List<ProfileItem> profileItems, Pager pager) {
        // set-up pager
        if (!(profileItems == null || profileItems.isEmpty())) {
            pager.setCurrentPage(resource.getPage());
            pager.setItems(profileItems.size());
            pager.goRequestedPage();

            // limit results
            profileItems = profileItems.subList((int) pager.getStart(), (int) pager.getTo());

            pager.setItemsFound(profileItems.size());
        }
        return profileItems;
    }

    private List<ProfileItem> getProfileItems() {
        ItemDefinition itemDefinition;
        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();

        // must have ItemDefinition
        itemDefinition = resource.getDataCategory().getItemDefinition();
        if (itemDefinition != null) {

            ProfileService decoratedProfileServiceDAO = new OnlyActiveProfileService(profileService);

            if (resource.getProfileBrowser().isProRataRequest()) {
                decoratedProfileServiceDAO = new ProRataProfileService(profileService);
            }

            if (resource.getProfileBrowser().isSelectByRequest()) {
                decoratedProfileServiceDAO = new SelectByProfileService(decoratedProfileServiceDAO, resource.getProfileBrowser().getSelectBy());
            }

            ProfileBrowser browser = resource.getProfileBrowser();
            profileItems = decoratedProfileServiceDAO.getProfileItems(resource.getProfile(), resource.getDataCategory(), 
                    browser.getStartDate(), browser.getEndDate());
        }
        return profileItems;
    }

    private BigDecimal getTotalAmount(List<ProfileItem> profileItems) {
        BigDecimal totalAmount = Decimal.ZERO;
        BigDecimal amount;
        CO2AmountUnit returnUnit = resource.getProfileBrowser().getCo2AmountUnit();
        for (ProfileItem profileItem : profileItems) {
            amount = profileItem.getAmount().convert(returnUnit).getValue();
            totalAmount = totalAmount.add(amount);
        }
        return totalAmount;
    }

    private void setBuilder(ProfileItem pi) {
        if (resource.getProfileBrowser().requestedCO2InExternalUnit()) {
            pi.setBuilder(new ProfileItemBuilder(pi, resource.getProfileBrowser().getCo2AmountUnit()));
        } else {
            pi.setBuilder(new ProfileItemBuilder(pi));
        }
    }

    public Map<String, Object> getTemplateValues() {
        Profile profile = resource.getProfile();
        DataCategory dataCategory = resource.getDataCategory();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getProfileBrowser());
        values.put("profile", profile);
        values.put("dataCategory", dataCategory);
        return values;
    }

    public org.apache.abdera.model.Element getAtomElement() {
        if (resource.isGet()) {
            return getAtomElementForGet();
        } else {
            return getAtomElementForPost();
        }
    }

    // Generate the Atom feed in response to a GET request to a ProfileCategory.
    // The request may contains query (search) parameters which may constrain and modify the returned ProfileItems.
    private org.apache.abdera.model.Element getAtomElementForGet() {

        AtomFeed atomFeed = AtomFeed.getInstance();

        final Feed feed = atomFeed.newFeed();
        feed.setBaseUri(resource.getRequest().getAttributes().get("previousHierachicalPart").toString());
        feed.setTitle("Profile " + resource.getProfile().getDisplayName() + ", Category " + resource.getDataCategory().getName());

        atomFeed.newID(feed).setText("urn:dataCategory:" + resource.getDataCategory().getUid());

        atomFeed.addGenerator(feed, resource.getAPIVersion());

        atomFeed.addLinks(feed, "");

        Person author = atomFeed.newAuthor(feed);
        author.setName(resource.getProfile().getDisplayPath());

        Date epoch = new Date(0);
        Date lastModified = epoch;

        List<ProfileItem> profileItems = getProfileItems();

        atomFeed.addName(feed, resource.getDataCategory().getName());
        atomFeed.addTotalAmount(feed, getTotalAmount(profileItems).toString(), resource.getProfileBrowser().getCo2AmountUnit().toString());

        Pager pager = resource.getPager();
        int numOfProfileItems = profileItems.size();
        if (numOfProfileItems > pager.getItemsPerPage()) {
            profileItems = pageResults(profileItems, pager);
            FeedPagingHelper.setNext(feed, feed.getBaseUri() + "?page=" + pager.getNextPage());
            if (pager.getCurrentPage() != 1) {
                FeedPagingHelper.setPrevious(feed, feed.getBaseUri() + "?page=" + pager.getPreviousPage());
            }
            FeedPagingHelper.setFirst(feed, feed.getBaseUri().toString());
            FeedPagingHelper.setLast(feed, feed.getBaseUri() + "?page=" + pager.getLastPage());
        }

        // If the GET contained query (search) parameters, add OpenSearch elements describing the query and the results.
        if (resource.getProfileBrowser().isQuery())  {

            if (numOfProfileItems > pager.getItemsPerPage()) {
                feed.addExtension(OpenSearchExtensionFactory.ITEMS_PER_PAGE).setText("" + pager.getItemsPerPage());
                feed.addExtension(OpenSearchExtensionFactory.START_INDEX).setText("1");
                feed.addExtension(OpenSearchExtensionFactory.TOTAL_RESULTS).setText(""+ pager.getItems());
            }
            org.apache.abdera.model.Element query = feed.addExtension(OpenSearchExtensionFactory.QUERY);
            query.setAttributeValue("role","request");
            query.setAttributeValue(AtomFeed.Q_NAME_START_DATE,resource.getProfileBrowser().getStartDate().toString());
            if (resource.getProfileBrowser().getEndDate() != null) {
                query.setAttributeValue(AtomFeed.Q_NAME_END_DATE,resource.getProfileBrowser().getEndDate().toString());
            }
        }

        
        atomFeed.addChildCategories(feed, resource);

        CO2AmountUnit returnUnit = resource.getProfileBrowser().getCo2AmountUnit();

        // Add all ProfileItems as Entries in the Atom feed.

        //Iterate over in reverse order (i.e. most recent first). Abdera Feed sorting methods do not seem to have
        // any meaningful effect (SM - 18/02/2009)
        Collections.reverse(profileItems);
        for(ProfileItem profileItem : profileItems) {

            String amount = profileItem.getAmount().convert(returnUnit).toString();

            Entry entry = feed.addEntry();

            Text title = atomFeed.newTitle(entry);
            title.setText(profileItem.getDisplayName());
            Text subtitle = atomFeed.newSubtitle(entry);
            subtitle.setText(atomFeed.format(profileItem.getStartDate()) + ((profileItem.getEndDate() != null) ? " - " + atomFeed.format(profileItem.getEndDate()) : ""));

            atomFeed.addLinks(entry, profileItem.getUid());

            IRIElement eid = atomFeed.newID(entry);
            eid.setText("urn:item:" + profileItem.getUid());

            entry.setPublished(profileItem.getStartDate());
            entry.setUpdated(profileItem.getStartDate());

            atomFeed.addDataItem(entry, profileItem.getDataItem());

            atomFeed.addStartDate(entry, profileItem.getStartDate().toString());

            if (profileItem.getEndDate() != null) {
                atomFeed.addEndDate(entry, profileItem.getEndDate().toString());
            }

            atomFeed.addAmount(entry, amount, returnUnit.toString());

            atomFeed.addItemValuesWithLinks(entry, profileItem.getItemValues(), profileItem.getUid());

            HCalendar content = new HCalendar();

            content.addSummary(amount + " " + returnUnit.toString());
            content.addStartDate(profileItem.getStartDate());

            if (profileItem.getEndDate() != null) {
                content.addEndDate(profileItem.getEndDate());
            }

            if (profileItem.getName() != null) {
                atomFeed.addName(entry, profileItem.getName());
            }

            entry.setContentAsHtml(content.toString());

            Category cat = atomFeed.newItemCategory(entry);
            cat.setTerm(profileItem.getDataItem().getUid());

            cat.setLabel(profileItem.getDataItem().getItemDefinition().getName());
            if (profileItem.getModified().after(lastModified))
                lastModified = profileItem.getModified();
        }

        // If there are no ProfileItems in this feed, the lastModified date will be Date(0). In this case,
        // displaying the current Date is probably most sensible.
        if (lastModified.equals(epoch)) {
            feed.setUpdated(new Date());
        } else {
            feed.setUpdated(lastModified);
        }

        return feed;
    }

    // Generate the Atom feed in reponse to a POST to a ProfileCategory.
    // The feed will contain a single Atom Entry representing the new ProfileItem.
    private org.apache.abdera.model.Element getAtomElementForPost() {

        AtomFeed atomFeed = AtomFeed.getInstance();

        //TODO - Add batch support
        ProfileItem profileItem = resource.getProfileItems().get(0);

        CO2AmountUnit returnUnit = resource.getProfileBrowser().getCo2AmountUnit();
        String amount = profileItem.getAmount().convert(returnUnit).toString();

        Entry entry = atomFeed.newEntry();
        entry.setBaseUri(resource.getRequest().getAttributes().get("previousHierachicalPart").toString());

        Text title = atomFeed.newTitle(entry);
        title.setText(profileItem.getDisplayName());
        Text subtitle = atomFeed.newSubtitle(entry);
        subtitle.setText(atomFeed.format(profileItem.getStartDate()) + ((profileItem.getEndDate() != null) ? " - " + atomFeed.format(profileItem.getEndDate()) : ""));

        atomFeed.addLinks(entry, profileItem.getUid());

        IRIElement eid = atomFeed.newID(entry);
        eid.setText("urn:item:" + profileItem.getUid());

        entry.setPublished(profileItem.getStartDate());
        entry.setUpdated(profileItem.getStartDate());

        HCalendar content = new HCalendar();

        atomFeed.addAmount(entry, amount, returnUnit.toString());

        content.addSummary(profileItem.getAmount().convert(returnUnit) + " " + returnUnit.toString());
        content.addStartDate(profileItem.getStartDate());
        if (profileItem.getEndDate() != null) {
            content.addEndDate(profileItem.getEndDate());
        }
        entry.setContentAsHtml(content.toString());

        atomFeed.addStartDate(entry, profileItem.getStartDate().toString());

        if (profileItem.getEndDate() != null) {
            atomFeed.addEndDate(entry, profileItem.getEndDate().toString());
        }

        if (profileItem.getName() != null) {
            atomFeed.addName(entry, profileItem.getName());
        }

        atomFeed.addAmount(entry, amount, returnUnit.toString());

        atomFeed.addItemValuesWithLinks(entry, profileItem.getItemValues(), profileItem.getUid());

        Category cat = atomFeed.newItemCategory(entry);
        cat.setTerm(profileItem.getDataItem().getUid());
        cat.setLabel(profileItem.getDataItem().getItemDefinition().getName());

        return entry;
    }
}

