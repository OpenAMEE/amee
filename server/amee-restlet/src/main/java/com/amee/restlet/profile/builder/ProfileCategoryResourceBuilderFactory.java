package com.amee.restlet.profile.builder;

import com.amee.restlet.profile.ProfileCategoryResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
@Service
public class ProfileCategoryResourceBuilderFactory {

    @Autowired
    private com.amee.restlet.profile.builder.v1.ProfileCategoryResourceBuilder v1ProfileCategoryResourceBuilder;

    @Autowired
    private com.amee.restlet.profile.builder.v2.ProfileCategoryResourceBuilder v2ProfileCategoryResourceBuilder;

    public IProfileCategoryResourceBuilder createProfileCategoryResourceBuilder(ProfileCategoryResource resource) {
        if (resource.getAPIVersion().isVersionOne()) {
            return v1ProfileCategoryResourceBuilder;
        } else {
            return v2ProfileCategoryResourceBuilder;
        }
    }
}
