package gc.carbon;

import com.jellymold.utils.ThreadBeanHolder;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.profile.StartEndDate;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

public abstract class BaseBrowser implements Serializable {

    protected PathItem pathItem;
    private final StartEndDate startOfMonth = new StartEndDate(getStartOfMonthDate());

    protected StartEndDate startDate;
    protected StartEndDate endDate;

    public BaseBrowser() {
        super();
        setPathItem((PathItem) ThreadBeanHolder.get("pathItem"));
    }

    public void setPathItem(PathItem pathItem) {
        this.pathItem = pathItem;
    }

    public PathItem getPathItem() {
        return pathItem;
    }

    public void setStartDate(String date) {
        if (date != null) {
            startDate = new StartEndDate(date, false);
        }
    }

    public void setStartDate(Date startDate) {
        if (startDate != null) {
            this.startDate = new StartEndDate(startDate);
        }
    }

    public StartEndDate getStartDate() {
        return (startDate != null ? startDate : startOfMonth);
    }

    public void setEndDate(String date) {
        if (date != null) {
            endDate = new StartEndDate(date, false);
        }
    }

    public void setEndDate(Date endDate) {
        if (endDate != null) {
            this.endDate = new StartEndDate(endDate);
        }
    }

    public StartEndDate getEndDate() {
        return endDate;
    }

    public boolean isQuery() {
        return (startDate != null) || (endDate != null);
    }

    private String getStartOfMonthDate() {
        return StartEndDate.ISO_DATE_FORMAT.format(
                new DateTime().dayOfMonth().withMinimumValue().millisOfDay().withMinimumValue().getMillis());
    }
}
