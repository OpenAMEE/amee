package com.jellymold.kiwi.app;

import gc.carbon.auth.ResourceActions;

public class AppConstants {

    public final static String VIEW_APPS = "apps/apps.ftl";
    public final static String VIEW_APP = "apps/app.ftl";

    public final static String VIEW_ACTIONS = "apps/actions.ftl";
    public final static String VIEW_ACTION = "apps/action.ftl";

    public final static String ACTION_APP_PREFIX = "app";
    public final static String ACTION_APP_VIEW = ACTION_APP_PREFIX + ResourceActions.ACTION_VIEW;
    public final static String ACTION_APP_CREATE = ACTION_APP_PREFIX + ResourceActions.ACTION_CREATE;
    public final static String ACTION_APP_MODIFY = ACTION_APP_PREFIX + ResourceActions.ACTION_MODIFY;
    public final static String ACTION_APP_DELETE = ACTION_APP_PREFIX + ResourceActions.ACTION_DELETE;
    public final static String ACTION_APP_LIST = ACTION_APP_PREFIX + ResourceActions.ACTION_LIST;

    public final static String ACTION_ACTION_PREFIX = "app.action";
    public final static String ACTION_ACTION_VIEW = ACTION_ACTION_PREFIX + ResourceActions.ACTION_VIEW;
    public final static String ACTION_ACTION_CREATE = ACTION_ACTION_PREFIX + ResourceActions.ACTION_CREATE;
    public final static String ACTION_ACTION_MODIFY = ACTION_ACTION_PREFIX + ResourceActions.ACTION_MODIFY;
    public final static String ACTION_ACTION_DELETE = ACTION_ACTION_PREFIX + ResourceActions.ACTION_DELETE;
    public final static String ACTION_ACTION_LIST = ACTION_ACTION_PREFIX + ResourceActions.ACTION_LIST;
}