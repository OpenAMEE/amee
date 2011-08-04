package com.amee.restlet.profile.acceptor;

import com.amee.domain.item.profile.ProfileItem;
import com.amee.restlet.profile.ProfileCategoryResource;
import com.amee.restlet.utils.APIException;
import org.restlet.resource.Representation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IProfileCategoryRepresentationAcceptor {

    List<ProfileItem> accept(ProfileCategoryResource resource, Representation representation) throws APIException;
}
