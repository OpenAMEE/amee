package com.amee.admin.service;

import com.amee.admin.service.app.AppService;
import com.amee.domain.site.App;
import com.amee.service.BaseBrowser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class AppBrowser extends BaseBrowser {

    @Autowired
    AppService appService;

    // Apps

    private String appUid = null;
    private App app = null;

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
}
