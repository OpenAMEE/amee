package com.jellymold.kiwi.legacy;

import com.jellymold.kiwi.site.SiteService;
import com.jellymold.kiwi.User;
import com.jellymold.kiwi.auth.LoggedIn;
import org.apache.log4j.Logger;
import org.hibernate.validator.Valid;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Conversational;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.IfInvalid;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Outcome;
import org.jboss.seam.contexts.Contexts;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.util.List;

@Stateful
@Name("registerBean")

@Conversational(ifNotBegunOutcome = "home", initiator = true)
public class RegisterBean implements Register {

    private final static Logger log = Logger.getLogger(RegisterBean.class);

    @In(create = true)
    private transient EntityManager entityManager;

    @In(create = true)
    private transient FacesContext facesContext;

    @In(create = true)
    private transient SiteService siteService;

    private String verify;

    @Out(required = false)
    User user;

    @Valid
    private User newUser;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public User getInstance() {
        return newUser;
    }

    @Create
    @Begin
    public void initialize() {
        log.debug("initialize()");
        newUser = new User(siteService.getSite());
        Contexts.getSessionContext().remove(LoggedIn.LOGIN_KEY); // to be sure
    }

    @End(ifOutcome = {"home"})
    @IfInvalid(outcome = Outcome.REDISPLAY)
    public String register() {

        log.debug("register()");

        // TODO: this should be a custom validator
        if (!newUser.getPassword().equals(verify)) {
            facesContext.addMessage(null, new FacesMessage("re-enter your password"));
            verify = null;
            return Outcome.REDISPLAY;
        }

        // check that the username is unique within site
        List existing = entityManager.createQuery("select username from User where username = :username and site = :site")
                .setParameter("username", newUser.getUsername())
                .setParameter("site", siteService.getSite())
                .getResultList();

        // TODO: should we create an account now too?
        // TODO: just send an event and if something needs an account it can be created

        if (existing.size() == 0) {
            // save user, set as logged in and show home page
            entityManager.persist(newUser);
            user = newUser;
            Contexts.getSessionContext().set(LoggedIn.LOGIN_KEY, true);
            return "home";

        } else {
            // username was not unique
            facesContext.addMessage(null, new FacesMessage("username already exists"));
            return Outcome.REDISPLAY;
        }
    }

    @End
    public String cancel() {
        log.debug("cancel()");
        return "home";
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
