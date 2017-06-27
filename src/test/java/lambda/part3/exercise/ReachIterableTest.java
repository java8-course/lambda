package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
            final ReachIterable<T> self = this;
            return new ReachIterable<T>() {
                boolean matched = false;
                boolean hasNext = true;

                @Override
                public boolean forNext(Consumer<T> consumer) {
                    while (!matched && hasNext) {
                        hasNext = self.forNext(
                                element -> {
                                    matched = predicate.test(element);
                                    if (matched) consumer.accept(element);
                                }
                        );
                    }
                    matched = false;
                    return hasNext;
                }
            };
        }

        default <R> ReachIterable<R> map(final Function<T, R> mapper) {
            return consumer -> this.forNext(
                    element -> consumer.accept(mapper.apply(element))
            );
        }

        default <R> ReachIterable<R> flatMap(final Function<T, List<R>> mapper) {
            final ReachIterable<T> outer = this;
            return new ReachIterable<R>() {
                ReachIterable<R> inner = ReachIterable.from(Collections.emptyList());
                boolean outerHasNext = false;
                boolean innerHasNext = false;

                @Override
                public boolean forNext(Consumer<R> consumer) {
                    if (!innerHasNext) {
                        outerHasNext = outer.forNext(element -> inner = ReachIterable.from(mapper.apply(element)));
                    }
                    innerHasNext = inner.forNext(consumer);
                    return  innerHasNext || outerHasNext;
                }
            };
        }

        default boolean anyMatch(final Predicate<T> predicate) {
            return firstMatch(predicate).isPresent();
        }

        default boolean allMatch(final Predicate<T> predicate) {
            return !anyMatch(predicate.negate());
        }

        default boolean noneMatch(final Predicate<T> predicate) {
            return !anyMatch(predicate);
        }

        default Optional<T> firstMatch(final Predicate<T> predicate) {
            final Holder<T> needle = new Holder<>();
            final Consumer<T> testAndExtract = element -> { if (predicate.test(element)) needle.value = element; };
            boolean hasNext = true;
            while (Objects.isNull(needle.value) && hasNext) {
                hasNext = this.forNext(testAndExtract);
            }
            return Optional.ofNullable(needle.value);
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
        return employee -> employee.getJobHistory()
                                   .stream()
                                   .anyMatch(job -> job.getEmployer().equalsIgnoreCase(company));
    }

    private static Predicate<Employee> workedAs(final String position) {
        return employee -> employee.getJobHistory()
                                   .stream()
                                   .map(JobHistoryEntry::getPosition)
                                   .anyMatch(position::equalsIgnoreCase);
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
                    new JobHistoryEntry(1, "dev", "abc"),
                    new JobHistoryEntry(1, "dev", "google")
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
        final List<Employee> expected = Arrays.asList(
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
                                new JobHistoryEntry(1, "dev", "abc"),
                                new JobHistoryEntry(1, "dev", "google")
                        )
                ));
        final List<Employee> actual = new ArrayList<>();
        final ReachIterable<Employee> employeeIterable = ReachIterable.from(employees)
                                                                      .filter(workedIn("google"));
        while(employeeIterable.forNext(actual::add));
        assertEquals(expected, actual);
    }

    @Test
    public void filterIsWorkedPerOneTest() {
        final ReachIterable<Employee> employeeIterable = ReachIterable.from(employees)
                                                                      .filter(workedAs("qa"));
        employeeIterable.forNext(employee -> assertEquals(employees.get(1), employee));
        employeeIterable.forNext(employee -> assertEquals(employees.get(2), employee));
        assertFalse(employeeIterable.forNext(System.out::println));
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
                new JobHistoryEntry(1, "dev", "google"),
                new JobHistoryEntry(5, "qa", "epam")
        );
        final List<JobHistoryEntry> actual = new ArrayList<>();
        final ReachIterable<JobHistoryEntry> jobIterable = ReachIterable.from(employees)
                                                                        .flatMap(Employee::getJobHistory);
        while(jobIterable.forNext(actual::add));
        assertEquals(expected, actual);
    }

    @Test
    public void flatMapPerOnTest() {
        final List<JobHistoryEntry> expected = Arrays.asList(
                new JobHistoryEntry(2, "dev", "epam"),
                new JobHistoryEntry(1, "dev", "google"),
                new JobHistoryEntry(3, "qa", "yandex"),
                new JobHistoryEntry(1, "qa", "epam"),
                new JobHistoryEntry(1, "dev", "abc"),
                new JobHistoryEntry(1, "dev", "google"),
                new JobHistoryEntry(5, "qa", "epam")
        );
        final ReachIterable<JobHistoryEntry> jobIterable = ReachIterable.from(employees)
                                                                        .flatMap(Employee::getJobHistory);

        final AtomicInteger counter = new AtomicInteger();
        boolean hasNext = true;
        while (hasNext) {
            counter.incrementAndGet();
            hasNext = jobIterable.forNext(job -> assertTrue(expected.contains(job)));
        }
        assertEquals(expected.size(), counter.get());
    }

    @Test
    public void flatMapMappedHasFewerElementsTest() {
        final List<Character> expected = Arrays.asList(
                'a', 'b', 'c'
        );
        final List<Character> actual = new ArrayList<>();
        final Function<String, List<Character>> stringToChars = string -> {
            final List<Character> result = new ArrayList<>();
            for (int i = 0; i < string.length(); i++) result.add(string.charAt(i));
            return result;
        };
        final ReachIterable<Character> strings = ReachIterable.from(Arrays.asList("", "ab", "", "", "c", ""))
                                                           .flatMap(stringToChars);
        while(strings.forNext(actual::add));
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

class Holder<T> {
    T value;
}
