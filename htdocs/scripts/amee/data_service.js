var DataCategoryApiService = Class.create(ApiService, ({
        // Initialization
    initialize: function($super, params) {
        $super(params);
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
        }    },
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
    }
}));
