package com.jellymold.kiwi.auth;
//
//import org.jboss.seam.annotations.Interceptor;
////import org.jboss.seam.interceptors.BijectionInterceptor;
//import org.jboss.seam.interceptors.BusinessProcessInterceptor;
//import org.jboss.seam.interceptors.ConversationInterceptor;
//import org.jboss.seam.interceptors.RemoveInterceptor;
//import org.jboss.seam.interceptors.ValidationInterceptor;
//
//import javax.interceptor.AroundInvoke;
//import javax.interceptor.InvocationContext;
//
//@Autowiredterceptor(around = {BijectionInterceptor.class,
//        ValidationInterceptor.class,
//        ConversationInterceptor.class,
//        BusinessProcessInterceptor.class},
//        within = RemoveInterceptor.class)
//public class AuthenticatedInterceptor {
//
//    boolean checked = false;
//    boolean authenticated = false;
//
//    @AroundInvoke
//    public Object checkAuthenticated(InvocationContext invocation) throws Exception {
//        // short cut for multiple invocations
//        if (checked) {
//            return handleInvocation(invocation);
//        }
//        // authenticated if User object is available
//        authenticated = Contexts.lookupInStatefulContexts("user") != null;
//        checked = true;
//        return handleInvocation(invocation);
//    }
//
//    public Object handleInvocation(InvocationContext invocation) throws Exception {
//        if (authenticated) {
//            return invocation.proceed();
//        } else {
//            // TODO: do what?
//            return null;
//        }
//    }
//}