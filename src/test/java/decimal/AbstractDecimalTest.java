/*
 MIT License

 Copyright (c) 2016 Maxim Tomin

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package decimal;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static decimal.AbstractDecimal.NaN;

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
    public void testSet() throws Exception {
        assertEquals("123.00", quantity.set(price.fromDoubleRD(123.0)).toString());
        assertEquals("123.00", quantity.setRD(price.fromDoubleRD(123.005)).toString());
        assertEquals("123.01", quantity.set(price.fromDoubleRD(123.005), RoundingMode.UP).toString());

        assertEquals("123.00", quantity.set(123).toString());
        assertEquals("-123.00", quantity.set(-123).toString());
        assertEquals("NaN", quantity.set(Long.MAX_VALUE).toString());
        assertEquals("NaN", quantity.set(-Long.MAX_VALUE).toString());

        assertEquals("123.00000000", price.set(price.fromDoubleRD(123.0)).toString());
        assertEquals("123.00000000", price.set(quantity.fromDoubleRD(123.0)).toString());
        assertEquals("92233720368547758.00", quantity.fromLong(92233720368547758L).toString());
        assertEquals("NaN", price.set(quantity).toString());
        assertEquals("-92233720368547758.00", quantity.fromLong(-92233720368547758L).toString());
        assertEquals("NaN", price.set(quantity).toString());
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

        assertEquals("NaN", new TestDecimal(2).setRaw(AbstractDecimal.NaN).toString());

        StringBuilder sb = new StringBuilder();
        assertEquals("0.0123", new TestDecimal(4).setRaw(123).toStringBuilder(sb).toString());
        assertEquals("0.01230.0123", new TestDecimal(4).setRaw(123).toStringBuilder(sb).toString());
        assertEquals("0.01230.01230.0123", new TestDecimal(4).setRaw(123).toStringBuilder(sb).toString());
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
        assertEquals(1230000, new TestDecimal(4).parse("000000000000000123.00000000000000000").getRaw());

        assertEquals(-1230000, new TestDecimal(4).parse("-123").getRaw());
        assertEquals(0, new TestDecimal(4).parse("0").getRaw());

        assertEquals(Long.MAX_VALUE, new TestDecimal(0).parse("9223372036854775807").getRaw());
        assertEquals(-Long.MAX_VALUE, new TestDecimal(0).parse("-9223372036854775807").getRaw());
        assertEquals(Long.MAX_VALUE, new TestDecimal(0).parse("9223372036854775807.0").getRaw());
        assertEquals(-Long.MAX_VALUE, new TestDecimal(0).parse("-9223372036854775807.0").getRaw());
        assertEquals(Long.MAX_VALUE, new TestDecimal(0).parse("000009223372036854775807.0").getRaw());
        assertEquals(-Long.MAX_VALUE, new TestDecimal(0).parse("-0000009223372036854775807.0").getRaw());
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
        assertExceptionWhileParsing("");
        assertExceptionWhileParsing("none");
        assertExceptionWhileParsing("1.");

        try {
            new TestDecimal(1).parse("9223372036854775807");
            fail("Exception expected");
        } catch (ParseException e) {
        }
        try {
            new TestDecimal(1).parse("1", 1, 0);
            fail("Exception expected");
        } catch (ParseException e) {
        }
        try {
            new TestDecimal(1).parse("-1", 0, 1);
            fail("Exception expected");
        } catch (ParseException e) {
        }
    }

    @Test
    public void testToFromLong() throws Exception {
        assertEquals("123.0", new TestDecimal(1).fromLong(123).toString());
        assertEquals("1000000000000000000", new TestDecimal(0).fromLong(1000000000000000000L).toString());
        assertEquals("NaN", new TestDecimal(1).fromLong(1000000000000000000L).toString());
        assertEquals("NaN", new TestDecimal(0).fromLong(Long.MIN_VALUE).toString());

        assertEquals(123L, new TestDecimal(1).setRaw(1230).toLong(RoundingMode.UNNECESSARY));
        assertEquals(123L, new TestDecimal(1).setRaw(1235).toLongRD());
        assertEquals(124L, new TestDecimal(1).setRaw(1235).toLong(RoundingMode.UP));
        assertEquals(-123L, new TestDecimal(1).setRaw(-1235).toLong(RoundingMode.CEILING));
        assertEquals(-124L, new TestDecimal(1).setRaw(-1235).toLong(RoundingMode.UP));
    }

    @Test
    public void testToFromDouble() throws Exception {
        for (RoundingMode roundingMode : new RoundingMode[] {RoundingMode.DOWN, RoundingMode.FLOOR, RoundingMode.CEILING, RoundingMode.HALF_EVEN}) {
            assertEquals("123.0", new TestDecimal(1).fromDouble(123.0, roundingMode).toString());
            assertEquals("1000000000000000000", new TestDecimal(0).fromDouble(1e18, roundingMode).toString());
            assertEquals("NaN", new TestDecimal(1).fromDouble(1e18, roundingMode).toString());
            assertEquals("NaN", new TestDecimal(1).fromDouble(Double.POSITIVE_INFINITY, roundingMode).toString());
            assertEquals("NaN", new TestDecimal(1).fromDouble(Double.NEGATIVE_INFINITY, roundingMode).toString());
            assertEquals("NaN", new TestDecimal(1).fromDouble(Double.NaN, roundingMode).toString());
        }

        assertEquals("0", new TestDecimal(0).fromDouble(0.0, RoundingMode.DOWN).toString());
        assertEquals("0", new TestDecimal(0).fromDouble(0.5, RoundingMode.DOWN).toString());
        assertEquals("0", new TestDecimal(0).fromDouble(-0.5, RoundingMode.DOWN).toString());
        assertEquals("1", new TestDecimal(0).fromDouble(1.5, RoundingMode.DOWN).toString());
        assertEquals("-1", new TestDecimal(0).fromDouble(-1.5, RoundingMode.DOWN).toString());
        assertEquals("1", new TestDecimal(0).fromDouble(1.7, RoundingMode.DOWN).toString());
        assertEquals("-1", new TestDecimal(0).fromDouble(-1.7, RoundingMode.DOWN).toString());

        assertEquals("0", new TestDecimal(0).fromDouble(0.0, RoundingMode.UP).toString());
        assertEquals("1", new TestDecimal(0).fromDouble(0.5, RoundingMode.UP).toString());
        assertEquals("-1", new TestDecimal(0).fromDouble(-0.5, RoundingMode.UP).toString());
        assertEquals("2", new TestDecimal(0).fromDouble(1.5, RoundingMode.UP).toString());
        assertEquals("-2", new TestDecimal(0).fromDouble(-1.5, RoundingMode.UP).toString());
        assertEquals("2", new TestDecimal(0).fromDouble(1.7, RoundingMode.UP).toString());
        assertEquals("-2", new TestDecimal(0).fromDouble(-1.7, RoundingMode.UP).toString());

        assertEquals("0", new TestDecimal(0).fromDouble(0.0, RoundingMode.FLOOR).toString());
        assertEquals("0", new TestDecimal(0).fromDouble(0.5, RoundingMode.FLOOR).toString());
        assertEquals("-1", new TestDecimal(0).fromDouble(-0.5, RoundingMode.FLOOR).toString());
        assertEquals("1", new TestDecimal(0).fromDouble(1.5, RoundingMode.FLOOR).toString());
        assertEquals("-2", new TestDecimal(0).fromDouble(-1.5, RoundingMode.FLOOR).toString());
        assertEquals("1", new TestDecimal(0).fromDouble(1.7, RoundingMode.FLOOR).toString());
        assertEquals("-2", new TestDecimal(0).fromDouble(-1.7, RoundingMode.FLOOR).toString());

        assertEquals("0", new TestDecimal(0).fromDouble(0.0, RoundingMode.CEILING).toString());
        assertEquals("1", new TestDecimal(0).fromDouble(0.5, RoundingMode.CEILING).toString());
        assertEquals("0", new TestDecimal(0).fromDouble(-0.5, RoundingMode.CEILING).toString());
        assertEquals("2", new TestDecimal(0).fromDouble(1.5, RoundingMode.CEILING).toString());
        assertEquals("-1", new TestDecimal(0).fromDouble(-1.5, RoundingMode.CEILING).toString());
        assertEquals("2", new TestDecimal(0).fromDouble(1.7, RoundingMode.CEILING).toString());
        assertEquals("-1", new TestDecimal(0).fromDouble(-1.7, RoundingMode.CEILING).toString());

        assertEquals("0", new TestDecimal(0).fromDouble(0.0, RoundingMode.HALF_EVEN).toString());
        assertEquals("0", new TestDecimal(0).fromDouble(0.5, RoundingMode.HALF_EVEN).toString());
        assertEquals("0", new TestDecimal(0).fromDouble(-0.5, RoundingMode.HALF_EVEN).toString());
        assertEquals("2", new TestDecimal(0).fromDouble(1.5, RoundingMode.HALF_EVEN).toString());
        assertEquals("-2", new TestDecimal(0).fromDouble(-1.5, RoundingMode.HALF_EVEN).toString());
        assertEquals("2", new TestDecimal(0).fromDouble(1.7, RoundingMode.HALF_EVEN).toString());
        assertEquals("-2", new TestDecimal(0).fromDouble(-1.7, RoundingMode.HALF_EVEN).toString());
        assertEquals("1", new TestDecimal(0).fromDouble(1.3, RoundingMode.HALF_EVEN).toString());
        assertEquals("-1", new TestDecimal(0).fromDouble(-1.3, RoundingMode.HALF_EVEN).toString());

        assertTrue(123.0 == new TestDecimal(1).setRaw(1230).toDouble());
        assertTrue(123.5 == new TestDecimal(1).setRaw(1235).toDouble());
        assertTrue(12.30 == new TestDecimal(2).setRaw(1230).toDouble());
        assertTrue(12.35 == new TestDecimal(2).setRaw(1235).toDouble());
        assertTrue(Double.isNaN(new TestDecimal(2).setRaw(NaN).toDouble()));
    }

    @Test
    public void testCompareTo() throws ParseException {
        assertTrue(new TestDecimal(1).parse("123").compareTo(new TestDecimal(1).parse("123")) == 0);
        assertTrue(new TestDecimal(1).parse("123").compareTo(new TestDecimal(1).parse("124")) < 0);
        assertTrue(new TestDecimal(1).parse("124").compareTo(new TestDecimal(1).parse("123")) > 0);

        assertTrue(new TestDecimal(1).parse("123").compareTo(new TestDecimal(3).parse("123")) == 0);
        assertTrue(new TestDecimal(1).parse("123").compareTo(new TestDecimal(3).parse("124")) < 0);
        assertTrue(new TestDecimal(1).parse("124").compareTo(new TestDecimal(3).parse("123")) > 0);

        assertTrue(new TestDecimal(3).parse("123").compareTo(new TestDecimal(1).parse("123")) == 0);
        assertTrue(new TestDecimal(3).parse("123").compareTo(new TestDecimal(1).parse("124")) < 0);
        assertTrue(new TestDecimal(3).parse("124").compareTo(new TestDecimal(1).parse("123")) > 0);

        assertTrue(new TestDecimal(3).parse("0.0").compareTo(new TestDecimal(1).parse("0")) == 0);
        assertTrue(new TestDecimal(3).parse("-0.1").compareTo(new TestDecimal(1).parse("0")) < 0);
        assertTrue(new TestDecimal(3).parse("0.1").compareTo(new TestDecimal(1).parse("0")) > 0);

        assertTrue(new TestDecimal(3).parse("0.123").compareTo(new TestDecimal(1).parse("12.0")) < 0);
        assertTrue(new TestDecimal(3).parse("1.123").compareTo(new TestDecimal(0).parse("1")) > 0);
        assertTrue(new TestDecimal(3).parse("1.000").compareTo(new TestDecimal(0).parse("1")) == 0);

        assertTrue(new TestDecimal(9).parse("NaN").compareTo(new TestDecimal(0).parse("-1000000000000000000")) < 0);
        assertTrue(new TestDecimal(9).parse("-1000000000").compareTo(new TestDecimal(0).parse("NaN")) > 0);
        assertTrue(new TestDecimal(9).parse("NaN").compareTo(new TestDecimal(0).parse("NaN")) == 0);

        assertTrue(new TestDecimal(0).parse("9223372036854775807").compareTo(new TestDecimal(0).parse("9223372036854775807")) == 0);
        assertTrue(new TestDecimal(0).parse("9223372036854775807").compareTo(new TestDecimal(0).parse("-9223372036854775807")) > 0);
        assertTrue(new TestDecimal(0).parse("-9223372036854775807").compareTo(new TestDecimal(0).parse("9223372036854775807")) < 0);
        assertTrue(new TestDecimal(0).parse("-9223372036854775807").compareTo(new TestDecimal(0).parse("-9223372036854775807")) == 0);

        assertTrue(new TestDecimal(0).parse("9223372036854775806").compareTo(new TestDecimal(1).parse("922337203685477580.0")) > 0);
        assertTrue(new TestDecimal(0).parse("922337203685477580").compareTo(new TestDecimal(1).parse("922337203685477580.0")) == 0);
        assertTrue(new TestDecimal(0).parse("922337203685477579").compareTo(new TestDecimal(1).parse("922337203685477580.0")) < 0);

        assertTrue(new TestDecimal(1).parse("922337203685477580.0").compareTo(new TestDecimal(0).parse("9223372036854775806")) < 0);
        assertTrue(new TestDecimal(1).parse("922337203685477580.0").compareTo(new TestDecimal(0).parse("922337203685477580")) == 0);
        assertTrue(new TestDecimal(1).parse("922337203685477580.0").compareTo(new TestDecimal(0).parse("922337203685477579")) > 0);

        assertTrue(new TestDecimal(0).parse("-9223372036854775806").compareTo(new TestDecimal(1).parse("-922337203685477580.0")) < 0);
        assertTrue(new TestDecimal(0).parse("-922337203685477580").compareTo(new TestDecimal(1).parse("-922337203685477580.0")) == 0);
        assertTrue(new TestDecimal(0).parse("-922337203685477579").compareTo(new TestDecimal(1).parse("-922337203685477580.0")) > 0);

        assertTrue(new TestDecimal(1).parse("-922337203685477580.0").compareTo(new TestDecimal(0).parse("-9223372036854775806")) > 0);
        assertTrue(new TestDecimal(1).parse("-922337203685477580.0").compareTo(new TestDecimal(0).parse("-922337203685477580")) == 0);
        assertTrue(new TestDecimal(1).parse("-922337203685477580.0").compareTo(new TestDecimal(0).parse("-922337203685477579")) < 0);

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
        // same, round DOWN
        assertEquals("184467440737.09", quantity().plusRD(price("92233720368.54775807"), price("92233720368.54775807")).toString());
        assertEquals("-184467440737.09", quantity().plusRD(price("-92233720368.54775807"), price("-92233720368.54775807")).toString());

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

        assertEquals("123.00", quantity("123").addRD(price("0.00000001")).toString());
        assertEquals("122.99", quantity("123").addRD(price("-0.00000001")).toString());
        assertEquals("-122.99", quantity("-123").addRD(price("0.00000001")).toString());
        assertEquals("-123.00", quantity("-123").addRD(price("-0.00000001")).toString());

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
        // same, rounding DOWN
        assertEquals("184467440737.09", quantity().minusRD(price("92233720368.54775807"), price("-92233720368.54775807")).toString());
        assertEquals("-184467440737.09", quantity().minusRD(price("-92233720368.54775807"), price("92233720368.54775807")).toString());

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

        assertEquals("123.01", quantity("123").subtract(price("-0.00000001"), RoundingMode.UP).toString());
        assertEquals("123.00", quantity("123").subtract(price("0.00000001"), RoundingMode.UP).toString());
        assertEquals("-123.00", quantity("-123").subtract(price("-0.00000001"), RoundingMode.UP).toString());
        assertEquals("-123.01", quantity("-123").subtract(price("0.00000001"), RoundingMode.UP).toString());

        assertEquals("123.00", quantity("123").subtractRD(price("-0.00000001")).toString());
        assertEquals("122.99", quantity("123").subtractRD(price("0.00000001")).toString());
        assertEquals("-122.99", quantity("-123").subtractRD(price("-0.00000001")).toString());
        assertEquals("-123.00", quantity("-123").subtractRD(price("0.00000001")).toString());

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

        assertEquals("1230.00", quantity.productRD(price("123"), price("10.00000001")).toString());
        assertEquals("-1230.00", quantity.productRD(price("123"), price("-10.00000001")).toString());
        assertEquals("-1230.00", quantity.productRD(price("-123"), price("10.00000001")).toString());
        assertEquals("1230.00", quantity.productRD(price("-123"), price("-10.00000001")).toString());

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

        assertEquals("NaN", price("NaN").mulRD(quantity("10")).toString());
        assertEquals("NaN", price("123").mulRD(quantity("NaN")).toString());

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

        assertEquals("111.81", quantity.quotientRD(1230, 11).toString());
        assertEquals("-111.81", quantity.quotientRD(1230, -11).toString());
        assertEquals("-111.81", quantity.quotientRD(-1230, 11).toString());
        assertEquals("111.81", quantity.quotientRD(-1230, -11).toString());

        assertEquals("NaN", quantity.quotientRD(quantity("10000000000000000.00"), quantity("0.01")).toString());
        assertEquals("NaN", quantity.quotientRD(quantity("NaN"), quantity("10")).toString());
        assertEquals("NaN", quantity.quotientRD(quantity("1230"), quantity("NaN")).toString());
    }

    @Test
    public void testDiv() throws Exception {
        assertEquals("123.00", quantity("1230").div(quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", quantity("1230").div(quantity("-10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", quantity("-1230").div(quantity("10"), RoundingMode.UNNECESSARY).toString());
        assertEquals("123.00", quantity("-1230").div(quantity("-10"), RoundingMode.UNNECESSARY).toString());

        assertEquals("111.81", quantity("1230").divRD(quantity("11")).toString());
        assertEquals("-111.81", quantity("1230").divRD(quantity("-11")).toString());
        assertEquals("-111.81", quantity("-1230").divRD(quantity("11")).toString());
        assertEquals("111.81", quantity("-1230").divRD(quantity("-11")).toString());

        assertEquals("123.00", quantity("1230").div(10, RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", quantity("1230").div(-10, RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", quantity("-1230").div(10, RoundingMode.UNNECESSARY).toString());
        assertEquals("123.00", quantity("-1230").div(-10, RoundingMode.UNNECESSARY).toString());

        assertEquals("111.81", quantity("1230").divRD(11).toString());
        assertEquals("-111.81", quantity("1230").divRD(-11).toString());
        assertEquals("-111.81", quantity("-1230").divRD(11).toString());
        assertEquals("111.81", quantity("-1230").divRD(-11).toString());

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
            BigDecimal bd1 = BigDecimal.valueOf(value1.getRaw()).divide(BigDecimal.TEN.pow(value1.getScale()));
            BigDecimal bd2 = BigDecimal.valueOf(value2.getRaw()).divide(BigDecimal.TEN.pow(value2.getScale()));
            assertEquals(round(bd1.add(bd2), value1.getScale()), value1.clone().add(value2, RoundingMode.DOWN).getRaw());
            assertEquals(round(bd1.subtract(bd2), value1.getScale()), value1.clone().subtract(value2, RoundingMode.DOWN).getRaw());
            assertEquals(round(bd1.multiply(bd2), value1.getScale()), value1.clone().mul(value2, RoundingMode.DOWN).getRaw());
            assertEquals(round(bd1.divide(bd2), value1.getScale()), value1.clone().div(value2, RoundingMode.DOWN).getRaw());
            assertEquals(Integer.signum(bd1.compareTo(bd2)), Integer.signum(value1.compareTo(value2)));

            TestDecimal value3 = value1.clone().setRaw(value2.getRaw());
            BigDecimal bd3 = BigDecimal.valueOf(value3.getRaw()).divide(BigDecimal.TEN.pow(value3.getScale()));
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
