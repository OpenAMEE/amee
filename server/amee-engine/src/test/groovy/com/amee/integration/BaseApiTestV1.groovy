package com.amee.integration

import org.junit.Before

class BaseApiTestV1 extends BaseApiTest {

    @Before
    public void setUpDefaultUser() {
        setStandardUserV1()
    }
    
}
