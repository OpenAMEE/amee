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
                height: 320
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