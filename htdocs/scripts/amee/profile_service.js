var Profile = Class.create({
    initialize: function() {
    },
    addProfile: function() {
        new Ajax.Request(window.location.href, {
            method: 'post',
            parameters: 'profile=true',
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.addProfileSuccess.bind(this)
        });
    },
    addProfileSuccess: function(t) {
        window.location.href = window.location.href;
    }
});

var ProfileCategoryResource = Class.create({
    initialize: function(profileUid, categoryPath) {
        this.profileUid = profileUid;
        this.categoryPath = categoryPath;
        this.loaded = false;
        this.resource = null;
    },
    load: function() {
        var url = '/profiles/' + this.profileUid + this.categoryPath;
        var params = new Hash();
        params.set('method', 'get');
        new Ajax.Request(url + '?' + Object.toQueryString(params), {
            method: 'post',
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.loadSuccess.bind(this)
        });
    },
    loadSuccess: function(response) {
        this.response = response;
        this.loadedCallback();
    },
    loadedCallback: function() {
    },
    update: function() {
    }
});

var BaseProfileApiService = Class.create(ApiService, ({
    // Initialization
    initialize: function($super, params) {
        $super(params);
    },
    getTrailRootPath: function() {
        return 'profiles';
    },
    getTrailOtherPaths: function(json) {
        if (json.profile) {
            return [json.profile.uid];
        } else {
            return [];
        }
    }
}));

var ProfileItemsApiService = Class.create(BaseProfileApiService, ({
    // Initialization
    initialize: function($super, params) {
        $super(params);
    },
    renderApiResponse: function($super) {

        $super();

        var json = this.response.responseJSON;

        // update elements
        this.totalAmountElement = $(this.tAmountElementName);

        // create total
        var totalElement;
        if (json.totalAmount.value) {
            totalElement = new Element('p', {id : this.tAmountElementName}).insert("Total "
                    + this.getUnit(json) + " "
                    + json.totalAmount.value);
        } else {
            totalElement = new Element('p', {id : this.tAmountElementName}).insert("Total ");
        }

        // replace total amount
        this.totalAmountElement.replace(totalElement);
    },
    getTableHeadingElement: function(json) {
        return new Element('tr')
                .insert(this.getHeadingData('item'))
                .insert(this.getHeadingData(this.getUnit(json)))
                .insert(this.getHeadingData('Name'))
                .insert(this.getHeadingData('Start Date'))
                .insert(this.getHeadingData('End Date'))
                .insert(this.getHeadingData('Actions'));
    }
}));

var ProfileCategoryApiService = Class.create(ProfileItemsApiService, ({
    // Initialization
    initialize: function($super, params) {
        $super(params);
        // api category items
        this.headingCategory = params.headingCategory || "";
    },
    renderApiResponse: function($super) {

        var json = this.response.responseJSON;

        if (json.profileItems.length > 0) {
            $super();
        }

        if (json.profileCategories.length > 0) {
            // update elements
            this.headingCategoryElement = $(this.headingElementName);
            this.headingContentElement = $(this.headingElementName);

            // set section heading
            this.headingCategoryElement.innerHTML = this.headingCategory;

            // create table headings
            var tableBody = new Element('tbody').insert(this.getHeadingCategoryElement());

            // create table details
            var detailRows = this.getCategoryDetailRows(json);
            for (var i = 0; i < detailRows.length; i++) {
                tableBody.insert(detailRows[i]);
            }

            // replace table
            var tableElement = new Element('table', {id : this.contentElementName}).insert(tableBody);
            this.headingContentElement.replace(tableElement);
        }
    },
    getDetailRows: function(json) {
        var rows = [];
        if (json.profileItems) {
            for (var i = 0; i < json.profileItems.length; i++) {
                var profileItem = json.profileItems[i];
                var detailRow = new Element('tr', {id : profileItem.uid})
                        .insert(new Element('td', {id: profileItem.dataItem.uid}).insert(profileItem.dataItem.Label))
                        .insert(new Element('td').insert(profileItem.amount.value))
                        .insert(new Element('td').insert(profileItem.name))
                        .insert(new Element('td').insert(profileItem.startDate))
                        .insert(new Element('td').insert(profileItem.endDate));

                // create actions
                detailRow.insert(this.getActionsTableData({
                    deleteable: true,
                    method: "deleteProfileItem",
                    uid: profileItem.uid}));

                // update array
                rows[i] = detailRow;
            }
            return rows;
        }
        rows[0] = new Element("tr").insert(new Element("td"));
        return rows;
    },
    getHeadingCategoryElement: function() {
        return new Element('tr')
                .insert(this.getHeadingData('Path'))
                .insert(this.getHeadingData('Actions'));
    },
    getCategoryDetailRows: function(json) {
        var rows = [];
        if (json.profileItems) {
            for (var i = 0; i < json.profileCategories.length; i++) {
                var profileCategory = json.profileCategories[i];
                var detailRow = new Element('tr', {id : profileCategory.uid})
                        .insert(new Element('td').insert(profileCategory.name));

                // create actions
                detailRow.insert(this.getCategoryActionsTableData(profileCategory.path));

                // update array
                rows[i] = detailRow;
            }
            return rows;
        }
        rows[0] = new Element("tr").insert(new Element("td"));
        return rows;
    },
    getCategoryActionsTableData: function(path) {
        var actions = new Element('td');
        actions.insert(new Element('a', {href : this.getUrl(path)})
                .insert(new Element('img', {src : '/images/icons/page_edit.png', title : 'Edit', alt : 'Edit', border : 0 })));
        return actions;
    }
}));

var ProfileItemApiService = Class.create(BaseProfileApiService, ({
    // Initialization
    initialize: function($super, params) {
        $super(params);
    },
    renderApiResponse: function() {

        var json = this.response.responseJSON;

        if (json.profileItem) {
            var profileItem = json.profileItem;

            // render info
            if (profileItem.name != '') {
                $('name').replace(this.getInfoElement('name', 'Name', profileItem.name));
            }
            $('amount').replace(this.getInfoElement('amount', 'Amount', profileItem.amount.value + " " + profileItem.amount.unit));
            $('startDate').replace(this.getInfoElement('startDate', 'StartDate', profileItem.startDate));
            $('endDate').replace(this.getInfoElement('endDate', 'EndDate', profileItem.endDate));
            $('fullPath').replace(this.getInfoElement('fullPath', 'Full Path', window.location.pathname));
            $('dataItemLabel').replace(this.getInfoElement('dataItemLabel', 'Data Item Label', profileItem.dataItem.Label));
            $('itemDefinition').replace(this.getInfoElement('itemDefinition', 'Item Definition', profileItem.itemDefinition.name));
            $('environment').replace(this.getInfoElement('environment', 'Environment', profileItem.environment.name));
            $('uid').replace(this.getInfoElement('uid', 'UID', profileItem.uid));
            $('created').replace(this.getInfoElement('created', 'Created', profileItem.created));
            $('modified').replace(this.getInfoElement('modified', 'Modified', profileItem.modified));


            // render form table info
            var tableBody = new Element('tbody');
            tableBody.insert(this.getTableHeadingElement());
            tableBody.insert(this.getFormInfoElement('Name', 'name', profileItem.name, 30));
            tableBody.insert(this.getFormInfoElement('Start Date', 'startDate', profileItem.startDate, 20));
            tableBody.insert(this.getFormInfoElement('End Date', 'endDate', profileItem.endDate, 20));

            for (var i = 0; i < profileItem.itemValues.length; i++) {
                var itemValue = profileItem.itemValues[i];

                var newRow = new Element("tr");
                var dataLabel = new Element("td").insert(
                        new Element('a', {href : this.getUrl(itemValue.displayPath) }).update(itemValue.displayName));
                var dataInfo = new Element('td');
                var inputValue = new Element('input', {type : 'text', name : itemValue.displayPath, value : itemValue.value, size : 30});
                dataInfo.insert(inputValue);

                var unitSelectElement;
                var choices;
                var choice;
                var optionElement;
                var unitInfo;

                if (itemValue.unit) {
                    if (itemValue.itemValueDefinition.unit[i] && itemValue.itemValueDefinition.unit[i].choices) {

                        unitSelectElement = new Element('select', {name : itemValue.displayPath + "Unit"});
                        choices = itemValue.itemValueDefinition.unit[i].choices.split(",");

                        for (var k = 0; k < choices.length; k++) {
                            choice = choices[k];
                            if (itemValue.unit == choice) {
                                optionElement = new Element("option", {value : choice, selected : true}).update(choice);
                            } else {
                                optionElement = new Element("option", {value : choice}).update(choice);
                            }
                            unitSelectElement.insert(optionElement);
                        }
                        dataInfo.insert(unitSelectElement);

                    } else {
                        unitInfo = new Element('input', {type : 'text', name : itemValue.displayPath + "Unit", value : itemValue.unit, size : 30});
                        dataInfo.insert(unitInfo);
                    }
                }

                if (itemValue.perUnit) {
                    if (itemValue.itemValueDefinition.perUnit[i] && itemValue.itemValueDefinition.perUnit[i].choices) {

                        unitSelectElement = new Element('select', {name : itemValue.displayPath + "PerUnit"});
                        choices = itemValue.itemValueDefinition.perUnit[i].choices.split(",");

                        for (var k = 0; k < choices.length; k++) {
                            choice = choices[k];
                            if (itemValue.perUnit == choice) {
                                optionElement = new Element("option", {value : choice, selected : true}).update(choice);
                            } else {
                                optionElement = new Element("option", {value : choice}).update(choice);
                            }
                            unitSelectElement.insert(optionElement);
                        }
                        dataInfo.insert(unitSelectElement);

                    } else {
                        unitInfo = new Element('input', {type : 'text', name : itemValue.displayPath + "PerUnit", value : itemValue.perUnit, size : 30});
                        dataInfo.insert(unitInfo);
                    }
                }


                newRow.insert(dataLabel);
                newRow.insert(dataInfo);

                tableBody.insert(newRow);
            }

            var tableElement = new Element('table', {id : 'inputTable'}).insert(tableBody);
            $('inputTable').replace(tableElement);

            var btnSubmit = new Element('input', {type : 'button', value : 'Update'});
            $("inputSubmit").replace(btnSubmit);
            Event.observe(btnSubmit, "click", this.updateProfileItem.bind(this));
        }
    },
    getFormInfoElement: function(label, name, info, size) {
        var newRow = new Element("tr").insert(new Element("td").update(label));
        var dataElement = new Element("td");
        dataElement.insert(new Element('input', {type : 'text', name : name, value : info, size : size}));
        newRow.insert(dataElement);
        return newRow;
    },
    getInfoElement: function(id, heading, info) {
        var spanElement = new Element("span", {id : id});
        spanElement.update(heading + ": " + info);
        spanElement.insert(new Element('br'));
        return spanElement;
    },
    getTableHeadingElement: function(json) {
        return new Element('tr')
                .insert(this.getHeadingData('Name'))
                .insert(this.getHeadingData('Value'));
    },
    getHeadingData: function(heading) {
        return new Element('th').insert(heading);
    },
    updateProfileItem: function() {

        var method;
        if (window.location.search == "") {
            method = "?method=put";
        } else {
            method = "&method=put";
        }

        $('updateStatusSubmit').innerHTML = '';

        new Ajax.Request(window.location.href + method, {
            method: 'post',
            parameters: $('inputForm').serialize(),
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.updateProfileItemSuccess.bind(this),
            onFailure: this.updateProfileItemFail.bind(this)
        });
    },
    updateProfileItemSuccess: function(response) {
        // update elements and status
        this.response = response;
        $('updateStatusSubmit').replace(new Element('div', {id : 'updateStatusSubmit'}).insert(new Element('b').update('UPDATED!')));
        this.renderApiResponse();
    },
    updateProfileItemFail: function() {
        $('updateStatusSubmit').replace(new Element('div', {id : 'updateStatusSubmit'}).insert(new Element('b').update('ERROR!')));
    }
}));

var ProfilesApiService = Class.create(BaseProfileApiService, ({
    // Initialization
    initialize: function($super, params) {
        $super(params);
    },
    renderDataCategoryApiResponse: function() {
        // override but do nothing
    },
    getTableHeadingElement: function(json) {
        return new Element('tr')
                .insert(this.getHeadingData('Path'))
            // .insert(this.getHeadingData('Group'))
            // .insert(this.getHeadingData('User'))
                .insert(this.getHeadingData('Created'))
                .insert(this.getHeadingData('Actions'));
    },
    getActionsTableData: function(params) {
        params.deleteable = params.deleteable || false;
        var actions = new Element('td');
        actions.insert(new Element('a', {href : this.getUrl(params.uid)})
                .insert(new Element('img', {src : '/images/icons/page_edit.png', title : 'Edit', alt : 'Edit', border : 0 })));
        actions.insert(new Element('a', {
            onClick: params.method + '("' + params.uid + '") ; return false;',
            href: 'javascript:' + params.method + '("' + params.uid + '");'})
                .insert(new Element('img', {
            src: '/images/icons/page_delete.png',
            title: 'Delete',
            alt: 'Delete',
            border: 0})));
        return actions;
    },
    getDetailRows: function($super, json) {
        var rows = [];
        if (json.profiles) {
            for (var i = 0; i < json.profiles.length; i++) {
                var profile = json.profiles[i];
                var detailRow = new Element('tr', {id : 'Elem_' + profile.uid})
                        .insert(new Element('td').insert(profile.path))
                    // .insert(new Element('td').insert(profile.permission.group.name))
                    // .insert(new Element('td').insert(profile.permission.user.username))
                        .insert(new Element('td').insert(profile.created));

                // create actions
                detailRow.insert(this.getActionsTableData({
                    deleteable: true,
                    method: 'deleteProfile',
                    uid: profile.uid}));

                // update array
                rows[i] = detailRow;
            }
            return rows;
        } else {
            return $super(json);
        }
    }
}));

var ProfileItemValueApiService = Class.create(ProfileItemApiService, ({
    // Initialization
    initialize: function($super, params) {
        $super(params);
    },
    renderApiResponse: function() {

        var json = this.response.responseJSON;

        // render info
        if (json.itemValue) {
            var itemValue = json.itemValue;
            if (itemValue.value != '') {
                $('value').replace(this.getInfoElement('value', 'Value', itemValue.value));
            }
            $('fullPath').replace(this.getInfoElement('fullPath', 'Full Path', window.location.pathname));
            $('dataItemLabel').replace(this.getInfoElement('dataItemLabel', 'Data Item Label', itemValue.item.dataItem.Label));
            $('itemValueDefinition').replace(this.getInfoElement('itemValueDefinition', 'Item Value Definition', itemValue.itemValueDefinition.name));
            $('valueDefinition').replace(this.getInfoElement('valueDefinition', 'Value Definition', itemValue.itemValueDefinition.valueDefinition.name));
            $('valueType').replace(this.getInfoElement('valueType', 'Value Type', itemValue.itemValueDefinition.valueDefinition.valueType));
            $('environment').replace(this.getInfoElement('environment', 'Environment', itemValue.item.environment.name));
            $('uid').replace(this.getInfoElement('uid', 'UID', itemValue.uid));
            $('created').replace(this.getInfoElement('created', 'Created', itemValue.created));
            $('modified').replace(this.getInfoElement('modified', 'Modified', itemValue.modified));
        }

        // render form
        var inputValuesElement = new Element('span', {id : 'inputValues'});
        if (itemValue.itemValueDefinition.choices) {
            var choice, choiceName, choiceValue;
            var choices = itemValue.itemValueDefinition.choices.split(",");
            var selectElement = new Element('select', {name : 'value'});
            var selectedOption = false;
            for (var i = 0; i < choices.length; i++) {
                choice = choices[i].split("=");
                if (choice.length == 2) {
                    choiceName = choice[0];
                    choiceValue = choice[1];
                } else {
                    choiceName = choiceValue = choice[0];
                }
                selectedOption = itemValue.value == choiceValue;
                selectElement.insert(new Element('option', {value : choiceValue, selected : selectedOption}).update(choiceValue));
            }
            inputValuesElement.insert('Value: ');
            inputValuesElement.insert(selectElement);
            inputValuesElement.insert(new Element('br'));
        } else {
            this.addFormInfoElement('Value: ', inputValuesElement, 'value', itemValue.value, 30, 'margin-left:23px');
            if (itemValue.unit) {
                this.addFormInfoElement('Unit: ', inputValuesElement, 'unit', itemValue.unit, 30, 'margin-left:34px');
            }
            if (itemValue.perUnit) {
                this.addFormInfoElement('PerUnit: ', inputValuesElement, 'perUnit', itemValue.perUnit, 30, 'margin-left:9px');
            }
        }
        $('inputValues').replace(inputValuesElement);
        var btnSubmit = new Element('input', {type : 'button', value : 'Update'});
        $("inputSubmit").replace(btnSubmit);
        Event.observe(btnSubmit, "click", this.updateProfileItemValue.bind(this));
    },
    render: function() {
        this.renderTrail();
        this.renderApiResponse();
    },
    addFormInfoElement: function(label, pElement, name, info, size, style) {
        pElement.insert(label);
        if (style) {
            pElement.insert(new Element('input', {type : 'text', name : name, value : info, size : size, style : style}));
        } else {
            pElement.insert(new Element('input', {type : 'text', name : name, value : info, size : size}));
        }
        pElement.insert(new Element('br'));
    },
    updateProfileItemValue: function() {
        var method;
        if (window.location.search == "") {
            method = "?method=put";
        } else {
            method = "&method=put";
        }

        $('updateStatusSubmit').innerHTML = '';

        new Ajax.Request(window.location.href + method, {
            method: 'post',
            parameters: $('inputForm').serialize(),
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.updateProfileItemValueSuccess.bind(this),
            onFailure: this.updateProfileItemValueFail.bind(this)
        });
    },
    updateProfileItemValueSuccess: function() {
        // update elements and status
        this.response = response;
        $('updateStatusSubmit').replace(new Element('div', {id : 'updateStatusSubmit'}).insert(new Element('b').update('UPDATED!')));
        this.renderApiResponse();
    },
    updateProfileItemValueFail: function() {
        $('updateStatusSubmit').replace(new Element('div', {id : 'updateStatusSubmit'}).insert(new Element('b').update('ERROR!')));
    }
}));