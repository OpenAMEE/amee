package com.jellymold.kiwi.environment.user;

import com.jellymold.kiwi.*;
import com.jellymold.kiwi.environment.EnvironmentBrowser;
import com.jellymold.kiwi.environment.EnvironmentConstants;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.kiwi.environment.EnvironmentService;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
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
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class UsersResource extends BaseResource implements Serializable {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    private User newUser;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        setPage(request);
        setAvailable(isValid());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_USERS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        List<User> users = siteService.getUsers(environmentBrowser.getEnvironment(), pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("users", users);
        values.put("pager", pager);
        values.put("apiVersions", environmentBrowser.getApiVersions());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<User> users = siteService.getUsers(environmentBrowser.getEnvironment(), pager);
            pager.setCurrentPage(getPage());
            obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
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
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<User> users = siteService.getUsers(environmentBrowser.getEnvironment(), pager);
            pager.setCurrentPage(getPage());
            element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
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
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getUserActions().isAllowList()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    // TODO: prevent duplicate instances
    @Override
    public void acceptRepresentation(Representation entity) {
        User cloneUser;
        GroupUser newGroupUser;
        log.debug("post");
        if (environmentBrowser.getUserActions().isAllowCreate()) {
            Form form = getForm();
            // create new instance if submitted
            if (form.getFirstValue("name") != null) {
                if (siteService.getUserByUsername(environmentBrowser.getEnvironment(), form.getFirstValue("username")) == null) {
                    // create new instance
                    newUser = new User(environmentBrowser.getEnvironment());
                    newUser.setName(form.getFirstValue("name"));
                    newUser.setUsername(form.getFirstValue("username"));
                    newUser.setPasswordInClear(form.getFirstValue("password"));
                    newUser.setEmail(form.getFirstValue("email"));
                    if (form.getFirstValue("superUser") != null) {
                        newUser.setType(UserType.SUPER);
                    } else {
                        newUser.setType(UserType.STANDARD);
                    }
                    newUser.setApiVersion(environmentBrowser.getApiVersion(form.getFirstValue("apiVersion")));
                    if (newUser.getApiVersion() != null) {
                        siteService.save(newUser);
                        // now clone user -> group memberships
                        cloneUser = siteService.getUserByUid(
                                environmentBrowser.getEnvironment(), form.getFirstValue("cloneUserUid"));
                        if (cloneUser != null) {
                            for (GroupUser groupUser : siteService.getGroupUsers(cloneUser)) {
                                newGroupUser = new GroupUser(groupUser.getGroup(), newUser);
                                for (Role role : groupUser.getRoles()) {
                                    newGroupUser.addRole(role);
                                }
                                siteService.save(newGroupUser);
                            }
                        }
                    } else {
                        log.error("Unable to find api version '" + form.getFirstValue("apiVersion") + "'");
                        newUser = null;
                    }
                } else {
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
            } else {
                badRequest();
            }
        } else {
            notAuthorized();
        }
    }
}