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

        static <T> Traversable<T> from(final List<T> list) {
            return list::forEach;
        }

        default <R> Traversable<R> map(final Function<T, R> mapper) {
            final Traversable<T> self = this;
            return consumer -> self.forEach(
                    e -> consumer.accept(mapper.apply(e))
            );
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
        final List<Employee> result = Traversable.from(employees)
                .filter(e -> e.getPerson().getAge() > 30)
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

    @Test
    public void testThatMethodMapWorksCorrect() {
        final List<Employee> mappedEmployees =
                Traversable.from(employees)
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                        .map(e -> e.withJobHistory(changeAllQAToUppercase(e.getJobHistory())))
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
                                )),
                        new Employee(
                                new Person("John", "White", 50),
                                Collections.singletonList(
                                        new JobHistoryEntry(6, "QA", "epam")
                                ))
                );

        assertEquals(mappedEmployees, expectedResult);
    }

    private List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> jobHistory) {
        return Traversable.from(jobHistory)
                .map((JobHistoryEntry job) -> job.withDuration(job.getDuration() + 1)).toList();
    }

    private List<JobHistoryEntry> changeAllQAToUppercase(List<JobHistoryEntry> jobHistory) {
        return Traversable.from(jobHistory)
                .map((JobHistoryEntry job) -> {
                    String currentPosition = job.getPosition();
                    return currentPosition.equals("qa") ? job.withPosition("QA") : job;
                })
                .toList();
    }
}
