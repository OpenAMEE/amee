package gc.carbon.data;

import gc.carbon.AMEEResource;
import gc.carbon.domain.data.DataItem;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

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
public abstract class BaseDataResource extends AMEEResource {

    protected DataBrowser dataBrowser;
    private DataItem dataItem;

    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        this.dataBrowser = (DataBrowser) beanFactory.getBean("dataBrowser");
    }
    
    public String getFullPath() {
        if (pathItem != null) {
            return "/data" + pathItem.getFullPath();
        } else {
            return "/data";
        }
    }

    //TODO - Remove once we have refactored out usage of browser to retrieve DC in DataSheet Factories
    public void setDataCategory(String dataCategoryUid) {
        super.setDataCategory(dataCategoryUid);
        dataBrowser.setDataCategory(getDataCategory());
    }

    public void setDataItem(String dataItemUid) {
        if (dataItemUid.isEmpty()) return;
        dataItem = dataService.getDataItem(dataItemUid);
    }

    public DataItem getDataItem() {
        return dataItem;
    }
}
