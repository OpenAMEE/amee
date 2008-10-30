package com.jellymold.kiwi.legacy;

import org.apache.log4j.Logger;
import org.hibernate.validator.Valid;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Outcome;
import org.jboss.seam.annotations.Scope;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import com.jellymold.kiwi.User;
import com.jellymold.kiwi.auth.LoggedIn;

@Stateful
@Scope(ScopeType.EVENT)
@Name("changePassword")
@LoggedIn
public class ChangePasswordBean implements ChangePassword {

    private final static Logger log = Logger.getLogger(ChangePasswordBean.class);

    @In(create = true)
    private transient EntityManager entityManager;

    @In(create = true)
    private transient FacesContext facesContext;

    @In
    @Out
    @Valid
    private User user;

    private String verify;

    public String changePassword() {

        log.debug("changePassword()");

        if (user.getPassword().equals(verify)) {
            user = entityManager.merge(user);
            return "profile";
        } else {
            facesContext.addMessage(null, new FacesMessage("Re-enter new password"));
            revertUser();
            verify = null;
            return Outcome.REDISPLAY;
        }
    }

    public String cancel() {
        log.debug("cancel()");
        revertUser();
        return "profile";
    }

    private void revertUser() {
        log.debug("revertUser()");
        user = entityManager.find(User.class, user.getId());
    }

    public String getVerify() {
        return verify;
    }

    public void setVerify(String verify) {
        this.verify = verify;
    }

    @Destroy
    @Remove
    public void destroy() {
        log.debug("destroy()");
    }
}
