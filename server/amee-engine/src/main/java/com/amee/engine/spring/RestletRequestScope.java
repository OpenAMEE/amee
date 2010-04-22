package com.amee.engine.spring;

public class RestletRequestScope {

}
//
//public class RestletRequestScope implements Scope {
//
//    public Object get(String name, ObjectFactory objectFactory) {
//        Object scopedObject = Request.getCurrent().getAttributes().get(name);
//        if (scopedObject == null) {
//            scopedObject = objectFactory.getObject();
//            Request.getCurrent().getAttributes().put(name, scopedObject);
//        }
//        return scopedObject;
//    }
//
//    public Object remove(String name) {
//        Object scopedObject = Request.getCurrent().getAttributes().get(name);
//        if (scopedObject != null) {
//            Request.getCurrent().getAttributes().remove(name);
//            return scopedObject;
//        } else {
//            return null;
//        }
//    }
//
//    public void registerDestructionCallback(String name, Runnable callback) {
//        // not supported
//    }
//
//    public String getConversationId() {
//        // not supported
//        return null;
//    }
//}
