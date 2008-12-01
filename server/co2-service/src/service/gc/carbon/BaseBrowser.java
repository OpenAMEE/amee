package gc.carbon;

import com.jellymold.utils.ThreadBeanHolder;
import gc.carbon.builder.APIVersion;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.profile.StartEndDate;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public abstract class BaseBrowser implements Serializable {

    protected APIVersion apiVersion;
    protected PathItem pathItem;
    protected StartEndDate startDate = new StartEndDate(Calendar.getInstance().getTime());
    protected StartEndDate endDate;

    public BaseBrowser() {
        super();
        setPathItem((PathItem) ThreadBeanHolder.get("pathItem"));
    }

    public void setAPIVersion(APIVersion apiVersion) {
        this.apiVersion = apiVersion;
    }

    public APIVersion getAPIVersion() {
        return apiVersion;
    }

    public void setPathItem(PathItem pathItem) {
        this.pathItem = pathItem;
    }

    public PathItem getPathItem() {
        return pathItem;
    }

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
}
