package com.jellymold.plum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

@Service
public class SkinService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    // TODO: Springify
    // @In(required = false)
    private String skinPath;

    // TODO: Springify
    // @In(scope = ScopeType.EVENT, required = false)
    // @Out(scope = ScopeType.EVENT, required = false)
    private Skin skin;

    public Skin getSkin() {
        if (skin == null) {
            log.debug("getSkin() skinPath: " + skinPath);
            List<Skin> skins = entityManager.createQuery(
                    "SELECT DISTINCT s " +
                            "FROM Skin s " +
                            "LEFT JOIN FETCH s.importedSkins imp " +
                            "WHERE s.path = :path")
                    .setParameter("path", skinPath)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.skinService")
                    .getResultList();
            if (skins.size() == 1) {
                log.debug("getSkin() found Skin");
                skin = skins.get(0);
            } else {
                log.debug("getSkin() skin NOT found");
            }
        }
        // must have Skin to proceed
        if (skin == null) {
            // TODO: do something more elegant than this
            throw new RuntimeException("skin NOT found");
        }
        return skin;
    }

    public Skin getSkin(String path) {
        if (path != null) {
            List<Skin> skins = entityManager.createQuery(
                    "FROM Skin " +
                            "WHERE path = :path")
                    .setParameter("path", path.toLowerCase().trim())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.skinService")
                    .getResultList();
            if (skins.size() == 1) {
                return skins.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Skin getSkinByUID(String uid) {
        if (uid != null) {
            List<Skin> skins = entityManager.createQuery(
                    "FROM Skin " +
                            "WHERE uid = :uid")
                    .setParameter("uid", uid.toUpperCase().trim())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.skinService")
                    .getResultList();
            if (skins.size() == 1) {
                return skins.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Skin getSkinByName(String name) {
        if (name != null) {
            List<Skin> skins = entityManager.createQuery(
                    "SELECT a FROM Skin a " +
                            "WHERE a.name = :name")
                    .setParameter("name", name.trim())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.skinService")
                    .getResultList();
            if (skins.size() > 0) {
                return skins.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public List<Skin> getSkins() {
        List<Skin> skins = entityManager.createQuery(
                "FROM Skin s ORDER BY s.path")
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.skinService")
                .getResultList();
        return skins;
    }

    public void save(Skin skin) {
        entityManager.persist(skin);
    }
}