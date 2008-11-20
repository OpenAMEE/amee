package gc.carbon.profile;

import com.jellymold.kiwi.Environment;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
import com.jellymold.utils.domain.APIObject;
import gc.carbon.builder.APIVersion;
import gc.carbon.builder.resource.BuildableCategoryResource;
import gc.carbon.builder.resource.BuildableResource;
import gc.carbon.data.Calculator;
import gc.carbon.data.DataService;
import gc.carbon.domain.path.PathItem;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;

import javax.persistence.EntityManager;
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
public abstract class BaseProfileCategoryResource extends BaseProfileResource implements BuildableCategoryResource {

    public BaseProfileCategoryResource() {
        super();
    }

    public BaseProfileCategoryResource(Context context, Request request, Response response) {
        super(context, request, response);
    }


}