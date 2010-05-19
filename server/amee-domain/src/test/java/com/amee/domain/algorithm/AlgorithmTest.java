package com.amee.domain.algorithm;

import com.amee.domain.AMEEStatus;
import com.amee.domain.data.ItemDefinition;
import org.junit.Test;
import static org.junit.Assert.*;

public class AlgorithmTest {

    @Test
    public void isTrashed() {

        // An Algorithm should be considered trashed if:
        // itself is trashed or its ItemDefinition is trashed.

        ItemDefinition itemDef = new ItemDefinition();
        Algorithm algorithm = new Algorithm();
        algorithm.setItemDefinition(itemDef);
        assertFalse("Algorithm should not be trashed", algorithm.isTrash());

        algorithm.setStatus(AMEEStatus.TRASH);
        assertTrue("Algorithm should be trashed", algorithm.isTrash());

        algorithm.setStatus(AMEEStatus.ACTIVE);
        itemDef.setStatus(AMEEStatus.TRASH);
        assertTrue("Algorithm should be trashed", algorithm.isTrash());        
    }
}
