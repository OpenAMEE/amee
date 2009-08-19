var BaseDataApiService = Class.create(ApiService, ({
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
        params = params || {};
        method = method || '';
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
        this.dataCategoriesHeading = params.dataCategoriesHeading || 'Data Categories';
        this.dataCategoriesHeadingElementName = params.dataCategoriesHeadingElementName || 'apiDataCategoriesHeading';
        this.dataCategoriesContentElementName = params.dataCategoriesContentElementName || 'apiDataCategoriesContent';
        this.updateCategory = params.updateCategory || false;
        this.createCategory = params.createCategory || false;
    },
    renderApiResponse: function($super) {

        var json = this.response.responseJSON;

        if (json.children.dataItems.rows) {
            $super(json.children.pager);
        }

        if (json.children.dataCategories.length > 0) {

            // update elements
            this.dataCategoriesHeadingElement = $(this.dataCategoriesHeadingElementName);
            this.dataCategoriesContentElement = $(this.dataCategoriesContentElementName);
            
            // set section heading
            this.dataCategoriesHeadingElement.innerHTML = this.dataCategoriesHeading;

            // create table headings
            var tableBody = new Element('tbody').insert(this.getHeadingCategoryElement());

            // create table details
            var detailRows = this.getCategoryDetailRows(json);
            for (var i = 0; i < detailRows.length; i++) {
                tableBody.insert(detailRows[i]);
            }

            // replace table
            var tableElement = new Element('table', {id : this.dataCategoriesContentElementName}).insert(tableBody);
            this.dataCategoriesContentElement.replace(tableElement);
        }

        if (this.createCategory) {
            $('apiCreateDataCategory').replace(this.getCreateCategoryElement('apiCreateDataCategory', json));
        }

        if (this.updateCategory) {
            $('apiUpdateDataCategory').replace(this.getUpdateCategoryElement('apiUpdateDataCategory', json));
        }
    },
    getCreateCategoryElement: function(id, json) {

        var dataCategoryActions = DATA_ACTIONS.getActions('dataCategory');
        var dataItemActions = DATA_ACTIONS.getActions('dataItem');

        var createCatElement = new Element('div', {id : id});

        if ((dataCategoryActions.isAllowCreate() || dataItemActions.isAllowCreate()) &&
            ITEM_DEFINITIONS.available) {

            createCatElement.insert(new Element('h2').update('Create Data Category / Data Item'));

            var dataCategory = json.dataCategory;

            var formElement = new Element('form', {action : "#", id : this.createFormName});
            var typeSelectElement = new Element('select', {id : 'newObjectType', name : 'newObjectType', style : 'margin-left:52px'});
            var selectElement = new Element('select', {name : 'itemDefinitionUid'});
            var pElement = new Element('p');

            formElement.insert('Type: ');
            if (dataCategoryActions.isAllowCreate()) {
                typeSelectElement.insert(new Element('option', {value : 'DC'}).update('Data Category'));
            }
            if (dataCategory.itemDefinition && dataItemActions.isAllowCreate()) {
                typeSelectElement.insert(new Element('option', {value : 'DI'}).update('Data Item (for ' + dataCategory.itemDefinition.name + ')'));
            }
            formElement.insert(typeSelectElement).insert(new Element('br'));

            this.addFormInfoElement('Name: ', formElement, 'name', '', 30, 'margin-left:46px');
            this.addFormInfoElement('Path: ', formElement, 'path', '', 30, 'margin-left:54px');

            // item definitions
            selectElement.insert(new Element('option', {value : ''}).update('(No Item Definition)'));
            formElement.insert('Item Definition: ');
            var itemDefinitions = ITEM_DEFINITIONS.getItemDefinitions();
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

        var dataCategoryActions = DATA_ACTIONS.getActions('dataCategory');

        var updateCatElement = new Element('div', {id : id});

        if (dataCategoryActions.isAllowModify() && ITEM_DEFINITIONS.available) {

            updateCatElement.insert(new Element('h2').update('Update Data Category'));

            var dataCategory = json.dataCategory;

            var formElement = new Element('form', {action : "#", id : this.updateFormName});
            var selectElement = new Element('select', {name : 'itemDefinitionUid'});
            var pElement = new Element('p');

            this.addFormInfoElement('Name: ', formElement, 'name', dataCategory.name, 30, 'margin-left:88px');
            this.addFormInfoElement('Path: ', formElement, 'path', dataCategory.path, 30, 'margin-left:96px');

            formElement.insert('Deprecated: ').insert("Yes:");
            var radioElementYes = new Element('input', {type : "radio", name : 'deprecated', value: "true"});
            formElement.insert(radioElementYes).insert("No:");
            var radioElementNo = new Element('input', {type : "radio", name : 'deprecated', value: "false"});
            formElement.insert(radioElementNo).insert("<br>");
            if (dataCategory.deprecated) {
                radioElementYes.checked = true;
            } else {
                radioElementNo.checked = true;
            }

            // item definitions
            selectElement.insert(new Element('option', {value : ''}).update('(No Item Definition)'));
            formElement.insert('Item Definition: ');
            var itemDefinitions = ITEM_DEFINITIONS.getItemDefinitions();
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
    getTableHeadingElement: function(json) {
        return new Element('tr')
                .insert(this.getHeadingData('Item'))
                .insert(this.getHeadingData('Actions'));
    },
    getDetailRows: function(json) {
        var dataItemActions = DATA_ACTIONS.getActions('dataItem');
        var rows = [];
        if (json.children.dataItems) {
            for (var i = 0; i < json.children.dataItems.rows.length; i++) {
                var dataItem = json.children.dataItems.rows[i];
                var detailRow = new Element('tr', {id : 'Elem_' + dataItem.uid})
                        .insert(new Element('td').insert(dataItem.label));

                // create actions
                detailRow.insert(this.getActionsTableData({
                    deleteable: true,
                    actions: dataItemActions,
                    method: 'deleteDataItem',
                    uid: dataItem.uid,
                    path: dataItem.path}));

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

        var dataCategoryActions = DATA_ACTIONS.getActions('dataCategory');

        var actions = new Element('td');

        if (dataCategoryActions.isAllowView()) {
            actions.insert(new Element('a', {href : this.getUrl(path)})
                    .insert(new Element('img', {src : '/images/icons/page_edit.png', title : 'Edit', alt : 'Edit', border : 0 })));
        }

        if (dataCategoryActions.isAllowDelete()) {
            var dUrl = "'" + uid + "','" + window.location.pathname + "/" + path + "'";
            actions.insert(
                    new Element('a', {
                        onClick: dMethod + '(' + dUrl + ') ; return false;',
                        href: 'javascript:' + dMethod + '(' + dUrl + ');'})
                            .insert(
                            new Element('img', {
                                src: '/images/icons/page_delete.png',
                                title: 'Delete',
                                alt: 'Delete',
                                border: 0})));
        }
        return actions;
    },
    createDataCategory: function() {
        var elementList = [$(this.createFormName + "-name"), $(this.createFormName + "-path")];

        $(this.createFormStatusName).innerHTML = '';
        this.resetStyles(elementList);

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
        $(this.updateFormStatusName).innerHTML = '';
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
        this.createItemValue = params.createItemValue || false;
    },
    start: function() {
        var params = {};
        //params['select'] = 'all';
        this.load(params);
    },
    load: function(params) {
        this.response = null;
        params = params || "";
        params['method'] = 'get';
        new Ajax.Request(window.location.href + '?' + Object.toQueryString(params), {
            method: 'post',
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.loadSuccess.bind(this),
            onFailure: this.loadFailure.bind(this)});
    },
    renderApiResponse: function($super) {

        var json = this.response.responseJSON;
        this.path = json.path;

        if (json.dataItem) {
            this.uid = json.dataItem.uid;
            this.renderDataItemApiResponse(json.dataItem);
        }
        $super();
        if (this.updateItem && json.dataItem) {
            $('apiUpdateDataItem').replace(this.getUpdateItemElement('apiUpdateDataItem', json.dataItem));
        }
        if (this.createItemValue && json.dataItem) {
            $('apiCreateDataItemValue').replace(this.getCreateItemValueElement('apiCreateDataItemValue', json.dataItem));
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
    getTableHeadingElement: function(json) {
        return new Element('tr')
                .insert(this.getHeadingData('Name'))
                .insert(this.getHeadingData('Value Definition'))
                .insert(this.getHeadingData('Value Type'))
                .insert(this.getHeadingData('Value'))
                .insert(this.getHeadingData('Start Date'))
                .insert(this.getHeadingData('Actions'));
    },
    getDetailRows: function(json) {
        var dataItemActions = DATA_ACTIONS.getActions('dataItem');
        var rows = [];
        if (json.dataItem.itemValues) {
            var itemValues = json.dataItem.itemValues;
            var k = 0;
            for (var i = 0; i < itemValues.length; i++) {
                var itemValue = itemValues[i];
                //var itemValuesArray = itemValues[i];
                //for(var name in itemValuesArray) {
                    //var itemValueSeries = itemValuesArray[name];
                    //for (var j = 0; j < itemValueSeries.length; j++) {
                        //var itemValue = itemValueSeries[j];
                        var detailRow = new Element('tr', {id : 'Elem_' + itemValue.uid})
                                .insert(new Element('td').insert(itemValue.itemValueDefinition.name))
                                .insert(new Element('td').insert(itemValue.itemValueDefinition.valueDefinition.name))
                                .insert(new Element('td').insert(itemValue.itemValueDefinition.valueDefinition.valueType))
                                .insert(new Element('td').insert(itemValue.value))
                                .insert(new Element('td').insert(itemValue.startDate));

                        // create actions
                        detailRow.insert(this.getActionsTableData({
                            actions: dataItemActions,
                            method: '',
                            uid: itemValue.uid}));
                        // update array
                        rows[k] = detailRow;
                        k++;
                    //}
                //}
            }
        }
        return rows;
    },
    getCreateItemValueElement: function(id, dataItem) {

        var dataItemActions = DATA_ACTIONS.getActions('dataItem');

        var createItemValueElement = new Element('div', {id : id});

        if (dataItemActions.isAllowCreate() && ITEM_VALUE_DEFINITIONS.available) {

            createItemValueElement.insert(new Element('h2').update('Create Data Item Value'));

            var formElement = new Element('form', {action : "#", id : this.createFormName});
            var selectElement = new Element('select', {name : 'valueDefinitionUid'});
            var pElement = new Element('p');

            this.addFormInfoElement('startDate: ', formElement, 'startDate', '', 30, 'margin-left:46px');
            this.addFormInfoElement('value: ', formElement, 'value', '', 30, 'margin-left:54px');

            // item definitions
            selectElement.insert(new Element('option', {value : ''}).update(''));
            formElement.insert('Item Value Definition: ');
            var itemValueDefinitions = ITEM_VALUE_DEFINITIONS.getItemValueDefinitions();
            for (var i = 0; i < itemValueDefinitions.length; i++) {
                var itemValueDefinition = itemValueDefinitions[i];
                if (itemValueDefinition.fromData && !itemValueDefinition.drillDown) {
                    selectElement.insert(new Element('option', {value : itemValueDefinition.uid}).update(itemValueDefinition.name));
                }
            }
            formElement.insert(selectElement).insert(new Element('br')).insert(new Element('br'));

            // sumbit and event
            var btnSubmit = new Element('input', {type : 'button', value : 'Create'});
            formElement.insert(btnSubmit);
            Event.observe(btnSubmit, "click", this.createDataItemValue.bind(this));

            pElement.insert(formElement);
            createItemValueElement.insert(pElement);
        }

        return createItemValueElement;
    },
    createDataItemValue: function() {
        var elementList = [$(this.createFormName + "-startDate"), $(this.createFormName + "-value")];


        $(this.createFormStatusName).innerHTML = '';
        this.resetStyles(elementList);

        if (this.validateElementList(elementList)) {
            this.resetStyles(elementList)
            var params = {
                parameters: $(this.createFormName).serialize(),
                onSuccess: this.createDataItemValueSuccess.bind(this),
                onFailure: this.createDataItemValueFail.bind(this)
            };
            this.apiServiceCall(null, params);
        }
    },
    createDataItemValueSuccess: function() {
        // update elements and status
        $(this.createFormStatusName).replace(new Element('div', {id : this.createFormStatusName}).insert(new Element('b').update('CREATED!')));
        window.location.href = window.location.href;
    },
    createDataItemValueFail: function() {
        $(this.createFormStatusName).replace(new Element('div', {id : this.createFormStatusName}).insert(new Element('b').update('ERROR!')));
    },
    getUpdateItemElement: function(id, dataItem) {

        var dataItemActions = DATA_ACTIONS.getActions('dataItem');
        var updateItemElement = new Element('div', {id : id});

        if (dataItemActions.isAllowModify()) {
            var dateFormat = " (" + this.getDateFormat() + ")";
            var formElement = new Element('form', {action : "#", id : this.updateFormName});
            var pElement = new Element('p');

            updateItemElement.insert(new Element('h2').update('Update Data Item'));

            this.addFormInfoElement('Name: ', formElement, 'name', dataItem.name, 30, 'margin-left:21px');
            this.addFormInfoElement('Path: ', formElement, 'path', dataItem.path, 30, 'margin-left:29px');

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
        $(this.updateFormStatusName).innerHTML = '';

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
            window.location.href = this.getParentPath() + "/" + this.uid;
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
    start: function() {
        this.load({});
    },
    renderApiResponse: function($super) {

        var json = this.response.responseJSON;
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

        var dataItemActions = DATA_ACTIONS.getActions('dataItem');

        var updateItemElement = new Element('div', {id : id});

        if (dataItemActions.isAllowModify()) {
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
                var dateFormat = " (" + this.getDateFormat() + ")";
                this.addFormInfoElement('Value: ', formElement, 'value', itemValue.value, 30, 'margin-left:12px');
                this.addFormInfoElement('Start Date:', formElement, 'startDate', itemValue.startDate, 30, 'margin-left:12px', dateFormat);
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
        $(this.updateFormStatusName).replace(new Element('div', {id : this.updateFormStatusName})
                .insert(new Element('b')
                .update('UPDATED!')));
        window.location.href = window.location.href;
    }
}));

var DrillDown = Class.create({
    initialize: function(fullPath, apiVersion, dateFormat) {
        this.fullPath = fullPath;
        this.apiVersion = apiVersion || '1.0';
        this.dateFormat = dateFormat || 'date format not specified';
    },
    start: function() {
        var profileItemActions = PROFILE_ACTIONS.getActions('profileItem');
        var dataItemActions = DATA_ACTIONS.getActions('dataItem');
        if (profileItemActions.isAllowCreate() && dataItemActions.isAllowView()) {
            this.load();
        }
    },
    load: function(params) {
        params = params || {};
        this.response = null;
        var url = this.fullPath + '/drill';
        params['method'] = 'get';
        url = url + '?' + Object.toQueryString(params);
        new Ajax.Request(url, {
            method: 'post',
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.loadSuccess.bind(this)});
    },
    loadSuccess: function(response) {
        this.response = response;
        this.render();
    },
    addProfileItem: function() {
        new Ajax.Request(window.location.href, {
            method: 'post',
            parameters: $('createProfileFrm').serialize(),
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.addProfileItemSuccess.bind(this)
        });
    },
    addProfileItemSuccess: function(response) {
        window.location.href = window.location.href;
    },
    render: function() {

        var resource = this.response.responseJSON;

        // store stuff locally
        this.selectName = resource.choices.name;
        this.selections = resource.selections;

        // reset heading
        $("createProfileHeading").innerHTML = "Create Profile Item";

        // get and reset our div
        var div = $("createProfileItemDiv");
        div.innerHTML = '';
        // add list of previous selections
        var list = document.createElement('ul');
        for (var i = 0; i < resource.selections.length; i++) {
            var item = document.createElement('li');
            item.innerHTML = resource.selections[i].name + ': ' + resource.selections[i].value;
            list.appendChild(item);
        }
        div.appendChild(list);
        if (this.selectName == 'uid') {
            var choice = resource.choices.choices[0];
            this.uid = choice.value;
            // params for V1 and V2
            // dataItemUid
            div.appendChild(new Element('input', {type : 'hidden', name : 'dataItemUid', value : this.uid}));
            // name
            div.appendChild(document.createTextNode('Name: '));
            var nameInput = new Element('input', {type : 'text', name : 'name', id : 'name', style : 'margin-left:49px'});
            div.appendChild(nameInput);
            div.appendChild(document.createElement('br'));
            // V1 or V2 params?
            if (this.apiVersion == '1.0') {
                // V1 params
                div.appendChild(document.createTextNode('Valid From: '));
                var validFromInput = new Element('input', {type : 'text', name : 'validFrom', id : 'validFrom'});
                div.appendChild(validFromInput);
            } else {
                // V2 params
                // startDate
                div.appendChild(document.createTextNode('Start Date: '));
                var startDateInput = new Element('input', {type : 'text', name : 'startDate', id : 'startDate', style : 'margin-left:20px'});
                div.appendChild(startDateInput);
                div.appendChild(document.createTextNode("  (" + this.dateFormat + ")"));
                div.appendChild(document.createElement('br'));
                // endDate
                div.appendChild(document.createTextNode('End Date: '));
                var endDateInput = new Element('input', {type : 'text', name : 'endDate', id : 'endDate', style : 'margin-left:25px'});
                div.appendChild(endDateInput);
                div.appendChild(document.createTextNode("  (" + this.dateFormat + ")"));
                div.appendChild(document.createElement('br'));
                // duration
                div.appendChild(document.createTextNode('Duration: '));
                var durationInput = new Element('input', {type : 'text', name : 'duration', id : 'duration', style : 'margin-left:31px'});
                div.appendChild(durationInput);
                div.appendChild(document.createTextNode("  (e.g PT30M [30 mins])"));
                div.appendChild(document.createElement('br'));
            }
            div.appendChild(document.createElement('br'));
            var button = document.createElement('input');
            button.type = 'button';
            button.value = 'Add: ' + choice.value;
            button.name = 'Add: ' + choice.value;
            Event.observe(button, "click", this.addProfileItem.bind(this));
            div.appendChild(button);
        } else {
            // add the form select
            var select = document.createElement('select');
            select.id = resource.choices.name;
            select.name = resource.choices.name;
            var defaultOpt = document.createElement('option');
            defaultOpt.value = '';
            defaultOpt.appendChild(document.createTextNode('(select ' + resource.choices.name + ')'));
            select.appendChild(defaultOpt);
            for (var i = 0; i < resource.choices.choices.length; i++) {
                var choice = resource.choices.choices[i];
                var opt = document.createElement('option');
                opt.value = choice.value;
                opt.appendChild(document.createTextNode(choice.name));
                select.appendChild(opt);
            }
            Event.observe(select, "change", this.drillDownSelect.bind(this));
            div.appendChild(select);
        }
    },
    drillDownSelect: function(e) {
        var select = $(this.selectName);
        if (select.value != '') {
            var params = {};
            for (var i = 0; i < this.selections.length; i++) {
                params[this.selections[i].name] = this.selections[i].value;
            }
            params[this.selectName] = select.value;
            this.load(params);
        }
    }
});

// ItemDefinition
var ItemDefinition = Class.create({
    initialize: function(itemDefinition) {
        Object.extend(this, itemDefinition);
    }
});

// ItemDefinitions Resource
var ItemDefinitionsResource = Class.create({
    initialize: function() {
        this.itemDefinitions = [];
        this.path = '/definitions/itemDefinitions';
    },
    start: function() {
        this.load();
    },
    load: function() {
        this.itemDefinitions = [];
        var params = new Hash();
        params.set('method', 'get');
        params.set('itemsPerPage', '500');
        new Ajax.Request(this.path + '?' + Object.toQueryString(params), {
            method: 'post',
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.loadSuccess.bind(this),
            onFailure: this.loadFailure.bind(this)});
    },
    loadSuccess: function(response) {
        var resource = response.responseJSON;
        resource.itemDefinitions.each(function(itemDefinition) {
            this.itemDefinitions.push(new ItemDefinition(itemDefinition));
        }.bind(this));
        this.loaded = true;
        this.available = true;
        this.notify('loaded', this);
    },
    loadFailure: function() {
        this.loaded = true;
        this.available = false;
        this.notify('loaded', this);
    },
    getItemDefinitions: function() {
        return this.itemDefinitions;
    }
});
Object.Event.extend(ItemDefinitionsResource);

// ItemValueDefinition
var ItemValueDefinition = Class.create({
    initialize: function(itemValueDefinition) {
        Object.extend(this, itemValueDefinition);
    }
});

// ItemValueDefinitions Resource
var ItemValueDefinitionsResource = Class.create({
    initialize: function(itemDefinitionUid) {
        this.itemValueDefinitions = [];
        this.path = '/definitions/itemDefinitions/'+ itemDefinitionUid + '/itemValueDefinitions';
    },
    start: function() {
        this.load();
    },
    load: function() {
        this.itemDefinitions = [];
        var params = new Hash();
        params.set('method', 'get');
        new Ajax.Request(this.path + '?' + Object.toQueryString(params), {
            method: 'post',
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.loadSuccess.bind(this),
            onFailure: this.loadFailure.bind(this)});
    },
    loadSuccess: function(response) {
        var resource = response.responseJSON;
        resource.itemValueDefinitions.each(function(itemValueDefinition) {
            this.itemValueDefinitions.push(new ItemValueDefinition(itemValueDefinition));
        }.bind(this));
        this.loaded = true;
        this.available = true;
        this.notify('loaded', this);
    },
    loadFailure: function() {
        this.loaded = true;
        this.available = false;
        this.notify('loaded', this);
    },
    getItemValueDefinitions: function() {
        return this.itemValueDefinitions;
    }
});
Object.Event.extend(ItemValueDefinitionsResource);