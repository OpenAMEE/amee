#!/bin/sh

# Source folders
JM_SRC="/Development/JellyMoldOpen/projects"
DGEN_SRC="/Development/GlobalCoolEngine"
LIB_SRC="/Development/lib"

# Destination folders
DST="/Development/amee-deploy"
BIN_DST="$DST/bin"
CONF_DST="$DST/conf"
JM_DST="$DST/jm"
DGEN_DST="$DST/dgen"
LIB_DST="$DST/lib"

# bin
mkdir -p $BIN_DST
cp $DGEN_SRC/co2-engine/bin/after-install.sh $BIN_DST
cp $DGEN_SRC/co2-engine/bin/after-install.sql $BIN_DST
cp $DGEN_SRC/co2-engine/bin/amee $BIN_DST
cp $DGEN_SRC/co2-engine/bin/wrapper-macosx-x86-32 $BIN_DST
cp $DGEN_SRC/co2-engine/bin/wrapper-linux-x86-32 $BIN_DST

# conf
mkdir -p $CONF_DST
cp $DGEN_SRC/co2-engine/conf/wrapper.conf $CONF_DST
cp $DGEN_SRC/co2-engine/conf/*.xml $CONF_DST

# dgen libraries
mkdir -p $DGEN_DST
cp $DGEN_SRC/co2-domain/co2-domain.jar $DGEN_DST
cp $DGEN_SRC/co2-engine/co2-engine.jar $DGEN_DST
cp $DGEN_SRC/co2-engine-config/co2-engine-config.jar $DGEN_DST
cp $DGEN_SRC/co2-restlet/co2-restlet.jar $DGEN_DST
cp $DGEN_SRC/co2-service/co2-service.jar $DGEN_DST

# Jelly Mold libraries
mkdir -p $JM_DST
cp $JM_SRC/cache/restlet/cache-restlet.jar $JM_DST
cp $JM_SRC/cache/service/cache-service.jar $JM_DST
cp $JM_SRC/engine/service/engine-service.jar $JM_DST
cp $JM_SRC/kiwi/domain/kiwi-domain.jar $JM_DST
cp $JM_SRC/kiwi/restlet/kiwi-restlet.jar $JM_DST
cp $JM_SRC/kiwi/service/kiwi-service.jar $JM_DST
cp $JM_SRC/org.restlet.ext.seam/org.restlet.ext.seam.jar $JM_DST
cp $JM_SRC/plum/domain/plum-domain.jar $JM_DST
cp $JM_SRC/plum/restlet/plum-restlet.jar $JM_DST
cp $JM_SRC/plum/service/plum-service.jar $JM_DST
cp $JM_SRC/sheet/domain/sheet-domain.jar $JM_DST
cp $JM_SRC/utils/utils.jar $JM_DST

# Seam
mkdir -p $LIB_DST/jboss-seam/lib
cp $LIB_SRC/jboss-seam/jboss-seam.jar $LIB_DST/jboss-seam
cp $LIB_SRC/jboss-seam/lib/servlet-api.jar $LIB_DST/jboss-seam/lib
cp $LIB_SRC/jboss-seam/lib/thirdparty-all.jar $LIB_DST/jboss-seam/lib
cp $LIB_SRC/jboss-seam/lib/jboss-ejb3-all.jar $LIB_DST/jboss-seam/lib
cp $LIB_SRC/jboss-seam/lib/hibernate-all.jar $LIB_DST/jboss-seam/lib
cp $LIB_SRC/jboss-seam/lib/commons-codec-1.3.jar $LIB_DST/jboss-seam/lib
cp $LIB_SRC/jboss-seam/lib/commons-lang-2.1.jar $LIB_DST/jboss-seam/lib
cp $LIB_SRC/jboss-seam/lib/jboss-el-api.jar $LIB_DST/jboss-seam/lib
cp $LIB_SRC/jboss-seam/lib/jboss-el.jar $LIB_DST/jboss-seam/lib
cp $LIB_SRC/jboss-seam/lib/jsf-api.jar $LIB_DST/jboss-seam/lib
cp $LIB_SRC/jboss-seam/lib/jsf-impl.jar $LIB_DST/jboss-seam/lib
cp $LIB_SRC/jboss-seam/lib/jstl-1.1.0.jar $LIB_DST/jboss-seam/lib
cp $LIB_SRC/jboss-seam/lib/richfaces-3.0.1.jar $LIB_DST/jboss-seam/lib

# EHCache
mkdir -p $LIB_DST/ehcache
cp $LIB_SRC/ehcache/ehcache-1.3.0.jar $LIB_DST/ehcache

# backport-util-concurrent (for EHCache)
mkdir -p $LIB_DST/backport-util-concurrent
cp $LIB_SRC/backport-util-concurrent/backport-util-concurrent.jar $LIB_DST/backport-util-concurrent

# FreeMarker
mkdir -p $LIB_DST/freemarker/lib
cp $LIB_SRC/freemarker/lib/freemarker.jar $LIB_DST/freemarker/lib

# Jetty
mkdir -p $LIB_DST/jetty-6.1/lib/ext
cp $LIB_SRC/jetty-6.1/lib/jetty-6.1.2rc4.jar $LIB_DST/jetty-6.1/lib
cp $LIB_SRC/jetty-6.1/lib/ext/jetty-ajp-6.1.2rc4.jar $LIB_DST/jetty-6.1/lib/ext
cp $LIB_SRC/jetty-6.1/lib/jetty-util-6.1.2rc4.jar $LIB_DST/jetty-6.1/lib
cp $LIB_SRC/jetty-6.1/lib/ext/jetty-sslengine-6.1.2rc4.jar $LIB_DST/jetty-6.1/lib/ext
cp $LIB_SRC/jetty-6.1/lib/ext/jetty-java5-threadpool-6.1.2rc4.jar $LIB_DST/jetty-6.1/lib/ext

# Restlet
mkdir -p $LIB_DST/restlet/lib/org.apache.commons.fileupload_1.2
mkdir -p $LIB_DST/restlet/lib/org.json_2.0
cp $LIB_SRC/restlet/lib/org.restlet.jar $LIB_DST/restlet/lib
cp $LIB_SRC/restlet/lib/com.noelios.restlet.jar $LIB_DST/restlet/lib
cp $LIB_SRC/restlet/lib/org.restlet.ext.freemarker_2.3.jar $LIB_DST/restlet/lib
cp $LIB_SRC/restlet/lib/com.noelios.restlet.ext.net.jar $LIB_DST/restlet/lib
cp $LIB_SRC/restlet/lib/org.restlet.ext.json_2.0.jar $LIB_DST/restlet/lib
cp $LIB_SRC/restlet/lib/org.restlet.ext.fileupload_1.2.jar $LIB_DST/restlet/lib
cp $LIB_SRC/restlet/lib/org.apache.commons.fileupload_1.2/org.apache.commons.fileupload.jar $LIB_DST/restlet/lib/org.apache.commons.fileupload_1.2
cp $LIB_SRC/restlet/lib/com.noelios.restlet.ext.jetty_6.1.jar $LIB_DST/restlet/lib
cp $LIB_SRC/restlet/lib/org.json_2.0/org.json.jar $LIB_DST/restlet/lib/org.json_2.0

# MySQL
mkdir -p $LIB_DST/mysql
cp $LIB_SRC/mysql-connector-java/mysql-connector-java-5.1.0-bin.jar $LIB_DST/mysql

# Jakarta Commons
mkdir -p $LIB_DST/commons-io-1.2
cp $LIB_SRC/commons-io-1.2/commons-io-1.2.jar $LIB_DST/commons-io-1.2

# Rhino
mkdir -p $LIB_DST/rhino
cp $LIB_SRC/rhino/js.jar $LIB_DST/rhino

# JavaCSV
mkdir -p $LIB_DST/javacsv
cp $LIB_SRC/javacsv/javacsv2.0.jar $LIB_DST/javacsv

# Wrapper
mkdir -p $LIB_DST/wrapper/lib
cp $LIB_SRC/wrapper/lib/wrapper.jar $LIB_DST/wrapper/lib
cp $LIB_SRC/wrapper/lib/libwrapper-linux-x86-32.so $LIB_DST/wrapper/lib
cp $LIB_SRC/wrapper/lib/libwrapper-macosx-x86-32.jnilib $LIB_DST/wrapper/lib

# javassist
mkdir -p $LIB_DST/javassist
cp $LIB_SRC/javassist/javassist-3.4.ga.jar $LIB_DST/javassist/javassist-3.4.ga.jar

# SVNKit
mkdir -p $LIB_DST/svnkit
cp $LIB_SRC/svnkit/svnkit.jar $LIB_DST/svnkit/svnkit.jar

# commons-net
mkdir -p $LIB_DST/commons-net
cp $LIB_SRC/commons-net/commons-net-1.4.1.jar $LIB_DST/commons-net/commons-net-1.4.1.jar

# quartz
mkdir -p $LIB_DST/quartz
cp $LIB_SRC/quartz/quartz-1.5.2.jar $LIB_DST/quartz/quartz-1.5.2.jar

# commons-cli
mkdir -p $LIB_DST/commons-cli
cp $LIB_SRC/commons-cli/commons-cli-1.1.jar $LIB_DST/commons-cli/commons-cli-1.1.jar
