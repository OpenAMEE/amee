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
package com.amee.restlet.data;

import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.item.data.DataItem;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.data.DataConstants;
import com.amee.service.item.DataItemServiceImpl;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class DataItemLookupResource extends AuthorizeResource implements Serializable {

    @Autowired
    private DataItemServiceImpl dataItemService;

    private String dataItemUid = "";
    private DataItem dataItem = null;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        dataItemUid = request.getResourceRef().getQueryAsForm().getFirstValue("dataItemUid", "");
        dataItem = dataItemService.getItemByUid(dataItemUid);
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(getRootDataCategory());
        if (dataItem != null) {
            entities.addAll(dataItem.getHierarchy());
        }
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_DATA_ITEM_LOOKUP;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("dataItemUid", dataItemUid);
        if (dataItem != null) {
            values.put("dataItem", dataItem);
            values.put("itemValueDefinitions", dataItem.getItemDefinition().getItemValueDefinitions());
            values.put("itemValuesMap", dataItemService.getItemValuesMap(dataItem));
        }
        return values;
    }
}