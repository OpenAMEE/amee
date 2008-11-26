package com.jellymold.engine;

import com.jellymold.kiwi.SiteApp;
import com.jellymold.kiwi.Target;
import com.jellymold.kiwi.TargetBuilder;
import com.jellymold.kiwi.TargetType;
import com.jellymold.kiwi.environment.SiteService;
import org.restlet.*;
import org.restlet.ext.seam.SpringController;
import org.restlet.ext.spring.SpringBeanFinder;
import org.springframework.context.ApplicationContext;

import java.util.Set;

public class EngineApplication extends Application {

    private ApplicationContext springContext;
    private SpringController springController;
    private String filterNames = "";

    public EngineApplication() {
        super();
    }

    public EngineApplication(String name) {
        this();
        setName(name);
    }

    public EngineApplication(Context parentContext, String name) {
        super(parentContext);
        setName(name);
        setStatusService(new EngineStatusService(true));
        this.springContext = (ApplicationContext) parentContext.getAttributes().get("springContext");
        this.springController = (SpringController) parentContext.getAttributes().get("springController");
    }

    public Restlet createRoot() {
        TargetBuilder targetBuilder;
        Router router = new Router(getContext());
        // get the SiteApp for this Application
        SiteService siteService = (SiteService) springContext.getBean("siteService");
        SiteApp siteApp = siteService.getSiteAppByUid(getName()); // application.name = app.uid
        if ((siteApp != null) && (siteApp.getApp() != null)) {
            // bind Restlets based on App Targets
            bindTargets(router, siteApp.getApp().getTargets());
            // bind Restlets based on dynamically created App Targets
            targetBuilder = getTargetBuilder(siteApp);
            if (targetBuilder != null) {
                bindTargets(router, targetBuilder.getTargets());
            }
        }
        return router;
    }

    public TargetBuilder getTargetBuilder(SiteApp siteApp) {
        TargetBuilder targetBuilder = null;
        if (!siteApp.getApp().getTargetBuilder().isEmpty()) {
            targetBuilder = (TargetBuilder) springContext.getBean(siteApp.getApp().getTargetBuilder());
            if (targetBuilder != null) {
                targetBuilder.setEnvironment(siteApp.getEnvironment());
            }
        }
        return targetBuilder;
    }

    protected void bindTargets(Router router, Set<Target> targets) {
        for (Target target : targets) {
            if (target.isEnabled()) {
                if (!target.isDefaultTarget()) {
                    router.attach(target.getUriPattern(), getRestlet(target));
                } else {
                    router.attachDefault(getRestlet(target));
                }
            }
        }
    }

    protected Restlet getRestlet(Target target) {
        Restlet restlet = null;
        if (target.getType().equals(TargetType.SEAM_COMPONENT_RESOURCE)) {
            restlet = new SpringBeanFinder(springContext, target.getTarget());
        } else if (target.getType().equals(TargetType.DIRECTORY_RESOURCE)) {
            restlet = new Directory(getContext(), target.getTarget());
        } else {
            try {
                restlet = (Restlet) Class.forName(target.getTarget()).newInstance();
            } catch (ClassNotFoundException e) {
                // swallow
            } catch (IllegalAccessException e) {
                // swallow
            } catch (InstantiationException e) {
                // swallow
            }
        }
        return restlet;
    }

    public String getFilterNames() {
        return filterNames;
    }

    public void setFilterNames(String filterNames) {
        if (filterNames == null) {
            filterNames = "";
        }
        this.filterNames = filterNames;
    }

    public ApplicationContext getSpringContext() {
        return springContext;
    }

    public SpringController getSpringController() {
        return springController;
    }
}