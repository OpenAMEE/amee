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
package com.amee.restlet.data;

import com.amee.domain.sheet.Sheet;
import com.amee.service.data.DataConstants;
import com.amee.service.data.DataSheetService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

// TODO: is this actually used by anybody?
// TODO: move to builder model

@Component
@Scope("prototype")
public class DataCategorySheetResource extends BaseDataResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DataSheetService dataSheetService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setDataCategory(request.getAttributes().get("categoryUid").toString());
        setAvailable(isValid());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (getDataCategory() != null);
    }

    @Override
    public String getTemplatePath() {
        return getAPIVersion() + "/" + DataConstants.VIEW_DATA_SHEET;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Sheet sheet = dataSheetService.getSheet(dataBrowser);
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
