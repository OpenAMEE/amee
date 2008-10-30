package com.jellymold.plum;

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
import java.util.HashSet;
import java.util.Set;

@Service
public class PlumImporter {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SkinService skinService;

    public PlumImporter() {
        super();
    }

    public void doImport() {

        File exportDir;
        File skinsDir;

        // export dir
        exportDir = new File("export");

        // skins dir
        skinsDir = new File(exportDir, "skins");

        // import skins
        importSkins(skinsDir);
    }

    private void importSkins(File exportDir) {

        // iterate over the skin files
        for (File file : exportDir.listFiles()) {
            if (file.isFile()) {
                importSkin(file);
            }
        }
    }

    private Skin importSkin(File file) {
        Skin skin = null;
        FileInputStream fos;
        try {
            fos = new FileInputStream(file);
            skin = importSkin(APIUtils.getRootElement(fos));
        } catch (FileNotFoundException e) {
            log.debug("importSkin() caught FileNotFoundException: " + e.getMessage());
        } catch (DocumentException e) {
            log.debug("importSkin() caught DocumentException: " + e.getMessage());
        }
        return skin;
    }

    private Skin importSkin(Element element) {

        Skin skin;

        // find or create Skin
        skin = getSkin(element);
        if (skin == null) {
            skin = new Skin();
            skinService.save(skin);
        }

        // update Skin from Element
        log.debug("importSkin() populating Skin: " + skin.getUid());
        skin.populate(element);

        // save everything
        entityManager.flush();

        // set parent again
        if (element.element("Parent") != null) {
            Element parentElem = element.element("Parent");
            Skin parent = skinService.getSkinByUID(parentElem.attributeValue("uid"));
            if (parent == null) {
                parent = skinService.getSkin(parentElem.elementText("Path"));
                if (parent == null) {
                    parent = new Skin();
                    parent.setUid(parentElem.attributeValue("uid"));
                    parent.setPath(parentElem.elementText("Path"));
                    skinService.save(parent);
                    // save everything
                    entityManager.flush();
                }
            }
            skin.setParent(parent);
        }

        // set imported skins
        Set<Skin> importedSkins = new HashSet<Skin>();
        Element importedSkinsElem = element.element("ImportedSkins");
        if (importedSkinsElem != null) {
            for (Object e : importedSkinsElem.elements()) {
                Element importedSkinElem = (Element) e;
                Skin importedSkin = skinService.getSkinByUID(importedSkinElem.attributeValue("uid"));
                if (importedSkin == null) {
                    importedSkin = skinService.getSkin(importedSkinElem.elementText("Path"));
                    if (importedSkin == null) {
                        importedSkin = new Skin();
                        importedSkin.setUid(importedSkinElem.attributeValue("uid"));
                        importedSkin.setPath(importedSkinElem.elementText("Path"));
                        skinService.save(importedSkin);
                        // save everything
                        entityManager.flush();
                    }
                }
                importedSkins.add(importedSkin);
            }
        }
        skin.setImportedSkins(importedSkins);

        // save everything
        entityManager.flush();

        return skin;
    }

    private Skin getSkin(Element element) {
        Skin skin;
        skin = skinService.getSkinByUID(element.attributeValue("uid"));
        if (skin == null) {
            skin = skinService.getSkin(element.elementText("Path"));
        }
        return skin;
    }
}
