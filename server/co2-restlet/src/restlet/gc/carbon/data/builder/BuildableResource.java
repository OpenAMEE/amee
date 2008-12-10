package gc.carbon.data.builder;

import com.jellymold.utils.domain.APIObject;
import gc.carbon.APIVersion;
import gc.carbon.profile.ProfileBrowser;
import gc.carbon.domain.Builder;
import gc.carbon.domain.profile.builder.BuildableProfileItem;

import java.util.Date;
import java.util.List;
import java.util.Set;

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
public interface BuildableResource extends Builder {
    List<? extends BuildableProfileItem> getProfileItems();

    APIObject getDataCategory();

    APIObject getProfile();

    Date getProfileDate();

    Date getStartDate();

    Date getEndDate();

    BuildableProfileItem getProfileItem();

    APIVersion getVersion();

    String getFullPath();

    boolean isGet();

    boolean isPut();

    boolean isPost();

    boolean hasParent();

    Set<? extends APIObject> getChildrenByType(String type);

    ProfileBrowser getProfileBrowser();

}