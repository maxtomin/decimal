package org.maxtomin.decimal;

public class BaseDecimal {
    static final int WORD_BITS = 32;
    static final long WORD_CARRY = 1L << WORD_BITS;
    static final long WORD_LO_MASK = WORD_CARRY - 1;

    private long a; // accumulator

    long getA() {
        return a;
    }

    long mulhi_63_32(long a_63, long b_32) {
        long hi_31 = hi_32(a_63);

        long rLo_64 = lo_32(a_63) * b_32;
        long rHi_63 = hi_31 * b_32 + hi_32(rLo_64);

        a = lo_32(rLo_64);
        return rHi_63;
    }

    long muldiv_63o_63(long v_63, int m_31, long d_63) {
        long offset_63o = 0;
        if (v_63 >= d_63) {
            // v * m / d = (v / d * d + v % d) * m / d = v / d * m + v % d * m / d
            offset_63o = v_63 / d_63;
            v_63 = v_63 % d_63;

            offset_63o *= m_31; // overflow possible here
        }
        assert v_63 < d_63 : "and therefore quotient < m_31 <= Integer.MAX_VALUE";

        long p_63 = mulhi_63_32(v_63, m_31);
        long p_32 = a;

        if (d_63 <= Integer.MAX_VALUE) {
            assert v_63 <= Integer.MAX_VALUE;
            v_63 *= m_31; // no overflow

            // simple division
            offset_63o += v_63 / d_63; // quotient (considering the offset)
            a = v_63 % d_63; // remainder
            return offset_63o;
        }

        // normalizing:
        int shift = Long.numberOfLeadingZeros(d_63) - 1; // don't touch the sign bit
        assert shift < 32;
        d_63 <<= shift;
        assert Long.numberOfLeadingZeros(p_63) >= shift + 1 : "otherwise quotient would be > Integer.MAX_VALUE";
        p_63 <<= shift;
        p_32 <<= shift;
        p_63 |= hi_32(p_32);
        p_32 = lo_32(p_32);

        // approximate division
        long d_31 = hi_32(d_63);
        long d_32 = lo_32(d_63);
        long qhat_32 = p_63 / d_31; // approximate division (high parts)
        qhat_32 = lo_32(qhat_32); // exact quotient can not be bigger than Integer.MAX_VALUE

        // multiply back
        long phat_63 = mulhi_63_32(d_63, qhat_32);
        long phat_32 = a;

        // our divisor is between 2^62 and 2^63 - 1 after normalization
        // we decreased dividend by max 2^32 - 1
        // so quotient is decreased by max 2^32 / 2^62 < 1

        // we decreased divisor by max 2^32 - 1 or by max fraction 2^32 / 2^62
        // so quotient is increased by max fraction 2^32 / 2^62 or by max absolute 2^32 / 2^62 * 2^31 = 2^63 / 2^62 = 2

        // phat must be < p, applying corrections (no more than 2)
        int counter = 0;
        while (greaterThan(phat_63, phat_32, p_63, p_32)) {
            assert ++counter <= 2;
            --qhat_32;

            // delta -= d
            phat_63 -= d_31;
            phat_32 -= d_32;
            if (phat_32 < 0) {
                --phat_63;
                phat_32 += WORD_CARRY;
            }
        }
        long remainder = ((p_63 - phat_63) << WORD_BITS) + p_32 - phat_32; // remainder
        if (remainder >= d_63 || remainder < 0) { // unsigned compare
            remainder -= d_63;
            qhat_32++;
        }
        assert remainder >= 0 && remainder < d_63 : "no more than 1 correction up";

        a = remainder >> shift;
        return offset_63o + qhat_32;
    }

    long mulscale_63o_31(long a_63, long b_63, int scale) {
        long a_31 = hi_32(a_63);
        long a_32 = lo_32(a_63);

        long p = a_32 * lo_32(b_63); // lo parts
        long p_32 = lo_32(p);
        long p_63o = hi_32(p);

        // we don't care about overflow to word 3 there:
        p_63o += a_32 * hi_32(b_63);
        p_63o += a_31 * b_63;

        // long division: words 2,1,0 by int POW10[scale]
        long result_63o = downScale_63_31(p_63o, scale);
        return (result_63o << WORD_BITS) + downScale_63_31((a << WORD_BITS) | p_32, scale);
    }

    long downScale_63_31(long v_63, int scale) {
        // switch + division by constant is faster than "v_63 / PO10[scale]"
        switch (scale) {
            case 0:
                a = 0;
                return v_63;
            case 1:
                a = v_63 % 10;
                return v_63 / 10;
            case 2:
                a = v_63 % 100;
                return v_63 / 100;
            case 3:
                a = v_63 % 1000;
                return v_63 / 1000;
            case 4:
                a = v_63 % 10000;
                return v_63 / 10000;
            case 5:
                a = v_63 % 100000;
                return v_63 / 100000;
            case 6:
                a = v_63 % 1000000;
                return v_63 / 1000000;
            case 7:
                a = v_63 % 10000000;
                return v_63 / 10000000;
            case 8:
                a = v_63 % 100000000;
                return v_63 / 100000000;
            case 9:
                a = v_63 % 1000000000;
                return v_63 / 1000000000;
            default:
                throw new IllegalArgumentException("Incorrect scale: " + scale);
        }
    }

    private static boolean greaterThan(long ah_63, long al_32, long bh_63, long bl_32) {
        return ah_63 > bh_63 || ah_63 == bh_63 && al_32 >= bl_32;
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
