package lambda.part1.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Lambdas03 {

    // SAM interface
    //

    @FunctionalInterface
    private interface GenericSum<T> {
        T sum(T a, T b);

        default T twice(T t) {
            return sum(t,t);
        }
    }

    @Test
    void generic0() {
        final GenericSum<Integer> sum =
                new GenericSum<Integer>() {
                    @Override
                    public Integer sum(Integer i1, Integer i2) {
                        System.out.print("before sum");
                        return i1 + i2;
                    }
                };

        assertEquals(sum.sum(1, 2), Integer.valueOf(3));
    }

    @Test
    void generic1() {
        final GenericSum<Integer> sum =
                (Integer i1, Integer i2) -> {
                    System.out.print("before sum");
                    return i1 + i2;
                };

        assertEquals(sum.sum(1, 2), Integer.valueOf(3));
    }

    @Test
    void generic2() {
        final GenericSum<Integer> sum = (i1, i2) -> i1 + i2;

        assertEquals(sum.twice(1), Integer.valueOf(2));
    }

    private static String stringSum(String s1, String s2) {
        return s1 + s2;
    }

    @Test
    void strSum() {
        final GenericSum<String> sum = Lambdas03::stringSum;

        assertEquals(sum.sum("a", "b"), "ab");
    }

    private final String delimeter = "-";

    private String stringSumWithDelimeter(String s1, String s2) {
        return s1 + delimeter + s2;
    }

    @Test
    void strSum2() {
        final GenericSum<String> sum = this::stringSumWithDelimeter;

        assertEquals(sum.sum("a", "b"), "a-b");
    }

}
