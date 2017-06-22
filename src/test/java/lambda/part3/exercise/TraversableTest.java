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

/**
 * @author Denis Verkhoturov, mod.satyr@gmail.com
 */
public class TraversableTest {
    interface Traversable<T> {
        void forEach(Consumer<T> consumer);

        static <T> Traversable<T> from(final List<T> list) {
            return list::forEach;
        }

        default <R> Traversable<R> map(final Function<T, R> mapper) {
            final Traversable<T> self = this;
            return consumer -> self.forEach(element -> consumer.accept(mapper.apply(element)));
        }

        default Traversable<T> filter(final Predicate<T> predicate) {
            final Traversable<T> self = this;
            return consumer -> self.forEach(
                    element -> { if (predicate.test(element)) consumer.accept(element); }
            );
        }

        default <R> Traversable<R> flatMap(final Function<T, Traversable<R>> function) {
            final Traversable<T> self = this;
            return consumer -> self.forEach(
                    element -> function.apply(element).forEach(consumer)
            );
        }

        default List<T> toList() {
            final List<T> result = new ArrayList<>();
            this.forEach(result::add);
            return result;
        }
    }

    private List<Employee> employees;

    @Before
    public void setUp() {
        employees = Arrays.asList(
                new Employee(
                        new Person("a", "Galt", 30),
                        Arrays.asList(
                                new JobHistoryEntry(2, "dev", "epam"),
                                new JobHistoryEntry(1, "dev", "google")
                        )
                ),
                new Employee(
                        new Person("b", "Doe", 40),
                        Arrays.asList(
                                new JobHistoryEntry(3, "qa", "yandex"),
                                new JobHistoryEntry(1, "qa", "epam"),
                                new JobHistoryEntry(1, "dev", "abc")
                        )
                ),
                new Employee(
                        new Person("c", "White", 50),
                        Collections.singletonList(
                                new JobHistoryEntry(5, "qa", "epam")
                        )
                )
        );
    }

    private List<JobHistoryEntry> fixed(List<JobHistoryEntry> jobHistory) {
        return Traversable.from(jobHistory)
                          .map(history -> history.getPosition().equals("qa") ? history.withPosition("QA") : history)
                          .toList();
    }

    private List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> jobHistory) {
        return Traversable.from(jobHistory)
                          .map(history -> history.withDuration(history.getDuration() + 1))
                          .toList();
    }

    @Test
    public void mapTest() {
        final List<Employee> actual =
                Traversable.from(employees)
                           .map(employee -> employee.withPerson(employee.getPerson().withFirstName("John")))
                           .map(employee -> employee.withJobHistory(addOneYear(employee.getJobHistory())))
                           .map(employee -> employee.withJobHistory(fixed(employee.getJobHistory())))
                           .toList();

        final List<Employee> expected = Arrays.asList(
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
                        )),
                new Employee(
                        new Person("John", "White", 50),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "QA", "epam")
                        )
                )
        );

        assertEquals(expected, actual);
    }

    @Test
    public void filterTest() {
        final List<Employee> actual = Traversable.from(employees)
                                                 .filter(employee -> "Galt".equals(employee.getPerson().getLastName()))
                                                 .toList();

        final List<Employee> expected = Collections.singletonList(
                new Employee(
                        new Person("a", "Galt", 30),
                        Arrays.asList(
                                new JobHistoryEntry(2, "dev", "epam"),
                                new JobHistoryEntry(1, "dev", "google")
                        )
                )
        );

        assertEquals(expected, actual);
    }

    @Test
    public void flatMapTest() {
        final List<JobHistoryEntry> actual = Traversable.from(employees)
                                                 .flatMap(employee -> Traversable.from(employee.getJobHistory()))
                                                 .toList();

        final List<JobHistoryEntry> expected = Arrays.asList(
                new JobHistoryEntry(2, "dev", "epam"),
                new JobHistoryEntry(1, "dev", "google"),
                new JobHistoryEntry(3, "qa", "yandex"),
                new JobHistoryEntry(1, "qa", "epam"),
                new JobHistoryEntry(1, "dev", "abc"),
                new JobHistoryEntry(5, "qa", "epam")
        );

        assertEquals(expected, actual);
    }
}
