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

import static org.junit.Assert.assertEquals;

public class Mapping {

    private static class MapHelper<T> {
        private final List<T> list;

        public MapHelper(List<T> list) {
            this.list = list;
        }

        public List<T> getList() {
            return list;
        }

        // [T] -> (T -> R) -> [R]
        // [T1, T2, T3] -> (T -> R) -> [R1, R2, R3]
        public <R> MapHelper<R> map(Function<T, R> f) {
            final List<R> result = new ArrayList<>();
            list.forEach(t ->
                    result.add(f.apply(t)));
            return new MapHelper<>(result);
        }

        // [T] -> (T -> [R]) -> [R]

        // map: [T, T, T], T -> [R] => [[], [R1, R2], [R3, R4, R5]]
        // flatMap: [T, T, T], T -> [R] => [R1, R2, R3, R4, R5]
        public <R> MapHelper<R> flatMap(Function<T, List<R>> f) {
            final List<R> result = new ArrayList<R>();
            list.forEach((T t) ->
                    f.apply(t).forEach(result::add)
            );

            return new MapHelper<R>(result);
        }
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
                                        new JobHistoryEntry(5, "qa", "epam")
                                ))
                );

        final List<Employee> mappedEmployees =
                new MapHelper<>(employees)
                .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                .map(e -> e.withJobHistory(qaToUpperCase(e.getJobHistory())))
                .getList();

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

    // [JobHistoryEntry] -> (JobHistoryEntry -> JobHistoryEntry) -> [JobHistoryEntry]
    private static List<JobHistoryEntry> qaToUpperCase(List<JobHistoryEntry> list) {
        return new MapHelper<>(list)
                .map(entry ->
                        entry.getPosition().equals("qa") ? entry.withPosition("QA") : entry)
                .getList();
    }

    // [JobHistoryEntry] -> (JobHistoryEntry -> JobHistoryEntry) -> [JobHistoryEntry]
    private static List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> list) {
        return new MapHelper<>(list)
                .map(entry ->
                        entry.withDuration(entry.getDuration() + 1))
                .getList();
    }

    private static class LazyMapHelper<T, R> {

        private final List<T> list;
        private final Function<T, R> function;

        private LazyMapHelper(List<T> list, Function<T, R> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyMapHelper<T, T> from(List<T> list) {
            return new LazyMapHelper<>(list, Function.identity());
        }

        public List<R> force() {
            return new MapHelper<>(list).map(function).getList();
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            Function<T, R2> newFunction = function.andThen(f);
            return new LazyMapHelper<>(list, newFunction);
        }
    }

    interface  ReachIterable<T> {
        //hasNext and next
        boolean tryGet(Consumer<T> c);


        //TODO boolean anyMatch(Predicate<T> p)
        //TODO boolean allMatch(Predicate<T> p)
        //TODO boolean noneMatch(Predicate<T> p)

        //TODO Optional<T> firstMatch(Predicate<T> p)

        static <T> ReachIterable<T> from(List<T> list) {
            return new ReachIterable<T>() {
                @Override
                public boolean tryGet(Consumer<T> c) {
                    return false;
                }
            };
        }
    }

    interface Traversable<T> {
        void forEach(Consumer<T> c);

        default Traversable<T> filter(Predicate<T> p) {
            final Traversable<T> self = this;
            return new Traversable<T>() {
                @Override
                public void forEach(Consumer<T> c) {
                    self.forEach(t -> {
                        if (p.test(t))
                            c.accept(t);
                    });
                }
            };
        }

        default <R> Traversable<R> flatMap(Function<T, List<R>> f) {
            final Traversable<T> self = this;
            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    self.forEach(
                            t -> f.apply(t)
                                    .forEach(c));
                }
            };
        }

        default <R> Traversable<R> map(Function<T, R> f) {
            final Traversable<T> self = this;
            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    self.forEach(t -> c.accept(f.apply(t)));
                }
            };
        }

        default List<T> toList() {
            final List<T> list = new ArrayList<>();
            forEach(list::add);
            return list;
        }

        static <T> Traversable<T> from(List<T> list) {
            return new Traversable<T>() {
                @Override
                public void forEach(Consumer<T> c) {
                    list.forEach(c);
                }
            };
        }
    }

    @Test
    public void testFlatMap() {
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
                                ))
                );

        System.err.println(
                Arrays.toString(
                        Traversable.from(employees).flatMap(Employee::getJobHistory).toList().toArray()));
    }

    @Test
    public void testFilter() {
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
                                ))
                );

        System.err.println(
                Arrays.toString(
                        Traversable.from(employees).filter(e -> e.getPerson().getFirstName().equals("a")).toList().toArray()));
    }

    @Test
    public void lazy_mapping() {
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
                LazyMapHelper.from(employees)
                .map(e -> e.withPerson(e.getPerson().withFirstName("John"))) // change name to John
                .map(e -> e.withJobHistory(addOneYear(e.getJobHistory()))) // add 1 year to experience duration
                .map(e -> e.withJobHistory(qaToUpperCase(e.getJobHistory()))) // replace qa with QA
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
}
