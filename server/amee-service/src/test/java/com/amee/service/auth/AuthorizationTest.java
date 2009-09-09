/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.service.auth;

import com.amee.service.ServiceTest;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class AuthorizationTest extends ServiceTest {

    @Autowired
    private AuthorizationService authorizationService;

    @Test
    public void standardUserViewPublicDataCategory() {
        AuthorizationContext ac = new AuthorizationContext();
        ac.addPrinciple(serviceData.USER_STANDARD);
        ac.addPrinciple(serviceData.GROUP_STANDARD);
        ac.addEntity(serviceData.DC_ROOT);
        ac.addEntity(serviceData.DC_PUBLIC);
        ac.addEntries("view");
        assertTrue("Standard user should be able to view public data category.", authorizationService.isAuthorized(ac));
    }

    @Test
    public void standardUserNotViewPremiumDataCategory() {
        AuthorizationContext ac = new AuthorizationContext();
        ac.addPrinciple(serviceData.USER_STANDARD);
        ac.addPrinciple(serviceData.GROUP_STANDARD);
        ac.addEntity(serviceData.DC_ROOT);
        ac.addEntity(serviceData.DC_PREMIUM);
        ac.addEntries("view");
        assertFalse("Standard user should not be able to view premium data category.", authorizationService.isAuthorized(ac));
    }
}
