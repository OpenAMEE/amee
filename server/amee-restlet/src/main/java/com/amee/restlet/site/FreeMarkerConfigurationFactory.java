package com.amee.restlet.site;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import com.amee.domain.cache.CacheableFactory;
import com.amee.core.ThreadBeanHolder;
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

        // addItemValue loader for this skin if possible
        if (skin.getPath().length() > 0) {

            // work out path and get directory for this Skin
            path = System.getProperty("amee.SkinRoot", "/var/www/amee/skins") + "/" + skin.getPath();
            file = new File(path);

            // addItemValue loader for this Skin
            if (file.exists() && file.isDirectory()) {
                try {
                    loaders.add(new FileTemplateLoader(file));
                } catch (IOException e) {
                    // swallow
                }
            }
        }

        // addItemValue loader for parent Skin if present
        if (skin.getParent() != null) {
            loaders.add(getFreeMarkerTemplateLoader(skin.getParent()));
        }

        // addItemValue loader for import Skins if present
        for (Skin s : skin.getImportedSkins()) {
            loaders.add(getFreeMarkerTemplateLoader(s));
        }

        // TODO: if only one TemplateLoader in List then just return that
        return new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()]));
    }

    public String getKey() {
        String skinPath = (String) ThreadBeanHolder.get("skinPath");
        if (skinPath == null) {
            skinPath = "client-default";
        }
        return skinPath;
    }

    public String getCacheName() {
        return "FreeMarkerConfigurations";
    }
}