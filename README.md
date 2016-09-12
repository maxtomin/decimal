# Decimal - non-allocating fixed-point arithmetic

Fixed-point arithmetics allowimplies predictable absolute rounding error, which can be critical in financial calculations.
Unlike floating-point numbers, precision of a value depends solely on its type and does not depend on how big or small it is.

## Functional and reliable
- Full control over rounding, all rodungin BigInteger modes  are supported
- NaN support to indicate overflows and division by 0
- Thouroughly unit-tested and random-tested against BigDecimal
- Non-allocating (unless explicitly specified)

## Fast
Faster than BigDecimal, just 2-4 times slower than native multiplication and division

JMH benchmark:
<pre>
Benchmark                            Mode  Cnt    Score    Error  Units Comment
DecimalBenchmark.control             avgt   20   10.285 ±  0.460  ns/op Initializing data only
DecimalBenchmark.multiply            avgt   20   10.955 ±  0.260  ns/op Native long multiply
DecimalBenchmark.quotient            avgt   20   72.968 ±  0.812  ns/op Native long divide
DecimalBenchmark.multiplyDecimal     avgt   20   37.389 ±  0.356  ns/op Decimal multiply 
DecimalBenchmark.quotientDecimal     avgt   20  145.661 ± 27.945  ns/op Decimal divide
DecimalBenchmark.multiplyBigDecimal  avgt   20  130.809 ±  1.150  ns/op BigDecimal multiply 
DecimalBenchmark.quotientBigDecimal  avgt   20  186.165 ±  4.110  ns/op BigDecimal divide
</pre>
