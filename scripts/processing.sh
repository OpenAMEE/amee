out="/Users/stevematthew/IdeaProjects/amee-processing/out/production/amee-processing"
libs="/Development/AMEE/git/new/amee/lib"
processing="/Applications/Processing.app/Contents/Resources/Java/"
joda="/Users/stevematthew/development/joda-time-1.5.2-src"
cp="${out}:${processing}/core.jar:${joda}/joda-time-1.5.2.jar:${libs}/restlet/org.mortbay.jetty_6.1/org.mortbay.jetty.jar:${libs}/restlet/org.mortbay.jetty_6.1/org.mortbay.jetty.util.jar:${libs}/restlet/org.restlet.ext.fileupload.jar:${libs}/restlet/org.mortbay.jetty_6.1/org.mortbay.jetty.ajp.jar:${libs}/restlet/org.json.jar:${libs}/restlet/org.restlet.jar:${libs}/restlet/org.apache.commons.fileupload.jar:${libs}/restlet/com.noelios.restlet.ext.jetty.jar:${libs}/restlet/com.noelios.restlet.ext.net.jar:${libs}/restlet/org.restlet.ext.spring.jar:${libs}/restlet/com.noelios.restlet.jar:${libs}/restlet/org.mortbay.jetty_6.1/org.mortbay.jetty.https.jar:${libs}/restlet/org.restlet.ext.freemarker.jar:${libs}/restlet/org.restlet.ext.json.jar:${libs}/jdom/jdom.jar:${libs}/jdom/xalan.jar:${libs}/jdom/xml-apis.jar:${libs}/jdom/xerces.jar:${libs}/jetty/jetty.jar:${libs}/jetty/jetty-ajp.jar:${libs}/jetty/jetty-util.jar:${libs}/spring/j2ee/persistence.jar:${libs}/spring/j2ee/servlet-api.jar:${libs}/spring/j2ee/jta.jar"

echo ${cp}
java -classpath ${cp} net.dgen.amee.processing.History /home/energy/electricity/ 20081208T1532 20081208T1703
#java -classpath ${cp} net.dgen.amee.processing.History /data/home/energy/quantity 20081202T1225 20081206T0000

