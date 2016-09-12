package decimal.sample;

import java.text.ParseException;

public class Sample {
    private final Decimal margin;
    private final Quantity2 cumQuantity = new Quantity2();
    private final Quantity2 contraQuantity = new Quantity2();
    private final Quantity2 cumContraQuantity = new Quantity2();
    private final Price priceWithMargin = new Price();
    private final Price avgPrice = new Price();

    public Sample(int margin) {
        this.margin = Decimal.create(margin).divRD(10000L).add(1);
    }

    private Price calculateAvgPrice(Quantity2[] quantities, Price[] prices) {
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

        Quantity2 q1 = Quantity2.create("100");
        Quantity2 q2 = Quantity2.create(200);

        Sample sample = new Sample(5); // 5 bp margin
        System.out.println(sample.calculateAvgPrice(new Quantity2[]{q1, q2}, new Price[]{p1, p2}));
    }
}