package com.amee.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * An entity for storing misc. metadata values for other entities.
 */
@Entity
@Table(name = "METADATA")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Metadata extends AMEEEntity {

    public final static int NAME_SIZE = 255;
    // 32767 because this is bigger than 255, smaller than 65535 and fits into an exact number of bits.
    public final static int VALUE_SIZE = 32767;

    @Embedded
    private AMEEEntityReference entityReference = new AMEEEntityReference();

    @Column(name = "NAME", nullable = false, length = NAME_SIZE)
    @Index(name = "NAME_IND")
    private String name = "";

    @Column(name = "VALUE", nullable = false, length = VALUE_SIZE)
    private String value = "";

    public Metadata() {
        super();
    }

    public Metadata(IAMEEEntityReference entityReference, String name) {
        this();
        setEntityReference(entityReference);
        setName(name);
    }

    public Metadata(IAMEEEntityReference entityReference, String name, String value) {
        this(entityReference, name);
        setValue(value);
    }

    @Override
    public String toString() {
        return "Metadata_" + getUid();
    }

    public ObjectType getObjectType() {
        return ObjectType.MD;
    }

    public IAMEEEntityReference getEntityReference() {
        return entityReference;
    }

    public void setEntityReference(IAMEEEntityReference entityReference) {
        this.entityReference = new AMEEEntityReference(entityReference);
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (value == null) {
            value = "";
        }
        this.value = value;
    }
}
