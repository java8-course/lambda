package lambda.part3.exercise;

import com.sun.org.apache.regexp.internal.RE;
import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class Mapping {

    private List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> jobs) {
        return new MapHelper<>(jobs).map(j -> j.withDuration(j.getDuration() + 1)).getList();
    }

    private List<JobHistoryEntry> upperCaseQa(List<JobHistoryEntry> jobs) {
        return new MapHelper<>(jobs).map(j -> j.getPosition().equals("qa") ? j.withPosition(j.getPosition().toUpperCase()) : j).getList();
        //TOASK how should i format this code? Should i use statement lambda for better reading?
    }

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
            for (T t : list) {
                result.add(f.apply(t));
            }
            return new MapHelper<>(result);
        }

        // [T] -> (T -> [R]) -> [R]

        // map: [T, T, T], T -> [R] => [[], [R1, R2], [R3, R4, R5]]
        // flatMap: [T, T, T], T -> [R] => [R1, R2, R3, R4, R5]
        public <R> MapHelper<R> flatMap(Function<T, List<R>> f) {
            final List<R> result = new ArrayList<>();
            list.forEach((T t) ->
                    result.addAll(f.apply(t))
            );

            return new MapHelper<>(result);
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
                        .map(e -> e.withJobHistory(upperCaseQa(e.getJobHistory()))).getList();

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


    private static class LazyMapHelper<T, R> {
        private final Function<T, R> function;
        private final List<T> list;


        public LazyMapHelper(List<T> list, Function<T, R> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyMapHelper<T, T> from(List<T> list) {
            return new LazyMapHelper<>(list, Function.identity());
        }

        public List<R> force() {
            final List<R> result = new ArrayList<>();
            for (T t : list) {
                result.add(function.apply(t));
            }
            return result;
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            return new LazyMapHelper<>(list, function.andThen(f));
        }
    }

    private static class LazyFlatMapHelper<T, R> {
        private List<T> list;
        private Function<T, List<R>> function;

        public LazyFlatMapHelper(List<T> list, Function<T, List<R>> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyFlatMapHelper<T, T> from(List<T> list) {
            return new LazyFlatMapHelper<>(list, Arrays::asList);
        }

        public List<R> force() {
            final List<R> result = new ArrayList<>();
            for (T t : list) {
                result.addAll(function.apply(t));
            }
            return result;
        }

        // (T -> boolean) -> (T -> [T])
        // filter: [T1, T2] -> (T -> boolean) -> [T2]
        // flatMap": [T1, T2] -> (T -> [T]) -> [T2]
        public LazyFlatMapHelper<T, R> filter(final Predicate<T> predicate) {
            return new LazyFlatMapHelper<>(list, e -> predicate.test(e) ? function.apply(e) : Collections.emptyList());
        }

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            final Function<R, List<R2>> listFunction = rR2TorListR2(f);
            return flatMap(listFunction);
        }

        // (R -> R2) -> (R -> [R2])
        private <R2> Function<R, List<R2>> rR2TorListR2(Function<R, R2> f) {
            return e -> Collections.singletonList(f.apply(e));
        }

        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
            return new LazyFlatMapHelper<>(list, t -> new MapHelper<>(function.apply(t)).flatMap(f).getList());
        }
    }

    interface Traversable<T> {
        void forEach(Consumer<T> c);

        default List<T> toList() {
            final List<T> result = new ArrayList<>();
            forEach(result::add);
            return result;
        }

        static <T> Traversable<T> from(List<T> list) {
            return new Traversable<T>() {
                @Override
                public void forEach(Consumer<T> c) {
                    list.forEach(c);
                }
            };
        }

        default Traversable<T> filter(Predicate<T> p) {
            return c -> this.forEach(v -> {
                if (p.test(v)) {
                    c.accept(v);
                }
            });
        }

        default <R> Traversable<R> flatMap(Function<T, Traversable<R>> f) {
            return c -> this.forEach(t -> f.apply(t).forEach(c));
        }

        default <R> Traversable<R> map(Function<T, R> f) {
            return c -> this.forEach(t -> c.accept(f.apply(t)));
        }
    }

    @Test
    public void traversable() {
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
                        .filter(e -> e.getPerson().getFirstName().equals("a")).toList();

        final List<Employee> expectedResult =
                Arrays.asList(
                        new Employee(
                                new Person("a", "Galt", 30),
                                Arrays.asList(
                                        new JobHistoryEntry(2, "dev", "epam"),
                                        new JobHistoryEntry(1, "dev", "google")
                                ))
                );

        assertEquals(filteredEmployees, expectedResult);

        final List<JobHistoryEntry> flattedJobs =
                Traversable.from(employees)
                        .filter(e -> e.getPerson().getLastName().length() < 5)
                        .flatMap(e -> Traversable.from(e.getJobHistory())).toList();

        final List<JobHistoryEntry> jobs = Arrays.asList(
                new JobHistoryEntry(2, "dev", "epam"),
                new JobHistoryEntry(1, "dev", "google"),
                new JobHistoryEntry(3, "qa", "yandex"),
                new JobHistoryEntry(1, "qa", "epam"),
                new JobHistoryEntry(1, "dev", "abc")
        );

        assertEquals(flattedJobs, jobs);
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
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                        .map(e -> e.withJobHistory(upperCaseQa(e.getJobHistory()))).force();

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


