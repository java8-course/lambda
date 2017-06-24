package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class Mapping {

    interface Traversable<T> {
        void forEach(Consumer<T> c);

        static <T> Traversable<T> from(List<T> list) {
            return list::forEach;
        }

        default <R> Traversable<R> map(Function<T, R> f) {
            Traversable<T> self = this;

            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    self.forEach(t -> c.accept(f.apply(t)));
                }
            };
        }

        default <R> Traversable<R> flatMap(Function<T, Traversable<R>> f) {
            Traversable<T> self = this;

            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    self.forEach(
                            e -> f.apply(e).forEach(c)
                    );
                }
            };
        }

        default Traversable<T> filter(Predicate<T> p) {
            Traversable<T> self = this;

            return new Traversable<T>() {
                @Override
                public void forEach(Consumer<T> c) {
                    self.forEach(e -> {
                        if (p.test(e)) {
                            c.accept(e);
                        }
                    });
                }
            };
        }

        default List<T> force() {
            ArrayList<T> result = new ArrayList<>();
            forEach(result::add);
            return result;
        }

    }
//
//    interface ReachIterable<T> {
//        boolean forNext(Consumer<T> c);
//
//        //filter
//        //map
//        //flatMap
//
//        //boolean anyMatch(Predicate<T>)
//        //allMatch
//        //nonMatch
    //Option<T> firstMatch(Predicate<T)
    //force
//    }


    @Test
    public void testTraversableMap() {
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
                                        new JobHistoryEntry(5, "qa", "epam")
                                ))
                );

        final List<Employee> mappedEmployees =
                Traversable.from(employees)
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                        .map(e -> e.withJobHistory(replaceQA(e.getJobHistory())))
                        .force();

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

    private List<JobHistoryEntry> replaceQA(List<JobHistoryEntry> jobHistory) {
        return Traversable.from(jobHistory)
                .map(e -> e.withPosition(e.getPosition().equals("qa") ? "QA" : e.getPosition()))
                .force();
    }

    private List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> jobHistory) {
        return Traversable.from(jobHistory)
                .map(e -> e.withDuration(e.getDuration() + 1))
                .force();
    }

    @Test
    public void testTravesableFilter() {
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
                                        new JobHistoryEntry(5, "qa", "epam")
                                ))
                );

        final List<Employee> filteredEmployees =
                Traversable.from(employees)
                        .filter(e -> e.getPerson().getLastName().equals("White"))
                        .force();

        final List<Employee> expected =
                Arrays.asList(
                        new Employee(
                                new Person("c", "White", 50),
                                Collections.singletonList(
                                        new JobHistoryEntry(5, "qa", "epam")
                                ))
                );

        assertEquals(expected, filteredEmployees);
    }

    @Test
    public void testTraversableFlatMap() {
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
                                        new JobHistoryEntry(5, "qa", "epam")
                                ))
                );

        final List<JobHistoryEntry> filteredEmployees =
                Traversable.from(employees)
                        .flatMap(e -> Traversable.from(e.getJobHistory()))
                        .force();

        final List<JobHistoryEntry> expected =
                Arrays.asList(
                        new JobHistoryEntry(2, "dev", "epam"),
                        new JobHistoryEntry(1, "dev", "google"),
                        new JobHistoryEntry(3, "qa", "yandex"),
                        new JobHistoryEntry(1, "qa", "epam"),
                        new JobHistoryEntry(1, "dev", "abc"),
                        new JobHistoryEntry(5, "qa", "epam")
                );

        assertEquals(expected, filteredEmployees);
    }
}