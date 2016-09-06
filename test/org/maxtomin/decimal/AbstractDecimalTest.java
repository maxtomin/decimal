package org.maxtomin.decimal;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.maxtomin.decimal.AbstractDecimal.NaN;

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

    @Test
    public void testAdd() throws Exception {
        assertEquals("125.00", quantity("123").add(quantity("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00", quantity("123").add(quantity("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00", quantity("-123").add(quantity("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00", quantity("-123").add(quantity("-2"), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00000000", price("123").add(quantity("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00000000", price("123").add(quantity("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00000000", price("-123").add(quantity("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00000000", price("-123").add(quantity("-2"), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00", quantity("123").add(price("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00", quantity("123").add(price("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00", quantity("-123").add(price("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00", quantity("-123").add(price("-2"), RoundingMode.UNNECESSARY).toString());

        assertEquals("123.01", quantity("123").add(price("0.00000001"), RoundingMode.UP).toString());
        assertEquals("123.00", quantity("123").add(price("-0.00000001"), RoundingMode.UP).toString());
        assertEquals("-123.00", quantity("-123").add(price("0.00000001"), RoundingMode.UP).toString());
        assertEquals("-123.01", quantity("-123").add(price("-0.00000001"), RoundingMode.UP).toString());

        assertEquals("125.00000000", price("123").add(2).toString());
        assertEquals("121.00000000", price("123").add(-2).toString());
        assertEquals("-121.00000000", price("-123").add(2).toString());
        assertEquals("-125.00000000", price("-123").add(-2).toString());

        // overflow while down-scaling
        assertEquals("NaN", quantity("92233720368547758.07").add(price("0.01"), RoundingMode.UNNECESSARY).toString());
        assertEquals("92233720368547758.06", quantity("92233720368547758.07").add(price("-0.01"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-92233720368547758.06", quantity("-92233720368547758.07").add(price("0.01"), RoundingMode.UNNECESSARY).toString());
        assertEquals("NaN", quantity("-92233720368547758.07").add(price("-0.01"), RoundingMode.UNNECESSARY).toString());

        // overflow while rounding
        assertEquals("NaN", quantity("92233720368547758.07").add(price("0.001"), RoundingMode.UP).toString());
        assertEquals("NaN", quantity("-92233720368547758.07").add(price("-0.001"), RoundingMode.UP).toString());
        assertEquals("92233720368547758.07", quantity("92233720368547758.07").add(price("0.001"), RoundingMode.DOWN).toString());
        assertEquals("-92233720368547758.07", quantity("-92233720368547758.07").add(price("-0.001"), RoundingMode.DOWN).toString());

        // overflow while adding after up-scale
        assertEquals("NaN", price("92233720368.54775807").add(1).toString());
        assertEquals("92233720367.54775807", price("92233720368.54775807").add(-1).toString());
        assertEquals("-92233720367.54775807", price("-92233720368.54775807").add(1).toString());
        assertEquals("NaN", price("-92233720368.54775807").add(-1).toString());

        // overflow while up-scaling (not always NaN)
        assertEquals("NaN", price("92233720368.54775807").add(100000000000L).toString());
        assertEquals("-7766279631.45224193", price("92233720368.54775807").add(-100000000000L).toString());
        assertEquals("7766279631.45224193", price("-92233720368.54775807").add(100000000000L).toString());
        assertEquals("NaN", price("-92233720368.54775807").add(-100000000000L).toString());
    }

    @Test
    public void testMinus() throws Exception {
        assertEquals("125.00", quantity().minus(quantity("123"), quantity("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00", quantity().minus(quantity("123"), quantity("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00", quantity().minus(quantity("-123"), quantity("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00", quantity().minus(quantity("-123"), quantity("2"), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00000000", price().minus(quantity("123"), quantity("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00000000", price().minus(quantity("123"), quantity("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00000000", price().minus(quantity("-123"), quantity("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00000000", price().minus(quantity("-123"), quantity("2"), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00", quantity().minus(price("123"), price("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00", quantity().minus(price("123"), price("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00", quantity().minus(price("-123"), price("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00", quantity().minus(price("-123"), price("2"), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00", quantity().minus(123, -2).toString());
        assertEquals("121.00", quantity().minus(123, 2).toString());
        assertEquals("-121.00", quantity().minus(-123, -2).toString());
        assertEquals("-125.00", quantity().minus(-123, 2).toString());

        // Addition overflows:  2 * 92233720368.54775807 = 184467440737.09551614
        assertEquals("184467440737.10", quantity().minus(price("92233720368.54775807"), price("-92233720368.54775807"), RoundingMode.UP).toString());
        assertEquals("0.00", quantity().minus(price("92233720368.54775807"), price("92233720368.54775807"), RoundingMode.UNNECESSARY).toString());
        assertEquals("0.00", quantity().minus(price("-92233720368.54775807"), price("-92233720368.54775807"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-184467440737.10", quantity().minus(price("-92233720368.54775807"), price("92233720368.54775807"), RoundingMode.UP).toString());

        // Scaling overflows (can not be prevented)
        assertEquals("NaN", price().minus(quantity("92233720368547758.07"), quantity("-92233720368547758.07"), RoundingMode.UP).toString());
        assertEquals("0.00000000", price().minus(quantity("92233720368547758.07"), quantity("92233720368547758.07"), RoundingMode.UP).toString());
        assertEquals("0.00000000", price().minus(quantity("-92233720368547758.07"), quantity("-92233720368547758.07"), RoundingMode.UP).toString());
        assertEquals("NaN", price().minus(quantity("-92233720368547758.07"), quantity("92233720368547758.07"), RoundingMode.UP).toString());

        // Addition AFTER Scaling overflows (can not be prevented)
        assertEquals("92233720368.54000000", price().minus(quantity("92233720368.54"), quantity("0"), RoundingMode.UP).toString());
        assertEquals("92233720368.54000000", price().minus(quantity("0"), quantity("-92233720368.54"), RoundingMode.UP).toString());
        assertEquals("NaN", price().minus(quantity("92233720368.54"), quantity("-92233720368.54"), RoundingMode.UP).toString());
        assertEquals("0.00000000", price().minus(quantity("92233720368.54"), quantity("92233720368.54"), RoundingMode.UP).toString());
        assertEquals("0.00000000", price().minus(quantity("-92233720368.54"), quantity("-92233720368.54"), RoundingMode.UP).toString());
        assertEquals("NaN", price().minus(quantity("-92233720368.54"), quantity("92233720368.54"), RoundingMode.UP).toString());

        // "subtract" of the same type is the same as "minus"
        assertEquals("125.00", quantity("123").subtract(quantity("-2")).toString());
        assertEquals("121.00", quantity("123").subtract(quantity("2")).toString());
        assertEquals("-121.00", quantity("-123").subtract(quantity("-2")).toString());
        assertEquals("-125.00", quantity("-123").subtract(quantity("2")).toString());
    }

    @Test
    public void testSubtract() throws Exception {
        assertEquals("125.00", quantity("123").subtract(quantity("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00", quantity("123").subtract(quantity("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00", quantity("-123").subtract(quantity("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00", quantity("-123").subtract(quantity("2"), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00000000", price("123").subtract(quantity("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00000000", price("123").subtract(quantity("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00000000", price("-123").subtract(quantity("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00000000", price("-123").subtract(quantity("2"), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00", quantity("123").subtract(price("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00", quantity("123").subtract(price("2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00", quantity("-123").subtract(price("-2"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00", quantity("-123").subtract(price("2"), RoundingMode.UNNECESSARY).toString());

        assertEquals("123.01", quantity("123.").subtract(price("-0.00000001"), RoundingMode.UP).toString());
        assertEquals("123.00", quantity("123").subtract(price("0.00000001"), RoundingMode.UP).toString());
        assertEquals("-123.00", quantity("-123").subtract(price("-0.00000001"), RoundingMode.UP).toString());
        assertEquals("-123.01", quantity("-123").subtract(price("0.00000001"), RoundingMode.UP).toString());

        assertEquals("125.00000000", price("123").subtract(-2).toString());
        assertEquals("121.00000000", price("123").subtract(2).toString());
        assertEquals("-121.00000000", price("-123").subtract(-2).toString());
        assertEquals("-125.00000000", price("-123").subtract(2).toString());

        // overflow while down-scaling
        assertEquals("NaN", quantity("92233720368547758.07").subtract(price("-0.01"), RoundingMode.UNNECESSARY).toString());
        assertEquals("92233720368547758.06", quantity("92233720368547758.07").subtract(price("0.01"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-92233720368547758.06", quantity("-92233720368547758.07").subtract(price("-0.01"), RoundingMode.UNNECESSARY).toString());
        assertEquals("NaN", quantity("-92233720368547758.07").subtract(price("0.01"), RoundingMode.UNNECESSARY).toString());

        // overflow while rounding
        assertEquals("NaN", quantity("92233720368547758.07").subtract(price("-0.001"), RoundingMode.UP).toString());
        assertEquals("NaN", quantity("-92233720368547758.07").subtract(price("0.001"), RoundingMode.UP).toString());
        assertEquals("92233720368547758.07", quantity("92233720368547758.07").subtract(price("-0.001"), RoundingMode.DOWN).toString());
        assertEquals("-92233720368547758.07", quantity("-92233720368547758.07").subtract(price("0.001"), RoundingMode.DOWN).toString());

        // overflow while subtracting after up-scale
        assertEquals("NaN", price("92233720368.54775807").subtract(-1).toString());
        assertEquals("92233720367.54775807", price("92233720368.54775807").subtract(1).toString());
        assertEquals("-92233720367.54775807", price("-92233720368.54775807").subtract(-1).toString());
        assertEquals("NaN", price("-92233720368.54775807").subtract(1).toString());

        // overflow while up-scaling (not always NaN)
        assertEquals("NaN", price("92233720368.54775807").subtract(-100000000000L).toString());
        assertEquals("-7766279631.45224193", price("92233720368.54775807").subtract(100000000000L).toString());
        assertEquals("7766279631.45224193", price("-92233720368.54775807").subtract(-100000000000L).toString());
        assertEquals("NaN", price("-92233720368.54775807").subtract(100000000000L).toString());

        assertEquals("0", new TestDecimal(0).parse("1").add(new TestDecimal(1).parse("-0.1"), RoundingMode.DOWN).toString());
    }

    @Test
    public void testProduct() throws Exception {
        assertEquals("1230.00", quantity.product(quantity("123"), quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00", quantity.product(quantity("123"), quantity("-10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00", quantity.product(quantity("-123"), quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("1230.00", quantity.product(quantity("-123"), quantity("-10"), RoundingMode.UNNECESSARY).toString());

        assertEquals("1230.00", quantity.product(price("123"), price("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00", quantity.product(price("123"), price("-10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00", quantity.product(price("-123"), price("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("1230.00", quantity.product(price("-123"), price("-10"), RoundingMode.UNNECESSARY).toString());

        assertEquals("1230.00000000", price.product(quantity("123"), quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00000000", price.product(quantity("123"), quantity("-10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00000000", price.product(quantity("-123"), quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("1230.00000000", price.product(quantity("-123"), quantity("-10"), RoundingMode.UNNECESSARY).toString());

        assertEquals("1230.01", quantity.product(price("123"), price("10.00000001"), RoundingMode.UP).toString());
        assertEquals("-1230.01", quantity.product(price("123"), price("-10.00000001"), RoundingMode.UP).toString());
        assertEquals("-1230.01", quantity.product(price("-123"), price("10.00000001"), RoundingMode.UP).toString());
        assertEquals("1230.01", quantity.product(price("-123"), price("-10.00000001"), RoundingMode.UP).toString());

        assertEquals("1230.00000000", price.product(123, 10).toString());
        assertEquals("-1230.00000000", price.product(123, -10).toString());
        assertEquals("-1230.00000000", price.product(-123, 10).toString());
        assertEquals("1230.00000000", price.product(-123, -10).toString());

        assertEquals("NaN", quantity.product(price("1000000000.00000000"), price("1000000000.00000000"), RoundingMode.UNNECESSARY).toString());
        assertEquals("NaN", quantity.product(1000000000L, 1000000000L).toString());
        assertEquals("10000000000000000.00", quantity.product(price("100000000.00000000"), price("100000000.00000000"), RoundingMode.UNNECESSARY).toString());
        assertEquals("10000000000000000.00", quantity.product(100000000, 100000000).toString());
        assertEquals("10000000000000000.00", quantity.product(10000000000L, 1000000).toString());
        assertEquals("10000000000000000.00", quantity.product(1000000, 10000000000L).toString());
        assertEquals("NaN", quantity.product(price("NaN"), price("100000000.00000000"), RoundingMode.UNNECESSARY).toString());
        assertEquals("NaN", quantity.product(price("100000000.00000000"), price("NaN"), RoundingMode.UNNECESSARY).toString());
    }

    @Test
    public void testMul() throws Exception {
        assertEquals("1230.00", quantity("123").mul(price("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00", quantity("123").mul(price("-10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00", quantity("-123").mul(price("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("1230.00", quantity("-123").mul(price("-10"), RoundingMode.UNNECESSARY).toString());

        assertEquals("1230.00000000", price("123").mul(quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00000000", price("123").mul(quantity("-10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00000000", price("-123").mul(quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("1230.00000000", price("-123").mul(quantity("-10"), RoundingMode.UNNECESSARY).toString());

        assertEquals("1230.00", quantity("123").mul(10).toString());
        assertEquals("-1230.00", quantity("123").mul(-10).toString());
        assertEquals("-1230.00", quantity("-123").mul(10).toString());
        assertEquals("1230.00", quantity("-123").mul(-10).toString());

        assertEquals("NaN", price("NaN").mul(quantity("10"), RoundingMode.DOWN).toString());
        assertEquals("NaN", price("123").mul(quantity("NaN"), RoundingMode.DOWN).toString());

        assertEquals("0.11", quantity("0.33").mul(quantity("0.33"), RoundingMode.UP).toString());
        assertEquals("0.10", quantity("0.33").mul(quantity("0.33"), RoundingMode.DOWN).toString());
    }

    @Test
    public void testQuotient() throws Exception {
        assertEquals("123.00", quantity.quotient(quantity("1230"), quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", quantity.quotient(quantity("1230"), quantity("-10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", quantity.quotient(quantity("-1230"), quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("123.00", quantity.quotient(quantity("-1230"), quantity("-10"), RoundingMode.UNNECESSARY).toString());

        assertEquals("123.00", quantity.quotient(1230, 10, RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", quantity.quotient(1230, -10, RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", quantity.quotient(-1230, 10, RoundingMode.UNNECESSARY).toString());
        assertEquals("123.00", quantity.quotient(-1230, -10, RoundingMode.UNNECESSARY).toString());

        assertEquals("NaN", quantity.quotient(quantity("10000000000000000.00"), quantity("0.01"), RoundingMode.DOWN).toString());
        assertEquals("NaN", quantity.quotient(quantity("NaN"), quantity("10"), RoundingMode.DOWN).toString());
        assertEquals("NaN", quantity.quotient(quantity("1230"), quantity("NaN"), RoundingMode.DOWN).toString());
    }

    @Test
    public void testDiv() throws Exception {
        assertEquals("123.00", quantity("1230").div(quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", quantity("1230").div(quantity("-10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", quantity("-1230").div(quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("123.00", quantity("-1230").div(quantity("-10"), RoundingMode.UNNECESSARY).toString());

        assertEquals("123.00", quantity("1230").div(10, RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", quantity("1230").div(-10, RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", quantity("-1230").div(10, RoundingMode.UNNECESSARY).toString());
        assertEquals("123.00", quantity("-1230").div(-10, RoundingMode.UNNECESSARY).toString());

        assertEquals("NaN", quantity("10000000000000000.00").div(quantity("0.01"), RoundingMode.UNNECESSARY).toString());
        assertEquals("NaN", quantity("NaN").div(quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("NaN", quantity("1230").div(quantity("NaN"), RoundingMode.UNNECESSARY).toString());
    }

    @Test
    public void powersOf2() throws Exception {
        for (int scale1 = 0; scale1 <= 9; ++scale1) {
            System.out.println(scale1);
            for (int scale2 = 0; scale2 <= 9; ++scale2) {
                for (int power1 = 0; power1 < 63; ++power1) {
                    for (int power2 = 0; power2 < 63; ++power2) {
                        comboTest(new TestDecimal(scale1).setRaw(1L << power1), new TestDecimal(scale2).setRaw(1L << power2));
                        comboTest(new TestDecimal(scale1).setRaw(-1L << power1), new TestDecimal(scale2).setRaw(1L << power2));
                        comboTest(new TestDecimal(scale1).setRaw(1L << power1), new TestDecimal(scale2).setRaw(-1L << power2));
                        comboTest(new TestDecimal(scale1).setRaw(-1L << power1), new TestDecimal(scale2).setRaw(-1L << power2));
                    }
                }
            }
        }
    }

    @Test
    public void powersOf10() throws Exception {
        for (int scale1 = 0; scale1 <= 9; ++scale1) {
            System.out.println(scale1);
            for (int scale2 = 0; scale2 <= 9; ++scale2) {
                for (int power1 = 0; power1 < 19; ++power1) {
                    for (int power2 = 0; power2 < 19; ++power2) {
                        long value1 = (long) Math.pow(10, power1);
                        long value2 = (long) Math.pow(10, power2);
                        comboTest(new TestDecimal(scale1).setRaw(value1), new TestDecimal(scale2).setRaw(value2));
                        comboTest(new TestDecimal(scale1).setRaw(-value1), new TestDecimal(scale2).setRaw(value2));
                        comboTest(new TestDecimal(scale1).setRaw(value1), new TestDecimal(scale2).setRaw(-value2));
                        comboTest(new TestDecimal(scale1).setRaw(-value1), new TestDecimal(scale2).setRaw(-value2));
                    }
                }
            }
        }
    }

    private void comboTest(TestDecimal value1, TestDecimal value2) {
        try {
            BigDecimal bd1 = value1.toBigDecimal();
            BigDecimal bd2 = value2.toBigDecimal();
            assertEquals(round(bd1.add(bd2), value1.getScale()), value1.clone().add(value2, RoundingMode.DOWN).getRaw());
            assertEquals(round(bd1.subtract(bd2), value1.getScale()), value1.clone().subtract(value2, RoundingMode.DOWN).getRaw());
            assertEquals(round(bd1.multiply(bd2), value1.getScale()), value1.clone().mul(value2, RoundingMode.DOWN).getRaw());
            assertEquals(round(bd1.divide(bd2), value1.getScale()), value1.clone().div(value2, RoundingMode.DOWN).getRaw());

            TestDecimal value3 = value1.clone().setRaw(value2.getRaw());
            BigDecimal bd3 = value3.toBigDecimal();
            assertEquals(round(bd1.add(bd3), value1.getScale()), value1.clone().plus(value1, value3, RoundingMode.DOWN).getRaw());
            assertEquals(round(bd1.subtract(bd3), value1.getScale()), value1.clone().minus(value1, value3, RoundingMode.DOWN).getRaw());
            assertEquals(round(bd1.multiply(bd3), value1.getScale()), value1.clone().product(value1, value3, RoundingMode.DOWN).getRaw());
            assertEquals(round(bd1.divide(bd3), value1.getScale()), value1.clone().quotient(value1, value3, RoundingMode.DOWN).getRaw());
        } catch (AssertionError e) {
            throw new RuntimeException("Failed for " + value1 + " and " + value2 + ": " + e.getMessage(), e);
        }
    }

    private long round(BigDecimal value, int scale) {
        value = value.multiply(BigDecimal.TEN.pow(scale));
        if (value.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0 ||
            value.compareTo(BigDecimal.valueOf(-Long.MAX_VALUE)) < 0) {
            return AbstractDecimal.NaN;
        }
        return value.longValue();
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
