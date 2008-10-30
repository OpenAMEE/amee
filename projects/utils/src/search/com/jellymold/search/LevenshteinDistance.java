package com.jellymold.search;

/**
 * Java implementation of the Levenshtein Distance (LD) (http://www.merriampark.com/ld.htm). LD is a measure of the
 * similarity between two strings, which we will refer to as the source string (s) and the target string (t).
 * The distance is the number of deletions, insertions, or substitutions required to transform s into t.
 * <UL>
 * <LI>If s is "test" and t is "test", then LD(s,t) = 0, because
 * no transformations are needed. The strings are already
 * identical.
 * <LI>If s is "test" and t is "tent", then LD(s,t) = 1, because one
 * substitution (change "s" to "n") is sufficient to transform s into t.
 * <p/>
 * </UL>
 * The greater the Levenshtein distance, the more different the strings are.
 * </P>
 * <p/>
 * Levenshtein distance is named after the Russian scientist Vladimir
 * Levenshtein, who devised the algorithm in 1965. If you can't spell or pronounce
 * Levenshtein, the metric is also sometimes called edit distance.
 * </P>
 * <p/>
 * The Levenshtein distance algorithm has been used in:
 * <UL>
 * <LI>Spell checking
 * <LI>Speech recognition
 * <LI>DNA analysis
 * <LI>Plagiarism detection
 * </UL>
 * </P>
 */
public class LevenshteinDistance {

    /**
     * Compute Levenshtein distance
     *
     * @param s source string
     * @param t target string
     * @return returns the distance (number of deletions, insertions, or substitutions required to transform s into t)
     */
    public static int distance(String s, String t) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1
        n = s.length();
        m = t.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];

        // Step 2
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Step 3
        for (i = 1; i <= n; i++) {

            s_i = s.charAt(i - 1);

            // Step 4
            for (j = 1; j <= m; j++) {

                t_j = t.charAt(j - 1);

                // Step 5
                if (s_i == t_j) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                // Step 6
                d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);

            }

        }

        // Step 7
        return d[n][m];
    }

    /**
     * Compute minimum of three values
     *
     * @param a a value
     * @param b b value
     * @param c c value
     * @return returns the minimum of three values
     */
    private static int Minimum(int a, int b, int c) {
        int mi;

        mi = a;
        if (b < mi) {
            mi = b;
        }
        if (c < mi) {
            mi = c;
        }
        return mi;

    }

}