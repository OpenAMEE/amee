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
package com.amee.restlet.profile.builder;

import com.amee.restlet.profile.ProfileItemValueResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileItemValueResourceBuilderFactory {

    @Autowired
    private com.amee.restlet.profile.builder.v1.ProfileItemValueResourceBuilder v1ProfileItemValueResourceBuilder;

    @Autowired
    private com.amee.restlet.profile.builder.v2.ProfileItemValueResourceBuilder v2ProfileItemValueResourceBuilder;

    public IProfileItemValueResourceBuilder createProfileItemValueResourceBuilder(ProfileItemValueResource resource) {
        if (resource.getAPIVersion().isVersionOne()) {
            return v1ProfileItemValueResourceBuilder;
        } else {
            return v2ProfileItemValueResourceBuilder;
        }
    }
}