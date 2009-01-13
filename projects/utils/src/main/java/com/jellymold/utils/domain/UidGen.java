package com.jellymold.utils.domain;

import java.util.Random;

// TODO: code read this
// TODO: These have a probability of 3.55271368 ? 10^-15 of not being unique :)

// TODO: test harness or main method tester
public class UidGen {

    public final static String SEPARATOR = "";
    public final static int PARTS = 6;
    public final static int PART_SIZE = 2;
    public final static int RANGE = (int) Math.pow(16, PART_SIZE);

    private static Random random = new Random();

    /**
     * Generates a 'unique' uid containing hex values.
     * <p/>
     * Example: 2DF512B4F183
     *
     * @return uid
     */
    public static String getUid() {
        StringBuffer uid = new StringBuffer();
        for (int i = 0; i < PARTS; i++) {
            addUidPart(uid);
            if (i != PARTS) {
                uid.append(SEPARATOR);
            }
        }
        return uid.toString().toUpperCase();
    }

    protected static void addUidPart(StringBuffer uid) {
        StringBuffer part = new StringBuffer();
        part.append(Integer.toHexString(random.nextInt(RANGE)));
        while (part.length() < PART_SIZE) {
            part.insert(0, '0'); // left pad with 0's
        }
        uid.append(part);
    }
}
