package com.amee.googleds;

import com.amee.base.transaction.TransactionController;
import com.amee.domain.IDataCategoryReference;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.Item;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.engine.Engine;
import com.amee.service.data.DataService;
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

/**
 * This Servlet is not currently in use and is deprecated. See:
 * - https://jira.amee.com/browse/PL-3427
 * - https://jira.amee.com/browse/PL-40
 */
@Deprecated
@SuppressWarnings("serial")
public class DataCategoryServlet extends DataSourceServlet {

    private TransactionController transactionController;
    private DataService dataService;

    @Override
    public void init() throws ServletException {
        super.init();
        transactionController = (TransactionController) Engine.getAppContext().getBean("transactionController");
        dataService = (DataService) Engine.getAppContext().getBean("dataService");
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
        path = path.substring(1).split("\\.")[0];
        IDataCategoryReference category = dataService.getDataCategoryByFullPath(path);
        if ((category != null) && (category.isItemDefinitionPresent())) {
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
