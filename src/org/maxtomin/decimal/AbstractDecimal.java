package org.maxtomin.decimal;

public abstract class AbstractDecimal {
    private static final int WORD_BITS = 32;
    private static final long WORD_CARRY = 1L << WORD_BITS;
    private static final long WORD_LO_MASK = WORD_CARRY - 1;

    private long a; // accumulator

    protected long mulhi_63_32(long a_63, long b_32) {
        long hi_31 = hi_32(a_63);

        long rLo_64 = lo_32(a_63) * b_32;
        long rHi_63 = hi_31 * b_32 + hi_32(rLo_64);

        a = lo_32(rLo_64);
        return rHi_63;
    }

    protected long muldiv_63o_63(long v_63, int m_32, long d_63) {
        long offset_63o = 0;
        if (v_63 > d_63) {
            // v * m / d = (v / d * d + v % d) * m / d = v / d * m + v % d * m / d
            offset_63o = v_63 / d_63;
            v_63 = v_63 % d_63;

            offset_63o *= m_32;
        }
        assert v_63 < d_63;

        long p_63 = mulhi_63_32(v_63, m_32);
        long p_32 = a;

        if (d_63 <= Integer.MAX_VALUE) {
            assert v_63 <= Integer.MAX_VALUE;
            // simple division
            offset_63o += v_63 / d_63;
            a = v_63 % d_63; // remainder
            return offset_63o;
        }

        // normalizing:
        int shift = Long.numberOfLeadingZeros(d_63) - 1; // don't touch the sign bit
        assert shift < 32;
        d_63 <<= shift;
        assert Long.numberOfLeadingZeros(p_63) >= shift + 1;
        p_63 <<= shift;
        p_32 <<= shift;
        p_63 |= hi_32(p_32);
        p_32 = lo_32(p_32);

        long d_31 = hi_32(d_63); // approximate divisor
        assert bits(d_31) == 31;
        // approximate division
        long qhat_33 = p_63 / d_31;
        assert bits(qhat_33) <= 33 : "0x7fffffffffffffff / 0x40000000 == 0x1ffffffff";

        long phat_63 = mulhi_63_32(qhat_33, d_31);
        long phat_32 = a;

        // correction 1 (decreasing qhat)
        if (greaterThan(phat_63, phat_32, p_63, p_32)) {
            --qhat_33;
            phat_32 -= d_31;
            if (phat_32 < 0) {
                phat_32 += WORD_CARRY;
                --phat_63;
            }
        }

        // correction 2 (decreasing qhat)
        if (greaterThan(phat_63, phat_32, p_63, p_32)) {
            --qhat_33;
            phat_32 -= d_31;
            if (phat_32 < 0) {
                phat_32 += WORD_CARRY;
                --phat_63;
            }
        }

        assert !greaterThan(phat_63, phat_32, p_63, p_32) : "no more than 2 corrections possible";
        assert phat_63 == p_63 || phat_63 == p_63 - 1;

        // "qhat_33 + 1" is true quantity (considering the offset)
        a = d_63 + phat_32 - p_32 - (phat_63 == p_63 ? WORD_CARRY : 0); // remainder
        return offset_63o + qhat_33 + 1;
    }

    private static boolean greaterThan(long ah_63, long al_32, long bh_63, long bl_32) {
        return ah_63 > bh_63 || ah_63 == bh_63 && al_32 > bl_32;
    }

    private static int bits(long v_64) {
        return 64 - Long.numberOfLeadingZeros(v_64);
    }

    private static long hi_32(long v_64) {
        return v_64 >>> WORD_BITS;
    }

    private static long lo_32(long v_64) {
        return v_64 & WORD_LO_MASK;
    }

    public static void main(String[] args) {
        System.out.println(Long.toHexString(Integer.MAX_VALUE / 2 + 1));
        System.out.println(Long.toHexString(Long.MAX_VALUE));
        System.out.println(Long.toHexString((Long.MAX_VALUE - 1) / (Integer.MAX_VALUE / 2 + 1)));
    }
}
