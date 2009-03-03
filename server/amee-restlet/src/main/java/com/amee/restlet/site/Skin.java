package com.amee.restlet.site;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Skin implements Serializable {

    private Skin parent;
    private List<Skin> children = new ArrayList<Skin>();
    private String path;
    private Set<Skin> importedSkins = new HashSet<Skin>();

    public Skin() {
        super();
    }

    public void addChildSkin(Skin child) {
        getChildren().add(child);
    }

    public void addImportedSkin(Skin importedSkin) {
        getImportedSkins().add(importedSkin);
    }

    public String toString() {
        return "Skin_" + getPath();
    }

    public Set<Skin> getImportedSkins() {
        return importedSkins;
    }

    public void setImportedSkins(Set<Skin> importedSkins) {
        this.importedSkins = importedSkins;
    }

    public boolean isParentAvailable() {
        return getParent() != null;
    }

    public boolean isChildrenAvailable() {
        return !getChildren().isEmpty();
    }

    public Skin getParent() {
        return parent;
    }

    public void setParent(Skin parent) {
        this.parent = parent;
        parent.addChildSkin(this);
    }

    public List<Skin> getChildren() {
        return children;
    }

    public void setChildren(List<Skin> children) {
        this.children = children;
        for (Skin s : children) {
            s.setParent(this);
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}