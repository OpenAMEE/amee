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
package com.amee.service;

import com.amee.domain.AMEEEntity;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.ObjectType;
import com.amee.domain.auth.Group;
import com.amee.domain.auth.GroupPrinciple;
import com.amee.domain.auth.Permission;
import com.amee.domain.auth.User;
import com.amee.domain.data.DataCategory;
import com.amee.domain.environment.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceData {

    public Environment ENVIRONMENT;

    public Group GROUP_1;
    public Group GROUP_2;

    public User USER_1;
    public User USER_2;

    public GroupPrinciple TEST_GROUP_PRINCIPLE_1;
    public GroupPrinciple TEST_GROUP_PRINCIPLE_2;
    public static GroupPrinciple TEST_GROUP_PRINCIPLE_3;

    public DataCategory DC_ROOT;
    public DataCategory DC_1;
    public DataCategory DC_2;

    public Permission PERMISSION_1;

    public Map<IAMEEEntityReference, List<Permission>> PRINCIPLE_TO_PERMISSIONS;

    public Map<ObjectType, Long> ID_MAP;

    public void init() {
        initCollections();
        initEnvironment();
        initGroupsAndUsers();
        initDataCategories();
        initPermissions();
    }

    private void initCollections() {
        PRINCIPLE_TO_PERMISSIONS = new HashMap<IAMEEEntityReference, List<Permission>>();
        ID_MAP = new HashMap<ObjectType, Long>();
    }

    private void initEnvironment() {
        ENVIRONMENT = new Environment("Environment");
        setId(ENVIRONMENT);
    }

    private void initGroupsAndUsers() {
        GROUP_1 = new Group(ENVIRONMENT, "Group One");
        GROUP_2 = new Group(ENVIRONMENT, "Group Two");
        setId(GROUP_1, GROUP_2);

        USER_1 = new User(ENVIRONMENT, "user_1", "password", "User One");
        USER_2 = new User(ENVIRONMENT, "user_2", "password", "User Two");
        setId(USER_1, USER_2);

        TEST_GROUP_PRINCIPLE_1 = new GroupPrinciple(GROUP_1, USER_1);
        TEST_GROUP_PRINCIPLE_2 = new GroupPrinciple(GROUP_2, USER_1);
        TEST_GROUP_PRINCIPLE_3 = new GroupPrinciple(GROUP_1, USER_2);
        setId(TEST_GROUP_PRINCIPLE_1, TEST_GROUP_PRINCIPLE_2, TEST_GROUP_PRINCIPLE_3);
    }

    private void initDataCategories() {
        DC_ROOT = new DataCategory(ENVIRONMENT, "Root", "root");
        DC_1 = new DataCategory(DC_ROOT, "DC 1", "dc_1");
        DC_2 = new DataCategory(DC_ROOT, "DC 2", "dc_2");
        setId(DC_ROOT, DC_1, DC_2);
    }

    private void initPermissions() {
        // User can view DataCategory
        PERMISSION_1 = new Permission(USER_1, DC_1, Permission.VIEW);
        setId(PERMISSION_1);
        addPermissionToPrinciple(USER_1, PERMISSION_1);
    }

    private void addPermissionToPrinciple(IAMEEEntityReference principle, Permission permission) {
        List<Permission> permissions = PRINCIPLE_TO_PERMISSIONS.get(principle);
        if (permissions == null) {
            permissions = new ArrayList<Permission>();
            PRINCIPLE_TO_PERMISSIONS.put(principle, permissions);
        }
        permissions.add(permission);
    }

    private void setId(AMEEEntity... entities) {
        for (AMEEEntity entity : entities) {
            setId(entity);
        }
    }

    private void setId(AMEEEntity entity) {
        entity.setId(getNextId(entity));
    }

    private Long getNextId(AMEEEntity entity) {
        Long id = ID_MAP.get(entity.getObjectType());
        if (id == null) {
            id = 0L;
        }
        id++;
        ID_MAP.put(entity.getObjectType(), id);
        return id;
    }
}
