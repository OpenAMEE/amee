package com.jellymold.kiwi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class UserPasswordToMD5 {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    public void updateUserPasswordToMD5() {
        updateUserPasswordToMD5(false);
    }

    public void updateUserPasswordToMD5(boolean dryRun) {
        String md5;
        List<User> users = entityManager.createQuery(
                "SELECT DISTINCT u " +
                        "FROM User u")
                .getResultList();
        for (User user : users) {
            md5 = User.getAsMD5(user.getPassword());
            if (!dryRun) {
                user.setPassword(md5);
                entityManager.persist(user);
                entityManager.flush();
            }
            log.debug("Password for user '" + user.getUsername() + "' hashed as '" + md5 + "'.");
        }
    }
}
