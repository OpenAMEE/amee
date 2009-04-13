package com.amee.domain.algorithm;

import com.amee.domain.AMEEEntity;
import com.amee.core.APIUtils;
import com.amee.domain.environment.Environment;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Inheritance
@Table(name = "ALGORITHM")
@DiscriminatorColumn(name = "TYPE", length = 3)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class AbstractAlgorithm extends AMEEEntity {

    public final static int NAME_SIZE = 255;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    @Index(name = "NAME_IND")
    private String name = "";

    @Lob
    @Column(name = "CONTENT", nullable = true)
    private String content = "";

    @Column(name = "CREATED")
    private Date created = Calendar.getInstance().getTime();

    @Column(name = "MODIFIED")
    private Date modified = Calendar.getInstance().getTime();

    public AbstractAlgorithm() {
        super();
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
        obj.put("content", getContent());
        if (detailed) {
            obj.put("created", getCreated());
            obj.put("modified", getModified());
            obj.put("environment", getEnvironment().getIdentityJSONObject());
        }
        return obj;
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        return APIUtils.getIdentityJSONObject(this);
    }

    @Transient
    public Element getElement(Document document) {
        return getElement(document, true);
    }

    public abstract String getElementName();

    @Transient
    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement(getElementName());
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "Content", getContent()));
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
            element.appendChild(getEnvironment().getIdentityElement(document));
        }
        return element;
    }

    @PrePersist
    public void onCreate() {
        Date now = Calendar.getInstance().getTime();
        setCreated(now);
        setModified(now);
    }

    @PreUpdate
    public void onModify() {
        setModified(Calendar.getInstance().getTime());
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content == null) {
            content = "";
        }
        this.content = content;
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
}
