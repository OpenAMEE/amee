package com.amee.domain.data;

import com.amee.domain.AMEEStatus;
import org.junit.Test;
import static org.junit.Assert.*;

public class DataCategoryTest {

    @Test
    public void testIsTrash() {

        // A DataCategory should be considered trashed if:
        // itself is trashed or its ItemDefinition is trashed.

        ItemDefinition itemDef = new ItemDefinition();
        DataCategory dc = new DataCategory();
        dc.setItemDefinition(itemDef);

        // ACTIVE DC + ACTIVE ID
        assertFalse("DataCategory should be active", dc.isTrash());

        // TRASHED DC + ACTIVE ID
        dc.setStatus(AMEEStatus.TRASH);
        assertTrue("DataCategory is trashed", dc.isTrash());

        // ACTIVE DC + TRASHED ID
        dc.setStatus(AMEEStatus.ACTIVE);
        itemDef.setStatus(AMEEStatus.TRASH);
        assertTrue("Linked ItemDefinition is trashed", dc.isTrash());

        // TRASHED DC + TRASHED ID
        dc.setStatus(AMEEStatus.TRASH);
        assertTrue("DataCategory is trashed", dc.isTrash());
    }
}
