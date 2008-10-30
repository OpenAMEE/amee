package com.jellymold.kiwi.legacy;

import javax.ejb.Local;

@Local
public interface ProfileDetails {

    public String update();

    public String cancel();

    public void destroy();
}
