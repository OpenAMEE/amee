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
package com.amee.domain;

import com.amee.core.ThreadBeanHolder;
import com.amee.domain.auth.User;
import com.amee.domain.data.LocaleName;
import com.amee.domain.environment.Environment;
import com.amee.domain.environment.EnvironmentObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@MappedSuperclass
public abstract class AMEEEnvironmentEntity extends AMEEEntity implements EnvironmentObject {

    public final static int UID_SIZE = 12;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @OneToMany(mappedBy = "entity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @MapKey(name = "locale")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Map<String, LocaleName> localeNames = new HashMap<String, LocaleName>();

    public AMEEEnvironmentEntity() {
        super();
    }

    public AMEEEnvironmentEntity(Environment environment) {
        this();
        setEnvironment(environment);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        if (environment != null) {
            this.environment = environment;
        }
    }

    public Map<String, LocaleName> getLocaleNames() {
        return localeNames;
    }

    @SuppressWarnings("unchecked")
    protected String getLocaleName() {
        String locale = (String) ThreadBeanHolder.get("locale");
        String name = null;
        LocaleName localeName = localeNames.get(locale);
        if (localeName != null) {
            name = localeName.getName();
        }
        return name;
    }

    public void putLocaleName(LocaleName localeName) {
        localeNames.put(localeName.getLocale(), localeName);
    }

}