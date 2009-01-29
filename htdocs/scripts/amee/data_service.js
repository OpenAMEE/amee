var BaseDataApiService  = Class.create(ApiService, ({
    // Initialization
    initialize: function($super, params) {
        $super(params);
        this.updateFormName = 'apiUpdateForm';
        this.createFormName = 'apiCreateForm';

        this.updateFormStatusName = 'apiUpdateSubmitStatus';
        this.createFormStatusName = 'apiCreateSubmitStatus';
    },
    getTrailRootPath: function() {
        return 'data';
    },
    apiServiceCall: function(method, params) {
        if (!params) {
            var params = {};
        }
        if (!method) {
            method = '';
        }
        params.requestHeaders = ['Accept', 'application/json'];
        params.method = 'post';
        new Ajax.Request(window.location.href + method, params);
    },
    addFormInfoElement: function(label, pElement, name, info, size, style, otherInfo) {
        pElement.insert(label);
        pElement.insert(new Element('input', {id : pElement.id + "-" + name, name : name, value : info, size : size, style : style}));
        if (otherInfo) {
            pElement.insert(otherInfo);
        }
        pElement.insert(new Element('br'));
    },
    resetStyles: function(elementList) {
        for (var i = 0; i < elementList.length; i++) {
            if (elementList[i]) {
                elementList[i].setStyle({
                    borderColor : '',
                    borderWidth : '',
                    borderStyle : ''
                });
            }
        }
    },
    validateElementList: function(elementList) {

        var rtnValue = true;

        for (var i = 0; i < elementList.length; i++) {
            var inElement = elementList[i];
            if (inElement) {
                if (inElement.value.replace(/^\s+|\s+$/g, '') == '') {
                    inElement.setStyle({
                        borderColor : 'red',
                        borderWidth : '2px',
                        borderStyle : 'solid'
                    });
                    rtnValue = false;
                }
            }
        }
        return rtnValue;
    }
}));

var DataCategoryApiService = Class.create(BaseDataApiService, ({
    // Initialization
    initialize: function($super, params) {
        $super(params);
        this.updateCategory = params.updateCategory || false;
        this.createCategory = params.createCategory || false;
        this.allowItemCreate = params.allowItemCreate || false;
    },
    renderApiResponse: function($super, response) {
        var json = response.responseJSON;

        if (json.children.dataItems.rows) {
            $super(response, json.children.pager);
        } else if (json.children.dataCategories.length > 0) {
            // update elements
            this.headingCategoryElement = $(this.headingElementName);
            this.headingContentElement = $(this.headingElementName);

            // set section heading
            this.headingCategoryElement.innerHTML = this.headingCategory;

            // create table headings
            var tableElement = new Element('table', {id : this.contentElementName}).insert(this.getHeadingCategoryElement());

            // create table details
            var detailRows = this.getCategoryDetailRows(json);
            for (var i = 0; i < detailRows.length; i++) {
                tableElement.insert(detailRows[i]);
            }

            // replace table
            this.headingContentElement.replace(tableElement);
        }

        if (this.createCategory) {
            $('apiCreateDataCategory').replace(this.getCreateCategoryElement('apiCreateDataCategory', json));
        }

        if (this.updateCategory) {
            $('apiUpdateDataCategory').replace(this.getUpdateCategoryElement('apiUpdateDataCategory', json));
        }
    },
    getCreateCategoryElement: function(id, json) {
        var createCatElement = new Element('div', {id : id});

        if(this.allowCreate || this.allowItemCreate) {
            createCatElement.insert(new Element('h2').update('Create Data Category'));

            var dataCategory = json.dataCategory;
            var itemDefinitions = json.itemDefinitions;
            
            var formElement = new Element('form', {action : "#", id : this.createFormName});
            var typeSelectElement = new Element('select', {id : 'newObjectType', name : 'newObjectType', style : 'margin-left:52px'});
            var selectElement = new Element('select', {name : 'itemDefinitionUid'});
            var pElement = new Element('p');

            formElement.insert('Type: ');
            if (this.allowCreate) {
                typeSelectElement.insert(new Element('option', {value : 'DC'}).update('Data Category'));
            }
            if (dataCategory.itemDefinition && this.allowItemCreate) {
                typeSelectElement.insert(new Element('option', {value : 'DI'}).update('Data Item (for' + dataCategory.itemDefinition.name + ')'));
            }
            formElement.insert(typeSelectElement).insert(new Element('br'));

            this.addFormInfoElement('Name: ', formElement, 'name', '', 30, 'margin-left:46px');
            this.addFormInfoElement('Path: ', formElement, 'path', '', 30, 'margin-left:54px');

            // item definitions
            selectElement.insert(new Element('option', {value : ''}).update('(No Item Definition)'));
            formElement.insert('Item Definition: ');
            for (var i = 0; i < itemDefinitions.length; i++) {
                var itemDefinition = itemDefinitions[i];
                selectElement.insert(new Element('option', {value : itemDefinition.uid}).update(itemDefinition.name));
            }
            formElement.insert(selectElement).insert(new Element('br')).insert(new Element('br'));

            // sumbit and event
            var btnSubmit = new Element('input', {type : 'button', value : 'Create'});
            formElement.insert(btnSubmit);
            Event.observe(btnSubmit, "click", this.createDataCategory.bind(this));

            pElement.insert(formElement);
            createCatElement.insert(pElement);
        }
        return createCatElement;

    },
    getUpdateCategoryElement: function(id, json) {

        var updateCatElement = new Element('div', {id : id});

        if(this.allowModify) {
            updateCatElement.insert(new Element('h2').update('Update Data Category'));

            var dataCategory = json.dataCategory;
            var itemDefinitions = json.itemDefinitions;

            var formElement = new Element('form', {action : "#", id : this.updateFormName});
            var selectElement = new Element('select', {name : 'itemDefinitionUid'});
            var pElement = new Element('p');

            this.addFormInfoElement('Name: ', formElement, 'name', dataCategory.name, 30, 'margin-left:46px');
            this.addFormInfoElement('Path: ', formElement, 'path', dataCategory.path, 30, 'margin-left:54px');

            // item definitions
            selectElement.insert(new Element('option', {value : ''}).update('(No Item Definition)'));
            formElement.insert('Item Definition: ');
            for (var i = 0; i < itemDefinitions.length; i++) {
                var itemDefinition = itemDefinitions[i];
                var option = new Element('option', {value : itemDefinition.uid}).update(itemDefinition.name);

                if (dataCategory.itemDefinition && dataCategory.itemDefinition.uid == itemDefinition.uid) {
                    option.selected = true;
                }
                selectElement.insert(option);
            }
            formElement.insert(selectElement).insert(new Element('br')).insert(new Element('br'));

            // sumbit and event
            var btnSubmit = new Element('input', {type : 'button', value : 'Update'});
            formElement.insert(btnSubmit);
            Event.observe(btnSubmit, "click", this.updateDataCategory.bind(this));
            
            pElement.insert(formElement);
            updateCatElement.insert(pElement);

        }
        return updateCatElement;
    },
    getHeadingElement: function(json) {
        return new Element('tr')
                .insert(this.getHeadingData('Item'))
                .insert(this.getHeadingData('Actions'));
    },
    getDetailRows: function(json) {
        if (json.children.dataItems) {
            var rows = [];
            for (var i = 0; i < json.children.dataItems.rows.length; i++) {
                var dataItem = json.children.dataItems.rows[i];
                var detailRow = new Element('tr', {id : 'Elem_' + dataItem.uid})
                    .insert(new Element('td').insert(dataItem.label));

                // create actions
                detailRow.insert(this.getActionsTableData(dataItem.path, 'deleteDataItem', dataItem.uid, dataItem.path));

                // update array
                rows[i] = detailRow;
            }
            return rows;
        } else {
            return $super(json);
        }
    },
    getHeadingCategoryElement: function() {
        return new Element('tr')
                .insert(this.getHeadingData('Path'))
                .insert(this.getHeadingData('Actions'));
    },
    getCategoryDetailRows: function(json) {
        var rows = [];
        if (json.children.dataCategories) {
            for (var i = 0; i < json.children.dataCategories.length; i++) {
                var dataCategory = json.children.dataCategories[i];
                var detailRow = new Element('tr', {id : dataCategory.uid})
                    .insert(new Element('td').insert(dataCategory.name));

                // create actions
                detailRow.insert(this.getCategoryActionsTableData('deleteDataCategory', dataCategory.uid, dataCategory.path));


                // update array
                rows[i] = detailRow;
            }
            return rows;
        }
        rows[0] = new Element("tr").insert(new Element("td"));
        return rows;
    },
    getCategoryActionsTableData: function(dMethod, uid, path) {
        var actions = new Element('td');

        if (this.allowView) {
            actions.insert(new Element('a', {href : this.getUrl(path)})
                .insert(new Element('img', {src : '/images/icons/page_edit.png', title : 'Edit', alt : 'Edit', border : 0 })));
        }

        if (this.allowDelete) {
            var dUrl = "'" + uid + "','" + window.location.pathname + "/" + path + "'";
            actions.insert(new Element('input',
                {
                onClick : dMethod + '(' + dUrl + ') ; return false;',
                type : 'image',
                src : '/images/icons/page_delete.png',
                title : 'Delete', alt : 'Delete', border : 0}));
        }


        return actions;
    },
    createDataCategory: function() {
        var elementList = [$(this.createFormName + "-name"), $(this.createFormName + "-path")];

        $(this.createFormStatusName).innerHTML='';
        this.resetStyles(elementList)

        if (this.validateElementList(elementList)) {
            this.resetStyles(elementList)
            var params = {
                parameters: $(this.createFormName).serialize(),
                onSuccess: this.createDataCategorySuccess.bind(this),
                onFailure: this.createDataCategoryFail.bind(this)
            };
            this.apiServiceCall(null, params);
        }

    },
    createDataCategorySuccess: function() {
        // update elements and status
        $(this.createFormStatusName).replace(new Element('div', {id : this.createFormStatusName}).insert(new Element('b').update('CREATED!')));
        window.location.href = window.location.href;
    },
    createDataCategoryFail: function() {
        $(this.createFormStatusName).replace(new Element('div', {id : this.createFormStatusName}).insert(new Element('b').update('ERROR!')));
    },
    updateDataCategory: function() {
        $(this.updateFormStatusName).innerHTML='';
        var elementList = [$(this.updateFormName + "-name"), $(this.updateFormName + "-path")];

        if (this.validateElementList(elementList)) {
            this.resetStyles(elementList)
            var method;
            if (window.location.search == "") {
                method = "?method=put";
            } else {
                method = "&method=put";
            }

            var params = {
                parameters: $(this.updateFormName).serialize(),
                onSuccess: this.updateDataCategorySuccess.bind(this),
                onFailure: this.updateDataCategoryFail.bind(this)
            };
            this.apiServiceCall(method, params);
        }
    },
    updateDataCategorySuccess: function() {
        // update elements and status
        $(this.updateFormStatusName).replace(new Element('div', {id : this.updateFormStatusName}).insert(new Element('b').update('UPDATED!')));
        window.location.href = window.location.href;
    },
    updateDataCategoryFail: function() {
        $(this.updateFormStatusName).replace(new Element('div', {id : this.updateFormStatusName}).insert(new Element('b').update('ERROR!')));
    }
}));

var DataItemApiService = Class.create(BaseDataApiService, ({
        // Initialization
    initialize: function($super, params) {
        $super(params);
        this.dataHeadingItem = params.dataHeadingItem || 'Data Item Details';
        this.dataHeadingItemElementName = params.dataHeadingItemElementName || 'apiDataItemHeading';
        this.dataContentElementName = params.dataContentElementName || "apiDataItemContent";

        this.updateItem = params.updateItem || false;

        var uid;
        var path;
    },
    renderApiResponse: function($super, response) {
        var json = response.responseJSON;
        this.path = json.path;

        if (json.dataItem) {
            this.uid = json.dataItem.uid;
            this.renderDataItemApiResponse(json.dataItem);
        }
        $super(response);
        if (this.updateItem && json.dataItem) {
            $('apiUpdateDataItem').replace(this.getUpdateItemElement('apiUpdateDataItem', json.dataItem));
        }

    },
    renderDataItemApiResponse: function(dataItem) {
        // update elements
        this.dataHeadingItemElement = $(this.dataHeadingItemElementName);
        this.dataContentElement = $(this.dataContentElementName);

        // set section heading
        if (this.dataHeadingItemElement) {
            this.dataHeadingItemElement.innerHTML = this.dataHeadingItem;
        }

        // set section details
        var pElement = new Element('p', {id : this.dataContentElementName});

        if (dataItem.name) {
            pElement.appendChild(document.createTextNode("Name: " + dataItem.name));
            pElement.insert(new Element("br"));
        }

        if (dataItem.path) {
            pElement.insert(new Element("br"));
            pElement.appendChild(document.createTextNode("Path: " + dataItem.path));
        }

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Start Date: " + dataItem.startDate));

        pElement.insert(new Element("br"));
        if (dataItem.endDate) {
            pElement.appendChild(document.createTextNode("End Date: " + dataItem.endDate));
        } else {
            pElement.appendChild(document.createTextNode("End Date: None"));
        }

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Full Path: " + window.location.pathname));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Label: " + dataItem.label));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Item Definition: " + dataItem.itemDefinition.name));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Environment: " + dataItem.environment.name));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("UID: " + dataItem.uid));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Created: " + dataItem.created));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Modified: " + dataItem.modified));


        this.dataContentElement.replace(pElement);
    },
    getHeadingElement: function(json) {
        return new Element('tr')
                .insert(this.getHeadingData('Name'))
                .insert(this.getHeadingData('Value Definition'))
                .insert(this.getHeadingData('Value Type'))
                .insert(this.getHeadingData('Value'))
                .insert(this.getHeadingData('Actions'));
    },
    getDetailRows: function(json) {
        var rows = [];

        if (json.dataItem.itemValues) {
            var itemValues = json.dataItem.itemValues;
            for (var i = 0; i < itemValues.length; i++) {
                var itemValue = itemValues[i];

                var detailRow = new Element('tr', {id : 'Elem_' + itemValue.uid})
                    .insert(new Element('td').insert(itemValue.itemValueDefinition.name))
                    .insert(new Element('td').insert(itemValue.itemValueDefinition.valueDefinition.name))
                    .insert(new Element('td').insert(itemValue.itemValueDefinition.valueDefinition.valueType))
                    .insert(new Element('td').insert(itemValue.value));

                // create actions
                detailRow.insert(this.getActionsTableData('', '', itemValue.displayPath));

                // update array
                rows[i] = detailRow;
            }
        }
        return rows;
    },
    getUpdateItemElement: function(id, dataItem) {

        var updateItemElement = new Element('div', {id : id});

        if(this.allowModify) {
            var dateFormat = " (" + this.getDateFormat() + ")";
            var formElement = new Element('form', {action : "#", id : this.updateFormName});
            var pElement = new Element('p');

            updateItemElement.insert(new Element('h2').update('Update Data Item'));

            this.addFormInfoElement('Name: ', formElement, 'name', dataItem.name, 30, 'margin-left:21px');
            this.addFormInfoElement('Path: ', formElement, 'path', dataItem.path, 30, 'margin-left:29px');
            this.addFormInfoElement('StartDate: ', formElement, 'startDate', dataItem.startDate, 30, 'margin-left:2px', dateFormat);

            var endDate = "";
            if (dataItem.endDate) {
                endDate = dataItem.endDate;
            }

            this.addFormInfoElement('EndDate: ', formElement, 'endDate', endDate, 30, 'margin-left:6px', dateFormat);

            pElement.insert(formElement);
            updateItemElement.insert(pElement);

            formElement.insert(new Element('br')).insert(new Element('br'));

            // sumbit and event
            var btnSubmit = new Element('input', {type : 'button', value : 'Update'});
            formElement.insert(btnSubmit);
            Event.observe(btnSubmit, "click", this.updateDataItem.bind(this));

            pElement.insert(formElement);
            updateItemElement.insert(pElement);
        }
        return updateItemElement;
    },
    getParentPath: function() {
        var pathItems = this.path.split("/");
        var linkPath = '';
        var rootPath = this.getTrailRootPath();

        // root path
        if (rootPath != '') {
            linkPath = "/" + rootPath;
        }

        // path items
        for (var i = 0; i < pathItems.length - 1; i++) {
            var pathItem = pathItems[i];
            if (pathItem == "") {
                continue;
            }
            linkPath = linkPath + "/" + pathItem;
        }
        return linkPath;
    },
    updateDataItem: function(event, elementList) {
        $(this.updateFormStatusName).innerHTML='';

        if (!elementList) {
            elementList = [];
        }
        this.resetStyles(elementList)

        if (this.validateElementList(elementList)) {
            this.resetStyles(elementList)
            var method;
            if (window.location.search == "") {
                method = "?method=put";
            } else {
                method = "&method=put";
            }

            var params = {
                parameters: $(this.updateFormName).serialize(),
                onSuccess: this.updateDataItemSuccess.bind(this),
                onFailure: this.updateDataItemFail.bind(this)
            };
            this.apiServiceCall(method, params);
        }

    },
    updateDataItemSuccess: function() {
        // update elements and status
        $(this.updateFormStatusName).replace(new Element('div', {id : this.updateFormStatusName}).insert(new Element('b').update('UPDATED!')));
        
        var pathElement = $(this.updateFormName + "-path");
        if (pathElement && pathElement.value != '') {
            window.location.href = this.getParentPath() + "/" + $(this.updateFormName + "-path").value;
        } else {
            window.location.href =  this.getParentPath() + "/" + this.uid;
        }
    },
    updateDataItemFail: function() {
        $(this.updateFormStatusName).replace(new Element('div', {id : this.updateFormStatusName}).insert(new Element('b').update('ERROR!')));
    }
}));


var DataItemValueApiService = Class.create(DataItemApiService, ({
    // Initialization
    initialize: function($super, params) {
        $super(params);
    },
    renderApiResponse: function($super, response) {
        var json = response.responseJSON;
        this.uid = json.itemValue.uid;

        if (json.itemValue) {
            this.renderDataItemValueApiResponse(json.itemValue);
        }

        if (this.updateItem && json.itemValue) {
            $('apiUpdateDataItemValue').replace(this.getUpdateItemValueElement('apiUpdateDataItemValue', json.itemValue));
        }

    },
    renderDataItemValueApiResponse: function(itemValue) {
        // update elements
        this.dataHeadingItemElement = $(this.dataHeadingItemElementName);
        this.dataContentElement = $(this.dataContentElementName);

        // set section heading
        if (this.dataHeadingItemElement) {
            this.dataHeadingItemElement.innerHTML = this.dataHeadingItem;
        }

        // set section details
        var pElement = new Element('p', {id : this.dataContentElementName});

        pElement.appendChild(document.createTextNode("Value: " + itemValue.name));
        pElement.insert(new Element("br"));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Full Path: " + window.location.pathname));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Item Definition: " + itemValue.itemValueDefinition.name));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Value Definition: " + itemValue.itemValueDefinition.valueDefinition.name));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Value Type: " + itemValue.itemValueDefinition.valueDefinition.valueType));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Item: " + itemValue.item.label));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Environment: " + itemValue.item.environment.name));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("UID: " + itemValue.uid));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Created: " + itemValue.created));

        pElement.insert(new Element("br"));
        pElement.appendChild(document.createTextNode("Modified: " + itemValue.modified));

        this.dataContentElement.replace(pElement);
    },
    updateDataItem: function($super) {
        $super(null, [$(this.updateFormName + "-value")]);
    },
    getUpdateItemValueElement: function(id, itemValue) {

        var updateItemElement = new Element('div', {id : id});

        if(this.allowModify) {
            var formElement = new Element('form', {action : "#", id : this.updateFormName});
            var pElement = new Element('p');

            updateItemElement.insert(new Element('h2').update('Update Data Item Value'));

            if (itemValue.itemValueDefinition.choices) {

                var selectElement = new Element('select', {name : 'value'});
                var choices = itemValue.itemValueDefinition.choices.split(",");
                
                for (var i = 0; i < choices.length; i++) {
                    var choice = choices[i];
                    var optionElement = new Element('option', {value : choice}).update(choice);
                    if (itemValue.value == choice) {
                        optionElement.selected = true;    
                    }
                    selectElement.insert(optionElement);
                }
                formElement.insert('Value: ');
                formElement.insert(selectElement);
            } else {
                this.addFormInfoElement('Value: ', formElement, 'value', itemValue.value, 30, 'margin-left:12px');
                if (itemValue.unit) {
                    this.addFormInfoElement('Unit: ', formElement, 'unit', itemValue.unit, 30, 'margin-left:21px');
                }
                if (itemValue.perUnit) {
                    this.addFormInfoElement('PerUnit: ', formElement, 'perUnit', itemValue.perUnit, 30, 'margin-left:1px');
                }
            }
            pElement.insert(formElement);
            updateItemElement.insert(pElement);

            formElement.insert(new Element('br')).insert(new Element('br'));

            // sumbit and event
            var btnSubmit = new Element('input', {type : 'button', value : 'Update'});
            formElement.insert(btnSubmit);
            Event.observe(btnSubmit, "click", this.updateDataItem.bind(this));

            pElement.insert(formElement);
            updateItemElement.insert(pElement);
        }
        return updateItemElement;
    },
    updateDataItemSuccess: function() {
        // update elements and status
        $(this.updateFormStatusName).replace(new Element('div', {id : this.updateFormStatusName}).insert(new Element('b').update('UPDATED!')));

        window.location.href = window.location.href;
    }
}));