package com.amee.domain.profile;

import com.amee.domain.AMEEStatus;
import com.amee.domain.auth.User;
import org.junit.Test;
import static org.junit.Assert.*;


public class ProfileTest {

    @Test
    public void isTrash() {

        // A Profile should be considered trashed if:
        // itself is trashed or its User is trashed.

        Profile profile = new Profile();
        User user = new User();
        profile.setUser(user);

        assertFalse("Profile should not be trashed", profile.isTrash());

        user.setStatus(AMEEStatus.TRASH);
        assertTrue("Profile should be trashed", profile.isTrash());
    }
}
