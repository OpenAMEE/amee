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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Locale;

/**
 * {@link LocaleName} implementation for {@link ItemDefinition}
 *
 */
@Entity
@DiscriminatorValue("ID")
public class ItemDefinitionLocaleName extends LocaleName {

    public ItemDefinitionLocaleName() {
        super();
    }

    public ItemDefinitionLocaleName(ItemDefinition itemDefinition, Locale locale, String name) {
        super(itemDefinition, locale, name);
    }
}
