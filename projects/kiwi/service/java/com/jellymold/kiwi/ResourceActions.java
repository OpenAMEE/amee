package com.jellymold.kiwi;

import com.jellymold.kiwi.auth.AuthService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

public class ResourceActions implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    public final static String ACTION_VIEW = ".view";
    public final static String ACTION_CREATE = ".create";
    public final static String ACTION_MODIFY = ".modify";
    public final static String ACTION_DELETE = ".delete";
    public final static String ACTION_LIST = ".list";

    AuthService authService;

    Boolean view = null;
    Boolean create = null;
    Boolean modify = null;
    Boolean delete = null;
    Boolean list = null;

    String resourceViewAction;
    String resourceCreateAction;
    String resourceModifyAction;
    String resourceDeleteAction;
    String resourceListAction;

    public ResourceActions(String resource) {
        this.resourceViewAction = resource + ACTION_VIEW;
        this.resourceCreateAction = resource + ACTION_CREATE;
        this.resourceModifyAction = resource + ACTION_MODIFY;
        this.resourceDeleteAction = resource + ACTION_DELETE;
        this.resourceListAction = resource + ACTION_LIST;

        if (authService == null) {
            // TODO: SPRINGIFY
            // authService = (AuthService) Component.getInstance("authService", true);
        }
    }

    public boolean isAllowView() {
        if (view == null) {
            view = authService.isSuperUser() || (
                    authService.hasActions(resourceViewAction) &&
                            authService.isAllowView());
        }
        return view;
    }

    public boolean isAllowCreate() {
        if (create == null) {
            create = authService.isSuperUser() ||
                    (authService.hasActions(resourceCreateAction) &&
                            authService.isAllowModify());
        }
        return create;
    }

    public boolean isAllowModify() {
        if (modify == null) {
            modify = authService.isSuperUser() ||
                    (authService.hasActions(resourceModifyAction) &&
                            authService.isAllowModify());
        }
        return modify;
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
