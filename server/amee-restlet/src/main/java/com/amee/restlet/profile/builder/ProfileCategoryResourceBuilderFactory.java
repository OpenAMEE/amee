package com.amee.restlet.profile.builder;

import com.amee.restlet.profile.ProfileCategoryResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProfileCategoryResourceBuilderFactory {

    @Resource
    private IProfileCategoryResourceBuilder v1ProfileCategoryResourceBuilder;

    @Resource
    private IProfileCategoryResourceBuilder v2ProfileCategoryResourceBuilder;

    public IProfileCategoryResourceBuilder createProfileCategoryResourceBuilder(ProfileCategoryResource resource) {
        if (resource.getAPIVersion().isVersionOne()) {
            return v1ProfileCategoryResourceBuilder;
        } else {
            return v2ProfileCategoryResourceBuilder;
        }
    }
}
