package lambda.part1.exercise;

import org.junit.Test;

import java.util.StringJoiner;

import static org.junit.Assert.assertEquals;

public class Lambdas03Exercise {

    private interface GenericProduct<T> {
        T prod(T a, int i);

        default T twice(T t) {
            return prod(t, 2);
        }
    }

    @Test
    public void generic0() {
        final GenericProduct<Integer> prod = new GenericProduct<Integer>() {

            @Override
            public Integer prod(Integer a, int i) {
                return i * a;
            }
        };

        assertEquals(prod.prod(3, 2), Integer.valueOf(6));
    }

    @Test
    public void generic1() {
        final GenericProduct<Integer> prod = ((a, i) -> {
            return a * i;
        }); // Use statement lambda

        assertEquals(prod.prod(3, 2), Integer.valueOf(6));
    }

    @Test
    public void generic2() {
        final GenericProduct<Integer> prod = (a, i) -> a * i; // Use expression lambda

        assertEquals(prod.prod(3, 2), Integer.valueOf(6));
    }

    private static String stringProd(String s, int i) {
        final StringBuilder sb = new StringBuilder();
        for (int j = 0; j < i; j++) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Test
    public void sirProduct() {
        final GenericProduct<String> prod = (a, i) -> stringProd(a, i); // use stringProd;

        assertEquals(prod.prod("a", 2), "aa");
    }

    private final String delimiter = "-";

    private String stringProductWithDelimeter(String s, int i) {
        final StringJoiner sj = new StringJoiner(delimiter);
        for (int j = 0; j < i; j++) {
            sj.add(s);
        }
        return sj.toString();
    }

    @Test
    public void strProd2() {
        final GenericProduct<String> prod = this::stringProductWithDelimeter; // use stringProductWithDelimeter;

        assertEquals(prod.prod("a", 3), "a-a-a");
    }


}
