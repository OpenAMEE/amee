package com.jellymold.kiwi;

import com.jellymold.kiwi.app.AppService;
import com.jellymold.kiwi.environment.EnvironmentService;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.utils.domain.APIUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.List;

// TODO: make robust (check for nulls, error conditions)

@Service
public class KiwiImporter implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private AppService appService;

    @Autowired
    private SiteService siteService;

    public KiwiImporter() {
        super();
    }

    public void doImport() {

        File exportDir;
        File environmentsDir;

        // export dir
        exportDir = new File("export");

        // Apps
        importApps(exportDir);

        // Environments
        importEnvironments(exportDir);
    }

    // apps

    private void importApps(File exportDir) {

        App app;
        File appsDir;

        // environments dir
        appsDir = new File(exportDir, "apps");

        // iterate over app dirs
        for (File appDir : appsDir.listFiles()) {
            if (appDir.isDirectory() && !appDir.getName().equals(".svn")) {
                app = importApp(appDir);
                importAppActions(appDir, app);
                importAppTargets(appDir, app);
            }
        }

        // save everything
        entityManager.flush();
    }

    protected App importApp(File appDir) {
        App app = null;
        File appFile;
        FileInputStream fos;
        try {
            appFile = new File(appDir, "app.xml");
            fos = new FileInputStream(appFile);
            app = importApp(APIUtils.getRootElement(fos));
        } catch (FileNotFoundException e) {
            log.debug("Caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("Caught DocumentException: " + e.getMessage());
        }
        return app;
    }

    protected App importApp(Element element) {

        App app;

        // find or create App
        app = getApp(element);
        if (app == null) {
            log.debug("Creating new App");
            app = new App();
            appService.save(app);
        }

        // update App from Element
        log.debug("Populating App: " + app.getUid());
        app.populate(element);

        return app;
    }

    private App getApp(Element element) {
        App app;
        app = appService.getAppByUid(element.attributeValue("uid"));
        if (app == null) {
            app = appService.getAppByName(element.elementText("Name"));
        }
        return app;
    }

    // App Actions

    protected void importAppActions(File appDir, App app) {
        File appActionsFile;
        FileInputStream fos;
        try {
            appActionsFile = new File(appDir, "actions.xml");
            fos = new FileInputStream(appActionsFile);
            importAppActions(APIUtils.getRootElement(fos), app);
        } catch (FileNotFoundException e) {
            log.debug("Caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("Caught DocumentException: " + e.getMessage());
        }
    }

    protected void importAppActions(Element element, App app) {

        Action newAction;
        Element actionElem;

        // interate over Action XML elements
        for (Object childObj : element.elements()) {
            actionElem = (Element) childObj;
            newAction = new Action(app);
            newAction.populate(actionElem);
            // if Action already exists, update it
            for (Action action : app.getActions()) {
                if (action.equals(newAction)) {
                    action.populate(actionElem);
                    newAction = null;
                }
            }
            // if Action does not exist, add it
            if (newAction != null) {
                app.getActions().add(newAction);
            }
        }
    }

    // App Targets

    protected App importAppTargets(File appDir, App app) {
        File appTargetsFile;
        FileInputStream fos;
        try {
            appTargetsFile = new File(appDir, "targets.xml");
            fos = new FileInputStream(appTargetsFile);
            importAppTargets(APIUtils.getRootElement(fos), app);
        } catch (FileNotFoundException e) {
            log.debug("Caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("Caught DocumentException: " + e.getMessage());
        }
        return app;
    }

    protected void importAppTargets(Element element, App app) {

        Target newTarget;
        Element targetElem;

        // interate over Target XML elements
        for (Object childObj : element.elements()) {
            targetElem = (Element) childObj;
            newTarget = new Target(app);
            newTarget.populate(targetElem);
            // if Target already exists, update it
            for (Target target : app.getTargets()) {
                if (target.equals(newTarget)) {
                    target.populate(targetElem);
                    newTarget = null;
                }
            }
            // if Target does not exist, add it
            if (newTarget != null) {
                app.getTargets().add(newTarget);
            }
        }
    }

    // environments

    private void importEnvironments(File exportDir) {

        Environment environment;
        File environmentsDir;

        // environments dir
        environmentsDir = new File(exportDir, "environments");

        // iterate over environment dirs
        for (File environmentDir : environmentsDir.listFiles()) {
            if (environmentDir.isDirectory() && !environmentDir.getName().equals(".svn")) {
                environment = importEnvironment(environmentDir);
                importScheduledTasks(environment, environmentDir);
                importRoles(environment, environmentDir);
                importUsers(environment, environmentDir);
                importGroups(environment, environmentDir);
                importGroupUsers(environment, environmentDir);
                importSites(environment, environmentDir);
            }
        }
    }

    protected Environment importEnvironment(File environmentDir) {
        Environment environment = null;
        File environmentFile;
        FileInputStream fos;
        try {
            environmentFile = new File(environmentDir, "environment.xml");
            fos = new FileInputStream(environmentFile);
            environment = importEnvironment(APIUtils.getRootElement(fos));
        } catch (FileNotFoundException e) {
            log.debug("Caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("Caught DocumentException: " + e.getMessage());
        }
        return environment;
    }

    protected Environment importEnvironment(Element element) {

        Environment environment;

        // find Environment based on uid
        environment = environmentService.getEnvironmentByUid(element.attributeValue("uid"));
        if (environment == null) {
            // find Environment based on name
            environment = environmentService.getEnvironmentByName(element.elementText("Name"));
            if (environment == null) {
                // need to create a new environment
                log.debug("Creating new Environment");
                environment = new Environment();
                environmentService.save(environment);
            }
        }

        // update Environment from Element
        log.debug("Populating Environment: " + environment.getUid());
        environment.populate(element);

        // save everything
        entityManager.flush();

        return environment;
    }

    // Scheduled Tasks

    protected void importScheduledTasks(Environment environment, File environmentDir) {
        File scheduledTasksFile;
        FileInputStream fos;
        try {
            scheduledTasksFile = new File(environmentDir, "scheduledTasks.xml");
            fos = new FileInputStream(scheduledTasksFile);
            importScheduledTasks(APIUtils.getRootElement(fos), environment);
        } catch (FileNotFoundException e) {
            log.debug("Caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("Caught DocumentException: " + e.getMessage());
        }
    }

    protected void importScheduledTasks(Element element, Environment environment) {
        for (Object scheduledTaskElem : element.elements()) {
            importScheduledTask((Element) scheduledTaskElem, environment);
        }
    }

    protected void importScheduledTask(Element element, Environment environment) {

        ScheduledTask scheduledTask;

        // find ScheduledTask based on uid
        scheduledTask = environmentService.getScheduledTaskByUid(environment, element.attributeValue("uid"));
        if (scheduledTask == null) {
            // find ScheduledTask based on name
            scheduledTask = environmentService.getScheduledTaskByName(environment, element.elementText("Name"));
            if (scheduledTask == null) {
                // need to create a new ScheduledTask
                log.debug("Creating new ScheduledTask");
                scheduledTask = new ScheduledTask(environment);
                environmentService.save(scheduledTask);
            }
        }

        // update ScheduledTask from Element
        log.debug("Populating ScheduledTask: " + scheduledTask.getUid());
        scheduledTask.populate(element);

        // save everything
        entityManager.flush();
    }

    // Roles

    protected void importRoles(Environment environment, File environmentDir) {
        File rolesFile;
        FileInputStream fos;
        try {
            rolesFile = new File(environmentDir, "roles.xml");
            fos = new FileInputStream(rolesFile);
            importRoles(APIUtils.getRootElement(fos), environment);
        } catch (FileNotFoundException e) {
            log.debug("Caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("Caught DocumentException: " + e.getMessage());
        }
    }

    protected void importRoles(Element element, Environment environment) {
        for (Object roleElem : element.elements()) {
            importRole((Element) roleElem, environment);
        }
    }

    protected void importRole(Element roleElem, Environment environment) {

        Role role;
        Action action;
        Element roleActionElem;

        // get or create Role
        role = getRole(environment, roleElem);
        if (role == null) {
            log.debug("Creating new Role");
            role = new Role(environment);
            siteService.save(role);
        }

        // update Role from Element
        log.debug("Populating Role: " + role.getUid());
        role.populate(roleElem);

        // Actions
        for (Object o : roleElem.element("Actions").elements()) {
            roleActionElem = (Element) o;
            // find Action by uid
            action = appService.getActionByUid(roleActionElem.attributeValue("uid"));
            if (action == null) {
                // find Action by key
                action = appService.getActionByKey(roleActionElem.elementText("Key"));
            }
            // add Action to Role if found
            if (action != null) {
                role.add(action);
            }
        }

        // save everything
        entityManager.flush();
    }

    private Role getRole(Environment environment, Element roleElem) {
        Role role = siteService.getRoleByUid(environment, roleElem.attributeValue("uid"));
        if (role == null) {
            role = siteService.getRoleByName(environment, roleElem.elementText("Name"));
        }
        return role;
    }

    // Users

    protected void importUsers(Environment environment, File environmentDir) {
        File usersFile;
        FileInputStream fos;
        try {
            usersFile = new File(environmentDir, "users.xml");
            fos = new FileInputStream(usersFile);
            importUsers(APIUtils.getRootElement(fos), environment);
        } catch (FileNotFoundException e) {
            log.debug("Caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("Caught DocumentException: " + e.getMessage());
        }
    }

    protected void importUsers(Element element, Environment environment) {
        for (Object userElem : element.elements()) {
            importUser((Element) userElem, environment);
        }
    }

    protected void importUser(Element element, Environment environment) {

        User user;

        // get or create User
        user = getUser(environment, element);
        if (user == null) {
            log.debug("Creating new User");
            user = new User(environment);
            siteService.save(user);
        }

        // update User from Element
        log.debug("Populating User: " + user.getUid());
        user.populate(element);

        // save everything
        entityManager.flush();
    }

    private User getUser(Environment environment, Element element) {
        User user = siteService.getUserByUid(environment, element.attributeValue("uid"));
        if (user == null) {
            user = siteService.getUserByUsername(environment, element.elementText("Username"));
        }
        return user;
    }

    // Groups

    protected void importGroups(Environment environment, File environmentDir) {
        File groupsFile;
        FileInputStream fos;
        try {
            groupsFile = new File(environmentDir, "groups.xml");
            fos = new FileInputStream(groupsFile);
            importGroups(APIUtils.getRootElement(fos), environment);
        } catch (FileNotFoundException e) {
            log.debug("Caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("Caught DocumentException: " + e.getMessage());
        }
    }

    protected void importGroups(Element element, Environment environment) {
        for (Object groupElem : element.elements()) {
            importGroup((Element) groupElem, environment);
        }
    }

    protected void importGroup(Element groupElem, Environment environment) {

        Group group;

        // get or create Group
        group = getGroup(environment, groupElem);
        if (group == null) {
            log.debug("Creating new Group");
            group = new Group(environment);
            siteService.save(group);
        }

        // update Group from Element
        log.debug("Populating Group: " + group.getUid());
        group.populate(groupElem);

        // save everything
        entityManager.flush();
    }

    private Group getGroup(Environment environment, Element groupElem) {
        Group group = siteService.getGroupByUid(environment, groupElem.attributeValue("uid"));
        if (group == null) {
            group = siteService.getGroupByName(environment, groupElem.elementText("Name"));
        }
        return group;
    }

    // GroupUsers

    protected void importGroupUsers(Environment environment, File environmentDir) {
        File groupUsersFile;
        FileInputStream fos;
        try {
            groupUsersFile = new File(environmentDir, "groupUsers.xml");
            fos = new FileInputStream(groupUsersFile);
            importGroupUsers(APIUtils.getRootElement(fos), environment);
        } catch (FileNotFoundException e) {
            log.debug("Caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("Caught DocumentException: " + e.getMessage());
        }
    }

    protected void importGroupUsers(Element element, Environment environment) {
        for (Object groupUserElem : element.elements()) {
            importGroupUser((Element) groupUserElem, environment);
        }
    }

    protected void importGroupUser(Element groupUserElem, Environment environment) {

        GroupUser groupUser;
        Group group;
        User user;
        Role role;
        Element rolesElem;

        // get Group and User
        group = getGroup(environment, groupUserElem.element("Group"));
        user = getUser(environment, groupUserElem.element("User"));

        // must have Group and User
        if ((group != null) && (user != null)) {

            // find GroupUser based on uid
            groupUser = siteService.getGroupUserByUid(environment, groupUserElem.attributeValue("uid"));
            if (groupUser == null) {
                // find GroupUser based on name
                groupUser = siteService.getGroupUser(group, user);
            }
            if (groupUser == null) {
                // need to create a new GroupUser
                log.debug("Creating new GroupUser");
                groupUser = new GroupUser(group, user);
                siteService.save(groupUser);
            }

            // update GroupUser from Element
            log.debug("Populating GroupUser: " + groupUser.getUid());
            groupUser.populate(groupUserElem);

            // roles
            rolesElem = groupUserElem.element("Roles");
            if (rolesElem != null) {
                for (Element roleElem : (List<Element>) rolesElem.elements()) {
                    role = getRole(environment, roleElem);
                    // add Role to GroupUser if found
                    if (role != null) {
                        groupUser.add(role);
                    }
                }
            }

            // save everything
            entityManager.flush();
        }
    }

    // Sites

    protected void importSites(Environment environment, File environmentDir) {

        Site site;
        File sitesDir;

        // sites dir
        sitesDir = new File(environmentDir, "sites");

        // iterate over site dirs
        for (File siteDir : sitesDir.listFiles()) {
            if (siteDir.isDirectory() && !siteDir.getName().equals(".svn")) {
                site = importSite(environment, siteDir);
                importSiteAliases(site, siteDir);
                importSiteApps(site, siteDir);
            }
        }
    }

    protected Site importSite(Environment environment, File siteDir) {
        Site site = null;
        File siteFile;
        FileInputStream fos;
        try {
            siteFile = new File(siteDir, "site.xml");
            fos = new FileInputStream(siteFile);
            site = importSite(environment, APIUtils.getRootElement(fos));
        } catch (FileNotFoundException e) {
            log.debug("Caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("Caught DocumentException: " + e.getMessage());
        }
        return site;
    }

    protected Site importSite(Environment environment, Element element) {

        Site site;

        // find Site based on uid
        site = getSite(environment, element);
        if (site == null) {
            // need to create a new Site
            log.debug("Creating new Site");
            site = new Site(environment);
            siteService.save(site);
        }

        // update Site from Element
        log.debug("Populating Site: " + site.getUid());
        site.populate(element);

        // save everything
        entityManager.flush();

        return site;
    }

    private Site getSite(Environment environment, Element element) {
        Site site = siteService.getSiteByUid(environment, element.attributeValue("uid"));
        if (site == null) {
            site = siteService.getSiteByName(environment, element.elementText("Name"));
        }
        return site;
    }

    // SiteAliases

    protected void importSiteAliases(Site site, File siteDir) {
        File siteAliasesFile;
        FileInputStream fos;
        try {
            siteAliasesFile = new File(siteDir, "siteAliases.xml");
            fos = new FileInputStream(siteAliasesFile);
            importSiteAliases(APIUtils.getRootElement(fos), site);
        } catch (FileNotFoundException e) {
            log.debug("Caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("Caught DocumentException: " + e.getMessage());
        }
        // save everything
        entityManager.flush();
    }

    protected void importSiteAliases(Element element, Site site) {
        for (Object aliasesElem : element.elements()) {
            importSiteAlias((Element) aliasesElem, site);
        }
    }

    protected void importSiteAlias(Element siteAliasElem, Site site) {
        SiteAlias newSiteAlias = new SiteAlias(site);
        newSiteAlias.populate(siteAliasElem);
        // if newSiteAlias already exists, update it
        for (SiteAlias siteAlias : site.getSiteAliases()) {
            if (siteAlias.equals(newSiteAlias)) {
                siteAlias.populate(siteAliasElem);
                newSiteAlias = null;
            }
        }
        // if newSiteAlias does not exist, add it
        if (newSiteAlias != null) {
            site.getSiteAliases().add(newSiteAlias);
        }
    }

    // SiteApps

    protected void importSiteApps(Site site, File siteDir) {
        File siteAppsFile;
        FileInputStream fos;
        try {
            siteAppsFile = new File(siteDir, "siteApps.xml");
            fos = new FileInputStream(siteAppsFile);
            importSiteApps(APIUtils.getRootElement(fos), site);
        } catch (FileNotFoundException e) {
            log.debug("Caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("Caught DocumentException: " + e.getMessage());
        }
        // save everything
        entityManager.flush();
    }

    protected void importSiteApps(Element element, Site site) {
        for (Object siteAppsElem : element.elements()) {
            importSiteApp((Element) siteAppsElem, site);
        }
    }

    protected void importSiteApp(Element siteAppElem, Site site) {

        App app;
        SiteApp newSiteApp;

        // get existing App
        app = getApp(siteAppElem.element("App"));

        // must have an App
        if (app != null) {
            newSiteApp = new SiteApp(app, site);
            newSiteApp.populate(siteAppElem);
            // if newSiteApp already exists, update it
            for (SiteApp siteApp : site.getSiteApps()) {
                if (siteApp.equals(newSiteApp)) {
                    siteApp.populate(siteAppElem);
                    newSiteApp = null;
                }
            }
            // if newSiteApp does not exist, add it
            if (newSiteApp != null) {
                site.getSiteApps().add(newSiteApp);
            }
        }
    }
}
