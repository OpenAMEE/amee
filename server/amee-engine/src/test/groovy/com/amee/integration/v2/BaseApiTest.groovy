package com.amee.integration.v2

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

        // Set standard user as default.
        setStandardUser()
    }

    def setStandardUser() {
        client.auth.basic config.api.standard.user, config.api.standard.password
        // Can't use the built-in RESTClient auth handling as we don't send the WWW-Authenticate header in v2
        def auth = 'Basic ' + (config.api.standard.user + ':' + config.api.standard.password).bytes.encodeBase64().toString()
        client.headers.Authorization = auth
    }

    def setAdminUser() {
        client.auth.basic config.api.admin.user, config.api.admin.password
        def auth = 'Basic ' + (config.api.admin.user + ':' + config.api.admin.password).bytes.encodeBase64().toString()
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
}