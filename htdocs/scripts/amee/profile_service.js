var ProfileCategoryResource = Class.create();
ProfileCategoryResource.prototype = {
  initialize: function(profileUid, categoryPath) {
    this.profileUid = profileUid;
    this.categoryPath = categoryPath;
    this.loaded = false;
    this.resource = null;
  },
  load: function() {
    var url = '/profiles/' + this.profileUid + this.categoryPath;
    var myAjax = new Ajax.Request(url, {
      method: 'get',
      requestHeaders: ['Accept', 'application/json'],
        onSuccess: this.loadSuccess.bind(this)
      });
    },
  loadSuccess: function(response) {
    this.resource = eval('(' + response.responseText + ')');
    this.loaded = true;
    this.loadedCallback();
  },
  loadedCallback: function() {
  },
  update: function() {
  }
};

var DrillDown = Class.create();
DrillDown.prototype = {
    initialize: function(fullPath, apiVersion, dateFormat) {
        this.fullPath = fullPath;
        this.apiVersion = apiVersion || '1.0';
        this.dateFormat = dateFormat || 'date format not specified';
		var selectName = null;
		var selections = null;
		var uid = null;
    },
  	addProfileItem: function() {
	    var myAjax = new Ajax.Request(window.location.href, {
	        method: 'post',
			parameters: $('createProfileFrm').serialize(),
			requestHeaders: ['Accept', 'application/json'],
	        onSuccess: this.addProfileItemSuccess.bind(this)
	    });
	},
	addProfileItemSuccess: function(response) {
		window.location.href = window.location.href;
	},
    loadDrillDown: function(params) {
		var url = this.fullPath + '/drill';
		if (params != '') {
			url = url + '?' + params;
		}
        var myAjax = new Ajax.Request(url, {
            method: 'get',
			requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.loadDrillDownSuccess.bind(this)
        });
    },
    loadDrillDownSuccess: function(response) {
        var obj = eval('(' + response.responseText + ')');
        this.drillDownLoadedCallback(obj);
    },
    drillDownLoadedCallback: function(obj) {
        // store stuff locally
        this.selectName = obj.choices.name;
  	    this.selections = obj.selections;

        // reset heading 
        $("createProfileHeading").innerHTML="Create Profile Item";

		// get and reset our div
		var div = $("createProfileItemDiv");
		div.innerHTML = '';
		// add list of previous selections
		var list = document.createElement('ul');
		for (var i = 0; i < obj.selections.length; i++) {
			var item = document.createElement('li');
			item.innerHTML = obj.selections[i].name + ': ' + obj.selections[i].value;
			list.appendChild(item);
		}
		div.appendChild(list);
		if (this.selectName == 'uid') {
            var choice = obj.choices.choices[0];
            this.uid = choice.value;
            if (this.apiVersion == '1.0') {
                div.appendChild(document.createTextNode('Valid From: '));
                var validFromInput = new Element('input', {type : 'text', name : 'validFrom', id : 'validFrom'});
                div.appendChild(validFromInput);
            } else {
                div.appendChild(new Element('input', {type : 'hidden', name : 'dataItemUid', value : this.uid}));
                div.appendChild(document.createTextNode('Start Date: '));
                var startDateInput = new Element('input', {type : 'text', name : 'startDate', id : 'startDate', style : 'margin-left:20px'});

                div.appendChild(startDateInput);
                div.appendChild(document.createTextNode("  (" + this.dateFormat + ")"));
                div.appendChild(document.createElement('br'));

                div.appendChild(document.createTextNode('End Date: '));
                var endDateInput = new Element('input', {type : 'text', name : 'endDate', id : 'endDate', style : 'margin-left:25px'});
                div.appendChild(endDateInput);
                div.appendChild(document.createTextNode("  (" + this.dateFormat + ")"));
                div.appendChild(document.createElement('br'));

                div.appendChild(document.createTextNode('Duration: '));
                var durationInput = new Element('input', {type : 'text', name : 'duration', id : 'duration', style : 'margin-left:31px'});
                div.appendChild(durationInput);
                div.appendChild(document.createTextNode("  (e.g PT30M [30 mins])"));
                div.appendChild(document.createElement('br'));

                div.appendChild(document.createTextNode('Name: '));
                var nameInput = new Element('input', {type : 'text', name : 'name', id : 'name', style : 'margin-left:49px'});
                div.appendChild(nameInput);
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
			select.id = obj.choices.name;
			select.name = obj.choices.name;
			var defaultOpt = document.createElement('option');
			defaultOpt.value = '';
            defaultOpt.appendChild(document.createTextNode('(select ' + obj.choices.name + ')'));
			select.appendChild(defaultOpt);
			for (var i = 0; i < obj.choices.choices.length; i++) {
				var choice = obj.choices.choices[i];
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
			var params = '';
			for (var i = 0; i < this.selections.length; i++) {
		    	if (params != '') {
					params = params + '&';
				}
				params = params + this.selections[i].name + '=' + this.selections[i].value;
			}
	    	if (params != '') {
				params = params + '&';
			}
			params = params + escape(this.selectName) + '=' + escape(select.value);
			this.loadDrillDown(params);
		}
    }
};

var ProfileCategoryApiService = Class.create(ApiService, ({
        // Initialization
    initialize: function($super, params) {
        $super(params);
    },
    getActionsTableData: function($super, uid) {
        return $super("profileItem.uid", "deleteProfileItem", uid);
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
                detailRow.insert(this.getActionsTableData(profileItem.uid));

                // update array
                rows[i] = detailRow;
            }
            return rows;
        }
        rows[0] = new Element("tr").insert(new Element("td"));
        return rows;
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

        if (this.allowView) {
            actions.insert(new Element('a', {href : this.getUrl(path)})
                .insert(new Element('img', {src : '/images/icons/page_edit.png', title : 'Edit', alt : 'Edit', border : 0 })));
        }
        return actions;
    }
}));

var ProfileItemApiService = Class.create(ApiService, ({
        // Initialization
    initialize: function($super, params) {
        $super(params);
    },
    renderApiResponse: function(response) {
        var json = response.responseJSON;

        if (json.profileItem) {
            var profileItem = json.profileItem;

            // render info
            if (profileItem.name != '') {
                $('name').replace(this.getInfoElement('name','Name', profileItem.name));
            }
            $('amount').replace(this.getInfoElement('amount','Amount', profileItem.amount.value + " " + profileItem.amount.unit));
            $('startDate').replace(this.getInfoElement('startDate','StartDate', profileItem.startDate));
            $('endDate').replace(this.getInfoElement('endDate','EndDate', profileItem.endDate));
            $('fullPath').replace(this.getInfoElement('fullPath','Full Path', window.location.pathname));
            $('dataItemLabel').replace(this.getInfoElement('dataItemLabel','Data Item Label', profileItem.dataItem.Label));
            $('itemDefinition').replace(this.getInfoElement('itemDefinition','Item Definition', profileItem.itemDefinition.name));
            $('environment').replace(this.getInfoElement('environment','Environment', profileItem.environment.name));
            $('uid').replace(this.getInfoElement('uid','UID', profileItem.uid));
            $('created').replace(this.getInfoElement('created','Created', profileItem.created));
            $('modified').replace(this.getInfoElement('modified','Modified', profileItem.modified));


            // render form table info
            var tableElement = new Element('table', {id : 'inputTable'});
            tableElement.insert(this.getHeadingElement());
            tableElement.insert(this.getFormInfoElement('Name', 'name', profileItem.name, 30));
            tableElement.insert(this.getFormInfoElement('Start Date', 'startDate', profileItem.startDate, 20));
            tableElement.insert(this.getFormInfoElement('End Date', 'endDate', profileItem.endDate, 20));

            for (var i = 0; i < profileItem.itemValues.length; i++) {
                var itemValue = profileItem.itemValues[i];

                if(this.allowModify) {
                    var tableRow;

                    var newRow = new Element("tr");
                    var dataLabel = new Element("td").insert(
                            new Element('a', {href : window.location.href + "/" + itemValue.displayPath}).update(itemValue.displayName));
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

                            unitSelectElement  = new Element('select', {name : itemValue.displayPath + "Unit"});
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

                            unitSelectElement  = new Element('select', {name : itemValue.displayPath + "PerUnit"});
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

                    tableElement.insert(newRow);
                    
                } else {
                    tableElement.insert(new Element("td").update(itemValue.displayName));
                    if (itemValue.perUnit) {
                        tableElement.insert(new Element("td").update(itemValue.value + " " + itemValue.perUnit));
                    } else {
                        tableElement.insert(new Element("td").update(itemValue.value));
                    }
                }
            }

            
            $('inputTable').replace(tableElement);

            if(this.allowModify) {
                var btnSubmit = new Element('input', {type : 'button', value : 'Update'});
                $("inputSubmit").replace(btnSubmit);
                Event.observe(btnSubmit, "click", this.updateProfileItem.bind(this));
            }
        }
    },
    getFormInfoElement: function(label, name, info, size) {
        var newRow = new Element("tr").insert(new Element("td").update(label));

        var dataElement = new Element("td");
        if (this.allowModify) {
            dataElement.insert(new Element('input', {type : 'text', name : name, value : info, size : size}));
        } else {
            dataElement.insert(info);
        }
        newRow.insert(dataElement);
        return newRow;
    },
    getInfoElement: function(id, heading, info) {
        var spanElement = new Element("span", {id : id});
        spanElement.update(heading + ": " + info);
        spanElement.insert(new Element('br'));
        return spanElement;
    },
    getHeadingElement: function(json) {
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

        $('updateStatusSubmit').innerHTML='';

        var myAjax = new Ajax.Request(window.location.href + method, {
            method: 'post',
            parameters: $('inputForm').serialize(),
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.updateProfileItemSuccess.bind(this),
            onFailure: this.updateProfileItemFail.bind(this)
        });
    },
    updateProfileItemSuccess: function(response) {
        // update elements and status
        $('updateStatusSubmit').replace(new Element('div', {id : 'updateStatusSubmit'}).insert(new Element('b').update('UPDATED!')));
        this.renderApiResponse(response);
    },
    updateProfileItemFail: function(response) {
        $('updateStatusSubmit').replace(new Element('div', {id : 'updateStatusSubmit'}).insert(new Element('b').update('ERROR!')));
    }

}));
