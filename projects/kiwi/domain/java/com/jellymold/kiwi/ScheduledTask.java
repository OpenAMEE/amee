package com.jellymold.kiwi;

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.PersistentObject;
import com.jellymold.utils.domain.UidGen;
import com.jellymold.utils.domain.DatedObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "SCHEDULED_TASK")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ScheduledTask implements EnvironmentObject, DatedObject, Comparable, Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = 12)
    private String uid = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @Column(name = "NAME", length = 100, nullable = false)
    private String name = "";

    @Column(name = "COMPONENT", length = 100, nullable = false)
    private String component = "";

    @Column(name = "METHOD", length = 100, nullable = false)
    private String method = "";

    @Column(name = "CRON", length = 100, nullable = false)
    private String cron = "";

    @Column(name = "DURATION", nullable = false)
    private Long duration = 0L;

    @Column(name = "RUN_ON_SHUTDOWN", nullable = false)
    private Boolean runOnShutdown = false;

    @Column(name = "SERVERS", length = 255, nullable = false)
    private String servers = "";

    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    @Version
    @Column(name = "VERSION")
    private Long version;

    public ScheduledTask() {
        super();
        setUid(UidGen.getUid());
    }

    public ScheduledTask(Environment environment) {
        this();
        setEnvironment(environment);
    }

    public String toString() {
        return "ScheduledTask_" + getUid();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduledTask)) return false;
        ScheduledTask scheduledTask = (ScheduledTask) o;
        return getUid().equalsIgnoreCase(scheduledTask.getUid());
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        ScheduledTask scheduledTask = (ScheduledTask) o;
        return getUid().compareToIgnoreCase(scheduledTask.getUid());
    }

    public int hashCode() {
        return getUid().toLowerCase().hashCode();
    }

    @Transient
    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("name", getName());
        obj.put("component", getComponent());
        obj.put("method", getMethod());
        obj.put("cron", getCron());
        obj.put("duration", getDuration());
        obj.put("runOnShutdown", getRunOnShutdown());
        obj.put("servers", getServers());
        obj.put("enabled", isEnabled());
        if (detailed) {
            obj.put("environment", getEnvironment().getIdentityJSONObject());
            obj.put("created", getCreated());
            obj.put("modified", getModified());
        }
        return obj;
    }

    @Transient
    public JSONObject getIdentityJSONObject() throws JSONException {
        return APIUtils.getIdentityJSONObject(this);
    }

    @Transient
    public Element getElement(Document document) {
        return getElement(document, true);
    }

    @Transient
    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("ScheduledTask");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "Component", getComponent()));
        element.appendChild(APIUtils.getElement(document, "Method", getMethod()));
        element.appendChild(APIUtils.getElement(document, "Cron", getCron()));
        element.appendChild(APIUtils.getElement(document, "Duration", getDuration().toString()));
        element.appendChild(APIUtils.getElement(document, "RunOnShutdown", getRunOnShutdown().toString()));
        element.appendChild(APIUtils.getElement(document, "Servers", getServers()));
        element.appendChild(APIUtils.getElement(document, "Enabled", "" + isEnabled()));
        if (detailed) {
            element.appendChild(getEnvironment().getIdentityElement(document));
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
        }
        return element;
    }

    @Transient
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }

    @Transient
    public void populate(org.dom4j.Element element) {
        setUid(element.attributeValue("uid"));
        setName(element.elementText("Name"));
        setComponent(element.elementText("Component"));
        setMethod(element.elementText("Method"));
        setCron(element.elementText("Cron"));
        setDuration(element.elementText("Duration"));
        setRunOnShutdown(element.elementText("RunOnShutdown"));
        setServers(element.elementText("Servers"));
        setEnabled(element.elementText("Enabled"));
    }

    @PrePersist
    public void onCreate() {
        setCreated(Calendar.getInstance().getTime());
        setModified(getCreated());
    }

    @PreUpdate
    public void onModify() {
        setModified(Calendar.getInstance().getTime());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        if (uid != null) {
            this.uid = uid;
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        if (environment != null) {
            this.environment = environment;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        this.name = name;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        if (component == null) {
            component = "";
        }
        this.component = component;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        if (method == null) {
            method = "";
        }
        this.method = method;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        if (cron == null) {
            cron = "";
        }
        this.cron = cron;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        if ((duration == null) || (duration < 0)) {
            duration = 0L;
        }
        this.duration = duration;
    }

    public void setDuration(String value) {
        Long duration = null;
        if (value != null) {
            try {
                duration = Long.valueOf(value);
            } catch (NumberFormatException e) {
                // swallow
            }
        }
        setDuration(duration);
    }

    public Boolean getRunOnShutdown() {
        return runOnShutdown;
    }

    public void setRunOnShutdown(Boolean runOnShutdown) {
        if (runOnShutdown != null) {
            this.runOnShutdown = runOnShutdown;
        }
    }

    public void setRunOnShutdown(String runOnShutdown) {
        setRunOnShutdown(Boolean.valueOf(runOnShutdown));
    }

    public String getServers() {
        return servers;
    }

    public boolean isServerEnabled(String serverName) {
        if ((serverName == null) || (serverName.length() == 0) || (getServers().length() == 0)) {
            return true;
        }
        for (String s : getServers().split(",")) {
            if (s.trim().equalsIgnoreCase(serverName.trim())) {
                return true;
            }
        }
        return false;
    }

    public void setServers(String servers) {
        if (servers == null) {
            servers = "";
        }
        this.servers = servers;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        if (enabled != null) {
            this.enabled = enabled;
        }
    }

    public void setEnabled(String enabled) {
        setEnabled(Boolean.valueOf(enabled));
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}