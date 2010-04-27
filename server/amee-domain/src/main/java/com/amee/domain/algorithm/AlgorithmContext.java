package com.amee.domain.algorithm;

import com.amee.base.utils.XMLUtils;
import com.amee.domain.environment.Environment;
import com.amee.domain.ObjectType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

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
        super(environment);
    }

    public AlgorithmContext(Environment environment, String content) {
        super(environment, content);
    }

    public String toString() {
        return "AlgorithmContext_" + getUid();
    }

    public Element getIdentityElement(Document document) {
        return XMLUtils.getIdentityElement(document, this);
    }

    @Override
    public String getElementName() {
        return "AlgorithmContext";
    }

    public ObjectType getObjectType() {
        return ObjectType.ALC;
    }
}
