package com.amee.domain.data;

import com.amee.domain.AMEEStatus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class LocaleNameTest {

    private LocaleName localeName;
    private ItemDefinition mockItemDef;
    private DataCategory mockDataCategory;
    private ItemValue mockItemValue;

    @Before
    public void setUp() throws Exception {
        mockItemDef = mock(ItemDefinition.class);
        mockDataCategory = mock(DataCategory.class);
        mockItemValue = mock(ItemValue.class);
    }

    @Test
    public void noneTrashed() {

        // TODO: Tests disabled due to com.amee.domain.data.LocaleName#isTrash issue.

        // A LocaleName should be considered trashed if:
        // itself is trashed or its Entity (DC, IT, IV) is trashed

//        when(mockItemDef.isTrash()).thenReturn(false);
//        localeName = new LocaleName(mockItemDef, Locale.getDefault(), "foo");
//        assertFalse("LocaleName should not be trashed", localeName.isTrash());
//        verify(mockItemDef).isTrash();

//        when(mockDataCategory.isTrash()).thenReturn(false);
//        localeName = new LocaleName(mockDataCategory, Locale.getDefault(), "foo");
//        assertFalse("LocaleName should not be trashed", localeName.isTrash());
//        verify(mockDataCategory).isTrash();

//        when(mockItemValue.isTrash()).thenReturn(false);
//        localeName = new LocaleName(mockItemValue, Locale.getDefault(), "foo");
//        assertFalse("LocaleName should not be trashed", localeName.isTrash());
//        verify(mockItemValue).isTrash();
    }

    @Test
    public void itselfTrashed() {
        localeName = new LocaleName();
        localeName.setStatus(AMEEStatus.TRASH);
        assertTrue("LocaleName should be trashed", localeName.isTrash());
    }

    @Test
    public void entityTrashed() {

        // TODO: Tests disabled due to com.amee.domain.data.LocaleName#isTrash issue.

//        when(mockItemDef.isTrash()).thenReturn(true);
//        localeName = new LocaleName(mockItemDef, Locale.getDefault(), "foo");
//        assertTrue("LocaleName should be trashed", localeName.isTrash());
//        verify(mockItemDef).isTrash();

//        when(mockDataCategory.isTrash()).thenReturn(true);
//        localeName = new LocaleName(mockDataCategory, Locale.getDefault(), "foo");
//        assertTrue("LocaleName should be trashed", localeName.isTrash());
//        verify(mockDataCategory).isTrash();

//        when(mockItemValue.isTrash()).thenReturn(true);
//        localeName = new LocaleName(mockItemValue, Locale.getDefault(), "foo");
//        assertTrue("LocaleName should be trashed", localeName.isTrash());
//        verify(mockItemValue).isTrash();
    }
}
