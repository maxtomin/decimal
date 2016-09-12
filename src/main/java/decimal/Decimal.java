package decimal;

import java.text.ParseException;

/**
 * Reference implementation of {@link AbstractDecimal} for maximum supported precision (9 dp).
 * Values from -9223372036.854775807 to 9223372036.854775807 (inclusive), which should be good enough for small numbers.
 */
public class Decimal extends AbstractDecimal<Decimal> {
    /**
     * Must not be changed!
      */
    public static final Decimal MIN_VALUE = new Decimal().setRaw(-Long.MAX_VALUE);
    /**
     * Must not be changed!
     */
    public static final Decimal MAX_VALUE = new Decimal().setRaw(Long.MAX_VALUE);

    @Override
    protected int getScale() {
        return 9; // must be constant
    }

    public static Decimal create(double value) {
        return new Decimal().fromDoubleRD(value);
    }

    public static Decimal create(String value) throws ParseException {
        return new Decimal().parse(value);
    }

    public static Decimal create(long value) {
        return new Decimal().fromLong(value);
    }


    public static void main(String[] args) {
        System.out.println(Decimal.MAX_VALUE);

    }
}
