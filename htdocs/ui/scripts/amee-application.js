var AMEEApplication = Ext.extend(Object, {
    constructor: function(config) {
        Ext.apply(this, config);
        AMEEApplication.superclass.constructor.call(this, config);
    },
    start: function() {
        this.getViewport();
        this.getDataCategoryPanel();
    },
    getDataCategoryPanel: function() {
        if (!this.dataCategoryPanel) {
            this.dataCategoryPanel = new DataCategoryPanel({
                application: this,
                listeners: {
                    dataCategoryChanged: this.onDataCategoryChanged.createDelegate(this)
                }
            });
        }
        return this.dataCategoryPanel;
    },
    getTreePanel: function() {
        if (!this.treePanel) {
            this.treePanel = new Ext.tree.TreePanel({
                region: 'west',
                collapsible: true,
                title: 'Categories',
                width: 200,
                autoScroll: true,
                split: true,
                bodyStyle: 'padding: 0px',
                margins: '0 0 0 0',
                dataUrl: '/ria/tree',
                requestMethod: 'GET',
                root: {
                    nodeType: 'async',
                    text: 'Root',
                    draggable: false,
                    id: 'root'
                },
                rootVisible: false,
                listeners: {
                    click: this.onTreeNodeClick.createDelegate(this)
                }
            });
        }
        return this.treePanel;
    },
    getMainPanel: function() {
        if (!this.mainPanel) {
            this.mainPanel = new Ext.Panel({
                title: 'Category: (please select a category to the left) ',
                collapsible: false,
                region: 'center',
                layout: 'fit',
                margins: '0 0 0 0',
                items: [this.getTabPanel()]
            });
        }
        return this.mainPanel;
    },
    getTabPanel: function() {
        if (!this.tabPanel) {
            this.tabPanel = new Ext.TabPanel({
                activeTab: 0,
                items: [{
                    title: 'Data Items',
                    html: '<div id=\'dataItemsDiv\'/>'
                }, {
                    title: 'Permissions',
                    html: 'N/A',
                    disabled: false,
                    html: '<div id=\'permissionsDiv\'></div><div id=\'userSearchDiv\'></div><div id=\'itemselector\' class=\'demo-ct\'></div>',
                    listeners: {
                        activate: this.onPermissionsTabActivate.createDelegate(this),
                        deactivate: this.onPermissionsTabDeactivate.createDelegate(this)
                    }
                }]
            });
        }
        return this.tabPanel;
    },
    getViewport: function() {
        if (!this.viewport) {
            this.viewport = new Ext.Viewport({
                renderTo: Ext.getBody(),
                layout: 'border',
                title: 'AMEE Platform',
                defaults: {
                    collapsible: true,
                    split: true,
                    bodyStyle: 'padding: 5px'
                },
                items: [
                    this.getTreePanel(),
                    this.getMainPanel()
                ]
            });
        }
        return this.viewport;
    },
    onTreeNodeClick: function(n) {
        this.getDataCategoryPanel().loadDataCategory({
            uid: n.attributes.id,
            name: n.attributes.text,
            path: n.attributes.path,
            fullPath: n.attributes.fullPath,
            itemDefinitionUid: n.attributes.itemDefinitionUid
        });
    },
    onDataCategoryChanged: function() {
        this.getMainPanel().setTitle('Category: ' + this.getDataCategoryPanel().dataCategory.name);         
    },
    onPermissionsTabActivate: function() {
        this.permissionsPanel = new PermissionsPanel({dataCategory: this.getDataCategoryPanel().dataCategory});
        this.permissionsPanel.load();
    },
    onPermissionsTabDeactivate: function() {
        if (this.permissionsPanel) {
            this.permissionsPanel.destroy();
        }
    }
});

var DataCategoryPanel = Ext.extend(Ext.util.Observable, {
    constructor: function(config) {
        this.addEvents('dataCategoryChanged');
        if (config) Ext.apply(this, config);
        DataCategoryPanel.superclass.constructor.call(this, config);
    },
    getStore: function(url) {
        return new Ext.data.JsonStore({
            autoDestroy: true,
            storeId: 'myStore',
            url: url,
            root: 'children.dataItems.rows',
            totalProperty: 'children.pager.items',
            idProperty: 'uid',
            fields: this.getStoreFields(),
            paramNames: {
                start: 'start',
                limit: 'itemsPerPage',
                sort: 'sortBy',
                dir: 'sortOrder'
            },
            remoteSort: true,
            restful: true
        });
    },
    getStoreFields: function() {
        var fields = [];
        Ext.each(this.dataCategory.itemDefinition.itemValueDefinitions, function(ivd) {
            if (ivd.fromData) {
                fields.push(ivd.path);
            }
        }.createDelegate(this));
        return fields;
    },
    getGrid: function(store) {
        return new Ext.grid.GridPanel({
            header: false,
            store: store,
            columns: this.getGridColumns(),
            stripeRows: true,
            height: 320,
            bbar: new Ext.PagingToolbar({
                pageSize: 10,
                store: store,
                displayInfo: true,
            })
        });
    },
    getGridColumns: function() {
        var column;
        var cObj = {};
        var cArr = [];
        var paths = [];
        var source;
        // Create all columns.
        Ext.each(this.dataCategory.itemDefinition.itemValueDefinitions, function(ivd) {
            if (ivd.fromData) {
                column = {};
                column['id'] = ivd.path;
                column['dataIndex'] = ivd.path;
                column['header'] = ivd.name;
                column['sortable'] = true;
                cObj[ivd.path] = column;
                paths.push(ivd.path);
            }
        }.createDelegate(this));
        // Add drill down columns first.
        Ext.each(this.dataCategory.itemDefinition.drillDown.split(','), function(path) {
            if (cObj[path]) {
                cArr.push(cObj[path]);
                paths.remove(path);
            }
        }.createDelegate(this));
        // Extract source column.
        if (cObj['source']) {
            source = cObj['source'];
            paths.remove('source');
        }
        // Add other columns.
        paths.sort();
        Ext.each(paths, function(path) {
            cArr.push(cObj[path]);
        }.createDelegate(this));
        // Add source column at end.
        if (source) {
            cArr.push(source);
        }
        return cArr;
    },
    loadDataCategory: function(obj) {
        if (this.grid) {
            this.grid.destroy();
        }
        var itemDefinition = null;
        if (obj.itemDefinitionUid) {
            itemDefinition = new ItemDefinition({
                uid: obj.itemDefinitionUid,
                listeners: {
                    load: this.onItemDefinitionLoad.createDelegate(this)
                }
            });
            itemDefinition.load();
        }
        this.dataCategory = new DataCategory({
            uid: obj.uid,
            name: obj.name,
            path: obj.path,
            fullPath: obj.fullPath,
            itemDefinition: itemDefinition
        });
        this.fireEvent('dataCategoryChanged');
    },
    onItemDefinitionLoad: function() {
        this.store = this.getStore("/data" + this.dataCategory.fullPath + ".json");
        var myMask = new Ext.LoadMask(Ext.getBody(), {msg: "Loading...", store: this.store});
        myMask.show();
        this.grid = this.getGrid(this.store);
        this.store.load({params: {start: 0, limit: 10}});
        this.grid.render('dataItemsDiv');
    }
});

var ItemDefinition = Ext.extend(Ext.util.Observable, {
    constructor: function(config) {
        this.itemValueDefinitions = [];
        this.addEvents('load');
        if (config) Ext.apply(this, config);
        ItemDefinition.superclass.constructor.call(this, config);
    },
    addItemValueDefinition: function(ivd) {
        this.itemValueDefinitions.push(ivd);
    },
    load: function() {
        Ext.Ajax.request({
            url: this.getUrl() + '.json',
            success: this.onLoadSuccess.createDelegate(this),
            failure: this.onLoadFailure.createDelegate(this)
        });
    },
    onLoadSuccess: function(response, opts) {
        Ext.apply(this, Ext.decode(response.responseText).itemDefinition);
        this.loadItemValueDefinitions();
    },
    onLoadFailure: function(response, opts) {
        console.log('server-side failure with status code ' + response.status);
    },
    loadItemValueDefinitions: function() {
        Ext.Ajax.request({
            url: this.getUrl() + '/itemValueDefinitions.json',
            success: this.onLoadItemValueDefinitionsSuccess.createDelegate(this),
            failure: this.onLoadItemValueDefinitionsFailure.createDelegate(this)
        });
    },
    onLoadItemValueDefinitionsSuccess: function(response, opts) {
        Ext.each(Ext.decode(response.responseText).itemValueDefinitions, function(ivd) {
            this.addItemValueDefinition(new ItemValueDefinition(ivd));
        }.createDelegate(this));
        this.fireEvent('load', this);
    },
    onLoadItemValueDefinitionsFailure: function(response, opts) {
        console.log('server-side failure with status code ' + response.status);
    },
    getUrl: function() {
        return '/definitions/itemDefinitions/' + this.uid;
    }
});

var ItemValueDefinition = Ext.extend(Object, {
    constructor: function(config) {
        Ext.apply(this, config);
        ItemValueDefinition.superclass.constructor.call(this, config);
    }
});

var DataCategory = Ext.extend(Object, {
    constructor: function(config) {
        Ext.apply(this, config);
        DataCategory.superclass.constructor.call(this, config);
    }
});



