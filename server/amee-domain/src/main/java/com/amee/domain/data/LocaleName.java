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

import com.amee.domain.AMEEEnvironmentEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides a Locale-to-name mapping for an {@link AMEEEnvironmentEntity} instance.
 *
 * The name in this context is that of the {@link com.amee.domain.path.Pathable} interface.
 *
 * Modelled as a One-to-Many relationship with the owning entity.
 *
 */
@Entity
@Inheritance
@Table(name = "LOCALE_NAME")
@DiscriminatorColumn(name = "ENTITY_TYPE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LocaleName extends AMEEEnvironmentEntity {

    // The default {@link Locale} within AMEE.
    // Typical use would be for initialising a new User locale.
    public static final Locale DEFAULT_LOCALE = Locale.UK;

    // The locale identifier (e.g. Fr or Fr_fr).
    @Column(name = "LOCALE", nullable = false)
    private String locale;

    // The locale-specific name.
    @Column(name = "NAME", nullable = false)
    private String name;

    // The owning entitiy.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENTITY_ID", nullable = false)
    private AMEEEnvironmentEntity entity;

    // The static map of available {@link Locale}.
    public static final Map<String, Locale> AVAILABLE_LOCALES = initLocales();

    public LocaleName() {
        super();
    }

    /**
     * Instanitate a new LocaleName.
     *
     * @param entity - the entity to which the LocaleName belongs.
     * @param locale - the {@link Locale} for this name.
     * @param name - the locale-specific name.
     */
    public LocaleName(AMEEEnvironmentEntity entity, Locale locale, String name) {
        super(entity.getEnvironment());
        this.entity = entity;
        this.locale = locale.toString();
        this.name = name;
    }

    private static Map<String, Locale> initLocales() {
        Map<String, Locale> localeMap = new TreeMap<String, Locale>();
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale l : locales) {
            localeMap.put(l.toString(),l);
        }
        return localeMap;
    }

    /**
     * Get the locale-specific name
     *
     * @return - the locale-specific name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the locale-specific name
     *
     * @param name - the locale-specific name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the locale identifier (e.g. Fr or Fr_fr)
     *
     * @return - the locale identifier
     */
    public String getLocale() {
        return locale;
    }
}