package com.jellymold.kiwi.legacy;

import javax.ejb.Local;

@Local
public interface ChangePassword {

    public String changePassword();

    public String cancel();

    public String getVerify();

    public void setVerify(String verify);

    public void destroy();
}