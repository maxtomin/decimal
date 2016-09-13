# Decimal - non-allocating fixed-point arithmetic

Fixed-point arithmetics implies predictable absolute rounding error, which can be critical in financial calculations.
Unlike floating-point numbers, precision of a value depends solely on its type and does not depend on how big or small it is.

## Functional and reliable
- Full control over rounding, all BigDecimal rodunding modes are supported
- NaN support to indicate overflows and division by 0
- Thouroughly unit-tested and random-tested against BigDecimal
- Non-allocating (unless explicitly specified)

## Fast
Faster than BigDecimal, just 2-4 times slower than native multiplication and division

JMH benchmark:
<pre>
Benchmark                            Mode  Cnt    Score   Error  Units Comment
DecimalBenchmark.control             avgt  200   10.321 ± 0.084  ns/op Initializing data only
DecimalBenchmark.multiply            avgt  200   10.807 ± 0.095  ns/op Native 64-bit multiply
DecimalBenchmark.multiplyDecimal     avgt  200   36.874 ± 0.125  ns/op Decimal multiply
DecimalBenchmark.multiplyBigDecimal  avgt  200  130.980 ± 0.347  ns/op BigDecimal multiply 
DecimalBenchmark.quotient            avgt  200   72.520 ± 0.259  ns/op Native 64-bit divide
DecimalBenchmark.quotientDecimal     avgt  200  146.981 ± 7.289  ns/op Decimal divide
DecimalBenchmark.quotientBigDecimal  avgt  200  185.196 ± 0.828  ns/op BigDecimal divide
</pre>
