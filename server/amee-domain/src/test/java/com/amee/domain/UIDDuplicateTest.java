package com.amee.domain;

import com.amee.domain.data.ItemValue;
import org.junit.Assert;
import org.junit.Test;

public class UIDDuplicateTest {

    @Test
    public void canHaveDifferentUIDs() {
        ItemValue iv1 = new ItemValue();
        iv1.addUid();
        ItemValue iv2 = new ItemValue();
        Assert.assertTrue("Should be able to have different UIDs.", iv2.addUid());
    }

    @Test
    public void canDetectDifferentUIDs() {
        ItemValue iv1 = new ItemValue();
        iv1.addUid();
        ItemValue iv2 = new ItemValue();
        iv2.setUid(iv1.getUid());
        Assert.assertFalse("Should not be able to have the same UIDs.", iv2.addUid());
    }
}
