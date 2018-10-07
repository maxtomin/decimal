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

package maximtomin;

import decimal.sample.Price;
import decimal.sample.Quantity;
import org.openjdk.jmh.annotations.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class DecimalBenchmark {
    private long seed = System.nanoTime();
    private long value1;
    private long value2;

    @Setup(Level.Iteration)
    public void setup() {
        // Fast linear congruential generator with parameters from Wiki
        seed = seed * 6364136223846793005L + 1442695040888963407L;
        value1 = seed;
        seed = seed * 6364136223846793005L + 1442695040888963407L;
        value2 = seed;
    }

    @Benchmark
    public long control() {
        return new Quantity().setRaw(value1).getRaw() + new Price().setRaw(value2).getRaw();
    }

    @Benchmark
    public long multiply() {
        return value1 * value2;
    }

    @Benchmark
    public long quotient() {
        return value2 != 0 ? value1 / value2 : 0;
    }

    @Benchmark
    public long multiplyDecimal() {
        return new Quantity().setRaw(value1).mulRD(new Price().setRaw(value2)).getRaw();
    }

    @Benchmark
    public long quotientDecimal() {
        return new Price().quotientRD(new Quantity().setRaw(value1), new Quantity().setRaw(value2)).getRaw();
    }

    @Benchmark
    public long multiplyBigDecimal() {
        return new BigDecimal(value1).scaleByPowerOfTen(-2).multiply(new BigDecimal(value2).scaleByPowerOfTen(-8)).signum();
    }

    @Benchmark
    public long quotientBigDecimal() {
        return value2 != 0 ? new BigDecimal(value1).scaleByPowerOfTen(-8).divide(new BigDecimal(value2).scaleByPowerOfTen(-8), RoundingMode.DOWN).signum() : 0;
    }
}
