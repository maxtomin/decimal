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
package decimal.sample;

import decimal.Decimal;

import java.text.ParseException;

public class Sample {
    private final Decimal margin;
    private final Quantity cumQuantity = new Quantity();
    private final Quantity contraQuantity = new Quantity();
    private final Quantity cumContraQuantity = new Quantity();
    private final Price priceWithMargin = new Price();
    private final Price avgPrice = new Price();

    public Sample(int margin) {
        this.margin = Decimal.create(margin).divRD(10000L).add(1);
    }

    private Price calculateAvgPrice(Quantity[] quantities, Price[] prices) {
        cumQuantity.set(0);
        contraQuantity.set(0);

        for (int i = 0; i < quantities.length; i++) {
            cumQuantity.add(quantities[i]);
            priceWithMargin.set(prices[i]).mulRD(margin);
            contraQuantity.set(quantities[i]).mulRD(priceWithMargin);
            cumContraQuantity.add(contraQuantity);
        }

        return avgPrice.quotientRD(cumContraQuantity, cumQuantity);
    }

    public static void main(String[] args) throws ParseException {
        Price p1 = Price.create("1.5");
        Price p2 = Price.create(1.6);

        Quantity q1 = Quantity.create("100");
        Quantity q2 = Quantity.create(200);

        Sample sample = new Sample(5); // 5 bp margin
        System.out.println(sample.calculateAvgPrice(new Quantity[]{q1, q2}, new Price[]{p1, p2}));
    }
}
