package decimal.sample;

import decimal.AbstractDecimal;

import java.text.ParseException;

public class Quantity2 extends AbstractDecimal<Quantity2> {
    @Override
    protected int getScale() {
        return 2;
    }

    public static Quantity2 create(String value) throws ParseException {
        return new Quantity2().parse(value);
    }

    public static Quantity2 create(long value) throws ParseException {
        return new Quantity2().fromDoubleRD(value);
    }
}