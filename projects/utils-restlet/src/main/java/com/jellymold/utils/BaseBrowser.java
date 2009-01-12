package com.jellymold.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

public abstract class BaseBrowser implements Serializable {

    protected final Log log = LogFactory.getLog(getClass());

}
