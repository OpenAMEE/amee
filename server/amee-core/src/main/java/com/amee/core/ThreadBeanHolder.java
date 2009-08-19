package com.amee.core;

import java.util.HashMap;
import java.util.Map;

public abstract class ThreadBeanHolder {

    private static final ThreadLocal<Map<String, Object>> BEAN_HOLDER = new ThreadLocal<Map<String, Object>>();

    public static Object get(String name) {
        if (BEAN_HOLDER.get() != null) {
            return BEAN_HOLDER.get().get(name);
        } else {
            return null;
        }
    }

    public static void set(String name, Object obj) {
        if (BEAN_HOLDER.get() == null) {
            BEAN_HOLDER.set(new HashMap<String, Object>());
        }
        BEAN_HOLDER.get().put(name, obj);
    }

    public static void clear() {
        BEAN_HOLDER.set(null);
    }
}