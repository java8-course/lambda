package lambda.part3.exercise;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static lambda.part3.exercise.Mapping.addOneYear;
import static lambda.part3.exercise.Mapping.changeToQa;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created by whobscr on 06.07.17.
 */
public class ReachIterableTest {
    private interface ReachIterable<T> {

        boolean tryGet(Consumer<T> c);

        default <R> ReachIterable<R> map(Function<T, R> f) {
            return new ReachIterable<R>() {
                @Override
                public boolean tryGet(Consumer<R> c) {
                    return ReachIterable.this.tryGet(t -> c.accept(f.apply(t)));
                }
            };
        }

        default <R> ReachIterable<R> flatMap(Function<T, ReachIterable<R>> mapper) {
            return new ReachIterable<R>() {
                @Override
                public boolean tryGet(Consumer<R> c) {
                    return ReachIterable.this.tryGet(e -> mapper.apply(e).tryGet(c));
                }
            };
        }

        default ReachIterable<T> filter(Predicate<T> p) {
            return new ReachIterable<T>() {
                @Override
                public boolean tryGet(Consumer<T> c) {
                    return ReachIterable.this.tryGet(e -> {
                        if (p.test(e)) c.accept(e);
                    });
                }
            };
        }

        default List<T> toList() {
            final ArrayList<T> ts = new ArrayList<>();
            while (tryGet(ts::add));
            return ts;
        }

        default boolean allMatch(Predicate<T> p) {
            return !anyMatch(p.negate());
        }

        default boolean noneMatch(Predicate<T> p) {
            return anyMatch(p.negate());
        }

        default boolean anyMatch(Predicate<T> p) {
            return firstMatch(p).isPresent();
        }

        default Optional<T> firstMatch(Predicate<T> p) {
            Map.Entry<Integer, Optional<T>> curElem =
                    new AbstractMap.SimpleEntry<>(0, Optional.empty());
            while (tryGet(c -> curElem.setValue(Optional.of(c)))) {
                if (curElem.getValue().isPresent()
                        && p.test(curElem.getValue().get()))
                    return Optional.of(curElem.getValue().get());
            }
            return Optional.empty();
        }
    }

    static <T> ReachIterable<T> from(Iterable<T> iterable) {
        return new ReachIterable<T>() {
            private Iterator<T> iterator = iterable.iterator();

            @Override
            public boolean tryGet(Consumer<T> action) {
                if (iterator.hasNext()) {
                    action.accept(iterator.next());
                    return true;
                } else return false;
            }
        };
    }

    @Test
    public void emptyArray() {
        assertFalse(from(new ArrayList<Integer>())
                            .tryGet(c -> {}));
    }

    @Test
    public void fiveElementsArrayMapEven() {
        List<Integer> integers = new ArrayList<>();
        integers.add(1); integers.add(2); integers.add(3);
        integers.add(4); integers.add(5); integers.add(6);

        final ReachIterable<Integer> reachIterable = from(integers);
        integers = reachIterable.map(c -> c % 2).toList();
        final ArrayList<Integer> expected = new ArrayList<>();

        expected.add(1); expected.add(0); expected.add(1);
        expected.add(0); expected.add(1); expected.add(0);
        assertThat(integers, is(expected));
    }

    @Test
    public void fiveElementsArrayFilterEven() {
        List<Integer> integers = new ArrayList<>();
        integers.add(1); integers.add(2); integers.add(3);
        integers.add(4); integers.add(5); integers.add(6);

        final ReachIterable<Integer> reachIterable = from(integers);
        integers = reachIterable.filter(c -> c % 2 == 0).toList();
        final ArrayList<Integer> expected = new ArrayList<>();

        expected.add(2); expected.add(4); expected.add(6);
        assertThat(integers, is(expected));
    }

    @Test
    public void trueNoneMatchGreaterThanFive() {
        List<Integer> integers = new ArrayList<>();
        integers.add(4); integers.add(2); integers.add(3);
        integers.add(1); integers.add(5);

        assertTrue(from(integers)
                        .noneMatch(c -> c > 5));
    }

    @Test
    public void trueAllMatchLessThanFive() {
        List<Integer> integers = new ArrayList<>();
        integers.add(4); integers.add(2); integers.add(5);
        integers.add(1); integers.add(6); integers.add(3);

        assertTrue(from(integers)
                        .allMatch(c -> c < 7 && c > 0));
    }

    @Test
    public void falseAllMatchLessThanFive() {
        List<Integer> integers = new ArrayList<>();
        integers.add(1); integers.add(2); integers.add(3);
        integers.add(4); integers.add(5); integers.add(6);

        assertFalse(from(integers)
                        .allMatch(c -> c < 5));
    }

    @Test
    public void mapping() {
        final List<Employee> employees =
                Arrays.asList(
                        new Employee(
                                new Person("a", "Galt", 30),
                                Arrays.asList(
                                        new JobHistoryEntry(2, "dev", "epam"),
                                        new JobHistoryEntry(1, "dev", "google")
                                )),
                        new Employee(
                                new Person("b", "Doe", 40),
                                Arrays.asList(
                                        new JobHistoryEntry(3, "qa", "yandex"),
                                        new JobHistoryEntry(1, "qa", "epam"),
                                        new JobHistoryEntry(1, "dev", "abc")
                                )),
                        new Employee(
                                new Person("c", "White", 50),
                                Collections.singletonList(
                                        new JobHistoryEntry(5, "qa", "forForeach")
                                ))
                );

        final List<Employee> result =
                from(employees)
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                        .map(e -> e.withJobHistory(changeToQa(e.getJobHistory())))
                        .filter(e -> from(e.getJobHistory()).anyMatch(j -> j.getEmployer().equals("epam")))
                        .toList();

        final List<Employee> expectedResult =
                Arrays.asList(
                        new Employee(
                                new Person("John", "Galt", 30),
                                Arrays.asList(
                                        new JobHistoryEntry(3, "dev", "epam"),
                                        new JobHistoryEntry(2, "dev", "google")
                                )),
                        new Employee(
                                new Person("John", "Doe", 40),
                                Arrays.asList(
                                        new JobHistoryEntry(4, "QA", "yandex"),
                                        new JobHistoryEntry(2, "QA", "epam"),
                                        new JobHistoryEntry(2, "dev", "abc")
                                ))
                );
        assertThat(result, is(expectedResult));
        assertTrue(from(result)
                    .allMatch(e -> from(e.getJobHistory())
                            .anyMatch(entry -> entry.getEmployer().equals("epam"))));
        assertTrue(from(result)
                .allMatch(e -> from(e.getJobHistory())
                        .noneMatch(entry -> entry.getPosition().equals("qa"))));
        assertFalse(from(result)
                .anyMatch(e -> from(e.getJobHistory())
                        .anyMatch(entry -> entry.getEmployer().equals("forForeach"))));
    }
}
