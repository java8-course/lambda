package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class TraversableTests {

    private List<Employee> employees;

    private interface Traversable<T> {
        void forEach(final Consumer<T> consumer);

        static <T> Traversable from(final List<T> list) {
            return list::forEach;
        }

        default <R> Traversable map(final Function<T, R> mapper) {
            return null;
        }

        default Traversable<T> filter(final Predicate<T> predicate) {
            final Traversable<T> self = this;
            return consumer -> self.forEach(
                    e -> {
                        if (predicate.test(e)) consumer.accept(e);
                    }
            );
        }

        default <R> Traversable<R> flatMap(final Function<T, Traversable<R>> function) {
            return null;
        }

        default List<T> toList() {
            List<T> result = new ArrayList<>();
            this.forEach(result::add);
            return result;
        }
    }

    @Before
    public void init() {
        employees =
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
                                        new JobHistoryEntry(5, "qa", "epam")
                                ))
                );
    }

    @Test
    public void testThatWeCanCreateTraversableAndGetAListFromIt() {
        final List<Employee> result = Traversable.from(employees)
                .toList();

        final List<Employee> expected =
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
                                        new JobHistoryEntry(5, "qa", "epam")
                                ))
                );

        assertEquals(result, expected);
    }

    @Test
    public void testThatMethodFilterWorksCorrect() {
        Predicate<Employee> getEmployeeOlderThan30 = e -> e.getPerson().getAge() > 30;
        final List<Employee> result = Traversable.from(employees)
                .filter(getEmployeeOlderThan30)
                .toList();
        final List<Employee> expected =
                Arrays.asList(
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
                                        new JobHistoryEntry(5, "qa", "epam")
                                ))
                );

        assertEquals(result, expected);
    }
}
