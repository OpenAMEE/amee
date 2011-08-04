package com.amee.restlet.profile.builder;

import com.amee.restlet.profile.ProfileItemValueResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProfileItemValueResourceBuilderFactory {

    @Resource
    private IProfileItemValueResourceBuilder v1ProfileItemValueResourceBuilder;

    @Resource
    private IProfileItemValueResourceBuilder v2ProfileItemValueResourceBuilder;

    public IProfileItemValueResourceBuilder createProfileItemValueResourceBuilder(ProfileItemValueResource resource) {
        if (resource.getAPIVersion().isVersionOne()) {
            return v1ProfileItemValueResourceBuilder;
        } else {
            return v2ProfileItemValueResourceBuilder;
        }
    }
}