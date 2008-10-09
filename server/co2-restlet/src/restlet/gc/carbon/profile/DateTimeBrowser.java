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
package gc.carbon.profile;

import org.apache.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.Request;
import static gc.carbon.profile.ProfileDateTime.*;

import java.util.Date;

public class DateTimeBrowser {

    private final static Logger log = Logger.getLogger(DateTimeBrowser.class);

    // startDate defaults to current date if not present in the request
    private ProfileDateTime startDate = new ProfileDateTime();

    // endDate is not defaulted. Null value is significant and implies an unbounded range.
    private ProfileDateTime endDate;

    private ProfileDateTime profileDate = new ProfileDateTime();

    private boolean calendar = false;

    public DateTimeBrowser(Request request) {
        Form form = request.getResourceRef().getQueryAsForm();
        setDates(form);
    }

    private void setDates(Form form)  {
        //- duration: ISO-8601 duration

        // If startDate is present in the request, then any profileDate parameter in the request is ignored.
        // If neither startDate or profileDate is present in the request then the behaviour is that for a profileDate representing the current month.
        if (form.getFirstValue("startDate") != null) {
            startDate = new ProfileDateTime(form.getFirstValue("startDate"), "startDate", ISO);
            // Set the endDate if this is present in the request
            if (form.getFirstValue("endDate") != null) {
                endDate = new ProfileDateTime(form.getFirstValue("endDate"), "endDate", ISO);               
            }
            this.calendar = true;
        }  else {
            profileDate = new ProfileDateTime(form.getFirstValue("profileDate"), "profileDate", FULL);
            profileDate.setOutputFormatTo(MONTH);
        }
    }

    public ProfileDateTime getProfileDate() {
        return profileDate;
    }

    public ProfileDateTime getStartDate() {
        return startDate;
    }

    public ProfileDateTime getEndDate() {
        return endDate;    
    }

    public boolean isCalendar() {
        return calendar;
    }

}