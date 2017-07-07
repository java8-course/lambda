package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static lambda.part3.exercise.Mapping.addOneYear;
import static lambda.part3.exercise.Mapping.changeToQa;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Created by whobscr on 06.07.17.
 */
public class Traverser {
    private interface Traversable<T> {
        void forEach(Consumer<T> c);

        default <R> Traversable<R> map(Function<T, R> f) {
            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    Traversable.this.forEach(t -> c.accept(f.apply(t)));
                }
            };
        }

        default <R> Traversable<R> flatMap(Function<T, Traversable<R>> mapper) {
            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    Traversable.this.forEach(e -> mapper.apply(e).forEach(c));
                }
            };

        }

        default Traversable<T> filter(Predicate<T> p) {
            return new Traversable<T>() {
                @Override
                public void forEach(Consumer<T> c) {
                    Traversable.this.forEach(e -> {
                        if (p.test(e)) c.accept(e);
                    });
                }
            };
        }

        default List<T> toList() {
            final ArrayList<T> ts = new ArrayList<>();
            forEach(ts::add);
            return ts;
        }
    }

    static <T> Traversable<T> from(List<T> list) {
        return new Traversable<T>() {
            @Override
            public void forEach(Consumer<T> action) {
                list.forEach(action);
            }
        };
    }

    @Test
    public void emptyArray() {
        from(new ArrayList<Integer>()).forEach(c -> {});
    }

    @Test
    public void fiveElementsArrayMapEven() {
        List<Integer> integers = new ArrayList<>();
        integers.add(1); integers.add(2); integers.add(3);
        integers.add(4); integers.add(5); integers.add(6);

        final Traversable<Integer> reachIterable = from(integers);
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

        final Traversable<Integer> reachIterable = from(integers);
        integers = reachIterable.filter(c -> c % 2 == 0).toList();
        final ArrayList<Integer> expected = new ArrayList<>();

        expected.add(2); expected.add(4); expected.add(6);
        assertThat(integers, is(expected));
    }

    @Test
    public void createEmployeesAndGetEpamEmployeesHistory() {
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
                                        new JobHistoryEntry(5, "qa", "JoshkinaLolo")
                                ))
                );

        final List<Employee> mappedEmployees =
                    from(employees)
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                        .map(e -> e.withJobHistory(changeToQa(e.getJobHistory())))
                        .filter(e -> e.getJobHistory().stream().anyMatch(j -> j.getEmployer().equals("epam")))
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

        assertEquals(mappedEmployees, expectedResult);
    }
}
