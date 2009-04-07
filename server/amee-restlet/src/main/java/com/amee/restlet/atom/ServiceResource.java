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
package com.amee.restlet.atom;

import com.amee.core.ObjectType;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.path.PathItem;
import com.amee.domain.profile.StartEndDate;
import com.amee.restlet.AMEEResource;
import com.amee.restlet.profile.builder.v2.AtomFeed;
import com.amee.service.data.OnlyActiveDataService;
import org.apache.abdera.model.*;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.restlet.resource.WriterRepresentation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

@Component("serviceResource")
@Scope("prototype")
public class ServiceResource extends AMEEResource {

    public static AtomFeed ATOM_FEED = AtomFeed.getInstance();

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        List<Variant> variants = super.getVariants();
        variants.clear();
        variants.add(new Variant(MediaType.APPLICATION_ATOM_SERVICE_XML));
        setAvailable(isValid());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && pathItem.getObjectType().equals(ObjectType.DC);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        if (variant.getMediaType().equals(MediaType.APPLICATION_ATOM_SERVICE_XML)) {
            representation = getAtomRepresentation();
            representation.setCharacterSet(CharacterSet.UTF_8);
            // TODO: Make sure this is the correct thing to do
            representation.setExpirationDate(new org.joda.time.DateTime().withHourOfDay(23).withSecondOfMinute(59).withMinuteOfHour(59).toDate());
            representation.setModificationDate(new org.joda.time.DateTime().toDate());
        }
        return representation;
    }

    @Override
    public org.apache.abdera.model.Element getAtomElement() {
        final Service service = ATOM_FEED.newService();
        Workspace ws = ATOM_FEED.newWorkspace(service);
        ws.setBaseUri(getRequest().getResourceRef().getBaseRef().getParentRef().toString());
        ws.setTitle("DataCategories");
        addCollections(pathItem, ws);
        return service;
    }

    @Override
    protected Representation getAtomRepresentation() {
        return new WriterRepresentation(MediaType.APPLICATION_ATOM_SERVICE_XML) {
            public void write(Writer writer) throws IOException {
                AtomFeed.getInstance().getWriter().writeTo(getAtomElement(), writer);
            }
        };
    }

    private void addCollections(PathItem pathItem, Workspace ws) {
        Set<PathItem> pathItems = pathItem.getChildrenByType("DC");
        if (pathItems.isEmpty()) {
            addCollection(ws, pathItem, false);
        } else {
            for (PathItem pi : pathItem.getChildrenByType("DC")) {
                addCollection(ws, pi, true);
            }
        }
    }

    private void addCollection(Workspace ws, PathItem pathItem, boolean recurse) {
        DataCategory dc = dataService.getDataCategory(pathItem.getUid());
        if (dc.getItemDefinition() != null) {

            Reference href = new Reference(getRequest().getResourceRef().getParentRef(), pathItem.getFullPath());

            Collection col = ATOM_FEED.newCollection(ws);
            col.setHref(href.toString().substring(1));
            col.setTitle(pathItem.getFullPath());
            col.setAcceptsEntry();

            Categories cats = ATOM_FEED.newItemCategories(col);
            cats.setFixed(true);
            for (DataItem di : new OnlyActiveDataService(dataService).getDataItems(dc, new StartEndDate())) {
                Category cat = ATOM_FEED.newCategory(cats);
                cat.setTerm(di.getUid());
                cat.setLabel(di.getItemDefinition().getName() + " (" + di.getItemValuesString() + ")");
            }
            if (recurse) {
                addCollections(pathItem, ws);
            }
        } else {
            if (recurse) {
                addCollections(pathItem, ws);
            }
        }
    }
}