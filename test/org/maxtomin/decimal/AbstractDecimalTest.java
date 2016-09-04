package org.maxtomin.decimal;

import org.junit.Test;

import java.math.RoundingMode;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.maxtomin.decimal.AbstractDecimal.*;

public class AbstractDecimalTest {
    private final TestDecimal quantity = new TestDecimal(2);
    private final TestDecimal price = new TestDecimal(8);

    @Test
    public void testNaN() throws Exception {
        assertEquals(NaN, quantity.setRaw(NaN).getRaw());
        assertEquals(true, quantity.isNaN());
        try {
            quantity.checkNotNaN();
            fail("Exception expected");
        } catch (ArithmeticException e) {
        }
    }

    @Test
    public void testNegate() throws Exception {
        assertEquals(-123, quantity.setRaw(123).negate().getRaw());
        assertEquals(123, quantity.negate().getRaw());
        assertEquals(-123, quantity.negate().getRaw());
        assertEquals(NaN, quantity.setRaw(NaN).negate().getRaw());
        assertEquals(0, quantity.setRaw(0).negate().getRaw());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("123", new TestDecimal(0).setRaw(123).toString());
        assertEquals("12.3", new TestDecimal(1).setRaw(123).toString());
        assertEquals("1.23", new TestDecimal(2).setRaw(123).toString());
        assertEquals("0.123", new TestDecimal(3).setRaw(123).toString());
        assertEquals("0.0123", new TestDecimal(4).setRaw(123).toString());

        assertEquals("-123", new TestDecimal(0).setRaw(-123).toString());
        assertEquals("-12.3", new TestDecimal(1).setRaw(-123).toString());
        assertEquals("-1.23", new TestDecimal(2).setRaw(-123).toString());
        assertEquals("-0.123", new TestDecimal(3).setRaw(-123).toString());
        assertEquals("-0.0123", new TestDecimal(4).setRaw(-123).toString());

        assertEquals("0", new TestDecimal(0).setRaw(0).toString());
        assertEquals("0.0", new TestDecimal(1).setRaw(0).toString());
        assertEquals("0.00", new TestDecimal(2).setRaw(0).toString());
        assertEquals("1.00", new TestDecimal(2).setRaw(100).toString());
    }

    @Test
    public void testParse() throws Exception {
        assertEquals(123, new TestDecimal(0).parse("123").getRaw());
        assertEquals(123, new TestDecimal(1).parse("12.3").getRaw());
        assertEquals(123, new TestDecimal(2).parse("1.23").getRaw());
        assertEquals(123, new TestDecimal(3).parse("0.123").getRaw());
        assertEquals(123, new TestDecimal(4).parse("0.0123").getRaw());

        assertEquals(1230, new TestDecimal(4).parse("0.123").getRaw());
        assertEquals(12300, new TestDecimal(4).parse("1.23").getRaw());
        assertEquals(123000, new TestDecimal(4).parse("12.3").getRaw());
        assertEquals(1230000, new TestDecimal(4).parse("123").getRaw());

        assertEquals(-1230000, new TestDecimal(4).parse("-123").getRaw());
        assertEquals(0, new TestDecimal(4).parse("0").getRaw());

        assertEquals(Long.MAX_VALUE, new TestDecimal(0).parse("9223372036854775807").getRaw());
        assertEquals(-Long.MAX_VALUE, new TestDecimal(0).parse("-9223372036854775807").getRaw());
        assertEquals(NaN, new TestDecimal(0).parse("NaN").getRaw());
        assertEquals(NaN, new TestDecimal(0).parse("nAn").getRaw());

        assertExceptionWhileParsing("9223372036854775808");
        assertExceptionWhileParsing("9223372036854775809");
        assertExceptionWhileParsing("9223372036854775810");
        assertExceptionWhileParsing("92233720368547758100000000");
        assertExceptionWhileParsing("-9223372036854775808"); // must be represented as 'NaN'
        assertExceptionWhileParsing("-9223372036854775809");
        assertExceptionWhileParsing("-9223372036854775810");
        assertExceptionWhileParsing("-92233720368547758100000000");
        assertExceptionWhileParsing("1.23.34");
        assertExceptionWhileParsing("rubbish");

        try {
            new TestDecimal(1).parse("9223372036854775807");
            fail("Exception expected");
        } catch (ParseException e) {
        }
    }

    @Test
    public void testPlus() throws Exception {
        assertEquals("125.00", quantity().plus(quantity("123"), quantity("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00", quantity().plus(quantity("123"), quantity("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00", quantity().plus(quantity("-123"), quantity("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00", quantity().plus(quantity("-123"), quantity("-2"), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00000000", price().plus(quantity("123"), quantity("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00000000", price().plus(quantity("123"), quantity("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00000000", price().plus(quantity("-123"), quantity("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00000000", price().plus(quantity("-123"), quantity("-2"), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00", quantity().plus(price("123"), price("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00", quantity().plus(price("123"), price("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00", quantity().plus(price("-123"), price("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00", quantity().plus(price("-123"), price("-2"), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00", quantity().plus(123, 2).toString());
        assertEquals("121.00", quantity().plus(123, -2).toString());
        assertEquals("-121.00", quantity().plus(-123, 2).toString());
        assertEquals("-125.00", quantity().plus(-123, -2).toString());

        // Addition overflows:  2 * 92233720368.54775807 = 184467440737.09551614
        assertEquals("184467440737.10", quantity().plus(price("92233720368.54775807"), price("92233720368.54775807"), RoundingMode.UP).toString());
        assertEquals("0.00", quantity().plus(price("92233720368.54775807"), price("-92233720368.54775807"), RoundingMode.UNNECESSARY).toString());
        assertEquals("0.00", quantity().plus(price("-92233720368.54775807"), price("92233720368.54775807"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-184467440737.10", quantity().plus(price("-92233720368.54775807"), price("-92233720368.54775807"), RoundingMode.UP).toString());

        // Scaling overflows (can not be prevented)
        assertEquals("NaN", price().plus(quantity("92233720368547758.07"), quantity("92233720368547758.07"), RoundingMode.UP).toString());
        assertEquals("0.00000000", price().plus(quantity("92233720368547758.07"), quantity("-92233720368547758.07"), RoundingMode.UP).toString());
        assertEquals("0.00000000", price().plus(quantity("-92233720368547758.07"), quantity("92233720368547758.07"), RoundingMode.UP).toString());
        assertEquals("NaN", price().plus(quantity("-92233720368547758.07"), quantity("-92233720368547758.07"), RoundingMode.UP).toString());

        // Addition AFTER Scaling overflows (can not be prevented)
        assertEquals("92233720368.54000000", price().plus(quantity("92233720368.54"), quantity("0"), RoundingMode.UP).toString());
        assertEquals("92233720368.54000000", price().plus(quantity("0"), quantity("92233720368.54"), RoundingMode.UP).toString());
        assertEquals("NaN", price().plus(quantity("92233720368.54"), quantity("92233720368.54"), RoundingMode.UP).toString());
        assertEquals("0.00000000", price().plus(quantity("92233720368.54"), quantity("-92233720368.54"), RoundingMode.UP).toString());
        assertEquals("0.00000000", price().plus(quantity("-92233720368.54"), quantity("92233720368.54"), RoundingMode.UP).toString());
        assertEquals("NaN", price().plus(quantity("-92233720368.54"), quantity("-92233720368.54"), RoundingMode.UP).toString());

        // "add" of the same type is the same as "plus"
        assertEquals("125.00", quantity("123").add(quantity("2")).toString());
        assertEquals("121.00", quantity("123").add(quantity("-2")).toString());
        assertEquals("-121.00", quantity("-123").add(quantity("2")).toString());
        assertEquals("-125.00", quantity("-123").add(quantity("-2")).toString());
    }

    private TestDecimal quantity() throws ParseException {
        return new TestDecimal(quantity.getScale());
    }

    private TestDecimal quantity(String value) throws ParseException {
        return quantity().parse(value);
    }

    private TestDecimal price() {
        return new TestDecimal(price.getScale());
    }

    private TestDecimal price(String value) throws ParseException {
        return price().parse(value);
    }

    private void assertExceptionWhileParsing(String s) {
        try {
            new TestDecimal(0).parse(s);
            fail("Exception expected");
        } catch (ParseException e) {
        }
    }
}
