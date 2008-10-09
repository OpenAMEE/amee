package gc.carbon.profile;

import gc.carbon.path.PathItem;
import gc.carbon.data.DataService;
import gc.carbon.data.Calculator;

import java.util.List;

import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
import com.jellymold.kiwi.Environment;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import javax.persistence.EntityManager;

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
public abstract class BaseProfileResource extends BaseResource {

    public BaseProfileResource() {
        super();
    }

    public BaseProfileResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    public abstract List<ProfileItem> getProfileItems();

    public abstract ProfileSheetService getProfileSheetService();

    public abstract ProfileBrowser getProfileBrowser();

    public abstract PathItem getPathItem();

    public abstract Pager getPager();

    public abstract ProfileService getProfileService();

    public abstract DataService getDataService();

    public abstract Environment getEnvironment();

    public abstract Calculator getCalculator();
    
    public abstract EntityManager getEntityManager();

    public abstract DateTimeBrowser getDateTimeBrowser();
}
