package gc.carbon;

import com.jellymold.kiwi.environment.EnvironmentService;
import com.jellymold.utils.HeaderUtils;
import com.jellymold.utils.ThreadBeanHolder;
import gc.carbon.builder.APIVersion;
import gc.carbon.data.DataBrowser;
import gc.carbon.domain.path.PathItem;
import gc.carbon.profile.ProfileBrowser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

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
public abstract class BaseResource extends com.jellymold.utils.BaseResource implements BeanFactoryAware {

    private final Log log = LogFactory.getLog(getClass());

    protected APIVersion apiVersion;
    protected BeanFactory beanFactory;
    
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        apiVersion = (APIVersion) request.getAttributes().get("apiVersion");
    }

    public int getItemsPerPage() {
        int itemsPerPage = EnvironmentService.getEnvironment().getItemsPerPage();
        String itemsPerPageStr = getRequest().getResourceRef().getQueryAsForm().getFirstValue("itemsPerPage");
        if (itemsPerPageStr == null) {
            itemsPerPageStr = HeaderUtils.getHeaderFirstValue("ItemsPerPage", getRequest());
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

    public DataBrowser getDataBrowser() {
        DataBrowser dataBrowser = (DataBrowser) ThreadBeanHolder.get("dataBrowser");
        if (dataBrowser == null) {
            dataBrowser = (DataBrowser) beanFactory.getBean("dataBrowser");
            dataBrowser.setAPIVersion(apiVersion);
            ThreadBeanHolder.set("dataBrowser", dataBrowser);
        }
        return dataBrowser;
    }

    public ProfileBrowser getProfileBrowser() {
        ProfileBrowser profileBrowser = (ProfileBrowser) ThreadBeanHolder.get("profileBrowser");
        if (profileBrowser == null) {
            profileBrowser = (ProfileBrowser) beanFactory.getBean("profileBrowser");
            profileBrowser.setAPIVersion(apiVersion);
            ThreadBeanHolder.set("profileBrowser", profileBrowser);
        }
        return profileBrowser;
    }

    public PathItem getPathItem() {
        return (PathItem) ThreadBeanHolder.get("pathItem");
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
