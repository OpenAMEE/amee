package com.amee.domain.data;

import com.amee.domain.AMEEStatus;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ItemValueDefinitionTest {

    private ItemValueDefinition itemValueDef;
    private ItemDefinition mockItemDef;

    @Before
    public void setUp(){
        itemValueDef = new ItemValueDefinition();
        mockItemDef = mock(ItemDefinition.class);
        itemValueDef.setItemDefinition(mockItemDef);
    }

    @Test
    public void noneTrashed() {

        // An ItemValueDefinition should be considered trashed if:
        // itself is trashed or its ItemDefinition is trashed.

        when(mockItemDef.isTrash()).thenReturn(false);
        assertFalse("ItemValueDefinition should not be trashed", itemValueDef.isTrash());
        verify(mockItemDef).isTrash();
    }

    @Test
    public void itselfTrashed() {
        itemValueDef.setStatus(AMEEStatus.TRASH);
        assertTrue("ItemValueDefinition should be trashed", itemValueDef.isTrash());
    }

    @Test
    public void itemDefTrashed() {
        when(mockItemDef.isTrash()).thenReturn(true);
        assertTrue("ItemValueDefinition should be trashed", itemValueDef.isTrash());
        verify(mockItemDef).isTrash();
    }
}
