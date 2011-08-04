package com.amee.restlet.profile.acceptor;

import com.amee.domain.item.profile.ProfileItem;
import com.amee.restlet.profile.ProfileItemResource;
import org.restlet.data.Form;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IProfileItemFormAcceptor {

    public List<ProfileItem> accept(ProfileItemResource resource, Form form);

}
