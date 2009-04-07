package com.amee.domain.algorithm;

import com.amee.core.ObjectType;
import com.amee.domain.APIUtils;
import com.amee.domain.environment.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * This class defines a global algorithm context that can be inhereted by algorithms
 */
@Entity
@DiscriminatorValue("ALC")
public class AlgorithmContext extends AbstractAlgorithm {

    public AlgorithmContext() {
        super();
    }

    public AlgorithmContext(Environment environment) {
        this();
        setEnvironment(environment);
    }

    public AlgorithmContext(Environment environment, String content) {
        this(environment);
        setContent(content);
    }

    @Transient
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }

    @Transient
    public String getElementName() {
        return "AlgorithmContext";
    }

    public String toString() {
        return "AlgorithmContext_" + getUid();
    }

    @Transient
    public ObjectType getObjectType() {
        return ObjectType.ALC;
    }
}
