package gc.carbon.auth;

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
