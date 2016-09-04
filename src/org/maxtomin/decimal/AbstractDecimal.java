package org.maxtomin.decimal;

import java.math.RoundingMode;
import java.text.ParseException;

public abstract class AbstractDecimal<T extends AbstractDecimal> extends BaseDecimal implements Comparable<T> {
    public static final long NaN = Long.MIN_VALUE;

    protected abstract int getScale();

    @SuppressWarnings("unchecked")
    public T self() {
        return (T) this;
    }

    public long getRaw() {
        return a;
    }

    public T setRaw(long raw) {
        a = raw;
        return self();
    }

    public boolean isNaN() {
        return getRaw() == NaN;
    }

    public T checkNotNaN() {
        if (isNaN()) {
            throw new ArithmeticException("Last operation was invalid (overflow or division by zero)");
        }
        return self();
    }

    public T negate() {
        return !isNaN() ? setRaw(-getRaw()) : self();
    }

    public <V extends AbstractDecimal> T plus(V a, V b, RoundingMode roundingMode) {
        if (a.getScale() != b.getScale()) {
            throw new IllegalArgumentException("Scales must be the same");
        }

        int scale = getScale() - a.getScale();
        if (scale < 0 && !a.isNaN() && !b.isNaN()) {
            // no overflow is possible
            long result = a.getRaw();
            long remainder = b.getRaw();
            if (result >= 0 && remainder >= 0) { // unsigned overflow is not possible, ok with signed one
                result = unsignedDownScale_64_31(result + remainder, -scale);
                remainder = getRaw();
            } else if (result < 0 && remainder < 0) { // same as above, but negate everything before and after
                result = -unsignedDownScale_64_31(-result - remainder, -scale);
                remainder = -getRaw();
            } else { // no overflow is possible
                result = downScale_63_31(result + remainder, -scale);
                remainder = getRaw();
            }
            return setRaw(round(result, remainder, POW10[-scale], roundingMode));
        }

        return plus(a.getRaw(), b.getRaw(), scale);
    }

    public T plus(long a, long b) {
        return plus(a, b, getScale());
    }

    private T plus(long a, long b, int scale) {
        long result = addWithOverflow(a, b);
        if (scale > 0) {
            result = scaleWithOverflow(result, scale);
        }
        return setRaw(result);
    }

    public <V extends AbstractDecimal> T add(V a, RoundingMode roundingMode) {
        int scale = getScale() - a.getScale();
        if (scale < 0 && !isNaN() && !a.isNaN()) {
            long self = getRaw();
            long other = downScale_63_31(a.getRaw(), POW10[scale]);
            long remainder = getRaw();

            long result = addWithOverflow(self, other);

            return setRaw(round(result, remainder, POW10[-scale], roundingMode));
        }

        return add(a.getRaw(), scale);
    }

    public T add(T a) {
        return plus(this, a, RoundingMode.DOWN);
    }

    public T add(long a) {
        return add(a, getScale());
    }

    private T add(long a, int scale) {
        long raw = getRaw();
        if (scale > 0) {
            long scaled = scaleWithOverflow(a, scale);
            if (scaled == NaN && a != NaN && raw != NaN) {
                // trying to eliminate overflow (possible when a and raw has different signs and abs(raw) is big enough)

                // another bigger limit for unsigned multiplication (if we are out of this as well - give up)
                long unsignedLimit = SCALE_OVERFLOW_LIMITS[scale] * 2;
                if (raw < 0 && a > 0 && a <= unsignedLimit) {
                    a = a * POW10[scale];
                    assert a < 0 : "Overflow to sign expected";
                    a += raw; // subtracting abs(raw)
                    if (a >= 0) {
                        // no more overflow
                        return setRaw(a);
                    }
                } else if (raw > 0 && a >= -unsignedLimit && a < 0) {
                    a = -a * POW10[scale]; // negate a before multiplying
                    assert a < 0 : "Overflow to sign expected";
                    a -= raw; // adding raw (in negated terms)
                    if (a >= 0) {
                        // no more overflow
                        return setRaw(-a); // don't forget to negate "a" back
                    }
                }
            }
            a = scaled;
        }

        return setRaw(addWithOverflow(raw, a));
    }

    public <V extends AbstractDecimal> T minus(V a, V b, RoundingMode roundingMode) {
        b.negate();
        plus(a, b, roundingMode);
        return setRaw(-getRaw()); // negate back without NaN check
    }

    public <V extends AbstractDecimal> T subtract(V a, RoundingMode roundingMode) {
        a.negate();
        add(a, roundingMode);
        return setRaw(-getRaw()); // negate back without NaN check
    }

    public <V extends AbstractDecimal> T product(V a, V b, RoundingMode roundingMode) {
        if (a.getScale() != b.getScale()) {
            throw new IllegalArgumentException("Scales must be the same");
        }

        int scale = a.getScale() + b.getScale() - getScale();
        if (scale >= 0) {
            return setRaw(scaleWithOverflow(mulWithOverflow(a.getRaw(), a.getRaw()), scale));
        } else {
            return setRaw(mulScaleRound(a.getRaw(), b.getRaw(), -scale, roundingMode));
        }
    }

    public T product(long a, long b, RoundingMode roundingMode) {
        return setRaw(mulScaleRound(a, b, getScale(), roundingMode));
    }

    public <V extends AbstractDecimal> T mul(V a, RoundingMode roundingMode) {
        return setRaw(mulScaleRound(getRaw(), a.getRaw(), a.getScale(), roundingMode));
    }

    public T mul(long a) {
        return setRaw(mulWithOverflow(getRaw(), a));
    }

    public <V extends AbstractDecimal> T quotient(V a, V b, RoundingMode roundingMode) {
        if (a.getScale() != b.getScale()) {
            throw new IllegalArgumentException("Scales must be the same");
        }
        return quotient(a.getRaw(), b.getRaw(), roundingMode);
    }

    public T quotient(long a, long b, RoundingMode roundingMode) {
        return setRaw(scaleDivRound(a, getScale(), b, roundingMode));
    }

    public <V extends AbstractDecimal> T div(V a, RoundingMode roundingMode) {
        return setRaw(scaleDivRound(getRaw(), a.getScale(), a.getRaw(), roundingMode));
    }

    public T div(long a, RoundingMode roundingMode) {
        if (isNaN() || a == NaN || a == 0) {
            return setRaw(NaN);
        }

        // if a is negative - negate both numerator and denominator (to comply with "round" contract)
        long signA = a >> 63;
        a = negIf(a, signA);
        long raw = negIf(getRaw(), signA);

        return setRaw(round(raw / a, raw % a, a, roundingMode));
    }

    @Override
    public int compareTo(AbstractDecimal o) {
        long saved = getRaw();

        int scale = getScale() - o.getScale();
        int result;
        if (scale >= 0) {
            long first = downScale_63_31(getRaw(), scale);
            result = first > o.getRaw() ? 1 :
                    first < o.getRaw() ? -1 :
                            (int) getRaw();
        } else {
            long second = downScale_63_31(o.getRaw(), -scale);
            result = o.getRaw() > second ? 1 :
                    o.getRaw() < second ? -1 :
                            (int) -getRaw();
        }

        setRaw(saved);
        return result;
    }

    public String toString() {
        return toStringBuilder(new StringBuilder(21)).toString();
    }

    public T parse(CharSequence charSequence) throws ParseException {
        int length = charSequence.length();
        if (length == 0) {
            throw new ParseException("Empty string", 0);
        }

        int index = 0;
        boolean negative = false;
        char ch = charSequence.charAt(0);
        switch (ch) {
            case '-':
                if (length == 1) {
                    throw new ParseException("Single '-' is not expected", 0);
                }
                negative = true;
                index++;
                break;
            case 'N':
            case 'n':
                if (length != 3 ||
                        charSequence.charAt(1) != 'a' && charSequence.charAt(1) != 'A' ||
                        charSequence.charAt(2) != 'n' && charSequence.charAt(2) != 'N')
                    throw new ParseException("Unexpected alphanumeric value", 0);
                return setRaw(NaN);
            default:
                // go on
        }

        long result = 0;
        int dotPos = length;
        while (index < length) {
            ch = charSequence.charAt(index++);
            if (ch == '.') {
                if (dotPos != length) {
                    throw new ParseException("Double '.' found", index);
                }
                dotPos = index;
            } else if (ch >= '0' && ch <= '9') {
                if (result > Long.MAX_VALUE / 10) {
                    throw new ParseException("Overflow", 0);
                }
                result *= 10;
                result += ch - '0';
                if (result < 0) {
                    throw new ParseException("Overflow", 0);
                }
            } else {
                throw new ParseException("Unexpected " + ch, index);
            }
        }

        result = scaleWithOverflow(result, getScale() - (length - dotPos));
        if (result == NaN) {
            throw new ParseException("Overflow while scaling up", 0);
        }
        return setRaw(negative ? -result : result);
    }

    public StringBuilder toStringBuilder(StringBuilder sb) {
        long raw = getRaw();
        if (raw == NaN) {
            sb.append("NaN");
            return sb;
        }

        if (raw < 0) {
            sb.append('-');
            raw = -raw;
        }

        int length = stringSize(raw);
        int scale = getScale();
        if (scale >= length) {
            sb.append(ZEROES[scale - length + 1]);
        }
        sb.append(raw);

        if (scale > 0) {
            sb.append('.');
            for (int index = sb.length() - 2; index > 0; index--) {
                sb.setCharAt(index + 1, sb.charAt(index));
                if (--scale == 0) {
                    sb.setCharAt(index, '.');
                    return sb;
                }
            }
        }

        return sb;
    }

    private static final int MAX_LONG_SIZE = Long.toString(Long.MAX_VALUE).length();
    private static int stringSize(long value) {
        long product = 10;
        for (int size = 1; size < MAX_LONG_SIZE; size++) {
            if (value < product)
                return size;
            product *= 10;
        }
        return 19;
    }

    private static long addWithOverflow(long a, long b) {
        long result = a + b;
        if (a == NaN || b == NaN || (result < 0) != (a < 0) && (result < 0) != (b < 0)) {
            return NaN;
        }
        return result;
    }

    private static long scaleWithOverflow(long value, int scale) {
        if (value >= -SCALE_OVERFLOW_LIMITS[scale] && value <= SCALE_OVERFLOW_LIMITS[scale]) { // and not NaN
            value = value * POW10[scale];
            return value;
        } else {
            return NaN;
        }
    }

    private long mulWithOverflow(long a, long b) {
        if (a > Integer.MIN_VALUE && a <= Integer.MAX_VALUE &&
                b > Integer.MIN_VALUE && b <= Integer.MAX_VALUE) { // and not NaN
            // can multiply without overflow
            return a * b;
        } else {
            return mulScaleRound(a, b, 0, RoundingMode.UNNECESSARY);
        }
    }

}
