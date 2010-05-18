package com.amee.domain.data;

import com.amee.domain.AMEEStatus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ItemValueTest {

    private ItemValue iv;
    private Item mockItem;
    private ItemValueDefinition mockIvd;

    @Before
    public void setUp() {
        iv = new ItemValue();

        mockItem = mock(Item.class);
        iv.setItem(mockItem);

        mockIvd = mock(ItemValueDefinition.class);
        iv.setItemValueDefinition(mockIvd);

        
    }

    @Test
    public void noneTrashed() {

        // An ItemValue should be considered trashed if:
        // itself is trashed or its Item is trashed or its ItemValueDefinition is trashed

        when(mockItem.isTrash()).thenReturn(false);
        when(mockIvd.isTrash()).thenReturn(false);

        // ACTIVE IV + ACTIVE ITEM + ACTIVE IVD
        assertFalse("ItemValue should not be trashed", iv.isTrash());

        verify(mockItem).isTrash();
        verify(mockIvd).isTrash();
    }

    @Test
    public void itselfTrashed() {

        iv.setStatus(AMEEStatus.TRASH);

        // TRASHED IV + ACTIVE ITEM + ACTIVE IVD
        assertTrue("Item value should be trashed", iv.isTrash());
    }

    @Test
    public void itemTrashed() {

        when(mockItem.isTrash()).thenReturn(true);

        // ACTIVE IV + TRASHED ITEM + ACTIVE IVD
        assertTrue("ItemValue should be trashed", iv.isTrash());
        verify(mockItem).isTrash();
    }

    @Test
    public void ivdTrashed() {

        when(mockIvd.isTrash()).thenReturn(true);

        // ACTIVE IV + ACTIVE ITEM + TRASHED IVD
        assertTrue("ItemValue should be trashed", iv.isTrash());
        verify(mockIvd).isTrash();
    }
}
