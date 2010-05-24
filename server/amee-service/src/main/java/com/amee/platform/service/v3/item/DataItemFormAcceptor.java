package com.amee.platform.service.v3.item;

import com.amee.base.resource.MissingAttributeException;
import com.amee.base.resource.NotFoundException;
import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceAcceptor;
import com.amee.base.validation.ValidationException;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class DataItemFormAcceptor implements ResourceAcceptor {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private DataItemValidationHelper validationHelper;

    @Transactional(rollbackFor = {ValidationException.class})
    public JSONObject handle(RequestWrapper requestWrapper) throws ValidationException {
        DataItemAcceptorRenderer renderer = new DataItemAcceptorJSONRenderer();
        renderer.start();
        // Get DataCategory identifier.
        String dataCategoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
        if (dataCategoryIdentifier != null) {
            // Get DataCategory.
            DataCategory dataCategory = dataService.getDataCategoryByIdentifier(
                    environmentService.getEnvironmentByName("AMEE"), dataCategoryIdentifier);
            if (dataCategory != null) {
                // Get DataItem identifier.
                String dataItemIdentifier = requestWrapper.getAttributes().get("itemIdentifier");
                if (dataItemIdentifier != null) {
                    // Get DataItem.
                    DataItem dataItem = dataService.getDataItemByUid(dataCategory, dataItemIdentifier);
                    if (dataItem != null) {
                        validationHelper.setDataItem(dataItem);
                        if (validationHelper.isValid(requestWrapper.getFormParameters())) {
                            renderer.ok();
                        } else {
                            throw new ValidationException(validationHelper.getValidationResult());
                        }
                    } else {
                        throw new NotFoundException();
                    }
                } else {
                    throw new MissingAttributeException("itemIdentifier");
                }
            } else {
                throw new NotFoundException();
            }
        } else {
            throw new MissingAttributeException("categoryIdentifier");
        }
        return (JSONObject) renderer.getResult();
    }

    public interface DataItemAcceptorRenderer {

        public void start();

        public void ok();

        public Object getResult();
    }

    public static class DataItemAcceptorJSONRenderer implements DataItemAcceptorRenderer {

        private JSONObject rootObj;

        public DataItemAcceptorJSONRenderer() {
            super();
        }

        public void start() {
            rootObj = new JSONObject();
        }

        public void ok() {
            put(rootObj, "status", "OK");
        }

        public JSONObject getResult() {
            return rootObj;
        }

        protected JSONObject put(JSONObject o, String key, Object value) {
            try {
                return o.put(key, value);
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }
    }
}