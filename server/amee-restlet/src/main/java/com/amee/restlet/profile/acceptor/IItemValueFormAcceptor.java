package com.amee.restlet.profile.acceptor;

import com.amee.domain.item.BaseItemValue;
import com.amee.restlet.profile.ProfileItemValueResource;
import org.restlet.data.Form;
import org.springframework.stereotype.Service;

@Service
public interface IItemValueFormAcceptor {

    public BaseItemValue accept(ProfileItemValueResource resource, Form form);

}
