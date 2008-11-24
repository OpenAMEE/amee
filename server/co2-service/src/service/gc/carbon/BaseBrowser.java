package gc.carbon;

import com.jellymold.kiwi.Environment;
import gc.carbon.data.DataService;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.profile.StartEndDate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Calendar;

public abstract class BaseBrowser implements Serializable {

    @Autowired
    protected DataService dataService;

    @Autowired
    protected Environment environment;

    // TODO: Springify
    // @In(scope = ScopeType.EVENT, required = false)
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

    public void setAPIVersion(String v) {
        apiVersion = v;
    }

    public boolean isAPIVersionOne() {
        return apiVersion == null || apiVersion.equals("1.0");
    }
}
