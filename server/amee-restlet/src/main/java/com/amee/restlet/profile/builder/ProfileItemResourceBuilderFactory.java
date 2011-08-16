package com.amee.restlet.profile.builder;

import com.amee.restlet.profile.ProfileItemResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProfileItemResourceBuilderFactory {

    @Resource
    private IProfileItemResourceBuilder v1ProfileItemResourceBuilder;

    @Resource
    private IProfileItemResourceBuilder v2ProfileItemResourceBuilder;

    public IProfileItemResourceBuilder createProfileItemResourceBuilder(ProfileItemResource resource) {
        if (resource.getAPIVersion().isVersionOne()) {
            return v1ProfileItemResourceBuilder;
        } else {
            return v2ProfileItemResourceBuilder;
        }
    }
}