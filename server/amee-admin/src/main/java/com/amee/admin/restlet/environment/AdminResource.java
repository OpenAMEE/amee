package com.amee.admin.restlet.environment;

import com.amee.restlet.BaseResource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component("adminResource")
@Scope("prototype")
public class AdminResource extends BaseResource implements Serializable {

    @Override
    public String getTemplatePath() {
        return "admin.ftl";
    }
}