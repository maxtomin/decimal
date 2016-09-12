/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
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
