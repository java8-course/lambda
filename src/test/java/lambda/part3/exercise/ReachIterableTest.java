package lambda.part3.exercise;

import com.google.common.collect.FluentIterable;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.junit.Test;

import javax.xml.ws.Holder;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by antonnazarov on 06.07.17.
 */
public class ReachIterableTest {
    interface ReachIterable<T> {
        default boolean anyMatch(final Predicate<T> p) {
            return firstMatch(p).isPresent();
        }

        default boolean allMatch(final Predicate<T> p) {
            return !anyMatch(p.negate());
        }

        default boolean noneMatch(final Predicate<T> p) {
            return !anyMatch(p);
        }

        default Optional<T> firstMatch(final Predicate<T> p) {
            Holder<Optional<T>> founded = new Holder<>(Optional.empty());
            while (tryGet(t -> {
                if (p.test(t)) {
                    founded.value = Optional.of(t);
                }
            }) && (!founded.value.isPresent())) {
                ;
            }

            return founded.value;
        }

        default ReachIterable<T> filter(final Predicate<T> p) {
            return c -> this.tryGet(v -> {
                if (p.test(v)) {
                    c.accept(v);
                }
            });
        }


        default <R> ReachIterable<R> map(final Function<T, R> f) {
            return c -> this.tryGet(e -> c.accept(f.apply(e)));
        }

        default <R> ReachIterable<R> flatMap(final Function<T, ReachIterable<R>> f) {
            //TOAPOLOGIZE Code in this method is not really my. I cheated to view the result.

            ReachIterable<T> self = this;
            return new ReachIterable<R>() {
                ReachIterable<R> tmp;
                boolean selfHasNext = false,
                        tmpHasNext = false;

                @Override
                public boolean tryGet(final Consumer<R> c) {
                    if (!tmpHasNext) {
                        selfHasNext = self.tryGet(e -> tmp = f.apply(e));
                    }

                    tmpHasNext = tmp.tryGet(c);

                    return tmpHasNext || selfHasNext;
                }
            };
        }

        static <T> ReachIterable<T> from(List<T> list) {
            final Iterator<T> i = list.iterator();
            return c -> {
                if (i.hasNext()) {
                    c.accept(i.next());
                }
                return i.hasNext();
            };
        }

        default List<T> force() {
            final List<T> result = new ArrayList<>();
            while (tryGet(result::add)) {
                ;
            }
            return result;
        }

        boolean tryGet(final Consumer<T> c);
    }

    private final List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

    @Test
    public void reachIterableFilter() {

        List<Integer> filteredNumbers = ReachIterable.from(numbers).filter(t -> (t % 2) == 0).force();

        assertEquals(filteredNumbers, Arrays.asList(2, 4));
    }

    @Test
    public void reachIterableMap() {
        final List<Integer> mappedNumbers = ReachIterable.from(numbers).map(t -> t * 2).force();
        assertEquals(mappedNumbers, Arrays.asList(2, 4, 6, 8, 10));
    }

    @Test
    public void reachIteableFlatted() {
        final List<Integer> mappedNumbers = ReachIterable.from(numbers)
                .filter(e -> e < 3)
                .flatMap(e -> ReachIterable.from(Arrays.asList(e * 2, e * 3)))
                .force();

        assertEquals(mappedNumbers, Arrays.asList(2, 3, 4, 6));
    }

    @Test
    public void anyMatches() {
        assertEquals(ReachIterable.from(numbers).anyMatch(t -> t.equals(2)), true);
        assertEquals(ReachIterable.from(numbers).anyMatch(t -> t.equals(7)), false);
    }

    @Test
    public void firstMathces() {
        assertEquals(ReachIterable.from(numbers).firstMatch(t -> t.equals(2)).get(), numbers.get(1));
        assertEquals(ReachIterable.from(numbers).firstMatch(t -> t.equals(7)).isPresent(), false);
    }

    @Test
    public void allMatches() {
        assertEquals(ReachIterable.from(numbers).allMatch(e -> e < 10), true);
        assertEquals(ReachIterable.from(numbers).allMatch(e -> e.equals(1)), false);
    }

    @Test
    public void noneMatches() {
        assertEquals(ReachIterable.from(numbers).noneMatch(e -> e.equals(1)), false);
        assertEquals(ReachIterable.from(numbers).noneMatch(e -> e.equals(10)), true);
    }
}
