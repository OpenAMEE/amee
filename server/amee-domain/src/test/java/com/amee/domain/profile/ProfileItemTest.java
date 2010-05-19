package com.amee.domain.profile;

import com.amee.domain.AMEEStatus;
import com.amee.domain.data.DataItem;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProfileItemTest {
    private ProfileItem profileItem;
    private DataItem mockDataItem;
    private Profile mockProfile;

    @Before
    public void setUp() throws Exception {

        profileItem = new ProfileItem();
        mockDataItem = mock(DataItem.class);
        profileItem.setDataItem(mockDataItem);
        mockProfile = mock(Profile.class);
        profileItem.setProfile(mockProfile);
    }

    @Test
    public void noneTrashed() {
        
        // A ProfileItem should be considered trashed if:
        // itself is trashed or its DataItem is trashed or its Profile is trashed.

        when(mockDataItem.isTrash()).thenReturn(false);
        when(mockProfile.isTrash()).thenReturn(false);

        assertFalse("ProfileItem should not be trashed", profileItem.isTrash());

        verify(mockDataItem).isTrash();
        verify(mockProfile).isTrash();
    }

    @Test
    public void itselfTrashed() {
        profileItem.setStatus(AMEEStatus.TRASH);
        assertTrue("ProfileItem should be trashed", profileItem.isTrash());
    }

    @Test
    public void dataItemTrashed() {
        when(mockDataItem.isTrash()).thenReturn(true);

        assertTrue("ProfileItem should be trashed", profileItem.isTrash());

        verify(mockDataItem).isTrash();
    }

    @Test
    public void profileTrashed() {
        when(mockProfile.isTrash()).thenReturn(true);

        assertTrue("ProfileItem should be trashed", profileItem.isTrash());

        verify(mockProfile).isTrash();
    }
}
