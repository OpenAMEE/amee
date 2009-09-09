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
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.environment.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceData {

    public Environment ENVIRONMENT;
    public Group GROUP_STANDARD, GROUP_PREMIUM;
    public User USER_STANDARD, USER_PREMIUM;
    public GroupPrinciple GROUP_STANDARD_USER_STANDARD, GROUP_STANDARD_USER_PREMIUM, GROUP_PREMIUM_USER_PREMIUM;
    public ItemDefinition ID_PUBLIC, ID_PREMIUM;
    public DataCategory DC_ROOT, DC_PUBLIC, DC_PREMIUM;
    public DataItem DI_PUBLIC, DI_PREMIUM;
    public Permission PERMISSION_1, PERMISSION_2, PERMISSION_3;
    public Map<IAMEEEntityReference, List<Permission>> PRINCIPLE_TO_PERMISSIONS;
    public Map<ObjectType, Long> ID_MAP;

    public void init() {
        initCollections();
        initEnvironment();
        initDefinitions();
        initDataCategories();
        initDataItems();
        initGroupsAndUsers();
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

    private void initDefinitions() {
        ID_PUBLIC = new ItemDefinition(ENVIRONMENT, "Item Definition Public");
        ID_PREMIUM = new ItemDefinition(ENVIRONMENT, "Item Definition Premium");
        setId(ID_PUBLIC, ID_PREMIUM);
    }

    private void initDataCategories() {
        DC_ROOT = new DataCategory(ENVIRONMENT, "Root", "root");
        DC_PUBLIC = new DataCategory(DC_ROOT, "DC Public", "dc_public");
        DC_PREMIUM = new DataCategory(DC_ROOT, "DC 2", "dc_premium");
        setId(DC_ROOT, DC_PUBLIC, DC_PREMIUM);
    }

    private void initDataItems() {
        DI_PUBLIC = new DataItem(DC_PUBLIC, ID_PUBLIC);
        DI_PREMIUM = new DataItem(DC_PREMIUM, ID_PREMIUM);
        setId(DI_PUBLIC, DI_PREMIUM);
    }

    private void initGroupsAndUsers() {
        // Groups
        GROUP_STANDARD = new Group(ENVIRONMENT, "Group Standard");
        GROUP_PREMIUM = new Group(ENVIRONMENT, "Group Premium");
        setId(GROUP_STANDARD, GROUP_PREMIUM);
        // Users
        USER_STANDARD = new User(ENVIRONMENT, "user_standard", "password", "User Standard");
        USER_PREMIUM = new User(ENVIRONMENT, "user_premium", "password", "User Premium");
        setId(USER_STANDARD, USER_PREMIUM);
        // Users in Groups
        GROUP_STANDARD_USER_STANDARD = new GroupPrinciple(GROUP_STANDARD, USER_STANDARD);
        GROUP_STANDARD_USER_PREMIUM = new GroupPrinciple(GROUP_STANDARD, USER_PREMIUM);
        GROUP_PREMIUM_USER_PREMIUM = new GroupPrinciple(GROUP_PREMIUM, USER_PREMIUM);
        setId(GROUP_STANDARD_USER_STANDARD, GROUP_STANDARD_USER_PREMIUM, GROUP_PREMIUM_USER_PREMIUM);
    }

    private void initPermissions() {
        // Standard group members can view root data category.
        PERMISSION_1 = new Permission(GROUP_STANDARD, DC_ROOT, Permission.VIEW);
        setId(PERMISSION_1);
        addPermissionToPrinciple(GROUP_STANDARD, PERMISSION_1);
        // Standard group members can not view premium data category.
        PERMISSION_2 = new Permission(GROUP_STANDARD, DC_PREMIUM, Permission.VIEW_DENY);
        setId(PERMISSION_2);
        addPermissionToPrinciple(GROUP_STANDARD, PERMISSION_2);
        // Premium group members can view premium data category.
        PERMISSION_3 = new Permission(GROUP_PREMIUM, DC_PREMIUM, Permission.VIEW);
        setId(PERMISSION_3);
        addPermissionToPrinciple(GROUP_STANDARD, PERMISSION_3);
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
