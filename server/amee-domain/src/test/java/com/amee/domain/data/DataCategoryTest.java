package com.amee.domain.data;

import com.amee.domain.AMEEStatus;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DataCategoryTest {
    private DataCategory dc;
    private DataCategory mockParentDc;
    private DataCategory mockAliasDc;
    private ItemDefinition mockItemDef;


    @Before
    public void setUp() {

        dc = new DataCategory();

        mockItemDef = mock(ItemDefinition.class);
        dc.setItemDefinition(mockItemDef);

        mockParentDc = mock(DataCategory.class);
        dc.setDataCategory(mockParentDc);

        mockAliasDc = mock(DataCategory.class);
        dc.setAliasedTo(mockAliasDc);
    }

    @Test
    public void noneTrashed() {

        // A DataCategory should be considered trashed if:
        // itself is trashed or its ItemDefinition is trashed or its parent DataCategory is trashed.

        when(mockItemDef.isTrash()).thenReturn(false);
        when(mockParentDc.isTrash()).thenReturn(false);
        assertFalse("DataCategory should be active", dc.isTrash());

        verify(mockItemDef).isTrash();
        verify(mockParentDc).isTrash();
    }

    @Test
    public void itselfTrashed() {
        dc.setStatus(AMEEStatus.TRASH);
        assertTrue("DataCategory is trashed", dc.isTrash());
    }

    @Test
    public void parentTrashed() {
        when(mockParentDc.isTrash()).thenReturn(true);
        assertTrue("DataCategory should be trashed", dc.isTrash());
        verify(mockParentDc).isTrash();
    }

    @Test
    public void itemDefTrashed() {
        when(mockItemDef.isTrash()).thenReturn(true);
        assertTrue("Linked ItemDefinition is trashed", dc.isTrash());
        verify(mockItemDef).isTrash();
    }

    @Test
    public void aliasTrashed() {
        when(mockAliasDc.isTrash()).thenReturn(true);
        assertTrue("DataCategory should be trashed", dc.isTrash());
        verify(mockAliasDc).isTrash();
    }
}
