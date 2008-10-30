package com.jellymold.kiwi.legacy;

import javax.ejb.Local;

@Local
public interface BrowseAccountService {

    public void allUsersFactory();

    public void browseUserFactory();

    public void browseUserAccountsFactory();

    public void allAccountsFactory();

    public void browseAccountFactory();

    public void browseAccountUsersFactory();

    public void destroy();
}
