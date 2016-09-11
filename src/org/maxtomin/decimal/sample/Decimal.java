package org.maxtomin.decimal.sample;

import org.maxtomin.decimal.AbstractDecimal;

public class Decimal extends AbstractDecimal<Decimal> {
    @Override
    protected int getScale() {
        return 9;
    }

    public static Decimal create(double value) {
        return new Decimal().fromDoubleRD(value);
    }
}
