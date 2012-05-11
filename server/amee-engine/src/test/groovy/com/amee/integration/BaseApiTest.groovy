package com.amee.integration

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static org.junit.Assert.*
import groovyx.net.http.RESTClient

import org.joda.time.DateTimeZone
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
 * A base class for API integration tests.
 *
 * The Restlet container is started.
 */
abstract class BaseApiTest {
    
    static def config
    static def context
    static def container

    RESTClient client

    @BeforeClass
    static void start() {

        // Augment String with a random method.
        addRandomStringMethodToString()

        // Set the default timezone
        def timeZone = TimeZone.getTimeZone(System.getProperty("AMEE_TIME_ZONE", "UTC"))
        TimeZone.setDefault(timeZone)
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(timeZone))

        // Spring application context.
        context = new ClassPathXmlApplicationContext("classpath*:applicationContext*.xml")

        // Load config.
        config = new ConfigSlurper().parse(context.getResource('classpath:api.properties').getURL())

        // Configure Restlet server (ajp, http, etc).
        def server = context.getBean("ameeServer")
        def transactionController = context.getBean("transactionController")
        server.context.attributes.transactionController = transactionController
        server.context.attributes.springContext = context

        // Start the Restlet container.
        println "Starting container..."
        container = context.getBean("ameeContainer")
        container.start()
    }

    @AfterClass
    static void stop() {
        try {
            println "Stopping container..."
            container.stop()
            context.close()
        } catch (e) {
            // Do nothing.
        }
    }

    @Before
    void setUp() {
        // Get the HTTP client
        client = new RESTClient("${config.api.protocol}://${config.api.host}:${config.api.port}")

        // Accept JSON by default
        client.contentType = JSON
    }

    def setStandardUserV1() {
        client.auth.basic config.api.standard.v1.user, config.api.standard.v1.password
        def auth = 'Basic ' + (config.api.standard.v1.user + ':' + config.api.standard.v1.password).bytes.encodeBase64().toString()
        client.headers.Authorization = auth
    }
    
    def setStandardUserV2() {
        client.auth.basic config.api.standard.v2.user, config.api.standard.v2.password
        // Can't use the built-in RESTClient auth handling as we don't send the WWW-Authenticate header in v2
        def auth = 'Basic ' + (config.api.standard.v2.user + ':' + config.api.standard.v2.password).bytes.encodeBase64().toString()
        client.headers.Authorization = auth
    }

    def setAdminUserV1() {
        client.auth.basic config.api.admin.v1.user, config.api.admin.v1.password
        def auth = 'Basic ' + (config.api.admin.v1.user + ':' + config.api.admin.v1.password).bytes.encodeBase64().toString()
        client.headers.Authorization = auth
    }
    
    def setAdminUserV2() {
        client.auth.basic config.api.admin.v2.user, config.api.admin.v2.password
        def auth = 'Basic ' + (config.api.admin.v2.user + ':' + config.api.admin.v2.password).bytes.encodeBase64().toString()
        client.headers.Authorization = auth
    }

    def setRootUser() {
        client.auth.basic config.api.root.user, config.api.root.password
        def auth = 'Basic ' + (config.api.root.user + ':' + config.api.root.password).bytes.encodeBase64().toString()
        client.headers.Authorization = auth
    }

    def setEcoinventUser() {
        client.auth.basic config.api.ecoinvent.user, config.api.ecoinvent.password
        def auth = 'Basic ' + (config.api.ecoinvent.user + ':' + config.api.ecoinvent.password).bytes.encodeBase64().toString()
        client.headers.Authorization = auth
    }

    /**
     * Add a random character generator to the String class.
     */
    static void addRandomStringMethodToString() {
        String.metaClass.'static'.randomString = { length ->
            // The chars used for the random string.
            def list = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            // Make sure the list is long enough.
            list = list * (1 + length / list.size())
            // Shuffle it up good.
            Collections.shuffle(list)
            length > 0 ? list[0..length - 1].join() : ''
        }
    }
    
    /**
    * Returns true if contains infinity and NaN amounts.
    * NB: This only works for json data.
    *
    * @param amounts    the amounts element from the response
    * @return           <code>true</code> if both Infinity and NaN results are present in the specified <code>amounts</code>,
    *                   <code>false</code> otherwise
    */
   boolean hasInfinityAndNan(amounts) {
       def hasInfinity = false
       def hasNan = false

       amounts.each {
           if (it.type == 'infinity' && it.value == 'Infinity') {
               hasInfinity = true
           }
           if (it.type == 'nan' && it.value == 'NaN') {
               hasNan = true
           }
       }
       return hasInfinity && hasNan
   }
}