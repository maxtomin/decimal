package decimal.sample;

import decimal.AbstractDecimal;

import java.text.ParseException;

public class Quantity extends AbstractDecimal<Quantity> {
    @Override
    protected int getScale() {
        return 2;
    }

    public static Quantity create(String value) throws ParseException {
        return new Quantity().parse(value);
    }

    public static Quantity create(long value) throws ParseException {
        return new Quantity().fromDoubleRD(value);
    }
}