package decimal;

class TestDecimal extends AbstractDecimal<TestDecimal> {
    private final int scale;

    public TestDecimal(int scale) {
        this.scale = scale;
    }

    @Override
    protected int getScale() {
        return scale;
    }
}
