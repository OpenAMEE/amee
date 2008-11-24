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
import com.jellymold.utils.Pager;
import gc.carbon.domain.data.ItemDefinition;
import gc.carbon.domain.data.ItemValueDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;

public class DataCategoryResourceBuilder implements Serializable {

    private DataCategoryResource resource;

    public DataCategoryResourceBuilder(DataCategoryResource resource) {
        this.resource = resource;
    }

    private Sheet getSheet() {
        test();
        resource.getDataSheetFactory().setDataCategory(resource.getDataCategory());
        return (Sheet) resource.getDataSheetFactory().create();
    }


    public void addElement(Element parent, Document document) {
        Sheet sheet = getSheet();
        if (sheet != null) {
            Pager pager = resource.getPager();
            sheet = Sheet.getCopy(sheet, pager);
            pager.setCurrentPage(resource.getPage());
            parent.appendChild(sheet.getElement(document, false));
            parent.appendChild(pager.getElement(document));
        }
    }

    //TODO - See DC.getItemValuesMap()
    private void test() {
        ItemDefinition itemDefinition = resource.getDataCategory().getItemDefinition();
        for (ItemValueDefinition itemValueDefinition : itemDefinition.getItemValueDefinitions()) {
            if (itemValueDefinition.isFromData()) {
                System.out.println(itemValueDefinition.getPath() + " " + itemValueDefinition.getName());
            }
        }
    }
}