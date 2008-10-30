package com.jellymold.kiwi.legacy;

import com.jellymold.kiwi.Group;
import com.jellymold.kiwi.auth.LoggedIn;
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
@Name("accountDetails")
@Scope(ScopeType.EVENT)
@LoggedIn(actions = "account.details")
public class AccountDetailsBean implements Serializable, AccountDetails {

    private final static Logger log = Logger.getLogger(AccountDetailsBean.class);

    @In(create = true)
    private transient EntityManager entityManager;

    @In
    @Out(required = false)
    @Valid
    private Group browseAccount;

    public String update() {
        log.debug("update()");
        entityManager.merge(browseAccount);
        return "profile";
    }

    public String cancel() {
        log.debug("cancel()");
        revertAccount();
        return "profile";
    }

    protected void revertAccount() {
        log.debug("revertUser()");
        browseAccount = entityManager.find(Group.class, browseAccount.getId());
    }

    @Destroy
    @Remove
    public void destroy() {
        log.debug("destroy()");
    }
}