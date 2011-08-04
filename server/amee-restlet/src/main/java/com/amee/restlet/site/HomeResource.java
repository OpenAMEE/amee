package com.amee.restlet.site;

import com.amee.restlet.BaseResource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component("homeResource")
@Scope("prototype")
public class HomeResource extends BaseResource implements Serializable {

    @Override
    public String getTemplatePath() {
        return "default.ftl";
    }
}