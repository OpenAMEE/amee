package com.jellymold.kiwi;

import com.jellymold.kiwi.auth.AuthService;
import com.jellymold.utils.ThreadBeanHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;

public class ResourceActions implements Serializable {

    public final static String ACTION_VIEW = ".view";
    public final static String ACTION_CREATE = ".create";
    public final static String ACTION_MODIFY = ".modify";
    public final static String ACTION_DELETE = ".delete";
    public final static String ACTION_LIST = ".list";

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

    public boolean isAllowView() {
        if (view == null) {
            ApplicationContext springContext = (ApplicationContext) ThreadBeanHolder.get("springContext");
            AuthService authService = (AuthService) springContext.getBean("authService");
            view = authService.isSuperUser() || (
                    authService.hasActions(resourceViewAction) &&
                            authService.isAllowView());
        }
        return view;
    }

    public boolean isAllowCreate() {
        if (create == null) {
            ApplicationContext springContext = (ApplicationContext) ThreadBeanHolder.get("springContext");
            AuthService authService = (AuthService) springContext.getBean("authService");
            create = authService.isSuperUser() ||
                    (authService.hasActions(resourceCreateAction) &&
                            authService.isAllowModify());
        }
        return create;
    }

    public boolean isAllowModify() {
        if (modify == null) {
            ApplicationContext springContext = (ApplicationContext) ThreadBeanHolder.get("springContext");
            AuthService authService = (AuthService) springContext.getBean("authService");
            modify = authService.isSuperUser() ||
                    (authService.hasActions(resourceModifyAction) &&
                            authService.isAllowModify());
        }
        return modify;
    }

    public boolean isAllowDelete() {
        if (delete == null) {
            ApplicationContext springContext = (ApplicationContext) ThreadBeanHolder.get("springContext");
            AuthService authService = (AuthService) springContext.getBean("authService");
            delete = authService.isSuperUser() ||
                    (authService.hasActions(resourceDeleteAction) &&
                            authService.isAllowModify());
        }
        return delete;
    }

    public boolean isAllowList() {
        if (list == null) {
            ApplicationContext springContext = (ApplicationContext) ThreadBeanHolder.get("springContext");
            AuthService authService = (AuthService) springContext.getBean("authService");
            list = authService.isSuperUser() || (
                    authService.hasActions(resourceListAction) &&
                            authService.isAllowView());
        }
        return list;
    }
}
