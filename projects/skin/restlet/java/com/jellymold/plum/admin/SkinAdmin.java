package com.jellymold.plum.admin;

import com.jellymold.kiwi.ResourceActions;
import com.jellymold.kiwi.auth.AuthService;
import com.jellymold.plum.Skin;
import com.jellymold.plum.SkinService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

@Service
public class SkinAdmin implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private SkinService skinService;

    @Autowired
    private AuthService authService;

    private String skinUid = null;

    private Skin skin = null;

    private ResourceActions skinActions = new ResourceActions("skin");

    public String getSkinUid() {
        return skinUid;
    }

    public void setSkinUid(String skinUid) {
        this.skinUid = skinUid;
    }

    public ResourceActions getSkinActions() {
        return skinActions;
    }

    public Skin getSkin() {
        if ((skin == null) && (skinUid != null)) {
            skin = skinService.getSkinByUID(skinUid);
        }
        return skin;
    }
}