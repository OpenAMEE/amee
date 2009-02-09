package com.jellymold.kiwi.app;

import com.jellymold.kiwi.Action;
import com.jellymold.kiwi.App;
import com.jellymold.kiwi.ResourceActions;
import com.jellymold.utils.BaseBrowser;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@Scope("prototype")
public class AppBrowser extends BaseBrowser {

    @Autowired
    AppService appService;

    // Apps
    private String appUid = null;
    private App app = null;

    // Actions
    private String actionUid = null;
    private Action action = null;

    // ResourceActions
    private ResourceActions appActions = new ResourceActions(KiwiAppConstants.ACTION_APP_PREFIX);

    // Apps

    public String getAppUid() {
        return appUid;
    }

    public void setAppUid(String appUid) {
        this.appUid = appUid;
    }

    public App getApp() {
        if ((app == null) && (appUid != null)) {
            app = appService.getAppByUid(appUid);
        }
        return app;
    }

    // Actions

    public String getActionUid() {
        return actionUid;
    }

    public void setActionUid(String actionUid) {
        this.actionUid = actionUid;
    }

    public Action getAction() {
        if ((getApp() != null) && (action == null) && (actionUid != null)) {
            action = appService.getActionByUid(getApp(), actionUid);
        }
        return action;
    }

    // ResourceActions

    public ResourceActions getAppActions() {
        return appActions;
    }
}
