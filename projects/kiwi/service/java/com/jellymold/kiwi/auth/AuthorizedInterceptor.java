package com.jellymold.kiwi.auth;
//
////import org.jboss.seam.Component;
//import org.jboss.seam.annotations.Interceptor;
//import org.jboss.seam.interceptors.BijectionInterceptor;
//import org.jboss.seam.interceptors.RemoveInterceptor;
//
//import javax.interceptor.AroundInvoke;
//import javax.interceptor.InvocationContext;
//import java.lang.reflect.Method;
//
//@Autowiredterceptor(around = BijectionInterceptor.class, within = RemoveInterceptor.class)
//public class AuthorizedInterceptor {
//
//    (AuthorizedInterceptor.class);
//
//    boolean checked = false;
//    boolean authorized = false;
//
//    @AroundInvoke
//    public Object checkAuthorized(InvocationContext invocation) throws Exception {
//
//        Authorized anno;
//        String roles;
//        String actions;
//        AuthService authService;
//        Object target = invocation.getTarget();
//        Method method = invocation.getMethod();
//
//        // anything to check?
//        if (!target.getClass().isAnnotationPresent(Authorized.class) && !method.isAnnotationPresent(Authorized.class)) {
//            // nothing to do here
//            return invocation.proceed();
//        }
//
//        // have we checked before?
//        if (checked) {
//            return proceedOrReject(invocation);
//        }
//
//        // get AuthService for authorization
//        authService = (AuthService) Component.getInstance("authService", true);
//
//        // get the annotation
//        // first look for method annotation
//        anno = method.getAnnotation(Authorized.class);
//        if (anno == null) {
//            // then look for class annotation
//            anno = target.getClass().getAnnotation(Authorized.class);
//        }
//
//        // check roles first
//        roles = anno.roles();
//        if ((roles != null) && (roles.length() > 0)) {
//            if (!authService.hasRoles(roles)) {
//                log.debug("roles authorized");
//                authorized = true;
//            }
//        }
//
//        // check actions next, if necessary
//        if (!authorized) {
//            actions = anno.actions();
//            if ((actions != null) && (actions.length() > 0)) {
//                if (authService.hasActions(actions)) {
//                    log.debug("actions authorized");
//                    authorized = true;
//                }
//            }
//        }
//
//        // checked for the first time
//        checked = true;
//        return proceedOrReject(invocation);
//    }
//
//    protected Object proceedOrReject(InvocationContext invocation) throws Exception {
//        if (authorized) {
//            return invocation.proceed();
//        } else {
//            return null; // TODO: what should we return?
//        }
//    }
//}