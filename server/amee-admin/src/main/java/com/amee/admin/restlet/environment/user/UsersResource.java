package com.amee.admin.restlet.environment.user;

import com.amee.admin.restlet.environment.AdminBrowser;
import com.amee.domain.AMEEEntity;
import com.amee.domain.LocaleConstants;
import com.amee.domain.Pager;
import com.amee.domain.auth.Group;
import com.amee.domain.auth.GroupPrincipal;
import com.amee.domain.auth.User;
import com.amee.domain.auth.UserType;
import com.amee.restlet.AuthorizeResource;
import com.amee.restlet.utils.APIFault;
import com.amee.service.environment.AdminConstants;
import com.amee.service.environment.SiteService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Component
@Scope("prototype")
public class UsersResource extends AuthorizeResource implements Serializable {

    @Autowired
    private SiteService siteService;

    @Autowired
    private AdminBrowser adminBrowser;

    private User newUser;
    private String search;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        search = request.getResourceRef().getQueryAsForm().getFirstValue("search");
        if (search == null) {
            search = "";
        }
    }

    @Override
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(getRootDataCategory());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return AdminConstants.VIEW_USERS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager();
        List<User> users = siteService.getUsers(pager, search);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", adminBrowser);
        values.put("users", users);
        values.put("pager", pager);
        values.put("apiVersions", adminBrowser.getApiVersions());
        values.put("availableLocales", LocaleConstants.AVAILABLE_LOCALES.keySet());
        values.put("search", search);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager();
            List<User> users = siteService.getUsers(pager, search);
            pager.setCurrentPage(getPage());
            JSONArray usersArr = new JSONArray();
            for (User user : users) {
                usersArr.put(user.getJSONObject());
            }
            obj.put("users", usersArr);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("user", newUser.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("UsersResource");
        if (isGet()) {
            Pager pager = getPager();
            List<User> users = siteService.getUsers(pager, search);
            pager.setCurrentPage(getPage());
            Element usersElement = document.createElement("Users");
            for (User user : users) {
                usersElement.appendChild(user.getElement(document));
            }
            element.appendChild(usersElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newUser.getElement(document));
        }
        return element;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    // TODO: prevent duplicate instances
    @Override
    public void doAccept(Representation entity) {
        User cloneUser;
        String groupNames;
        Group group;
        GroupPrincipal newGroupPrincipal;
        Form form = getForm();
        // create new instance if submitted
        if (form.getFirstValue("name") != null) {
            if (siteService.getUserByUsername(form.getFirstValue("username")) == null) {
                // create new instance
                newUser = new User();
                newUser.setName(form.getFirstValue("name"));
                newUser.setUsername(form.getFirstValue("username"));
                newUser.setPasswordInClear(form.getFirstValue("password"));
                newUser.setEmail(form.getFirstValue("email"));
                if (form.getFirstValue("superUser") != null) {
                    newUser.setType(UserType.SUPER);
                } else {
                    newUser.setType(UserType.STANDARD);
                }
                if (form.getNames().contains("locale")) {
                    String locale = form.getFirstValue("locale");
                    if (LocaleConstants.AVAILABLE_LOCALES.containsKey(locale)) {
                        newUser.setLocale(locale);
                    }
                }
                if (form.getNames().contains("timeZone")) {
                    TimeZone timeZone = TimeZone.getTimeZone(form.getFirstValue("timeZone"));
                    newUser.setTimeZone(timeZone);
                }
                newUser.setAPIVersion(adminBrowser.getApiVersion(form.getFirstValue("apiVersion")));
                if (newUser.getAPIVersion() != null) {
                    siteService.save(newUser);
                    // We can either 'clone' Group membership from an existing User *OR* join specified Groups.
                    // Was a clone User supplied?
                    cloneUser = siteService.getUserByUid(form.getFirstValue("cloneUserUid"));
                    if (cloneUser != null) {
                        // Clone User was supplied.
                        // Clone Group memberships.
                        for (GroupPrincipal groupPrincipal : groupService.getGroupPrincipalsForPrincipal(cloneUser)) {
                            newGroupPrincipal = new GroupPrincipal(groupPrincipal.getGroup(), newUser);
                            groupService.save(newGroupPrincipal);
                        }
                    } else {
                        // Clone User was NOT supplied.
                        // Look for requested Groups to join.
                        if (form.getNames().contains("groups") && !(form.getFirstValue("groups") == null)) {
                            groupNames = form.getFirstValue("groups");
                            for (String groupName : groupNames.split(",")) {
                                groupName = groupName.trim();
                                group = groupService.getGroupByName(groupName);
                                if (group != null) {
                                    newGroupPrincipal = new GroupPrincipal(group, newUser);
                                    groupService.save(newGroupPrincipal);
                                } else {
                                    log.warn("Unable to find requested Group: '" + groupName + "'");
                                    badRequest(APIFault.INVALID_PARAMETERS);
                                    newUser = null;
                                }
                            }
                        }
                    }
                } else {
                    log.warn("Unable to find api version '" + form.getFirstValue("apiVersion") + "'");
                    badRequest(APIFault.INVALID_PARAMETERS);
                    newUser = null;
                }
            } else {
                badRequest(APIFault.DUPLICATE_ITEM);
                newUser = null;
            }
        }
        if (newUser != null) {
            if (isStandardWebBrowser()) {
                success();
            } else {
                // return a response for API calls
                super.handleGet();
            }
        }
    }
}