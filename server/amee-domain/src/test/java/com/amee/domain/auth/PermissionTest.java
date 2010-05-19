package com.amee.domain.auth;

import com.amee.domain.AMEEEntity;
import com.amee.domain.AMEEEntityReference;
import com.amee.domain.AMEEStatus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PermissionTest {

    private Permission permission;
    private AMEEEntityReference entityReference;
    private AMEEEntity mockEntity;

    @Before
    public void setUp() {
        permission = new Permission();
        entityReference = new AMEEEntityReference();

        mockEntity = mock(AMEEEntity.class);
        entityReference.setEntity(mockEntity);

        permission.setEntityReference(entityReference);
    }

    @Test
    public void noneTrashed() {

        // A Permission should be considered trashed if:
        // itself is trashed or its Entity is trashed.

        when(mockEntity.isTrash()).thenReturn(false);
        assertFalse("Permission should not be trashed|", permission.isTrash());
        verify(mockEntity).isTrash();
    }

    @Test
    public void itselfTrashed() {
        permission.setStatus(AMEEStatus.TRASH);
        assertTrue("Permission should be trashed", permission.isTrash());
    }

    @Test
    public void entityTrashed() {
        when(mockEntity.isTrash()).thenReturn(true);
        assertTrue("Permission should be trashed", permission.isTrash());
        verify(mockEntity).isTrash();
    }
}
