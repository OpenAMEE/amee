package com.jellymold.kiwi.legacy;

import com.jellymold.kiwi.User;

import javax.ejb.Local;

@Local
public interface Register {

    public User getInstance();

    public void initialize();

    public String register();

    public String cancel();

    public String getVerify();

    public void setVerify(String verify);

    public void destroy();
}