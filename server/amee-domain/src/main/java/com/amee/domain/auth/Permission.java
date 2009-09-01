package com.amee.domain.auth;

import com.amee.core.APIUtils;
import com.amee.domain.AMEEEntityReference;
import com.amee.domain.AMEEEnvironmentEntity;
import com.amee.domain.ObjectType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Permission.
 *
 * @author Diggory Briercliffe
 */
@Entity
@Table(name = "PERMISSION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Permission extends AMEEEnvironmentEntity implements Comparable {

    public final static int OBJECT_CLASS_MAX_SIZE = 255;
    public final static int ENTRIES_MAX_SIZE = 1000;

    /**
     * Constants for the various permission entry values.
     */
    public final static PermissionEntry OWN = new PermissionEntry("own");
    public final static PermissionEntry VIEW = new PermissionEntry("view");
    public final static PermissionEntry VIEW_DEPRECATED = new PermissionEntry("view.deprecated");
    public final static PermissionEntry CREATE = new PermissionEntry("create");
    public final static PermissionEntry MODIFY = new PermissionEntry("modify");
    public final static PermissionEntry DELETE = new PermissionEntry("delete");

    /**
     * Helpful PermissionEntry Sets.
     */
    public final static Set<PermissionEntry> OWN_VIEW = new HashSet<PermissionEntry>();
    public final static Set<PermissionEntry> OWN_CREATE = new HashSet<PermissionEntry>();
    public final static Set<PermissionEntry> OWN_MODIFY = new HashSet<PermissionEntry>();
    public final static Set<PermissionEntry> OWN_DELETE = new HashSet<PermissionEntry>();

    /**
     * Populate PermissionEntry Sets.
     */
    {
        OWN_VIEW.add(OWN);
        OWN_VIEW.add(VIEW);
        OWN_CREATE.add(OWN);
        OWN_CREATE.add(CREATE);
        OWN_MODIFY.add(OWN);
        OWN_MODIFY.add(MODIFY);
        OWN_DELETE.add(OWN);
        OWN_DELETE.add(DELETE);
    }

    /**
     * The entity that this permission is governing access to.
     */
    @Embedded
    private AMEEEntityReference entityReference = new AMEEEntityReference();

    /**
     * The principle that this permission is defining access permissions for.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "entityId", column = @Column(name = "PRINCIPLE_ID")),
            @AttributeOverride(name = "entityUid", column = @Column(name = "PRINCIPLE_UID")),
            @AttributeOverride(name = "entityType", column = @Column(name = "PRINCIPLE_TYPE"))})
    private AMEEEntityReference principleReference = new AMEEEntityReference();

    /**
     * A String containing permission entries structured as JSON.
     */
    @Column(name = "ENTRIES", length = ENTRIES_MAX_SIZE, nullable = false)
    @Lob
    private String entries = "";

    /**
     * Set of PermissionEntry instances.
     */
    @Transient
    private Set<PermissionEntry> entrySet;

    public Permission() {
        super();
    }

    public String toString() {
        return "Permission_" + getUid();
    }

    public int compareTo(Object o) {
        if (this == o) return 0;
        if (equals(o)) return 0;
        Permission permission = (Permission) o;
        return getUid().compareTo(permission.getUid());
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("created", getCreated());
        obj.put("modified", getModified());
        obj.put("environmentUid", getEnvironment().getUid());
        return obj;
    }

    // TODO: Never used.
    @Deprecated
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        return getJSONObject();
    }

    // TODO: Never used.
    @Deprecated
    public JSONObject getIdentityJSONObject() throws JSONException {
        return APIUtils.getIdentityJSONObject(this);
    }

    public Element getElement(Document document) {
        Element element = document.createElement("Permission");
        element.setAttribute("uid", getUid());
        element.setAttribute("created", getCreated().toString());
        element.setAttribute("modified", getModified().toString());
        element.appendChild(getEnvironment().getIdentityElement(document));
        return element;
    }

    // TODO: Never used.
    @Deprecated
    public Element getElement(Document document, boolean detailed) {
        return getElement(document);
    }

    // TODO: Never used.
    @Deprecated
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }

    public AMEEEntityReference getEntityReference() {
        return entityReference;
    }

    public void setEntityReference(AMEEEntityReference entityReference) {
        if (entityReference != null) {
            this.entityReference = entityReference;
        }
    }

    public AMEEEntityReference getPrincipleReference() {
        return principleReference;
    }

    public void setPrincipleReference(AMEEEntityReference principleReference) {
        if (principleReference != null) {
            this.principleReference = principleReference;
        }
    }

    public Set<PermissionEntry> getEntries() {
        if (entrySet == null) {
            entrySet = new HashSet<PermissionEntry>();
            JSONObject json = getEntriesJSONObject();
            try {
                if (json.has("entries")) {
                    JSONArray arr = json.getJSONArray("entries");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        String value = obj.has("value") ? obj.getString("value") : "";
                        String allow = obj.has("allow") ? obj.getString("allow") : "true";
                        entrySet.add(new PermissionEntry(value, allow));
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }
        return Collections.unmodifiableSet(entrySet);
    }

    /**
     * Add a PermissionEntry to the entries. Will internally make sure the entries set and string are updated.
     *
     * @param entry to add
     */
    public void addEntry(PermissionEntry entry) {
        // make sure entrySet is exists
        getEntries();
        // add the entry to entrySet
        entrySet.add(entry);
        // update the entries string
        updateEntriesJSONObject();
    }

    private void updateEntriesJSONObject() {
        try {
            JSONArray arr = new JSONArray();
            for (PermissionEntry entry : getEntries()) {
                JSONObject obj = new JSONObject();
                obj.put("value", entry.getValue());
                if (!entry.isAllow()) {
                    obj.put("allow", "false");
                }
                arr.put(obj);
            }
            JSONObject json = new JSONObject();
            json.put("entries", arr);
            entries = json.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
    }

    private JSONObject getEntriesJSONObject() {
        try {
            return new JSONObject(entries);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.PRM;
    }
}
