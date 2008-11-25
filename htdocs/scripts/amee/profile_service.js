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
    initialize: function(fullPath) {
        this.fullPath = fullPath;
		var selectName = null;
		var selections = null;
		var uid = null;
    },
  	addProfileItem: function() {
		validFrom = $('validFrom').value;
	    var myAjax = new Ajax.Request(window.location.href, {
	        method: 'post',
			parameters: 'dataItemUid=' + this.uid + '&validFrom=' + validFrom,
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
			div.appendChild(document.createTextNode('Valid From: '));
			var validFromInput = document.createElement('input');
			validFromInput.type = 'text';
			validFromInput.name = 'validFrom';
			validFromInput.id = 'validFrom';
			div.appendChild(validFromInput);
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