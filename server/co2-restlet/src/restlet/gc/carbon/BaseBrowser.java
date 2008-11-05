package gc.carbon;

import com.jellymold.kiwi.Environment;
import com.jellymold.utils.HeaderUtils;
import gc.carbon.data.DataService;
import gc.carbon.domain.path.PathItem;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.restlet.data.Request;

import java.io.Serializable;

public abstract class BaseBrowser implements Serializable {

    @In(create = true)
    protected DataService dataService;

    @In(required = false)
    protected Environment environment;

    @In(scope = ScopeType.EVENT, required = false)
    protected PathItem pathItem;

    public PathItem getPathItem() {
        return pathItem;
    }

    public int getItemsPerPage(Request request) {
        int itemsPerPage = environment.getItemsPerPage();
        String itemsPerPageStr = request.getResourceRef().getQueryAsForm().getFirstValue("itemsPerPage");
        if (itemsPerPageStr == null) {
            itemsPerPageStr = HeaderUtils.getHeaderFirstValue("ItemsPerPage", request);
        }
        if (itemsPerPageStr != null) {
            try {
                itemsPerPage = Integer.valueOf(itemsPerPageStr);
            } catch (NumberFormatException e) {
                // swallow
            }
        }
        return itemsPerPage;
    }
}
