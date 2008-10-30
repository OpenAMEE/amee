package com.jellymold.kiwi;

import com.jellymold.kiwi.app.AppService;
import com.jellymold.kiwi.environment.EnvironmentService;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.utils.domain.PersistentObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO: make robust (check for nulls, error conditions)

@Service
@Scope("prototype")
public class KiwiExporter implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private AppService appService;

    @Autowired
    private SiteService siteService;

    private OutputFormat format;

    public KiwiExporter() {
        super();
        format = new OutputFormat("XML", "UTF-8", true);
        format.setIndent(4);
    }

    public void doExport() {

        File exportDir;
        File environmentsDir;
        File appsDir;

        try {
            // export dir
            exportDir = new File("export");
            exportDir.mkdir();

            // export environments
            environmentsDir = new File(exportDir, "environments");
            environmentsDir.mkdir();
            exportEnvironments(environmentsDir);

            // export apps
            appsDir = new File(exportDir, "apps");
            appsDir.mkdir();
            exportApps(appsDir);

        } catch (IOException e) {
            log.error("Caught IOException: " + e.getMessage());
        }
    }

    // environments

    private void exportEnvironments(File environmentsDir) throws IOException {
        File environmentDir;
        // iterate over Environments
        for (Environment environment : environmentService.getEnvironments()) {
            // environment dir
            environmentDir = new File(environmentsDir, environment.getUid());
            environmentDir.mkdir();
            // export Environment
            exportEnvironment(environment, environmentDir);
            exportScheduledTasks(environment, environmentDir);
            exportUsers(environment, environmentDir);
            exportRoles(environment, environmentDir);
            exportGroups(environment, environmentDir);
            exportGroupUsers(environment, environmentDir);
            exportSites(environment, environmentDir);
        }
    }

    private void exportEnvironment(Environment environment, File environmentDir) throws IOException {
        exportObject(environment, environmentDir, "environment.xml");
    }

    private void exportScheduledTasks(Environment environment, File environmentDir) throws IOException {
        List<PersistentObject> objects = (new ArrayList<PersistentObject>());
        objects.addAll(environmentService.getScheduledTasks(environment));
        exportObjects("ScheduledTasks", objects, environmentDir, "scheduledTasks.xml");
    }

    private void exportUsers(Environment environment, File environmentDir) throws IOException {
        List<PersistentObject> objects = (new ArrayList<PersistentObject>());
        objects.addAll(siteService.getUsers(environment));
        exportObjects("Users", objects, environmentDir, "users.xml");
    }

    private void exportRoles(Environment environment, File environmentDir) throws IOException {
        List<PersistentObject> objects = (new ArrayList<PersistentObject>());
        objects.addAll(siteService.getRoles(environment));
        exportObjects("Roles", objects, environmentDir, "roles.xml");
    }

    private void exportGroups(Environment environment, File environmentDir) throws IOException {
        List<PersistentObject> objects = (new ArrayList<PersistentObject>());
        objects.addAll(siteService.getGroups(environment));
        exportObjects("Groups", objects, environmentDir, "groups.xml");
    }

    private void exportGroupUsers(Environment environment, File environmentDir) throws IOException {
        List<PersistentObject> objects = (new ArrayList<PersistentObject>());
        objects.addAll(siteService.getGroupUsers(environment));
        exportObjects("GroupUsers", objects, environmentDir, "groupUsers.xml");
    }

    // sites

    private void exportSites(Environment environment, File environmentDir) throws IOException {
        File siteDir;
        File sitesDir;
        // sites dir
        sitesDir = new File(environmentDir, "sites");
        sitesDir.mkdir();
        // iterate over Sites
        for (Site site : siteService.getSites(environment, null)) {
            // site dir
            siteDir = new File(sitesDir, site.getUid());
            siteDir.mkdir();
            // export Site
            exportSite(site, siteDir);
            exportSiteAliases(site, siteDir);
            exportSiteApps(site, siteDir);
        }
    }

    private void exportSite(Site site, File siteDir) throws IOException {
        exportObject(site, siteDir, "site.xml");
    }

    private void exportSiteAliases(Site site, File siteDir) throws IOException {
        List<PersistentObject> objects = (new ArrayList<PersistentObject>());
        objects.addAll(siteService.getSiteAliases(site, null));
        exportObjects("SiteAliases", objects, siteDir, "siteAliases.xml");
    }

    private void exportSiteApps(Site site, File siteDir) throws IOException {
        List<PersistentObject> objects = (new ArrayList<PersistentObject>());
        objects.addAll(siteService.getSiteApps(site, null));
        exportObjects("SiteApps", objects, siteDir, "siteApps.xml");
    }

    // apps

    private void exportApps(File appsDir) throws IOException {
        File appDir;
        // iterate over Apps
        for (App app : appService.getApps()) {
            // app dir
            appDir = new File(appsDir, app.getUid());
            appDir.mkdir();
            // export App
            exportApp(app, appDir);
            exportAppActions(app, appDir);
            exportAppTargets(app, appDir);
        }
    }

    private void exportApp(App app, File appDir) throws IOException {
        exportObject(app, appDir, "app.xml");
    }

    private void exportAppActions(App app, File appDir) throws IOException {
        List<PersistentObject> objects = (new ArrayList<PersistentObject>());
        objects.addAll(appService.getActions(app));
        exportObjects("Actions", objects, appDir, "actions.xml");
    }

    private void exportAppTargets(App app, File appDir) throws IOException {
        List<PersistentObject> objects = (new ArrayList<PersistentObject>());
        objects.addAll(appService.getTargets(app));
        exportObjects("Targets", objects, appDir, "targets.xml");
    }

    // util

    private void exportObjects(String elementName, List<PersistentObject> objects, File parentDir, String filename) throws IOException {
        XMLSerializer serializer;
        Document document;
        Element element;
        // setup output XMLSerializet
        serializer = new XMLSerializer(new FileOutputStream(new File(parentDir, filename)), format);
        serializer.asDOMSerializer();
        // setup Document and output
        document = new DocumentImpl();
        element = document.createElement(elementName);
        for (PersistentObject object : objects) {
            element.appendChild(object.getElement(document));
        }
        document.appendChild(element);
        serializer.serialize(document);
    }

    private void exportObject(PersistentObject object, File parentDir, String filename) throws IOException {
        XMLSerializer serializer;
        Document document;
        // setup output XMLSerializer
        serializer = new XMLSerializer(new FileOutputStream(new File(parentDir, filename)), format);
        serializer.asDOMSerializer();
        // setup Document and output
        document = new DocumentImpl();
        document.appendChild(object.getElement(document));
        serializer.serialize(document);
    }
}
