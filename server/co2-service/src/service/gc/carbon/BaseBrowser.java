package gc.carbon;

import com.jellymold.kiwi.Environment;
import gc.carbon.data.DataService;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.profile.StartEndDate;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.restlet.data.Form;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public abstract class BaseBrowser implements Serializable {

    @In(create = true)
    protected DataService dataService;

    @In(required = false)
    protected Environment environment;

    @In(scope = ScopeType.EVENT, required = false)
    protected PathItem pathItem;

    protected StartEndDate startDate = new StartEndDate(Calendar.getInstance().getTime());
    protected StartEndDate endDate;

    private String apiVersion;

    public void setStartDate(String date) {
        startDate = new StartEndDate(date);
    }

    public void setEndDate(String date) {
        if (date != null)
            endDate = new StartEndDate(date);
    }

    public StartEndDate getStartDate() {
        return startDate;
    }

    public StartEndDate getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        if (endDate != null)
            this.endDate = new StartEndDate(endDate);
    }

    public void setStartDate(Date startDate) {
        this.startDate = new StartEndDate(startDate);    
    }

    public void setAPIVersion(String v) {
        apiVersion = v;
    }


    public boolean isAPIVersionOne() {
        return apiVersion == null || apiVersion.equals("1.0");
    }
}
