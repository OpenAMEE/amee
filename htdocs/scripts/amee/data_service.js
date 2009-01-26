var DataCategoryApiService = Class.create(ApiService, ({
        // Initialization
    initialize: function($super, params) {
        $super(params);
        this.updateCategory = params.updateCategory || false;
        this.createCategory = params.createCategory || false;

        this.updateFormName = 'apiUpdateForm';
        this.createFormName = 'apiCreateForm';

        this.updateFormStatusName = 'apiUpdateSubmitStatus';
        this.createFormStatusName = 'apiCreateSubmitStatus';
    },
    renderApiResponse: function($super, response) {
        var json = response.responseJSON;
        Log.debug(json);

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
            $('apiCreateDataCategory').replace(this.getCreateCategoryElement('apiCreateDataCategory'));
        }

        if (this.updateCategory) {
            $('apiUpdateDataCategory').replace(this.getUpdateCategoryElement('apiUpdateDataCategory', json.dataCategory));
        }
    },
    getCreateCategoryElement: function(id) {
        var createCatElement = new Element('div', {id : id});

        if(this.allowModify) {


        }
        return createCatElement;

    },
    getUpdateCategoryElement: function(id, dataCategory) {

        var updateCatElement = new Element('div', {id : id});

        if(this.allowModify) {
            updateCatElement.insert(new Element('h2').update('Update Data Category'));

            var formElement = new Element('form', {action : "#", id : this.updateFormName});
            var pElement = new Element('p');

            this.addFormInfoElement('Name: ', formElement, 'name', dataCategory.name, 30, 'margin-left:20px');
            this.addFormInfoElement('Path: ', formElement, 'path', dataCategory.path, 30, 'margin-left:28px');

            //TODO: itemDefinition
//            Item Definition: <select name='itemDefinitionUid'>
//              <option value=''>(No Item Definition)</option>
//              <#list browser.itemDefinitions as id>
//                <option value='${id.uid}'<#if dataCategory.itemDefinition?? && dataCategory.itemDefinition.uid == id.uid> selected</#if>>${id.name}</option>
//              </#list>
//            </select><br/><br/>

            var btnSubmit = new Element('input', {type : 'button', value : 'Update'});
            formElement.insert(btnSubmit);
            Event.observe(btnSubmit, "click", this.updateDataCategory.bind(this));
            
            pElement.insert(formElement);
            updateCatElement.insert(pElement);

        }
        return updateCatElement;
    },
    addFormInfoElement: function(label, pElement, name, info, size, style) {
        pElement.insert(label);
        pElement.insert(new Element('input', {name : name, value : info, size : size, style : style}));
        pElement.insert(new Element('br'));
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
                detailRow.insert(this.getActionsTableData(dataItem.path, 'deleteDataItem', dataItem.uid));

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
    updateDataCategory: function() {
        var method;
        if (window.location.search == "") {
            method = "?method=put";
        } else {
            method = "&method=put";
        }

        $(this.updateFormStatusName).innerHTML='';

        var myAjax = new Ajax.Request(window.location.href + method, {
            method: 'post',
            parameters: $(this.updateFormName).serialize(),
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.updateDataCategorySuccess.bind(this),
            onFailure: this.updateDataCategoryFail.bind(this)
        });
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
