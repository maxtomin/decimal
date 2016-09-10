package org.maxtomin.decimal;

import java.math.RoundingMode;
import java.text.ParseException;

/**
 * Fixed point decimal, represented as a long mantissa and integer implied decimal points (dp) from 0 to 9, which is constant
 * for a concrete class (instance of same class must always have the same scale). Multiple subclases with different dps
 * can be created, e.g. Quantity with 2 dp and Price with 8 dp.
 * Supports basic arithmetic operations with full control of overflow and rounding.
 * Non-allocating (unless explicitly specified).
 * <p>
 * Special value {@link #NaN} is used to represent an invalid operation, including<ul>
 *     <li>Overflow</li>
 *     <li>Unexpected rounding (in particular, underflow)</li>
 *     <li>Division by zero</li>
 * </ul>
 * Any operations involving {@link #NaN} return {@link #NaN}.
 * <p>
 * All operations can involve at most 2 different scales as arguments and result, they can be divided into:<ul>
 *     <li>Unary operations, taking a single argument and modifying this object, a.g. a.add(5)</li>
 *     <li>Binary operations, taking a 2 argument of the <b>same</b> scale and putting result to this object, a.g. a.plus(3, 4)</li>
 * </ul>
 * A long value can be used instead of a Decimal argument with 0 dp.
 * <p>
 *
 * @param <T>
 */
public abstract class AbstractDecimal<T extends AbstractDecimal> extends BaseDecimal implements Comparable<T>, Cloneable {
    public static final long NaN = Long.MIN_VALUE;

    /**
     * Implied decimal points, must be constant for the class, must be between 0 and 9.
     */
    protected abstract int getScale();

    /**
     * @return type-casted this object
     */
    @SuppressWarnings("unchecked")
    public T self() {
        return (T) this;
    }

    /**
     * Raw long value without decimal points. Can be from {@link -Long#MAX_VALUE} to {@link Long#MAX_VALUE} with the
     * {@link Long#MIN_VALUE} reserved for NaN
     */
    public long getRaw() {
        return a;
    }

    /**
     * Raw long value without decimal points
     */
    public T setRaw(long raw) {
        a = raw;
        return self();
    }

    /**
     * true if the value is NaN.
     * All arithmetic operations with NaN returns NaN.
     */
    public boolean isNaN() {
        return getRaw() == NaN;
    }

    /**
     * Throws an exception (allocating) if the value is NaN
     */
    public T checkNotNaN() {
        if (isNaN()) {
            throw new ArithmeticException("Last operation was invalid (overflow or division by zero)");
        }
        return self();
    }

    /**
     * Change the sign of the number.
     */
    public T negate() {
        return !isNaN() ? setRaw(-getRaw()) : self();
    }

    /**
     * Adds 2 numbers of the same scale and puts result to this
     * Rounding is required if the arguments' scale is greater than this scale.
     */
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

    /**
     * Adds 2 longs and puts result to this
     * No rounding required
     */
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

    /**
     * Adds a number to this.
     * Rounding is required if the argument scale is greater than this scale.
     */
    public <V extends AbstractDecimal> T add(V a, RoundingMode roundingMode) {
        int scale = getScale() - a.getScale();
        if (scale < 0 && !isNaN() && !a.isNaN()) {
            long self = getRaw();
            long other = downScale_63_31(a.getRaw(), -scale);
            long remainder = getRaw();

            // have to inline addWithOverflow here to avoid extra if
            long result = self + other;
            if (self == NaN || other == NaN || (result < 0) != (self < 0) && (result < 0) != (other < 0)) {
                return setRaw(NaN);
            }

            int denominator = POW10[-scale];
            if (result < 0 && remainder > 0) {
                remainder -= denominator;
                ++result;
            } else if (result > 0 && remainder < 0) {
                remainder += denominator;
                --result;
            }

            return setRaw(round(result, remainder, denominator, roundingMode));
        }

        return add(a.getRaw(), scale);
    }

    /**
     * Adds a number of the same scale to this.
     * No rounding required.
     */
    public T add(T a) {
        return plus(this, a, RoundingMode.DOWN);
    }

    /**
     * Adds a long number to this.
     * No rounding required.
     */
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

    /**
     * Subtract 2 numbers of the same scale and puts result to this
     * Rounding is required if the arguments' scale is greater than this scale.
     */
    public <V extends AbstractDecimal> T minus(V a, V b, RoundingMode roundingMode) {
        b.negate();
        plus(a, b, roundingMode);
        b.negate();
        return self();
    }

    /**
     * Subtracts 2 longs and puts result to this
     * No rounding required
     */
    public T minus(long a, long b) {
        return plus(a, -b);
    }

    /**
     * Subtract a number from this.
     * Rounding is required if the argument scale is greater than this scale.
     */
    public <V extends AbstractDecimal> T subtract(V a, RoundingMode roundingMode) {
        a.negate();
        add(a, roundingMode);
        a.negate();
        return self();
    }

    /**
     * Subtract a number of the same scale from this.
     * No rounding required.
     */
    public T subtract(T a) {
        a.negate();
        plus(this, a, RoundingMode.DOWN);
        a.negate();
        return self();
    }

    /**
     * Subtract a long from this.
     * No rounding required.
     */
    public T subtract(long a) {
        return add(-a);
    }

    /**
     * Multiply 2 numbers of the same scale and put the result to this.
     * Rounding is required if the arguments scale combined is greater than this scale.
     */
    public <V extends AbstractDecimal> T product(V a, V b, RoundingMode roundingMode) {
        if (a.getScale() != b.getScale()) {
            throw new IllegalArgumentException("Scales must be the same");
        }

        int scale = a.getScale() + b.getScale() - getScale();
        if (scale >= 0) {
            return setRaw(mulScaleRound(a.getRaw(), b.getRaw(), scale, roundingMode));
        } else {
            return setRaw(scaleWithOverflow(mulWithOverflow(a.getRaw(), b.getRaw()), -scale));
        }
    }

    /**
     * Multiply 2 longs put the result to this.
     * Rounding is not required.
     */
    public T product(long a, long b) {
        return setRaw(scaleWithOverflow(mulWithOverflow(a, b), getScale()));
    }

    /**
     * Multiply this by the argument.
     * Rounding is required if argument scale is not zero.
     */
    public <V extends AbstractDecimal> T mul(V a, RoundingMode roundingMode) {
        return setRaw(mulScaleRound(getRaw(), a.getRaw(), a.getScale(), roundingMode));
    }

    /**
     * Multiply this by the argument.
     * Rounding is not required.
     */
    public T mul(long a) {
        return setRaw(mulWithOverflow(getRaw(), a));
    }

    /**
     * Divide first argument bye second and the result to this.
     * Rounding is always required.
     * Return {@link #NaN} if b is zero.
     */
    public <V extends AbstractDecimal> T quotient(V a, V b, RoundingMode roundingMode) {
        if (a.getScale() != b.getScale()) {
            throw new IllegalArgumentException("Scales must be the same");
        }
        return quotient(a.getRaw(), b.getRaw(), roundingMode);
    }

    /**
     * Divide first argument bye second and the result to this.
     * Rounding is always required.
     * Return {@link #NaN} if b is zero.
     */
    public T quotient(long a, long b, RoundingMode roundingMode) {
        return setRaw(scaleDivRound(a, getScale(), b, roundingMode));
    }

    /**
     * Divide this by the argument and put result into this.
     * Rounding is always required.
     * Return {@link #NaN} if a is zero.
     */
    public <V extends AbstractDecimal> T div(V a, RoundingMode roundingMode) {
        return setRaw(scaleDivRound(getRaw(), a.getScale(), a.getRaw(), roundingMode));
    }

    /**
     * Divide this by the argument and put result into this.
     * Rounding is always required.
     * Return {@link #NaN} if a is zero.
     */
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

    /**
     * Created a copy of the class with the same raw number.
     */
    @SuppressWarnings("unchecked")
    @Override
    public T clone() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Unexpected", e);
        }
    }

    /**
     * Compares 2 values considering the scale ("0.123" < "12.0" although its not true for raw values)
     * {@link #NaN} is smaller than any other number irrespective of scale. Two {@link #NaN}s are equal to each other.
     */
    @Override
    public int compareTo(AbstractDecimal o) {
        if (isNaN()) {
            return o.isNaN() ? 0 : -1;
        } else if (o.isNaN()) {
            return isNaN() ? 0 : 1;
        }

        long saved = getRaw();

        int scale = getScale() - o.getScale();
        int result;
        if (scale >= 0) {
            long first = downScale_63_31(getRaw(), scale);
            result = first > o.getRaw() ? 1 :
                    first < o.getRaw() ? -1 :
                            (int) getRaw(); // remainder matters
        } else {
            long first = getRaw();
            long second = downScale_63_31(o.getRaw(), -scale);
            result = first > second ? 1 :
                    first < second ? -1 :
                            (int) -getRaw(); // remainder matters
        }

        setRaw(saved);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte byteValue() {
        return (byte) toLong(RoundingMode.DOWN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short shortValue() {
        return (short) toLong(RoundingMode.DOWN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int intValue() {
        return (int) toLong(RoundingMode.DOWN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        return toLong(RoundingMode.DOWN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float floatValue() {
        return (float) toDouble();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValue() {
        return toDouble();
    }

    /**
     * Returns the whole part of the value, throws exception if {@link #NaN}
     */
    public long toLong(RoundingMode roundingMode) {
        if (isNaN()) {
            throw new ArithmeticException("NaN");
        }


        long raw = getRaw(); // will be overridden by remainder

        int scale = getScale();
        long result = round(downScale_63_31(raw, scale), getRaw(), POW10[scale], roundingMode);

        setRaw(raw);

        return result;
    }

    /**
     * Converts long to Decimal value. Can result in {@link #NaN} (if overflow)
     */
    public T fromLong(long value) {
        return setRaw(scaleWithOverflow(value, getScale()));
    }

    /**
     * Converts Decimal to floating-point number, returns {@link Double#NaN} if {@link #NaN}
     */
    public double toDouble() {
        return !isNaN() ? (double) getRaw() / POW10[getScale()] : Double.NaN;
    }

    /**
     * Converts doublie to Decimal value with the following rounding modes supported:<ul>
     *     <li>DOWN - simple cast</li>
     *     <li>FLOOR - uses {@link Math#floor}</li>
     *     <li>CEILING - uses {@link Math#ceil}</li>
     *     <li>HALF_EVEN - uses {@link Math#rint}</li>
     * </ul>
     */
    public T fromDouble(double value, RoundingMode roundingMode) {
        value *= POW10[getScale()];
        switch (roundingMode) {
            case DOWN:
                return setIntDouble(value);
            case FLOOR:
                return setIntDouble(Math.floor(value));
            case CEILING:
                return setIntDouble(Math.ceil(value));
            case HALF_EVEN:
                return setIntDouble(Math.rint(value));
            default:
                throw new IllegalArgumentException("Unsupported rounding mode for double: " + roundingMode);
        }
    }

    private T setIntDouble(double value) {
        return setRaw(value >= -Long.MAX_VALUE && value <= Long.MAX_VALUE ? (long) value : NaN);
    }

    /**
     * Converts to ASCII string. Allocating.
     * @see #toStringBuilder
     */
    public String toString() {
        return toStringBuilder(new StringBuilder(21)).toString();
    }

    /**
     * Converts to ASCII string. Shows number of dps as well, e.g. "1.00"
     * NaN values are displayed as "NaN"
     */
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

    /**
     * Parse a string (including NaN) and creates a value from it.
     * Unlike other methods, does NOT use NaN to indicate an error, uses ParseException instead.
     */
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
                while (length > dotPos && charSequence.charAt(length - 1) == '0') {
                    length--;
                }
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
            return mulScaleRound(a, b, 0, RoundingMode.DOWN);
        }
    }

}
