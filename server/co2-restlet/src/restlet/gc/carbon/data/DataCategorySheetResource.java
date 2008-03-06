/**
* This file is part of AMEE.
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
package gc.carbon.data;

import com.jellymold.sheet.Sheet;
import com.jellymold.utils.BaseResource;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.io.Serializable;
import java.util.Map;

// TODO: set content type to CSV 

@Name("dataCategorySheetResource")
@Scope(ScopeType.EVENT)
public class DataCategorySheetResource extends BaseResource implements Serializable {

    private final static Logger log = Logger.getLogger(DataCategorySheetResource.class);

    @In(create = true)
    private DataService dataService;

    @In(create = true)
    private DataBrowser dataBrowser;

    @In(create = true)
    private DataSheetService dataSheetService;

    public DataCategorySheetResource() {
        super();
    }

    public DataCategorySheetResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        dataBrowser.setDataCategoryUid(request.getAttributes().get("categoryUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (dataBrowser.getDataCategoryUid() != null);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_DATA_SHEET;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        DataCategory dataCategory = dataBrowser.getDataCategory();
        Sheet sheet = dataSheetService.getSheet(dataCategory);
        Map<String, Object> values = super.getTemplateValues();
        values.put("sheet", sheet);
        return values;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (dataBrowser.getDataCategoryActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }
}
