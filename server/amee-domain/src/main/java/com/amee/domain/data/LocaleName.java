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
package com.amee.domain.data;

import com.amee.core.ObjectType;
import com.amee.domain.AMEEEnvironmentEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Entity
@Inheritance
@Table(name = "LOCALE_NAME")
@DiscriminatorColumn(name = "ENTITY_TYPE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LocaleName extends AMEEEnvironmentEntity {

    public static final Locale DEFAULT_LOCALE = Locale.UK;

    @Column(name = "LOCALE", nullable = false)
    private String locale;

    @Column(name = "NAME", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENTITY_ID", nullable = false)
    private AMEEEnvironmentEntity entity;
    public static Map<String, Locale> AVAILABLE_LOCALES = new TreeMap<String, Locale>();

    public LocaleName() {
        super();
    }

    public LocaleName(AMEEEnvironmentEntity entity, Locale locale, String name) {
        super(entity.getEnvironment());
        this.entity = entity;
        this.locale = locale.toString();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getLocale() {
        return locale;
    }
}