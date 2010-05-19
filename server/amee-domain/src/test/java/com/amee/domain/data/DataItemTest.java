package com.amee.domain.data;

import com.amee.domain.AMEEStatus;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DataItemTest {

    private DataItem di;
    private DataCategory mockDc;
    private ItemDefinition mockItemDef;

    @Before
    public void setUp() {
        di = new DataItem();

        mockDc = mock(DataCategory.class);
        di.setDataCategory(mockDc);

        mockItemDef = mock(ItemDefinition.class);
        di.setItemDefinition(mockItemDef);
    }

    @Test
    public void noneTrashed() {

        // A DataItem should be considered trashed if:
        // itself is trashed or its DataCategory is trashed or its ItemDefinition is trashed

        when(mockDc.isTrash()).thenReturn(false);
        when(mockItemDef.isTrash()).thenReturn(false);

        // ACTIVE DI + ACTIVE DC + ACTIVE ID
        assertFalse("DataItem should not be trashed", di.isTrash());

        verify(mockDc).isTrash();
        verify(mockItemDef).isTrash();
    }

    @Test
    public void itselfTrashed() {
        di.setStatus(AMEEStatus.TRASH);

        assertTrue("DataItem should be trashed", di.isTrash());
    }

    @Test
    public void dataCategoryTrashed() {
        when(mockDc.isTrash()).thenReturn(true);
        assertTrue("DataItem should be trashed", di.isTrash());
        verify(mockDc).isTrash();
    }

    @Test
    public void itemDefinitionTrashed() {
        when(mockItemDef.isTrash()).thenReturn(true);
        assertTrue("DataItem should be trashed", di.isTrash());
        verify(mockItemDef).isTrash();
    }
}
