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

import com.amee.domain.AMEEEntity;
import com.amee.domain.AMEEEntityReference;
import com.amee.domain.AMEEEnvironmentEntity;
import com.amee.domain.AMEEStatus;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.ObjectType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Locale;

/**
 * Provides a Locale-to-name mapping for an {@link AMEEEnvironmentEntity} instance.
 * <p/>
 * The name in this context is that of the {@link com.amee.domain.path.Pathable} interface.
 * <p/>
 * Modelled as a One-to-Many relationship with the owning entity.
 */
@Entity
@Table(name = "LOCALE_NAME")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LocaleName extends AMEEEntity {

    // The locale identifier (e.g. Fr or Fr_fr).
    @Column(name = "LOCALE", nullable = false)
    private String locale;

    // The locale-specific name.
    @Column(name = "NAME", nullable = false)
    private String name = "";

    @Embedded
    private AMEEEntityReference entity = new AMEEEntityReference();

    public LocaleName() {
        super();
    }

    @Override
    public boolean isTrash() {
        // TODO: Currently entity.getEntity() may be null but should never be.
        return status.equals(AMEEStatus.TRASH) || ((entity.getEntity() == null) || entity.getEntity().isTrash());
    }

    /**
     * Instantiate a new LocaleName.
     *
     * @param entity - the entity to which the LocaleName belongs.
     * @param locale - the {@link Locale} for this name.
     * @param name   - the locale-specific name.
     */
    public LocaleName(IAMEEEntityReference entity, Locale locale, String name) {
        this.entity = new AMEEEntityReference(entity);
        this.locale = locale.toString();
        this.name = name;
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
        if (name == null) {
            name = "";
        }
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

    public ObjectType getObjectType() {
        return ObjectType.LN;
    }
}