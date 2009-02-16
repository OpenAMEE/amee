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
var Pager = Class.create();
Pager.prototype = {
    // Initialization
    initialize: function(params) {
        if (params.json) {
            json = params.json;
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
        var numberLink;
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
        this.apiService.apiRequest("page=" + page)
    }
};

// ------------------ pager ------------------------------

var ApiService = Class.create();
ApiService.prototype = {
    initialize: function(params) {

        if (!params) {
            params = {};
        }

        // api items
        this.heading = params.heading || "";
        this.headingElementName = params.headingElementName || "apiHeading";
        this.contentElementName = params.contentElementName || "apiContent";
        this.tAmountElementName = params.tAmountElementName || "apiTAmount";
        this.pagerTopElementName = params.pagerTopElementName || "apiTopPager";
        this.pagerBtmElementName = params.pagerBtmElementName || "apiBottomPager";

        this.apiVersion = params.apiVersion || '1.0';
        this.drillDown = params.drillDown || false;

        // api data category items
        this.dataHeadingCategory = params.dataHeadingCategory || "";
        this.dataHeadingCategoryElementName = params.dataHeadingCategoryElementName || 'apiDataCategoryHeading';
        this.dataContentElementName = params.dataHeadingContentElementName || 'apiDataCategoryContent';

        // permissions
        this.allowList = params.allowList || false;
        this.allowView = params.allowView || false;
        this.allowDelete = params.allowDelete || false;
        this.allowModify = params.allowModify || false;
        this.allowCreate = params.allowCreate || false;

        this.pager = null;
    },
    getDateFormat: function() {
        if (this.apiVersion == "1.0") {
            return "yyyyMMdd";
        } else {
            return "yyyy-MM-dd'T'HH:mmZ";
        }
    },
    apiRequest: function(params) {
        params = params || "";
        params = params.toQueryParams();
        params['method'] = 'get';
        new Ajax.Request(window.location.href + '?' + Object.toQueryString(params), {
            method: 'post',
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.processApiResponse.bind(this)
        });
    },
    updatePermissions: function(response) {
        var actions = response.responseJSON.actions;
        if (actions) {
            this.allowList = actions.allowList;
            this.allowView = actions.allowView;
            this.allowDelete = actions.allowDelete;
            this.allowModify = actions.allowModify;
            this.allowCreate = actions.allowCreate;
        }
    },
    processApiResponse: function(response) {
        this.renderTrail(response);
        this.updatePermissions(response);
        this.renderDataCategoryApiResponse(response);
        this.renderApiResponse(response);
    },
    renderTrail : function(response) {
        var json = response.responseJSON;
        var rootPath = this.getTrailRootPath();
        var otherPaths = this.getTrailOtherPaths(json);
        var linkPath = '';
        var apiTrailElement = $('apiTrail');

        if (apiTrailElement) {
            // reset
            apiTrailElement.update('');

            // root path
            if (rootPath != '') {
                apiTrailElement.insert(new Element('a', {href : '/' + this.getUrlWithSearch(rootPath)}).update(this.titleCase(rootPath)));
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
                apiTrailElement.insert(new Element('a', {href : this.getUrlWithSearch(linkPath)}).update(this.titleCase(pathItem)));
            }
        }
    },
    getUrlWithSearch: function(path) {
        return path + window.location.search;
    },
    titleCase: function(inStr) {
        var outStr = inStr.substr(0, 1).toUpperCase();
        outStr = outStr + inStr.substr(1, inStr.length - 1);
        return outStr;
    },
    getTrailRootPath: function() {
        return '';
    },
    getTrailOtherPaths: function(json) {
        return [];
    },
    renderApiResponse: function(response, pagerJSON) {
        var json = response.responseJSON;

        // update elements
        this.headingElement = $(this.headingElementName);
        this.contentElement = $(this.contentElementName);
        this.pagerTopElement = $(this.pagerTopElementName);
        this.pagerBtmElement = $(this.pagerBtmElementName);

        // set section heading
        this.headingElement.innerHTML = this.heading;

        // create table headings
        var tableElement = new Element('table', {id : this.contentElementName}).insert(this.getHeadingElement(json));

        // create table details
        var detailRows = this.getDetailRows(json);
        for (var i = 0; i < detailRows.length; i++) {
            tableElement.insert(detailRows[i]);
        }

        // replace table
        this.contentElement.replace(tableElement);

        // replace pager(s)
        if (!pagerJSON) {
            pagerJSON = response.responseJSON.pager;
        }
        if (this.pagerTopElement) {
            this.pager = new Pager({json : pagerJSON, apiService : this, pagerElementName : this.pagerTopElementName});
            this.pagerTopElement.replace(this.getPagerElements());
        }

        if (this.pagerTopElement) {
            this.pager = new Pager({json : pagerJSON, apiService : this, pagerElementName : this.pagerBtmElementName});
            this.pagerBtmElement.replace(this.getPagerElements());
        }
    },
    renderDataCategoryApiResponse: function(response) {

        var json = response.responseJSON;

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

            if (this.drillDown && json.path) {
                new DrillDown(
                        "/data" + json.path,
                        this.apiVersion,
                        this.getDateFormat(),
                        this.getActionsAllowCreate()
                        ).loadDrillDown();
            }
        }
    },
    getPagerElements: function() {
        return this.pager.getElements();
    },
    getHeadingElement: function(json) {
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
    getActionsTableData: function(urlKey, dMethod, uid, optViewPath) {
        var actions = new Element('td');

        if (this.getActionsAllowView()) {
            var eUrl = optViewPath || uid;

            actions.insert(new Element('a', {href : this.getUrl(eUrl)})
                    .insert(new Element('img', {src : '/images/icons/page_edit.png', title : 'Edit', alt : 'Edit', border : 0 })));
        }

        if (this.getActionsAllowDelete()) {
            var dUrl = "'" + urlKey + "','" + this.getUrl(uid) + "'";
            actions.insert(new Element('input',
            {
                onClick : dMethod + '(' + dUrl + ') ; return false;',
                type : 'image',
                src : '/images/icons/page_delete.png',
                title : 'Delete', alt : 'Delete', border : 0}));
        }
        return actions;
    },
    getActionsAllowView: function() {
        return this.allowView;
    },
    getActionsAllowCreate : function() {
        return this.allowCreate;
    },
    getActionsAllowDelete: function() {
        return this.allowDelete;
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
};