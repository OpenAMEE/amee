package com.amee.platform.service.v3.tag;

import com.amee.base.resource.NotFoundException;
import com.amee.base.resource.RequestWrapper;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.data.DataCategory;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.tag.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagResourceService {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private TagService tagService;

    /**
     * Get the entity that Tags should belong to.
     * <p/>
     * This base implementation returns null.
     *
     * @param requestWrapper
     * @return IAMEEEntityReference entity reference
     */
    public IAMEEEntityReference getEntity(RequestWrapper requestWrapper) {
        if (requestWrapper.getAttributes().containsKey("categoryIdentifier")) {
            String dataCategoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
            if (dataCategoryIdentifier != null) {
                DataCategory dataCategory = dataService.getDataCategoryByIdentifier(
                        environmentService.getEnvironmentByName("AMEE"),
                        dataCategoryIdentifier);
                if (dataCategory != null) {
                    return dataCategory;
                } else {
                    throw new NotFoundException();
                }
            }
        }
        return null;
    }
}
