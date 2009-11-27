package com.amee.restlet.site;

import com.amee.core.ThreadBeanHolder;
import com.amee.domain.cache.CacheableFactory;
import com.amee.domain.site.ISite;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class FreeMarkerConfigurationFactory implements CacheableFactory {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private SkinManager skinManager;

    public FreeMarkerConfigurationFactory() {
        super();
    }

    public Object create() {
        Configuration configuration = null;
        String skinPath = getKey();
        if (skinPath != null) {
            Skin skin = skinManager.getSkin(skinPath);
            if (skin != null) {
                configuration = new Configuration();
                configuration.setEncoding(Locale.ENGLISH, "UTF-8");
                configuration.setURLEscapingCharset("UTF-8");
                configuration.setTemplateLoader(getFreeMarkerTemplateLoader(skin));
            }
        }
        if (configuration == null) {
            log.error("Must have a Skin. Could not find: " + skinPath);
            throw new RuntimeException("Must have a Skin. Could not find: " + skinPath);
        }
        return configuration;
    }

    protected TemplateLoader getFreeMarkerTemplateLoader(Skin skin) {

        File file;
        String path;
        List<TemplateLoader> loaders = new ArrayList<TemplateLoader>();

        // add loader for this skin if possible
        if (skin.getPath().length() > 0) {

            // work out path and get directory for this Skin
            path = System.getProperty("amee.SkinRoot", "/var/www/amee/skins") + "/" + skin.getPath();
            file = new File(path);

            // add loader for this Skin
            if (file.exists() && file.isDirectory()) {
                try {
                    loaders.add(new FileTemplateLoader(file));
                } catch (IOException e) {
                    // swallow
                }
            }
        }

        // add loader for parent Skin if present
        if (skin.getParent() != null) {
            loaders.add(getFreeMarkerTemplateLoader(skin.getParent()));
        }

        // add loader for import Skins if present
        for (Skin s : skin.getImportedSkins()) {
            loaders.add(getFreeMarkerTemplateLoader(s));
        }

        // return a TemplateLoader
        if (loaders.isEmpty()) {
            // must have a TemplateLoader
            throw new RuntimeException("Need at least one TemplateLoader.");
        } else if (loaders.size() == 0) {
            // just return one TemplateLoader
            return loaders.get(0);
        } else {
            // return multiple TemplateLoaders
            return new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()]));
        }
    }

    public String getKey() {
        ISite site = (ISite) ThreadBeanHolder.get("activeSite");
        if ((site != null) && (site.getActiveSkinPath() != null)) {
            return site.getActiveSkinPath();
        } else {
            return "client-default";
        }
    }

    public String getCacheName() {
        return "FreeMarkerConfigurations";
    }
}