package com.jellymold.kiwi;


import com.jellymold.utils.ThreadBeanHolder;

import java.util.Calendar;

public class AuditableUtils {

    public static void onCreate(AuditableObject o) {
        o.getUid();
        if (o.getCreatedBy() == null) {
            Object user = ThreadBeanHolder.get("user");
            if (user instanceof User) {
                o.setCreatedBy((User) user);
                o.setModifiedBy((User) user);
            }
        }
        o.setCreated(Calendar.getInstance().getTime());
        o.setModified(o.getCreated());
    }

    public static void onModify(AuditableObject o) {
        Object user = ThreadBeanHolder.get("user");
        if (user instanceof User) {
            o.setModifiedBy((User) user);
        }
        o.setModified(Calendar.getInstance().getTime());
    }
}
