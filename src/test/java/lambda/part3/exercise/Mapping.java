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
            List<R> result = new ArrayList<>();
            list.forEach((t) -> result.add(f.apply(t)));
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

    private List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> jobHistory) {
        return new MapHelper<>(jobHistory)
                .map((e) -> e.withDuration(e.getDuration() + 1))
                .getList();
    }

    private List<JobHistoryEntry> qaToUpperCase(List<JobHistoryEntry> jobHistory) {
        return new MapHelper<>(jobHistory)
                .map((e) -> "qa".equals(e.getPosition()) ? e.withPosition("QA") : e)
                .getList();
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

        final List<Employee> mappedEmployees = new MapHelper<>(employees)
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


    private static class LazyMapHelper<T, R> {

        private List<T> list;
        private Function<T, R> function;

        private LazyMapHelper(List<T> list, Function<T, R> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyMapHelper<T, T> from(List<T> list) {
            return new LazyMapHelper<>(list, Function.identity());
        }

        public List<R> force() {
            List<R> result = new ArrayList<>();
            list.forEach((t) -> result.add(function.apply(t)));
            return result;
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            return new LazyMapHelper<>(list, this.function.andThen(f));
        }

    }

    private List<JobHistoryEntry> lazyAddOneYear(List<JobHistoryEntry> jobHistory) {
        return LazyMapHelper.from(jobHistory)
                .map((e) -> e.withDuration(e.getDuration() + 1))
                .force();
    }

    private List<JobHistoryEntry> lazyQaToUpperCase(List<JobHistoryEntry> jobHistory) {
        return LazyMapHelper.from(jobHistory)
                .map((e) -> "qa".equals(e.getPosition()) ? e.withPosition("QA") : e)
                .force();
    }

    private static class LazyFlatMapHelper<T, R> {

        private List<T> list;
        private Traversable<R> traversable;

        private LazyFlatMapHelper(List<T> list, Traversable<R> traversable) {
            this.list = list;
            this.traversable = traversable;
        }

        public static <T> LazyFlatMapHelper<T, T> from(List<T> list) {
            return new LazyFlatMapHelper<>(list, list::forEach);
        }

        public List<R> force() {
            List<R> result = new ArrayList<>();
            traversable.forEach(result::add);
            return result;
        }

        // TODO filter
        // (T -> boolean) -> (T -> [T])

        // filter: [T1, T2] -> (T -> boolean) -> [T2]
        public LazyFlatMapHelper<T, R> filter(Predicate<R> filter) {
            Traversable<R> newTravers = (c) -> traversable.forEach(t -> {
                if (filter.test(t)) c.accept(t);
            });
            return new LazyFlatMapHelper<>(list, newTravers);
        }
        // flatMap": [T1, T2] -> (T -> [T]) -> [T2]

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            final Function<R, List<R2>> listFunction = rR2TorListR2(f);
            return flatMap(listFunction);
        }

        // (R -> R2) -> (R -> [R2])
        private <R2> Function<R, List<R2>> rR2TorListR2(Function<R, R2> f) {
            return (r) -> Collections.singletonList(f.apply(r));
        }

        // TODO *
        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
            Traversable<R2> newTravers = (c) -> traversable.forEach(t -> f.apply(t).forEach(c));
            return new LazyFlatMapHelper<>(list, newTravers);
        }
    }

    interface Traversable<T> {
        void forEach(Consumer<T> c);
    }

    private List<JobHistoryEntry> flatLazyQaToUpperCase(List<JobHistoryEntry> jobHistory) {
        return LazyFlatMapHelper.from(jobHistory)
                .map((e) -> e.withDuration(e.getDuration() + 1))
                .force();
    }

    private List<JobHistoryEntry> flatLazyAddOneYear(List<JobHistoryEntry> jobHistory) {
        return LazyFlatMapHelper.from(jobHistory)
                .map((e) -> "qa".equals(e.getPosition()) ? e.withPosition("QA") : e)
                .force();
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
                .map(e -> e.withJobHistory(lazyAddOneYear(e.getJobHistory())))
                .map(e -> e.withJobHistory(lazyQaToUpperCase(e.getJobHistory())))
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


    @Test
    public void lazy_flat_mapping() {
        Employee aGalt = new Employee(
                new Person("a", "Galt", 30),
                Arrays.asList(
                        new JobHistoryEntry(2, "dev", "epam"),
                        new JobHistoryEntry(1, "dev", "google")
                ));
        Employee bDoe = new Employee(
                new Person("b", "Doe", 40),
                Arrays.asList(
                        new JobHistoryEntry(3, "qa", "yandex"),
                        new JobHistoryEntry(1, "qa", "epam"),
                        new JobHistoryEntry(1, "dev", "abc")
                ));
        Employee cWhite = new Employee(
                new Person("c", "White", 50),
                Collections.singletonList(
                        new JobHistoryEntry(5, "qa", "epam")
                ));
        Employee cBlack = new Employee(
                new Person("c", "Black", 50),
                Collections.singletonList(
                        new JobHistoryEntry(5, "qa", "epam")
                ));

        final List<List<Employee>> employees =
                Arrays.asList(
                        Arrays.asList(aGalt, bDoe),
                        Arrays.asList(cWhite));

        final List<Employee> mappedEmployees = LazyFlatMapHelper.from(employees)
                .flatMap(l -> l)
                .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                .map(e -> e.withJobHistory(flatLazyAddOneYear(e.getJobHistory())))
                .map(e -> e.withJobHistory(flatLazyQaToUpperCase(e.getJobHistory())))
                .filter(e -> !"Black".equals(e.getPerson().getLastName()))
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
