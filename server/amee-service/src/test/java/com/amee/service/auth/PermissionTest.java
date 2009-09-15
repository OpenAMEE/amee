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

import com.amee.domain.auth.Group;
import com.amee.domain.auth.User;
import com.amee.domain.data.DataItem;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;
import com.amee.service.ServiceTest;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class PermissionTest extends ServiceTest {

    @Autowired
    private PermissionService permissionService;

    @Test
    public void areValidPrinciples() {
        assertTrue("Should be a valid principle", permissionService.isValidPrinciple(new Group()));
        assertTrue("Should be a valid principle", permissionService.isValidPrinciple(new User()));
    }

    @Test
    public void areNotValidPrinciples() {
        assertFalse("Should not be a valid principle", permissionService.isValidPrinciple(new DataItem()));
        assertFalse("Should not be a valid principle", permissionService.isValidPrinciple(new Profile()));
    }

    @Test
    public void areValidPrinciplesToEntities() {
        assertTrue("Should be a valid principle-to-entity", permissionService.isValidPrincipleToEntity(new Group(), new DataItem()));
        assertTrue("Should be a valid principle-to-entity", permissionService.isValidPrincipleToEntity(new User(), new ProfileItem()));
    }

    @Test
    public void areNotValidPrinciplesToEntities() {
        assertFalse("Should not be a valid principle-to-entity", permissionService.isValidPrincipleToEntity(new DataItem(), new Profile()));
        assertFalse("Should not be a valid principle-to-entity", permissionService.isValidPrincipleToEntity(new User(), new Group()));
    }
}