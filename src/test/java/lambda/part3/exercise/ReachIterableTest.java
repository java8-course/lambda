package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Denis Verkhoturov, mod.satyr@gmail.com
 */
public class ReachIterableTest {
    interface ReachIterable<T> {
        boolean forNext(Consumer<T> consumer);

        default ReachIterable<T> filter(final Predicate<T> predicate) {
            return consumer -> this.forNext(
                    element -> { if (predicate.test(element)) consumer.accept(element); }
            );
        }

        default <R> ReachIterable<R> map(final Function<T, R> mapper) {
            return consumer -> this.forNext(
                    element -> consumer.accept(mapper.apply(element))
            );
        }

        default <R> ReachIterable<R> flatMap(final Function<T, List<R>> function) {
            return consumer -> this.forNext(
                    element -> {
                        final ReachIterable<R> innerIterable = ReachIterable.from(function.apply(element));
                        while (innerIterable.forNext(consumer));
                    }
            );
        }

        default boolean anyMatch(final Predicate<T> predicate) {
            final boolean[] isAnyMatched = { false };
            final Predicate<T> isMatched = element -> isAnyMatched[0] = predicate.test(element);
            while (!isAnyMatched[0] && this.forNext(isMatched::test));
            return isAnyMatched[0];
        }

        default boolean allMatch(final Predicate<T> predicate) {
            final boolean[] isAllMatched = { true };
            final Predicate<T> isMatched = element -> isAllMatched[0] = predicate.test(element);
            while (isAllMatched[0] && this.forNext(isMatched::test));
            return isAllMatched[0];
        }

        default boolean noneMatch(final Predicate<T> predicate) {
            final boolean[] isNoneMatched = { true };
            final Predicate<T> isMatched = element -> isNoneMatched[0] = predicate.negate().test(element);
            while (isNoneMatched[0] && this.forNext(isMatched::test));
            return isNoneMatched[0];
        }

        @SuppressWarnings("unchecked")
        default Optional<T> firstMatch(final Predicate<T> predicate) {
            final Object[] objects = { null };
            final Consumer<T> testAndExtract = element -> objects[0] =  predicate.test(element) ? element : null;
            while (objects[0] == null && this.forNext(testAndExtract));
            return Optional.ofNullable((T) objects[0]);
        }

        static <T> ReachIterable<T> from(final List<T> list) {
            final Iterator<T> iterator = list.iterator();
            return consumer -> {
                if (iterator.hasNext()) consumer.accept(iterator.next());
                return iterator.hasNext();
            };
        }
    }

    private static Predicate<Person> hasName(String name) {
        return person -> person.getLastName().equals(name);
    }

    private static Predicate<Employee> workedIn(final String company) {
        return employee -> ReachIterable.from(employee.getJobHistory())
                                        .anyMatch(job -> job.getEmployer().equalsIgnoreCase(company));
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

    @Test
    public void forNextTest() {
        final List<Employee> actual = new ArrayList<>();
        final ReachIterable<Employee> employeeIterable = ReachIterable.from(employees);
        while(employeeIterable.forNext(actual::add));
        assertEquals(employees, actual);
    }

    @Test
    public void filterTest() {
        final List<Employee> expected = Collections.singletonList(
                new Employee(
                        new Person("a", "Galt", 30),
                        Arrays.asList(
                                new JobHistoryEntry(2, "dev", "epam"),
                                new JobHistoryEntry(1, "dev", "google")
                        )
                )
        );
        final List<Employee> actual = new ArrayList<>();
        final ReachIterable<Employee> personIterable = ReachIterable.from(employees)
                                                                  .filter(workedIn("google"));
        while(personIterable.forNext(actual::add));
        assertEquals(expected, actual);
    }

    @Test
    public void mapTest() {
        final List<Person> expected = Arrays.asList(
                new Person("a", "Galt", 30),
                new Person("b", "Doe", 40),
                new Person("c", "White", 50)
        );
        final List<Person> actual = new ArrayList<>();
        final ReachIterable<Person> personIterable = ReachIterable.from(employees)
                                                                  .map(Employee::getPerson);
        while(personIterable.forNext(actual::add));
        assertEquals(expected, actual);
    }

    @Test
    public void flatMapTest() {
        final List<JobHistoryEntry> expected = Arrays.asList(
                new JobHistoryEntry(2, "dev", "epam"),
                new JobHistoryEntry(1, "dev", "google"),
                new JobHistoryEntry(3, "qa", "yandex"),
                new JobHistoryEntry(1, "qa", "epam"),
                new JobHistoryEntry(1, "dev", "abc"),
                new JobHistoryEntry(5, "qa", "epam")
        );
        final List<JobHistoryEntry> actual = new ArrayList<>();
        final ReachIterable<JobHistoryEntry> jobIterable = ReachIterable.from(employees)
                                                                        .flatMap(Employee::getJobHistory);
        while(jobIterable.forNext(actual::add));
        assertEquals(expected, actual);
    }

    @Test
    public void anyMatchTrueTest() {
        final ReachIterable<Person> personIterable = ReachIterable.from(employees)
                                                                  .map(Employee::getPerson);
        assertTrue(personIterable.anyMatch(hasName("Doe")));
    }

    @Test
    public void anyMatchFalseTest() {
        final ReachIterable<Person> personIterable = ReachIterable.from(employees)
                                                                  .map(Employee::getPerson);
        assertFalse(personIterable.anyMatch(hasName("Dumbledore")));
    }

    @Test
    public void allMatchTrueTest() {
        final ReachIterable<Employee> personIterable = ReachIterable.from(employees);
        assertTrue(personIterable.allMatch(workedIn("epam")));
    }

    @Test
    public void allMatchFalseTest() {
        final ReachIterable<Employee> personIterable = ReachIterable.from(employees);
        assertFalse(personIterable.allMatch(workedIn("google")));
    }

    @Test
    public void noneMatchTrueTest() {
        final ReachIterable<Employee> personIterable = ReachIterable.from(employees);
        assertTrue(personIterable.noneMatch(workedIn("MacDonald's")));
    }

    @Test
    public void noneMatchFalseTest() {
        final ReachIterable<Employee> personIterable = ReachIterable.from(employees);
        assertFalse(personIterable.noneMatch(workedIn("yandex")));
    }

    @Test
    public void firstMatchExistTest() {
        final Employee expected = new Employee(
                new Person("a", "Galt", 30),
                Arrays.asList(
                        new JobHistoryEntry(2, "dev", "epam"),
                        new JobHistoryEntry(1, "dev", "google")
                )
        );
        final ReachIterable<Employee> personIterable = ReachIterable.from(employees);
        final Optional<Employee> employeeOptional = personIterable.firstMatch(workedIn("epam"));
        assertTrue(employeeOptional.isPresent());
        assertEquals(expected, employeeOptional.get());
    }

    @Test
    public void noneMatchNotExistTest() {
        final ReachIterable<Employee> personIterable = ReachIterable.from(employees);
        final Optional<Employee> employeeOptional = personIterable.firstMatch(workedIn("MacDonald's"));
        assertFalse(employeeOptional.isPresent());
    }
}
