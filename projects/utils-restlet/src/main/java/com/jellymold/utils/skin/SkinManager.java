package com.jellymold.utils.skin;

import java.util.HashMap;
import java.util.Map;

public class SkinManager {

    private Map<String, Skin> skins = new HashMap<String, Skin>();

    public SkinManager() {
        super();
    }

    public Skin getSkin(String path) {
        return skins.get(path);
    }

    public Map<String, Skin> getSkins() {
        return skins;
    }

    public void setSkins(Map<String, Skin> skins) {
        this.skins = skins;
    }
}
