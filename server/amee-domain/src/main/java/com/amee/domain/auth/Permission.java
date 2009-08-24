package com.amee.domain.auth;

import com.amee.core.APIUtils;
import com.amee.core.PersistentObject;
import com.amee.domain.AMEEEntity;
import com.amee.domain.AMEEEntityReference;
import com.amee.domain.AMEEEnvironmentEntity;
import com.amee.domain.APIVersion;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.*;

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

    public final static String ENTRY_VIEW = "view";

    /**
     * Defines which 'principle' classes (keys) can relate to which 'entity' classes (values).
     */
    public final static Map<Class, Set<Class>> PRINCIPLE_ENTITY = new HashMap<Class, Set<Class>>();

    /**
     * Define which principles can relate to which entities.
     */
    {
        addPrincipleAndEntity(User.class, Profile.class);
        addPrincipleAndEntity(User.class, DataCategory.class);
        addPrincipleAndEntity(User.class, ProfileItem.class);
        addPrincipleAndEntity(User.class, DataItem.class);
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
            @AttributeOverride(name = "entityClass", column = @Column(name = "PRINCIPLE_CLASS"))})
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

    // TODO: Stuff below will go.

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "GROUP_ID")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "OBJECT_CLASS", length = OBJECT_CLASS_MAX_SIZE, nullable = false)
    @Index(name = "OBJECT_CLASS_IND")
    private String objectClass = "";

    @Column(name = "OBJECT_UID", nullable = false, length = UID_SIZE)
    @Index(name = "OBJECT_UID_IND")
    private String objectUid = "";

    @Column(name = "GROUP_ALLOW_VIEW")
    private boolean groupAllowView = false;

    @Column(name = "GROUP_ALLOW_MODIFY")
    private boolean groupAllowModify = false;

    @Column(name = "OTHER_ALLOW_VIEW")
    private boolean otherAllowView = false;

    @Column(name = "OTHER_ALLOW_MODIFY")
    private boolean otherAllowModify = false;

    public Permission() {
        super();
    }

    public Permission(Group group, User user) {
        super(group.getEnvironment());
        setGroup(group);
        setUser(user);
    }

    public Permission(Group group, User user, PersistentObject persistentObject) {
        this(group, user);
        setObject(persistentObject);
    }

    public void setObject(PersistentObject persistentObject) {
        setObjectClass(persistentObject.getClass().getName());
        setObjectUid(persistentObject.getUid());
    }

    public String toString() {
        return "Permission_" + getUid();
    }

    // TODO: Do this properly.
    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        Permission permission = (Permission) o;
        int result = getEnvironment().compareTo(permission.getEnvironment());
        if (result == 0) {
            result = getGroup().compareTo(permission.getGroup());
            if (result == 0) {
                result = getUser().compareTo(permission.getUser());
            }
        }
        return result;
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("created", getCreated());
        obj.put("modified", getModified());
        obj.put("environmentUid", getEnvironment().getUid());
        JSONObject groupObj = new JSONObject();
        groupObj.put("uid", getGroup().getUid());
        groupObj.put("name", getGroup().getName());
        obj.put("group", groupObj);
        JSONObject userObj = new JSONObject();
        userObj.put("uid", getUser().getUid());
        userObj.put("username", getUser().getUsername());
        obj.put("auth", userObj);
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
        element.appendChild(getGroup().getIdentityElement(document)
                .appendChild(APIUtils.getElement(document, "Name", getGroup().getName())));
        element.appendChild(getUser().getIdentityElement(document)
                .appendChild(APIUtils.getElement(document, "Username", getUser().getUsername())));
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

    private static void addPrincipleAndEntity(Class principleClass, Class entityClass) {
        Set<Class> entityClasses = PRINCIPLE_ENTITY.get(principleClass);
        if (entityClasses == null) {
            entityClasses = new HashSet<Class>();
            PRINCIPLE_ENTITY.put(principleClass, entityClasses);
        }
        entityClasses.add(entityClass);
    }

    public static boolean isValidPrinciple(AMEEEntity principle) {
        if (principle == null) throw new IllegalArgumentException();
        return PRINCIPLE_ENTITY.keySet().contains(principle.getClass());
    }

    public static boolean isValidEntity(AMEEEntity entity) {
        if (entity == null) throw new IllegalArgumentException();
        for (Set<Class> entities : PRINCIPLE_ENTITY.values()) {
            if (entities.contains(entity.getClass())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidPrincipleToEntity(AMEEEntity principle, AMEEEntity entity) {
        if ((principle == null) || (entity == null)) throw new IllegalArgumentException();
        return isValidPrinciple(principle) && PRINCIPLE_ENTITY.get(principle.getClass()).contains(entity.getClass());
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
                JSONArray arr = json.getJSONArray("entries");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String value = obj.has("value") ? obj.getString("value") : "";
                    String allow = obj.has("allow") ? obj.getString("allow") : "true";
                    entrySet.add(new PermissionEntry(value, allow));
                }
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }
        return Collections.unmodifiableSet(entrySet);
    }

    public void addEntry(PermissionEntry entry) {
        getEntries().add(entry);
        updateEntries();
    }

    private void updateEntries() {
        JSONObject entriesObj = new JSONObject();
        try {
            JSONArray entriesArr = new JSONArray();
            for (PermissionEntry entry : getEntries()) {
                JSONObject entryObj = new JSONObject();
                entryObj.put("value", entry.getValue());
                entryObj.put("allow", entry.isAllow());
            }
            entriesObj.put("entries", entriesArr);
        } catch (JSONException e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
        entries = entriesObj.toString();
    }

    private JSONObject getEntriesJSONObject() {
        try {
            return new JSONObject(entries);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        if (group != null) {
            this.group = group;
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user != null) {
            this.user = user;
        }
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(String objectClass) {
        if (objectClass == null) {
            objectClass = "";
        }
        this.objectClass = objectClass;
    }

    public String getObjectUid() {
        return objectUid;
    }

    public void setObjectUid(String objectUid) {
        if (objectUid == null) {
            objectUid = "";
        }
        this.objectUid = objectUid;
    }

    public boolean isGroupAllowView() {
        return groupAllowView;
    }

    public void setGroupAllowView(boolean groupAllowView) {
        this.groupAllowView = groupAllowView;
    }

    public boolean isGroupAllowModify() {
        return groupAllowModify;
    }

    public void setGroupAllowModify(boolean groupAllowModify) {
        this.groupAllowModify = groupAllowModify;
    }

    public boolean isOtherAllowView() {
        return otherAllowView;
    }

    public void setOtherAllowView(boolean otherAllowView) {
        this.otherAllowView = otherAllowView;
    }

    public boolean isOtherAllowModify() {
        return otherAllowModify;
    }

    public void setOtherAllowModify(boolean otherAllowModify) {
        this.otherAllowModify = otherAllowModify;
    }

    public APIVersion getAPIVersion() {
        return user.getAPIVersion();
    }
}
