var Permission = Ext.extend(Object, {
    constructor: function(config) {
        Ext.apply(this, config);
        Permission.superclass.constructor.call(this, config);
    }
});

var PermissionsPanel = Ext.extend(Ext.util.Observable, {
    constructor: function(config) {
        if (config) Ext.apply(this, config);
        PermissionsPanel.superclass.constructor.call(this, config);
    },
    load: function() {
        this.loadMask = new Ext.LoadMask(Ext.getBody(), {msg: "Loading..."});
        this.loadMask.show();
        this.getStore().load();
    },
    onStoreLoad: function() {
        if (this.getStore().getCount() > 0) {
            var uids = '';
            this.getStore().each(function(rec) {
                uids += rec.data.principal.uid + ',';
            }, this);
            this.getUsersStore(uids).load({params: {search:  uids}});
        } else {
            this.loadMask.hide();
        }
    },
    getUsersStore: function(search) {
        if (!this.usersStore) {
            this.usersStore = new Ext.data.JsonStore({
                autoDestroy: true,
                storeId: 'usersStore',
                url: '/users.json',
                root: 'users',
                totalProperty: 'pager.items',
                idProperty: 'uid',
                fields: ['uid', 'type', 'apiVersion', 'locale', 'username', 'name'],
                paramNames: {
                    start: 'start',
                    limit: 'itemsPerPage',
                    sort: 'sortBy',
                    dir: 'sortOrder'
                },
                remoteSort: true,
                restful: true,
                listeners: {
                    load: this.onUsersStoreLoad.createDelegate(this)
                }
            });
        }
        return this.usersStore;
    },
    onUsersStoreLoad: function() {
        this.getGrid().render('permissionsDiv');
        this.getUserSearchPanel();
        this.getForm();
        this.loadMask.hide();
    },
    getStore: function() {
        if (!this.store) {
            this.store = new Ext.data.JsonStore({
                autoDestroy: true,
                storeId: 'permissionsStore',
                url: '/permissions.json?entityUid=' + this.dataCategory.uid + '&entityType=DC',
                root: 'permissions',
                idProperty: 'uid',
                fields: this.getStoreFields(),
                restful: true,
                listeners: {
                    load: this.onStoreLoad.createDelegate(this)
                }
            });
        }
        return this.store;
    },
    getStoreFields: function() {
        var fields = [];
        fields.push('uid');
        fields.push('principal');
        fields.push('entries');
        fields.push('created');
        fields.push('modified');
        return fields;
    },
    getGrid: function(store) {
        if (!this.grid) {
            this.grid = new Ext.grid.GridPanel({
                header: false,
                store: this.getStore(),
                columns: this.getGridColumns(),
                stripeRows: true,
                height: 100
            });
        }
        return this.grid;
    },
    getGridColumns: function() {
        var columns = [];
        // Principal.
        columns.push({
            id: 'principal',
            dataIndex: 'principal',
            header: 'Principal',
            sortable: true,
            renderer: this.renderPrincipal.createDelegate(this)
        });
        // Entries.
        columns.push({
            id: 'entries',
            dataIndex: 'entries',
            header: 'Entries',
            sortable: false,
            renderer: this.renderEntries.createDelegate(this)
        });
        return columns;
    },
    getUserSearchPanel: function() {
        if (!this.userSearchPanel) {
            this.userSearchPanel = new Ext.grid.GridPanel({
                applyTo: 'userSearchDiv',
                title: 'User Search',
                height: 320,
                width: 450,
                stripeRows: true,
                autoScroll: true,
                store: this.getUserSearchStore(),
                columns: [
                    {
                        id: 'username',
                        dataIndex: 'username',
                        header: 'Username',
                        sortable: true,
                        width: 200,
                    }, {
                        id: 'name',
                        dataIndex: 'name',
                        header: 'Name',
                        sortable: true,
                        width: 200,
                    }
                ],
                tbar: [
                    'Search: ', ' ',
                    new Ext.ux.form.SearchField({
                        store: this.getUserSearchStore(),
                        width: 320,
                        paramName: 'search'
                    })
                ],
                bbar: new Ext.PagingToolbar({
                    store: this.getUserSearchStore(),
                    pageSize: 10,
                    displayInfo: true,
                    displayMsg: 'Users {0} - {1} of {2}',
                    emptyMsg: "No users to display"
                })
            });
        }
        return this.userSearchPanel;
    },
    getUserSearchStore: function() {
        if (!this.userSearchStore) {
            this.userSearchStore = new Ext.data.JsonStore({
                autoDestroy: true,
                storeId: 'userSearchStore',
                url: '/users.json',
                root: 'users',
                totalProperty: 'pager.items',
                idProperty: 'uid',
                fields: ['uid', 'type', 'apiVersion', 'locale', 'username', 'name'],
                paramNames: {
                    start: 'start',
                    limit: 'itemsPerPage',
                    sort: 'sortBy',
                    dir: 'sortOrder'
                },
                remoteSort: true,
                restful: true,
                listeners: {
                    load: this.onUsersStoreLoad.createDelegate(this)
                }
            });
            this.userSearchStore.load();
        }
        return this.userSearchStore;
    },
    getForm: function() {
        if (!this.form) {


           var ds = new Ext.data.ArrayStore({
                data: [
                    ['o','+OWN'],
                    ['o','-OWN'],
                    ['v', '+VIEW'],
                    ['v', '-VIEW'],
                    ['c', '+CREATE'],
                    ['c', '-CREATE'],
                    ['m', '+MODIFY'],
                    ['m', '-MODIFY'],
                    ['d', '+DELETE'],
                    ['d', '-DELETE'],
                ],
                fields: ['value','text'],
                sortInfo: {
                    field: 'value',
                    direction: 'ASC'
                }
            });

            this.form = new Ext.form.FormPanel({
                title: 'Permission Entries',
                width: 245,
                bodyStyle: 'padding:10px;',
                renderTo: 'itemselector',
                items:[{
                    xtype: 'itemselector',
                    name: 'itemselector',
                    hideLabel: true,
                    imagePath: '/scripts/ext/examples/ux/images/',
                    drawUpIcon: false,
                    drawDownIcon: false,
                    drawTopIcon: false,
                    drawBotIcon: false,
                    multiselects: [{
                        width: 100,
                        height: 220,
                        store: ds,
                        displayField: 'text',
                        valueField: 'value'
                    }, {
                        width: 100,
                        height: 220,
                        store: []
                    }]
                }],
                buttons: [{
                    text: 'Create',
                    handler: function(){
                        if (isForm.getForm().isValid()){
                            Ext.Msg.alert(
                                'Submitted Values',
                                'The following will be sent to the server: <br />' +
                                    isForm.getForm().getValues(true));
                        }
                    }
                }]
            });
        }
        return this.form
    },
    renderEntries: function(entries) {
        var i;
        var entry;
        var value = '';
        for (i = 0; i < entries.length; i++) {
            entry = entries[i];
            // Prefix with '+' or '-' depending on deny state.
            if (entry.allow) {
                value += '+';
            } else {
                value += '-';
            }
            // Turn PermissionEntry value into a nice word.
            switch (entry.value) {
                case 'o':
                    value += 'OWN';
                    break;
                case 'v':
                    value += 'VIEW';
                    break;
                case 'c':
                    value += 'CREATE';
                    break;
                case 'm':
                    value += 'MODIFY';
                    break;
                case 'd':
                    value += 'DELETE';
                    break;
                default:
                    value += entry.value;
            }
            if (i < (entries.length -1)) {
                value += ', ';
            }
        }
        return value;
    },
    renderPrincipal: function(principal) {
        var rec = this.getUsersStore().getById(principal.uid);
        if (rec) {
            return rec.data.name + ' (' + rec.data.username + ')';
        } else {
            return '(unknown)';
        }
    },
    destroy: function() {
        this.getGrid().destroy();
        this.getUserSearchPanel().destroy();
        this.getForm().destroy();
    }
});




var XXXUsersResource = Ext.extend(Ext.util.Observable, {
    constructor: function(config) {
        this.users = [];
        this.addEvents('load');
        if (config) Ext.apply(this, config);
        XXXUsersResource.superclass.constructor.call(this, config);
    },
    addUser: function(user) {
        this.users.push(user);
    },
    load: function() {
        Ext.Ajax.request({
            url: this.getUrl(),
            success: this.onLoadSuccess.createDelegate(this),
            failure: this.onLoadFailure.createDelegate(this)
        });
    },
    onLoadSuccess: function(response, opts) {
        Ext.each(
            Ext.decode(response.responseText).users,
                this.addUser.createDelegate(this));
        this.fireEvent('load', this);
    },
    onLoadFailure: function(response, opts) {
        console.log('server-side failure with status code ' + response.status);
        this.fireEvent('loadfailure', this);
    },
    getUrl: function() {
        return '/users.json' + ((!this.search) ? '' : '?search=' + this.search);
    }
});