package gc.carbon.builder.resource;

import com.jellymold.utils.domain.APIObject;
import com.jellymold.utils.Pager;
import com.jellymold.sheet.Sheet;
import gc.carbon.builder.domain.BuildableProfileItem;
import gc.carbon.builder.Builder;
import gc.carbon.builder.APIVersion;
import gc.carbon.profile.ProfileSheetService;

import java.util.List;
import java.util.Date;
import java.util.Set;
import java.math.BigDecimal;

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
public interface BuildableCategoryResource extends BuildableResource {

    Pager getPager();

    int getPage();

    Sheet getSheet();

    BigDecimal getTotalAmount(Sheet sheet);

    BigDecimal getTotalAmountPerMonth(Sheet sheet);
}
