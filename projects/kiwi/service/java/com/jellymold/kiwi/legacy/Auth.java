package com.jellymold.kiwi.legacy;

import com.jellymold.kiwi.Group;
import com.jellymold.kiwi.Role;

import javax.ejb.Local;

@Local
public interface Auth {

    public void create();

    public void loadGroupMember(Group group);

    public void clearGroupMember();

    public boolean isActions(String actions);

    public boolean isLoggedIn();

    public boolean isOwner();

    public boolean isAdmin();

    public boolean isUser();

    public Role getOwnerRole();

    public Role getAdminRole();

    public Role getUserRole();

    public void destroy();
}
