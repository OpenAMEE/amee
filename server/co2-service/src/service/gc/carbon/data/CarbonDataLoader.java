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

import com.csvreader.CsvReader;
import gc.carbon.domain.data.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.persistence.EntityManager;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.charset.Charset;

@Name("carbonDataLoader")
@Scope(ScopeType.EVENT)
public class CarbonDataLoader implements Serializable {

    private final static Logger log = Logger.getLogger(CarbonDataLoader.class);

    @In(create = true)
    private EntityManager entityManager;

    public boolean loadCarbonDataValues(FileItem fileItem, DataCategory dataCategory) {
        boolean success = false;
        ItemValueDefinition itemValueDefinition;
        ItemDefinition itemDefinition = dataCategory.getItemDefinition();
            if (itemDefinition != null) {
            // TODO: Move this to a generic method that can be called by all sheets
            try {
                int dataItemCount = 0;
                int itemValueCount = 0;
                Charset charset = Charset.forName("ISO-8859-1");
                CsvReader reader = new CsvReader(fileItem.getInputStream(), charset);
                // Read first row as columns
                reader.readHeaders();
                if (reader.getHeaders() != null) {
                    // iterate over CSV
                    while (reader.readRecord()) {
                        DataItem dataItem = new DataItem(dataCategory, itemDefinition);
                        dataItemCount++;
                        entityManager.persist(dataItem);
                        for (int i = 0; i < reader.getHeaders().length; i++) {
                            String columnName = reader.getHeaders()[i];
                            if (columnName != null) {
                                columnName = columnName.trim();
                                String columnValue = reader.get(columnName);
                                itemValueDefinition = itemDefinition.getItemValueDefinition(columnName);
                                if (itemValueDefinition != null) {
                                    new ItemValue(itemValueDefinition, dataItem, columnValue.trim());
                                    itemValueCount++;
                                }
                            }
                        }
                        flushAndClear(dataItemCount, itemValueCount);
                    }
                }
                reader.close();
                success = true;
                log.info("created " + dataItemCount + " Data Items and " + itemValueCount + " Item Values");
            } catch (FileNotFoundException e) {
                log.error(e.getMessage());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        } else {
            log.warn("ItemDefinition not available for this DataCategory");
        }
        return success;
    }

    private void flushAndClear(int dataItemCount, int itemValueCount) {
        if ((dataItemCount + itemValueCount) % 50 == 0) { // 50, same as the JDBC batch size
            log.warn("flush a batch of inserts and release memory");
            entityManager.flush();
            entityManager.clear();
        }
    }
}