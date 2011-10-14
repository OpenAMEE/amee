package com.amee.admin.restlet.environment;

import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.auth.AccessSpecification;
import com.amee.domain.auth.PermissionEntry;
import com.amee.restlet.AuthorizeResource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component("adminResource")
@Scope("prototype")
public class AdminResource extends AuthorizeResource implements Serializable {

    @Override
    public String getTemplatePath() {
        return "admin.ftl";
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(getRootDataCategory());
        return entities;
    }

    @Override
    public List<AccessSpecification> getGetAccessSpecifications() {
        List<AccessSpecification> accessSpecifications = new ArrayList<AccessSpecification>();
        for (IAMEEEntityReference entity : getDistinctEntities()) {

            // Require the ADMIN permission.
            accessSpecifications.add(new AccessSpecification(entity, PermissionEntry.ADMIN));
        }
        return accessSpecifications;
    }
}