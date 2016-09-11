package org.maxtomin.decimal.sample;

import org.maxtomin.decimal.AbstractDecimal;

import java.text.ParseException;

public class Price extends AbstractDecimal<Price> {
    @Override
    protected int getScale() {
        return 8;
    }

    public static Price create(String s) throws ParseException {
        return new Price().parse(s);
    }

    public static Price create(double value) {
        return new Price().fromDoubleRD(value);
    }
}
