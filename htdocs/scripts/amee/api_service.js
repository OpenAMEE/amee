// Global Ajax Responders
var loadingCount = 0;
Ajax.Responders.register({
    onCreate: function() {
        if (loadingCount == 0) {
            $('loading').show();
        }
        loadingCount++;
    },
    onComplete: function() {
        loadingCount--;
        if (loadingCount == 0) {
            $('loading').hide();
        }
    }
});

// ------------------ pager ------------------------------
var Pager = Class.create({
    // Initialization
    initialize: function(params) {
        if (params.json) {
            var json = params.json;
            this.start = json.start;
            this.from = json.from;
            this.to = json.to;
            this.items = json.items;
            this.currentPage = json.currentPage;
            this.requestedPage = json.requestedPage;
            this.nextPage = json.nextPage;
            this.previousPage = json.previousPage;
            this.lastPage = json.lastPage;
            this.itemsPerPage = json.itemsPerPage;
            this.itemsFound = json.itemsFound;
        } else {
            this.start = 0;
            this.from = 0;
            this.to = 0;
            this.items = 0;
            this.currentPage = 1;
            this.requestedPage = 1;
            this.nextPage = -1;
            this.previousPage = -1;
            this.lastPage = 1;
            this.itemsPerPage = 1;
            this.itemsFound = 0;
        }
        this.apiService = params.apiService;
        this.pagerElementName = params.pagerElementName || 'apiPager';
    },
    // Rendering elements
    getElements: function() {
        // only show pager if there is more than one page
        if (this.lastPage > 1) {
            return this.getPagerElement();
        } else {
            // remove pager
            return new Element("div", {id : this.pagerElementName});
        }
    },
    getPagerElement: function() {
        var formElement = new Element("form", {id : this.pagerElementName});
        var pagerDiv = new Element('div').addClassName("border textDiv padding");
        var previousElement;
        var nextElement;
        var pageElement;

        if (this.currentPage !== 1) {
            previousElement = new Element('button', {id : 'previousPage', onclick : 'return false;'}).update('&lt; previous');
            previousElement.observe('click', this.doPageItemLinkClick.bindAsEventListener(this));
        } else {
            previousElement = new Element('button', {id : 'previousPage', disabled : 'disabled'}).update('&lt; previous');
        }
        pagerDiv.insert(previousElement);

        if (this.currentPage !== this.lastPage) {
            nextElement = new Element('button', {id : 'nextPage', onclick : 'return false;'}).update('next &gt;');
            nextElement.observe('click', this.doPageItemLinkClick.bindAsEventListener(this));
        } else {
            nextElement = new Element('button', {id : 'nextPage', disabled : 'disabled'}).update('next &gt;');
        }
        pagerDiv.insert(nextElement);

        // pages
        pageElement = new Element('select', {id : 'pageElement'});
        for (page = 1; (page <= this.lastPage) && this.lastPage; page++) {
            var optionElement;
            if (this.currentPage == page) {
                optionElement = new Element('option', {selected : 'selected', value : page}).update(page);
            } else {
                optionElement = new Element('option', {value : page}).update(page);
            }
            pageElement.insert(optionElement);
        }
        pagerDiv.insert(' Page ');
        pagerDiv.insert(pageElement);
        pagerDiv.insert(' of ' + this.lastPage);
        pageElement.observe('change', this.doPageItemLinkClick.bindAsEventListener(this));

        pagerDiv.insert('. Showing ' + this.from + ' to ' + (this.start + this.itemsFound) + ' of ' + this.items + '.');

        formElement.insert(pagerDiv);
        return formElement;
    },
    getPageLink: function(page, current) {
        if (current) {
            var numberElement = new Element("b");
            numberElement.update(" " + page + " ");
            return numberElement;

        } else {
            var numberLink = new Element('a', {href: '#TODO'});
            numberLink.update(" " + page + " ");
            numberLink.observe('click', this.doPageItemLinkClick.bindAsEventListener(this));
            return numberLink;
        }
    },
    doPageItemLinkClick: function(event) {
        var element = event.element();
        if (element.id == 'nextPage') {
            this.goPage(this.nextPage);
        }
        else if (element.id == 'previousPage') {
            this.goPage(this.previousPage);
        } else {
            this.goPage(event.element()[event.element().selectedIndex].value);
        }
        return false;
    },
    // Navigation
    goPage: function(page) {
        this.apiService.load("page=" + page);
    }
});

// ------------------ pager ------------------------------

var ApiService = Class.create({
    initialize: function(params) {
        params = params || {};

        // set element names
        this.heading = params.heading || "";
        this.headingElementName = params.headingElementName || "apiHeading";
        this.contentElementName = params.contentElementName || "apiContent";
        this.tAmountElementName = params.tAmountElementName || "apiTAmount";
        this.pagerTopElementName = params.pagerTopElementName || "apiTopPager";
        this.pagerBtmElementName = params.pagerBtmElementName || "apiBottomPager";

        // api data category items
        this.dataHeadingCategory = params.dataHeadingCategory || "";
        this.dataHeadingCategoryElementName = params.dataHeadingCategoryElementName || 'apiDataCategoryHeading';
        this.dataContentElementName = params.dataHeadingContentElementName || 'apiDataCategoryContent';

        // init internal
        this.apiVersion = params.apiVersion || '1.0';
        this.response = null;
        this.pagerTop = null;
        this.pagerBtm = null;
    },
    getDateFormat: function() {
        if (this.apiVersion == "1.0") {
            return "yyyyMMdd";
        } else {
            return "yyyy-MM-dd'T'HH:mm:ssZ";
        }
    },
    start: function() {
        this.load();
    },
    load: function(params) {
        this.response = null;
        var url = window.location.href;
        if (params) {
            params = params.toQueryParams();
        } else {
            params = new Hash();
        }
        params.set('method', 'get');
        new Ajax.Request(url + '?' + Object.toQueryString(params), {
            method: 'post',
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.loadSuccess.bind(this),
            onFailure: this.loadFailure.bind(this)});
    },
    loadSuccess: function(response) {
        this.response = response;
        this.render();
    },
    loadFailure: function() {
    },
    render: function() {
        this.renderTrail();
        this.renderDataCategoryApiResponse();
        this.renderApiResponse();
    },
    renderTrail : function() {

        var json = this.response.responseJSON;
        var rootPath = this.getTrailRootPath();
        var otherPaths = this.getTrailOtherPaths(json);
        var linkPath = '';
        var apiTrailElement = $('apiTrail');

        if (apiTrailElement) {
            // reset
            apiTrailElement.update('');

            // root path
            if (rootPath != '') {
                apiTrailElement.insert(new Element('a', {href : '/' + this.getUrlWithSearch(rootPath)}).update(rootPath));
                linkPath = "/" + rootPath;
            }

            // other path
            if (otherPaths && otherPaths.length > 0) {
                for (var i = 0; i < otherPaths.length; i++) {
                    var otherPath = otherPaths[i];
                    linkPath = linkPath + "/" + otherPath;
                    apiTrailElement.insert(" / ");
                    apiTrailElement.insert(new Element('a', {href : this.getUrlWithSearch(linkPath)}).update(otherPath));
                }

            }
        }

        if (json.path && apiTrailElement) {
            var pathItems = json.path.split("/");

            // path items
            for (var i = 0; i < pathItems.length; i++) {
                var pathItem = pathItems[i];
                if (pathItem == "") {
                    continue;
                }
                linkPath = linkPath + "/" + pathItem;
                apiTrailElement.insert(" / ");
                apiTrailElement.insert(new Element('a', {href : this.getUrlWithSearch(linkPath)}).update(pathItem));
            }
        }
    },
    getUrlWithSearch: function(path) {
        return path + window.location.search;
    },
    getTrailRootPath: function() {
        return '';
    },
    getTrailOtherPaths: function(json) {
        return [];
    },
    renderApiResponse: function(pagerJSON) {

        var json = this.response.responseJSON;

        // update elements
        this.headingElement = $(this.headingElementName);
        this.contentElement = $(this.contentElementName);
        this.pagerTopElement = $(this.pagerTopElementName);
        this.pagerBtmElement = $(this.pagerBtmElementName);

        // set section heading
        this.headingElement.innerHTML = this.heading;

        // create table headings
        var tableBody = new Element('tbody').insert(this.getTableHeadingElement(json));

        // create table details
        var detailRows = this.getDetailRows(json);
        for (var i = 0; i < detailRows.length; i++) {
            tableBody.insert(detailRows[i]);
        }

        // replace table
        var tableElement = new Element('table', {id : this.contentElementName}).insert(tableBody);
        this.contentElement.replace(tableElement);

        // replace pager(s)
        if (!pagerJSON) {
            pagerJSON = json.pager;
        }

        if (this.pagerTopElement) {
            this.pagerTop = new Pager({json : pagerJSON, apiService : this, pagerElementName : this.pagerTopElementName});
            this.pagerTopElement.replace(this.pagerTop.getElements());
        }

        if (this.pagerTopElement) {
            this.pagerBtm = new Pager({json : pagerJSON, apiService : this, pagerElementName : this.pagerBtmElementName});
            this.pagerBtmElement.replace(this.pagerBtm.getElements());
        }
    },
    renderDataCategoryApiResponse: function() {

        var json = this.response.responseJSON;

        // data category information and drill down
        if (json.dataCategory) {

            var dataCategory = json.dataCategory;

            // update elements
            this.dataHeadingCategoryElement = $(this.dataHeadingCategoryElementName);
            this.dataContentElement = $(this.dataContentElementName);

            // set section heading
            if (this.dataHeadingCategoryElement) {
                this.dataHeadingCategoryElement.innerHTML = this.dataHeadingCategory;
            }

            if (this.dataContentElement) {
                // set section details
                var pElement = new Element('p', {id : this.dataContentElementName});

                pElement.insert("Name: " + dataCategory.name);
                if (dataCategory.path) {
                    pElement.insert(new Element("br"));
                    pElement.insert("Path: " + dataCategory.path);
                }

                pElement.insert(new Element("br"));
                pElement.insert("Full Path: " + window.location.pathname);

                if (dataCategory.itemDefinition) {
                    pElement.insert(new Element("br"));
                    pElement.insert("Item Definition: " + dataCategory.itemDefinition.name);
                }

                if (json.environment || dataCategory.environment) {
                    var env;
                    if (json.environment) {
                        env = json.environment;
                    } else {
                        env = dataCategory.environment;
                    }
                    pElement.insert(new Element("br"));
                    pElement.insert("Environment: " + env.name);
                }

                pElement.insert(new Element("br"));
                pElement.insert("Data Category UID: " + dataCategory.uid);

                pElement.insert(new Element("br"));
                pElement.insert("Created: " + dataCategory.created);

                pElement.insert(new Element("br"));
                pElement.insert("Modifed: " + dataCategory.modified);

                this.dataContentElement.replace(pElement);
            }
        }
    },
    getTableHeadingElement: function(json) {
        // implementation required
        return "";
    },
    getHeadingData: function(heading) {
        return new Element('th').insert(heading);
    },
    getUnit: function(json) {
        if (json.totalAmount) {
            return json.totalAmount.unit;
        }
        return "Unknown Unit";
    },
    getDetailRows: function(json) {
        var rows = [];
        rows[0] = new Element("tr").insert(new Element("td"));
        return rows;
    },
    getActionsTableData: function(params) {
        params.deleteable = params.deleteable || false;
        var actions = new Element('td');
        var path = params.path || params.uid;
        actions.insert(new Element('a', {href : this.getUrl(path)})
                .insert(new Element('img', {src : '/images/icons/page_edit.png', title : 'Edit', alt : 'Edit', border : 0 })));
        if (AUTHORIZATION_CONTEXT.isAllowDelete() && params.deleteable) {
            var methodParams = '"' + params.uid + '", "' + this.getUrl(params.uid) + '"';
            var link = new Element('a', {
                onClick: params.method + '(' + methodParams + ') ; return false;',
                href: 'javascript:' + params.method + '(' + methodParams + ');'});
            var image = new Element('img', {
                src: '/images/icons/page_delete.png',
                title: 'Delete',
                alt: 'Delete',
                border: 0});
            link.insert(image);
            actions.insert(link);
        }
        return actions;
    },
    getUrl: function(params) {
        var url = window.location.href;
        if (params) {
            url = url.substring(0, (url.length - window.location.search.length));
            if (!url.endsWith("/")) {
                url = url + "/";
            }
            return url + params + window.location.search;
        } else {
            return url;
        }
    }
});

// Authorization Context
var AuthorizationContext = Class.create({
    initialize: function(params) {
        this.entries = params.entries || [];
    },
    isOwn: function() {
        return this.hasAllowEntryForValue('o');
    },
    isAllowView: function() {
        return this.isOwn() || this.hasAllowEntryForValue('v');
    },
    isAllowCreate: function() {
        return this.isOwn() || this.hasAllowEntryForValue('c');
    },
    isAllowCreateProfile: function() {
        return this.hasAllowEntryForValue('c.pr');
    },
    isAllowModify: function() {
        return this.isOwn() || this.hasAllowEntryForValue('m');
    },
    isAllowDelete: function() {
        return this.isOwn() || this.hasAllowEntryForValue('d');
    },
    hasAllowEntryForValue: function(value) {
        var result = false;
        this.entries.each(function(entry) {
            if ((entry.value === value) && entry.allow) {
                result = true;
            }
        });
        return result;
    }
});

// Mock DOM Resource
var MockDomResource = Class.create({
    initialize: function() {
    },
    start: function() {
        this.load();
    },
    load: function() {
        document.observe('dom:loaded', this.loadSuccess.bind(this));
    },
    loadSuccess: function() {
        this.loaded = true;
        this.available = false;
        this.notify('loaded', this);
    }
});
Object.Event.extend(MockDomResource);

// Resource Loader
var ResourceLoader = Class.create({
    initialize: function(params) {
        params = params || {};
        this.resources = [];
        this.ignoreDomLoaded = params.ignoreDomLoaded || false;
    },
    start: function() {
        if (!this.ignoreDomLoaded) {
            this.addResource(new MockDomResource());
        }
        this.resources.each(function(resource) {
            if (resource.start) {
                resource.observe('loaded', this.onLoaded.bind(this));
                resource.start();
            }
        }.bind(this));
    },
    onLoaded: function(resource) {
        this.checkLoadStatus();
    },
    checkLoadStatus: function() {
        var loaded = true;
        this.resources.each(function(resource) {
            if (!resource.loaded) {
                loaded = false;
            }
        });
        if (loaded) {
            Log.debug('ResourceLoader.checkLoadStatus() Loaded!');
            this.notify('loaded', this);
        }
    },
    addResource: function(resource) {
        this.resources.push(resource);
    }
});
Object.Event.extend(ResourceLoader);

// CollectionItem
var CollectionItem = Class.create({
    initialize: function(item) {
        Object.extend(this, item);
    }
});

// CollectionResource
var CollectionResource = Class.create({
    initialize: function(params) {
        this.items = [];
        this.path = params.path;
        this.node = params.node;
    },
    start: function() {
        this.load();
    },
    load: function() {
        Log.debug('CollectionResource.load()');
        this.items = [];
        var params = this.getLoadParams();
        params.set('method', 'get');
        new Ajax.Request(this.path + '?' + Object.toQueryString(params), {
            method: 'post',
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.loadSuccess.bind(this),
            onFailure: this.loadFailure.bind(this)});
    },
    getLoadParams: function() {
        return new Hash();
    },
    loadSuccess: function(response) {
        Log.debug('CollectionResource.loadSuccess()');
        var resource = response.responseJSON;
        resource[this.node].each(function(item) {
            this.items.push(this.getItem(item));
        }.bind(this));
        this.loaded = true;
        this.available = true;
        this.notify('loaded', this);
    },
    loadFailure: function() {
        Log.warn('CollectionResource.loadFailure()');
        this.loaded = true;
        this.available = false;
        this.notify('loaded', this);
    },
    getItems: function() {
        return this.items;
    },
    getItem: function(item) {
        return new CollectionItem(item);
    },
    create: function(params) {
        new Ajax.Request(this.path + '?' + Object.toQueryString(this.getLoadParams()), {
            method: 'post',
            parameters: params,
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.createSuccess.bind(this),
            onFailure: this.createFailure.bind(this)});
    },
    createSuccess: function(response) {
        this.notify('created', this);
    },
    createFailure: function(response) {
    }
});

// PermissionsResource
var PermissionsResource = Class.create(CollectionResource, {
    initialize: function($super, params) {
        params = params || {};
        params.path = '/permissions';
        params.node = 'permissions';
        this.entityType = params.entityType || '';
        this.entityUid = params.entityUid || '';
        $super(params);
    },
    getLoadParams: function($super) {
        var params = $super();
        params.set('entityType', this.entityType);
        params.set('entityUid', this.entityUid);
        return params;
    }
});
Object.Event.extend(PermissionsResource);

// GroupsResource
var GroupsResource = Class.create(CollectionResource, {
    initialize: function($super, params) {
        params = params || {};
        params.path = '/groups';
        params.node = 'groups';
        $super(params);
    }
});
Object.Event.extend(GroupsResource);

// Permissions Editor
var PermissionsEditor = Class.create({
    initialize: function() {
        this.entityUid = null;
        this.entityType = null;
        this.container = null;
        this.content = null;
        this.groupsTab = null;
        this.usersTab = null;
        this.tabs = null;
        this.control = null;
        this.permissionsResource = null;
        this.groupsResource = null;
        this.groupPermissionsForm = null;
        this.usersResource = null;
    },
    open: function(params) {
        this.entityUid = params.entityUid || '';
        this.entityType = params.entityType || '';
        this.permissionsResource = null;
        this.render();
        this.control.open();
    },
    render: function() {
        this.renderContent();
        this.renderContainer();
        this.renderModal();
    },
    renderContent: function() {
        if (!this.content) {
            // Content Box
            this.content = new Element('div').addClassName("permissionsContent");
            // Tab Bar
            this.tabs = new Element('ul').addClassName('tabs');
            this.tabs.insert(new Element('li').addClassName('tab').insert(new Element('a', {href: '#groups'}).insert('Groups')));
            this.tabs.insert(new Element('li').addClassName('tab').insert(new Element('a', {href: '#users'}).insert('Users')));
            this.content.insert(this.tabs);
            // Users & Groups Tabs
            this.groupsTab = new Element('div', {id: 'groups'});
            this.usersTab = new Element('div', {id: 'users'});
            this.content.insert(this.groupsTab);
            this.content.insert(this.usersTab);
        }
    },
    renderContainer: function() {
        if (!this.container) {
            this.container = new Element('div').addClassName("permissionsModalHead clearfix");
            // Outer Box
            var outer = new Element('div').addClassName("permissionsOuterDiv")
                    .insert(new Element('h2').update("Permissions Editor"));
            // Inner Box
            var inner = new Element('div').addClassName("permissionsInnerDiv clearfix");
            inner.insert(this.content);
            outer.insert(inner);
            // Buttons Box
            var buttonsOuter = new Element('div').addClassName("permissionsButtonsOuter clearfix");
            var buttonsInner = new Element('div');
            var doneButton = new Element('button').update("Done");
            doneButton.observe("click", this.onDone.bindAsEventListener(this));
            buttonsInner.insert(doneButton);
            buttonsOuter.insert(buttonsInner);
            outer.insert(buttonsOuter);
            this.container.insert(outer);
        }
    },
    renderModal: function() {
        if (!this.control) {
            this.control = new Control.Modal(false, {
                width: 420,
                height: 120,
                afterOpen: this.afterOpen.bind(this)});
            this.control.container.insert(this.container);
            new Control.Tabs(this.tabs);
        }
    },
    afterOpen: function() {
        Log.debug('PermissionsEditor.afterOpen()');
        // Use ResourceLoader to observe loading of required Resources.
        var resourceLoader = new ResourceLoader({ignoreDomLoaded: true});
        resourceLoader.observe('loaded', this.loaded.bind(this));
        // Add PermissionsResource.
        if (!this.permissionsResource) {
            this.permissionsResource = new PermissionsResource({entityType: this.entityType, entityUid: this.entityUid});
            resourceLoader.addResource(this.permissionsResource);
        }
        // Add GroupsResource.
        if (!this.groupsResource) {
            this.groupsResource = new GroupsResource();
            resourceLoader.addResource(this.groupsResource);
        }
        // Add UsersResource.
        //if (!this.usersResource) {
        //    this.usersResource = new CollectionResource({path: '/users'});
        //    this.usersResource.observe('loaded', this.renderUsers.bind(this));
        //    resourceLoader.addResource(this.usersResource);
        // }
        // Load everything!
        resourceLoader.start();
    },
    loaded: function() {
        Log.debug('PermissionsEditor.loaded()');
        this.renderPermissions();
        this.renderGroups();
    },
    renderPermissions: function() {
        Log.debug('PermissionsEditor.renderPermissions()');
        // Populate permissions list.
        this.permissionsResource.getItems().each(function(permission) {
            
        }.bind(this));
    },
    renderGroups: function() {
        Log.debug('PermissionsEditor.renderGroups()');
        // Blank out groups tab.
        this.groupsTab.update();
        // Create permissions form.
        if (!this.groupPermissionsForm) {
            var groupPermissionsForm = new PermissionsForm({
                permissionsResource: this.permissionsResource,
                entityUid: this.entityUid,
                entityType: this.entityType,
                principalType: 'GRP',
                container: this.groupsTab});
            groupPermissionsForm.render();
        }
        groupPermissionsForm.reset();
        // Populate permissions form.
        this.groupsResource.getItems().each(function(group) {
            group.label = group.name;
            groupPermissionsForm.addPrincipal(group);
        }.bind(this));
    },
    renderUsers: function() {
    },
    onDone: function(event) {
        event.stop();
        this.control.close();
        return false;
    }
});
Object.Event.extend(PermissionsEditor);

// PermissionsForm
var PermissionsForm = Class.create({
    initialize: function(params) {
        this.permissionsResource = params.permissionsResource;
        this.entityUid = params.entityUid;
        this.entityType = params.entityType;
        this.principalType = params.principalType;
        this.container = params.container;
        this.form = null;
        this.principalSelect = null;
        this.entries = new Array(
        {code: 'o', label: 'Own', odd: true},
        {code: 'v', label: 'View', odd: false},
        {code: 'c', label: 'Create', odd: true},
        {code: 'm', label: 'Modify', odd: false},
        {code: 'd', label: 'Delete', odd: true});
    },
    reset: function() {
        if (this.principalSelect) {
            this.principalSelect.update();
        }
    },
    render: function() {

        Log.debug('PermissionsForm.render()');

        // The form.
        this.form = new Element('form', {action: '/permissions'});
        Event.observe(this.form, "submit", this.onCreatePermission.bind(this));

        // Some hiddens.
        this.form.insert(new Element('input', {type: 'hidden', name: 'entityUid', value: this.entityUid}));
        this.form.insert(new Element('input', {type: 'hidden', name: 'entityType', value: this.entityType}));
        this.form.insert(new Element('input', {type: 'hidden', name: 'principalType', value: this.principalType}));

        // Left & right side.
        var left = new Element('div').addClassName('permissionsFormLeft');
        var right = new Element('div').addClassName('permissionsFormRight');
        this.form.insert(left);
        this.form.insert(right);

        // Principal search.
        this.principalSearch = new Element('input', {type: 'text', size: 10});
        left.insert(this.principalSearch);
        left.insert('&nbsp;');
        var searchButton = new Element('input', {type: 'button', value: 'Search'});
        Event.observe(searchButton, "click", this.onPrincipalSearch.bind(this));
        left.insert(searchButton);
        left.insert(new Element('br'));
        left.insert(new Element('br'));

        // Principal select.
        this.principalSelect = new Element('select', {
            name: 'principalUid',
            multiple: 'multiple',
            size: 5});
        left.insert(this.principalSelect);

        // Allow permission entries.
        var allowBox = new Element('div');
        allowBox.insert('Allow: ');
        this.renderEntriesSelector(allowBox, 'selectAllowEntries');
        right.insert(allowBox);
        right.insert(new Element('br'));

        // Deny permission entries.
        var denyBox = new Element('div');
        denyBox.insert('Deny: ');
        this.renderEntriesSelector(denyBox, 'selectDenyEntries');
        right.insert(denyBox);
        right.insert(new Element('br'));

        // Create button.
        var create = new Element('input', {type: 'button', value: 'Create'});
        Event.observe(create, "click", this.onCreatePermission.bind(this));
        right.insert(create);

        // Add form to container.
        this.container.insert(this.form);
    },
    renderEntriesSelector: function(parent, id) {

        Log.debug('PermissionsForm.renderEntriesSelector()');

        // Entries selection.
        var entrySelect = this.getEntriesSelect(id);
        var moreEntriesContainer = this.getMoreEntriesContainer('allowEntriesMore');
        var selectMultiple = new Control.SelectMultiple(entrySelect, moreEntriesContainer, {
            checkboxSelector: 'table.select_multiple_table tr td input[type=checkbox]',
            nameSelector: 'table.select_multiple_table tr td.select_multiple_name',
            afterChange: function() {
                if (selectMultiple && selectMultiple.setSelectedRows)
                    selectMultiple.setSelectedRows();
            }
        });

        // Activate check boxes.
        selectMultiple.setSelectedRows = function() {
            this.checkboxes.each(function(checkbox) {
                var tr = $(checkbox.parentNode.parentNode);
                tr.removeClassName('selected');
                if (checkbox.checked)
                    tr.addClassName('selected');
            });
        }.bind(selectMultiple);
        selectMultiple.checkboxes.each(function(checkbox) {
            $(checkbox).observe('click', selectMultiple.setSelectedRows);
        });
        selectMultiple.setSelectedRows();

        // More link.
        var moreLink = new Element('a', {href: '', id: 'allowEntriesMoreOpen'}).update('More');
        moreLink.observe('click', function(event) {
            $(this.select).style.visibility = 'hidden';
            new Effect.BlindDown(this.container, {
                duration: 0.3
            });
            Event.stop(event);
            return false;
        }.bindAsEventListener(selectMultiple));

        // Active close button.
        moreEntriesContainer.closeButton.observe('click', function(event) {
            $(this.select).style.visibility = 'visible';
            new Effect.BlindUp(this.container, {
                duration: 0.3
            });
            Event.stop(event);
            return false;
        }.bindAsEventListener(selectMultiple));

        // Create and populate container.
        var container = new Element('div').addClassName('permission_entries_select_container');
        container.insert(entrySelect);
        container.insert('&nbsp;');
        container.insert(moreLink);
        container.insert(moreEntriesContainer);

        // Add container to parent.
        parent.insert(container);
    },
    getEntriesSelect: function(id) {
        Log.debug('PermissionsForm.getEntriesSelect()');
        var e = new Element('select', {id: id});
        e.insert(new Element('option', {value: ''}).update('(None)'));
        this.entries.each(function(entry) {
            e.insert(this.getEntryOption(entry));
        }.bind(this));
        return e;
    },
    getEntryOption: function(entry) {
        return new Element('option', {value: entry.code}).update(entry.label);
    },
    getMoreEntriesContainer: function(id) {

        Log.debug('PermissionsForm.getMoreEntriesContainer()');

        // Table body.
        var body = new Element('tbody');
        this.entries.each(function(entry) {
            body.insert(this.getMoreEntryRow(entry));
        }.bind(this));

        // Table.
        var table = new Element('table', {cellspacing: 0, cellpadding: 0, width: '100%'}).addClassName('select_multiple_table');
        table.insert(body);

        // Container, title, button.
        var e = new Element('div', {id: id, style: 'display:none;'}).addClassName('select_multiple_container');
        e.insert(new Element('div').addClassName('select_multiple_header').update('Select Multiple Entries'));
        e.insert(table);
        var closeButton = new Element('input', {type: 'button', value: 'Done'});
        e.insert(new Element('div').addClassName('select_multiple_submit').insert(closeButton));
        e.closeButton = closeButton;

        return e;
    },
    getMoreEntryRow: function(entry) {
        Log.debug('PermissionsForm.getMoreEntryRow()');
        var row = new Element('tr').addClassName(entry.odd ? 'odd' : 'even');
        row.insert(new Element('td').addClassName('select_multiple_name').update(entry.label));
        row.insert(new Element('td').addClassName('select_multiple_checkbox')
                .update(new Element('input', {type: 'checkbox', value: entry.code})));
        return row;
    },
    addPrincipal: function(principal) {
        if (principal.uid && principal.label) {
            this.principalSelect.insert(new Element('option', {value: principal.uid}).update(principal.label));
        }
    },
    onPrincipalSearch: function(event) {
        event.stop();
        alert('Search!');
        return false;
    },
    onCreatePermission: function(event) {
        Log.debug('PermissionsForm.onCreatePermission()');
        event.stop();
        var params = new Hash();
        params.set('allowEntries', $('selectAllowEntries').value);
        params.set('denyEntries', $('selectDenyEntries').value);
        params.set('principalUid', this.principalSelect.value);
        params.set('principalType', this.principalType);
        this.permissionsResource.create(params);
        Log.debug('PermissionsForm.onCreatePermission() done');
        return false;
    }
});