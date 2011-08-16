package com.amee.restlet.profile.acceptor;

import com.amee.domain.item.BaseItemValue;
import com.amee.restlet.profile.ProfileItemValueResource;
import org.restlet.resource.Representation;
import org.springframework.stereotype.Service;

@Service
public interface IItemValueRepresentationAcceptor {

    public BaseItemValue accept(ProfileItemValueResource resource, Representation representation);
    
}
