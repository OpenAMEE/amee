package com.jellymold.kiwi.legacy;

import com.jellymold.kiwi.User;
import org.apache.log4j.Logger;
import org.hibernate.validator.Valid;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import java.io.Serializable;

@Stateful
@Name("profileDetails")
@Scope(ScopeType.EVENT)
public class ProfileDetailsBean implements Serializable, ProfileDetails {

    private final static Logger log = Logger.getLogger(ProfileDetailsBean.class);

    @In(create = true)
    private transient EntityManager entityManager;

    @In
    @Out(required = false)
    @Valid
    private User user;

    public String update() {
        log.debug("update()");
        entityManager.merge(user);
        return "profile";
    }

    public String cancel() {
        log.debug("cancel()");
        revertUser();
        return "profile";
    }

    protected void revertUser() {
        log.debug("revertUser()");
        user = entityManager.find(User.class, user.getId());
    }

    @Destroy
    @Remove
    public void destroy() {
        log.debug("destroy()");
    }
}