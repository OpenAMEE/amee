package com.jellymold.plum;

import com.jellymold.utils.domain.PersistentObject;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class PlumExporter {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private SkinService skinService;

    private OutputFormat format;

    public PlumExporter() {
        super();
        format = new OutputFormat("XML", "UTF-8", true);
        format.setIndent(4);
    }

    public void doExport() {

        File exportDir;
        File skinsDir;

        try {
            // export dir
            exportDir = new File("export");
            exportDir.mkdir();

            // skins dir
            skinsDir = new File(exportDir, "skins");
            skinsDir.mkdir();

            // export skins
            exportSkins(skinsDir);

        } catch (IOException e) {
            log.error("Caught IOException: " + e.getMessage());
        }
    }

    private void exportSkins(File skinsDir) throws IOException {
        // iterate over Skins
        for (Skin skin : skinService.getSkins()) {
            // export Skin
            exportSkin(skin, skinsDir);
        }
    }

    private void exportSkin(Skin skin, File skinsDir) throws IOException {
        exportObject(skin, skinsDir, skin.getPath());
    }

    private void exportObject(PersistentObject object, File dir, String filename) throws IOException {
        XMLSerializer serializer;
        Document document;
        // setup output XMLSerializer
        serializer = new XMLSerializer(new FileOutputStream(new File(dir, filename)), format);
        serializer.asDOMSerializer();
        // setup Document and output
        document = new DocumentImpl();
        document.appendChild(object.getElement(document));
        serializer.serialize(document);
    }
}