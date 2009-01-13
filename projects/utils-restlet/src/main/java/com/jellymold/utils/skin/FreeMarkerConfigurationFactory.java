package com.jellymold.utils.skin;

import com.jellymold.utils.ThreadBeanHolder;
import com.jellymold.utils.cache.CacheableFactory;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class FreeMarkerConfigurationFactory implements CacheableFactory {

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

        // TODO: if only one TemplateLoader in List then just return that
        return new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()]));
    }

    public String getKey() {
        return (String) ThreadBeanHolder.get("skinPath");
    }

    public String getCacheName() {
        return "FreeMarkerConfigurations";
    }
}