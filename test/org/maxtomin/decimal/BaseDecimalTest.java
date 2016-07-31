package org.maxtomin.decimal;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BaseDecimalTest {
    private final BaseDecimal decimal = new BaseDecimal();

    @Test
    public void testMulHi() throws Exception {
        testMulHi(1, 1);
        testMulHi(0, 123);
        testMulHi(123, 0);
        testMulHi(1000000000, 1000000000);
        testMulHi(1000000000000L, 1000000000);
        testMulHi(1000000000000L, 3000000000L);
        testMulHi(Long.MAX_VALUE, Integer.MAX_VALUE);

        testMulHi(1234568L, Integer.MAX_VALUE);
        testMulHi(449033535071450778L, 2147483647);
        testMulHi(303601908757L, Integer.MAX_VALUE);
        testMulHi(449033535071450778L, Integer.MAX_VALUE);
        testMulHi(4026532095L, 16777215);
        testMulHi(67553994662215680L, 16777215);
    }

    @Test
    public void testMulDiv() throws Exception {
        testMulDiv(1, 1, 1);
        testMulDiv(1000, 1, 1);
        testMulDiv(1, 1, 1000);
        testMulDiv(10, 15, 7);
        testMulDivOverflow(Long.MAX_VALUE, Integer.MAX_VALUE, 1);
        testMulDiv(Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        testMulDiv(Long.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);

        testMulDiv(1234568L, Integer.MAX_VALUE, 1L);
        testMulDiv(449033535071450778L, 2147483647, 12L);
        testMulDiv(303601908757L, Integer.MAX_VALUE, 659820219978L);
        testMulDiv(449033535071450778L, Integer.MAX_VALUE, 659820219978L);
        testMulDiv(0x400000000000L, 0x400000, Long.MAX_VALUE);
        testMulDiv(4026532095L, 16777215, 67553994662215680L);
    }

    @Test
    public void testMulScale() throws Exception {
        testMulScale(1, 1, 0);
        testMulScale(1000, 1, 0);
        testMulScale(1, 1, 3);
        testMulScale(10, 15, 7);
        testMulScale(Long.MAX_VALUE, Long.MAX_VALUE, 1);
        testMulScale(Long.MAX_VALUE, Long.MAX_VALUE, 9);
        testMulScale(Long.MAX_VALUE, Long.MAX_VALUE, 18);

        testMulScale(1234568L, Integer.MAX_VALUE, 1);
        testMulScale(449033535071450778L, 2147483647, 2);
        testMulScale(4984198405165151231L, 6132198419878046132L,7);
        testMulScale(1540173641653250113L, 1015059321913633968L, 8);
        testMulScale(449033535071450778L, 3155170653582908051L, 9);
        testMulScale(303601908757L, 829267376026L, 5);
        testMulScale(449033535071450778L, 829267376026L, 3);
        testMulScale(1234568, 829267376026L, 0);
        testMulScale(6991754535226557229L, 7798003721120799096L, 2);
        testMulScale(9223372036854775807L, 2147483648L, 4);
        testMulScale(9223372032559808512L, 9223372036854775807L, 6);
        testMulScale(9223372032559808512L, 9223372036854775807L, 2);
        testMulScale(9223372036854775807L, 8446744073709551615L, 3);

        testMulScaleNoOverflow(1234568L, Integer.MAX_VALUE, 9);
        testMulScaleNoOverflow(449033535071450778L, 2147483647, 9);
        testMulScaleNoOverflow(4984198405165L, 6132198419878L, 9);
        testMulScaleNoOverflow(1540173641653L, 1015059321913L, 9);
        testMulScaleNoOverflow(44903353507145L, 3155170653582L, 9);
        testMulScaleNoOverflow(303601908757L, 829267376026L, 9);
        testMulScaleNoOverflow(4490335350714L, 829267376026L, 9);
        testMulScaleNoOverflow(1234568, 829267376026L, 9);
        testMulScaleNoOverflow(Integer.MAX_VALUE, 779800372112079L, 9);
        testMulScaleNoOverflow(Integer.MAX_VALUE, 2147483648L, 9);
        testMulScaleNoOverflow(Integer.MAX_VALUE, 922337203685477L, 9);
        testMulScaleNoOverflow(Integer.MAX_VALUE, 844674407370955L, 9);

        testMulScale(1234568L, Integer.MAX_VALUE, 18);
        testMulScale(449033535071450778L, 2147483647, 18);
        testMulScale(4984198405165151231L, 6132198419878046132L, 18);
        testMulScale(1540173641653250113L, 1015059321913633968L, 18);
        testMulScale(449033535071450778L, 3155170653582908051L, 18);
        testMulScale(303601908757L, 829267376026L, 18);
        testMulScale(449033535071450778L, 829267376026L, 18);
        testMulScale(1234568, 829267376026L, 18);
        testMulScale(6991754535226557229L, 7798003721120799096L, 18);
        testMulScale(9223372036854775807L, 2147483648L, 18);
        testMulScale(9223372032559808512L, 9223372036854775807L, 18);
        testMulScale(9223372032559808512L, 9223372036854775807L, 18);
        testMulScale(9223372036854775807L, 8446744073709551615L, 18);
    }

    private void testMulScale(long a, long b, int scale) {
        long q = decimal.mulscale_63o_31(a, b, scale);
        long r = decimal.getA();
        BigInteger[] dAndR = BigInteger.valueOf(a)
                .multiply(BigInteger.valueOf(b))
                .divideAndRemainder(BigInteger.TEN.pow(scale));
        if (dAndR[0].compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
            assertEquals("Quantity", dAndR[0], BigInteger.valueOf(q));
            assertEquals("Remainder", dAndR[1], BigInteger.valueOf(r));
        } // else - overflow
    }

    private void testMulScaleNoOverflow(long a, long b, int scale) {
        long q = decimal.mulscale_63o_31(a, b, scale);
        long r = decimal.getA();
        BigInteger[] dAndR = BigInteger.valueOf(a)
                .multiply(BigInteger.valueOf(b))
                .divideAndRemainder(BigInteger.TEN.pow(scale));
        assertEquals("Quantity", dAndR[0], BigInteger.valueOf(q));
        assertEquals("Remainder", dAndR[1], BigInteger.valueOf(r));
    }

    private void testMulDivOverflow(long v, int m, long d) {
        decimal.muldiv_63o_63(v, m, d); // just check asserts
        assertTrue(BigInteger.valueOf(v)
                .multiply(BigInteger.valueOf(m))
                .divide(BigInteger.valueOf(d))
                .compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0);
    }

    private void testMulDiv(long v, int m, long d) {
        try {
            long q = decimal.muldiv_63o_63(v, m, d);
            long r = decimal.getA();

            BigInteger[] dAndR = BigInteger.valueOf(v).multiply(BigInteger.valueOf(m)).divideAndRemainder(BigInteger.valueOf(d));
            if (dAndR[0].compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
                assertEquals("Quantity", dAndR[0], BigInteger.valueOf(q));
                assertEquals("Remainder", dAndR[1], BigInteger.valueOf(r));
            } // else - overflow
        } catch (AssertionError e) {
            System.out.println("Failed for " + v + ", " + m + ", " + d);
            throw e;
        }
    }

    private void testMulHi(long a, long b) {
        long hi = decimal.mulhi_63_32(a, b);
        long lo = decimal.getA();
        assertEquals(BigInteger.valueOf(hi).shiftLeft(BaseDecimal.WORD_BITS).add(BigInteger.valueOf(lo)),
                BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)));
    }

    private static long replaceBitsWithF(int v) {
        long result = 0;
        for (int i = 0; i < 16; i++) {
            if ((v & (1 << i)) != 0) {
                result |= (0xfL << (i * 4));
            }
        }
        return result;
    }

    public static void main(String[] args) {
        // bulk test testMulDiv
        BaseDecimalTest test = new BaseDecimalTest();
        for (int i = 0; i < 65536; i++) {
            long v = replaceBitsWithF(i);
            if (v < 0) {
                continue;
            }
            System.out.println(i);
            for (int j = 0; j < 256; j++) {
                long m = replaceBitsWithF(j);
                if (m < 0 || m > Integer.MAX_VALUE) {
                    continue;
                }
                for (int k = 0; k < 65536; k++) {
                    long d = replaceBitsWithF(k);
                    if (d <= 0) {
                        continue;
                    }
                    test.testMulDiv(v, (int) m, d);
                }
            }
        }
    }
}

