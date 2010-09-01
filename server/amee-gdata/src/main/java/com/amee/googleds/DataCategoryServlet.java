package com.amee.googleds;

import com.amee.base.transaction.TransactionController;
import com.amee.domain.data.*;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.engine.Engine;
import com.amee.service.data.DataService;
import com.amee.service.path.PathItemService;
import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class DataCategoryServlet extends DataSourceServlet {

    private TransactionController transactionController;
    private DataService dataService;
    private PathItemService pathItemService;

    @Override
    public void init() throws ServletException {
        super.init();
        transactionController = (TransactionController) Engine.getAppContext().getBean("transactionController");
        dataService = (DataService) Engine.getAppContext().getBean("dataService");
        pathItemService = (PathItemService) Engine.getAppContext().getBean("pathItemService");
    }

    public DataTable generateDataTable(Query query, HttpServletRequest request) {

        try {

            transactionController.begin(false);

            // Create a data-table,
            DataTable table = new DataTable();

            List<DataItem> items = getItems(request.getPathInfo());

            for (int i = 0; i < items.size(); i++) {

                if (i == 0) {
                    addColumnHeadings(items.get(i), table);
                }

                try {
                    addRow(items.get(i), table);
                } catch (TypeMismatchException e) {
                    e.printStackTrace();
                }
            }

            return table;

        } finally {
            transactionController.end();
        }

    }

    private void addRow(Item item, DataTable table) throws TypeMismatchException {

        TableRow row = new TableRow();

        for (ColumnDescription col : table.getColumnDescriptions()) {
            String value = item.getItemValue(col.getId()).getValue();
            if (col.getType().equals(ValueType.NUMBER)) {
                row.addCell(Float.parseFloat(value));
            } else {
                row.addCell(value);
            }
        }
        table.addRow(row);
    }

    private void addColumnHeadings(Item item, DataTable table) {

        ArrayList<ColumnDescription> cd = new ArrayList<ColumnDescription>();

        for (ItemValue itemValue : item.getItemValues()) {
            String name = itemValue.getName();
            String path = itemValue.getPath();
            ItemValueDefinition itemValueDefinition = itemValue.getItemValueDefinition();
            ValueType type;
            if (itemValueDefinition.isDouble()) {
                type = ValueType.NUMBER;
            } else if (itemValueDefinition.isDate()) {
                type = ValueType.DATETIME;
            } else {
                type = ValueType.TEXT;
            }
            cd.add(new ColumnDescription(path, type, name));
        }

        table.addColumns(cd);
    }

    private List<DataItem> getItems(String path) {

        String[] segmentArray = path.substring(1).split("\\.")[0].split("/");

        List<String> segments = new ArrayList<String>(segmentArray.length);
        for (String s : segmentArray) {
            segments.add(s);
        }

        PathItemGroup pathItemGroup = pathItemService.getPathItemGroup();
        PathItem pathItem = pathItemGroup.findBySegments(segments, false);
        DataCategory category = dataService.getDataCategoryByUid(pathItem.getUid());
        if (category.getItemDefinition() != null) {
            return dataService.getDataItems(category);
        } else {
            return new ArrayList<DataItem>(0);
        }
    }

    /**
     * NOTE: By default, this function returns true, which means that cross
     * domain requests are rejected. This check is disabled here so examples can
     * be used directly from the address bar of the browser. Bear in mind that
     * this exposes your data source to xsrf attacks. If the only use of the
     * data source url is from your application, that runs on the same domain,
     * it is better to remain in restricted mode.
     */
    protected boolean isRestrictedAccessMode() {
        return false;
    }

}
