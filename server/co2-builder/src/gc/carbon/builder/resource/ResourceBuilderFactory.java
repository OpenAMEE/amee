package gc.carbon.builder.resource;

import gc.carbon.builder.resource.current.ProfileCategoryResourceBuilder;
import gc.carbon.builder.resource.current.ProfileItemResourceBuilder;

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
public class ResourceBuilderFactory {

    public static ResourceBuilder createProfileCategoryRenderer(BuildableResource resource) {
        //if (resource.getVersion() == APIVersion.ONE) {
        if (resource.getVersion().equals("1.0")) {
           return new ProfileCategoryResourceBuilder(resource);
        } else {
            return new ProfileCategoryResourceBuilder(resource);
        }
    }

    public static ResourceBuilder createProfileItemRenderer(BuildableResource resource) {
        //if (resource.getVersion() == APIVersion.ONE) {
        if (resource.getVersion().equals("1.0")) {
            return new ProfileItemResourceBuilder(resource);
        } else {
            return new ProfileItemResourceBuilder(resource);
        }
    }
}
