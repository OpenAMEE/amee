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
            url = url + '?' + Object.toQueryString(params)
        }
        new Ajax.Request(url, {
            method: 'get',
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

                pElement.appendChild(document.createTextNode("Name: " + dataCategory.name));
                if (dataCategory.path) {
                    pElement.insert(new Element("br"));
                    pElement.appendChild(document.createTextNode("Path: " + dataCategory.path));
                }

                pElement.insert(new Element("br"));
                pElement.appendChild(document.createTextNode("Full Path: " + window.location.pathname));

                if (dataCategory.itemDefinition) {
                    pElement.insert(new Element("br"));
                    pElement.appendChild(document.createTextNode("Item Definition: " + dataCategory.itemDefinition.name));
                }

                if (json.environment || dataCategory.environment) {
                    var env;
                    if (json.environment) {
                        env = json.environment;
                    } else {
                        env = dataCategory.environment;
                    }
                    pElement.insert(new Element("br"));
                    pElement.appendChild(document.createTextNode("Environment: " + env.name));
                }

                pElement.insert(new Element("br"));
                pElement.appendChild(document.createTextNode("Data Category UID: " + dataCategory.uid));

                pElement.insert(new Element("br"));
                pElement.appendChild(document.createTextNode("Created: " + dataCategory.created));

                pElement.insert(new Element("br"));
                pElement.appendChild(document.createTextNode("Modifed: " + dataCategory.modified));

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
        if (params.deleteable && AUTHORIZATION_CONTEXT.isAllowDelete()) {
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
        this.observeDom = params.observeDom || true;
    },
    start: function() {
        if (this.observeDom) {
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
            this.notify('loaded', this);
        }
    },
    addResource: function(resource) {
        this.resources.push(resource);
    }
});
Object.Event.extend(ResourceLoader);