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
Benchmark                            Mode  Cnt    Score   Error  Units
DecimalBenchmark.control             avgt  200   10.072 ± 0.074  ns/op
DecimalBenchmark.multiply            avgt  200   10.625 ± 0.142  ns/op
DecimalBenchmark.multiplyDecimal     avgt  200   35.840 ± 0.121  ns/op
DecimalBenchmark.multiplyBigDecimal  avgt  200  126.098 ± 0.408  ns/op
DecimalBenchmark.quotient            avgt  200   70.728 ± 0.230  ns/op
DecimalBenchmark.quotientDecimal     avgt  200  138.581 ± 7.102  ns/op
DecimalBenchmark.quotientBigDecimal  avgt  200  179.650 ± 0.849  ns/op
</pre>

## License

[The MIT License](https://opensource.org/licenses/MIT)
