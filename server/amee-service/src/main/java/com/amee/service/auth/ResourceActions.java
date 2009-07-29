package com.amee.service.auth;

import com.amee.domain.AMEEEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

public class ResourceActions implements Serializable {

    public final static String ACTION_VIEW = ".view";
    public final static String ACTION_CREATE = ".create";
    public final static String ACTION_MODIFY = ".modify";
    public final static String ACTION_DELETE = ".delete";
    public final static String ACTION_LIST = ".list";

    @Autowired
    private AuthService authService;

    private Boolean view = null;
    private Boolean create = null;
    private Boolean modify = null;
    private Boolean delete = null;
    private Boolean list = null;

    private String resourceViewAction;
    private String resourceCreateAction;
    private String resourceModifyAction;
    private String resourceDeleteAction;
    private String resourceListAction;

    public ResourceActions() {
        super();
    }

    public ResourceActions(String resource) {
        this();
        setResource(resource);
    }

    public void setResource(String resource) {
        this.resourceViewAction = resource + ACTION_VIEW;
        this.resourceCreateAction = resource + ACTION_CREATE;
        this.resourceModifyAction = resource + ACTION_MODIFY;
        this.resourceDeleteAction = resource + ACTION_DELETE;
        this.resourceListAction = resource + ACTION_LIST;
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("allowList", isAllowList());
        obj.put("allowView", isAllowView());
        obj.put("allowCreate", isAllowCreate());
        obj.put("allowModify", isAllowModify());
        obj.put("allowDelete", isAllowDelete());
        return obj;
    }

    public boolean isAllowView(AMEEEntity entity) {
        if (view == null) {
            view = authService.isSuperUser() ||
                    (entity == null || !entity.isDeprecated()) &&
                    (authService.hasActions(resourceViewAction) &&
                            authService.isAllowView());
        }
        return view;
    }

    public boolean isAllowView() {
        return isAllowView(null);
    }

    public boolean isAllowCreate(AMEEEntity entity) {
        if (create == null) {
            create = authService.isSuperUser() ||
                    (entity == null || !entity.isDeprecated()) &&
                    (authService.hasActions(resourceCreateAction) &&
                            authService.isAllowModify());
        }
        return create;
    }

    public boolean isAllowCreate() {
        return isAllowCreate(null);
    }

    public boolean isAllowModify(AMEEEntity entity) {
        if (modify == null) {
            modify = authService.isSuperUser() ||
                    (entity == null || !entity.isDeprecated()) &&
                    (authService.hasActions(resourceModifyAction) &&
                            authService.isAllowModify());
        }
        return modify;
    }

    public boolean isAllowModify() {
        return isAllowModify(null);
    }

    public boolean isAllowDelete() {
        if (delete == null) {
            delete = authService.isSuperUser() ||
                    (authService.hasActions(resourceDeleteAction) &&
                            authService.isAllowModify());
        }
        return delete;
    }

    public boolean isAllowList() {
        if (list == null) {
            list = authService.isSuperUser() || (
                    authService.hasActions(resourceListAction) &&
                            authService.isAllowView());
        }
        return list;
    }
}
