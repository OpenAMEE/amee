package com.jellymold.kiwi.auth;
//
//import com.jellymold.kiwi.GroupUser;
////import org.jboss.seam.annotations.Interceptor;
////import org.jboss.seam.contexts.Lifecycle;
//import org.jboss.seam.interceptors.BijectionInterceptor;
//import org.jboss.seam.interceptors.BusinessProcessInterceptor;
//import org.jboss.seam.interceptors.ConversationInterceptor;
//import org.jboss.seam.interceptors.RemoveInterceptor;
//import org.jboss.seam.interceptors.ValidationInterceptor;
//
//import javax.faces.event.PhaseId;
//import javax.interceptor.AroundInvoke;
//import javax.interceptor.InvocationContext;
//import java.lang.reflect.Method;
//
//// TODO: need to deal with rollbacks and alternate outcomes
//
//@Autowiredterceptor(around = {BijectionInterceptor.class, ValidationInterceptor.class,
//        ConversationInterceptor.class, BusinessProcessInterceptor.class},
//        within = RemoveInterceptor.class)
//public class LoggedInInterceptor {
//
//    (LoggedInInterceptor.class);
//
//    // TODO: performance?
//    @AroundInvoke
//    public Object checkLoggedIn(InvocationContext invocation)
//            throws Exception {
//        boolean isLoggedIn = Contexts.getSessionContext().get(LoggedIn.LOGIN_KEY) != null;
//        if (Lifecycle.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
//            if (isLoggedIn && isAuthorized(invocation)) {
//                return invocation.proceed();
//            } else {
//                log.debug("checkLoggedIn() show login");
//                return "login";
//            }
//        } else {
//            if (isLoggedIn && isAuthorized(invocation)) {
//                return invocation.proceed();
//            } else {
//                Method method = invocation.getMethod();
//                if (method.getReturnType().equals(void.class)) {
//                    return null;
//                } else {
//                    return method.invoke(invocation.getTarget(), invocation.getParameters());
//                }
//            }
//        }
//    }
//
//    // TODO: performance?
//    private boolean isAuthorized(InvocationContext invocation) {
//
//        LoggedIn anno;
//
//        log.debug("isAuthorized() for: " + invocation.getMethod().getName());
//
//        // first check method annotation
//        anno = invocation.getMethod().getAnnotation(LoggedIn.class);
//        if (anno == null) {
//            // then check class annotation
//            anno = invocation.getMethod().getDeclaringClass().getAnnotation(LoggedIn.class);
//        }
//
//        // only check roles/actions if LoggedIn annotation has roles/actions specified
//        if (anno != null) {
//            log.debug("isAuthorized() got LoggedIn annotation");
//            // check roles
//            String roles = anno.roles();
//            if (roles != null && roles.length() > 0) {
//                log.debug("isAuthorized() roles: " + roles);
//                GroupUser groupUser = (GroupUser) Contexts.lookupInStatefulContexts("groupUser");
//                if (groupUser == null || !groupUser.hasRoles(roles)) {
//                    // roles not ok, refuse access
//                    log.debug("isAuthorized() not authorized");
//                    return false;
//                }
//            }
//            // check actions
//            String actions = anno.actions();
//            if (actions != null && actions.length() > 0) {
//                log.debug("isAuthorized() actions: " + actions);
//                GroupUser groupUser = (GroupUser) Contexts.lookupInStatefulContexts("groupUser");
//                if (groupUser == null || !groupUser.hasActions(actions)) {
//                    // actions not ok, refuse access
//                    log.debug("isAuthorized() not authorized");
//                    return false;
//                }
//            }
//        }
//
//        // authorized or roles/actions not required
//        log.debug("isAuthorized() authorized");
//        return true;
//    }
//}
