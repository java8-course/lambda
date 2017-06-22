package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import lambda.part3.example.Filtering;
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
            // TODO
            List<R> temp = new ArrayList<>();
            list.forEach(e -> temp.add(f.apply(e)));
            return new MapHelper<R>(temp);

//            throw new UnsupportedOperationException();
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
                        .map(e -> e.withJobHistory(qaToUppercase(e.getJobHistory())))
                /*
                .map(TODO) // change name to John .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                .map(TODO) // add 1 year to experience duration .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                .map(TODO) // replace qa with QA
                * */
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

    private List<JobHistoryEntry> qaToUppercase(List<JobHistoryEntry> jobHistory) {
        MapHelper<JobHistoryEntry> mapHelper = new MapHelper<>(jobHistory);
        return mapHelper.map(e -> e.withPosition(
                e.getPosition().equals("qa") ? "QA" : e.getPosition())).getList();
    }

    private List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> jobHistory) {
        MapHelper<JobHistoryEntry> mapHelper = new MapHelper<>(jobHistory);
        return mapHelper.map(e -> e.withDuration(e.getDuration() + 1)).getList();
    }


    private static class LazyMapHelper<T, R> {
        private final List<T> list;
        private final Function<T, R> function;

        public LazyMapHelper(List<T> list, Function<T, R> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyMapHelper<T, T> from(List<T> list) {
            return new LazyMapHelper<>(list, Function.identity());
        }

        public List<R> force() {
            // TODO
            return new MapHelper<T>(list).map(function).getList();
//            throw new UnsupportedOperationException();
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            // TODO
            Function<T, R2> newFunction = function.andThen(f);
            return new LazyMapHelper<T, R2>(this.list, newFunction);
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
            return new LazyFlatMapHelper<>(list, Collections::singletonList);
        }

        public List<R> force() {
            return new MapHelper<>(list).flatMap(function).getList();
        }

        // TODO filter
        // (T -> boolean) -> (T -> [T])
        // filter: [T1, T2] -> (T -> boolean) -> [T2]
        // flatMap": [T1, T2] -> (T -> [T]) -> [T2]

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            final Function<R, List<R2>> listFunction = f.andThen(Collections::singletonList);
            return flatMap(listFunction);
        }

        // (R -> R2) -> (R -> [R2])
        private <R2> Function<R, List<R2>> rR2TorListR2(Function<R, R2> f) {
            throw new UnsupportedOperationException();
        }
        public LazyFlatMapHelper<T, R> filter (Predicate<R> p){
            return flatMap(r -> p.test(r) ? Collections.singletonList(r) : Collections.emptyList());
        }

        // TODO *
        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
            Function<T, List<R2>> listFunction =
                    t -> new MapHelper<>(function.apply(t)).flatMap(f).getList();
            return new LazyFlatMapHelper<>(list, listFunction);
        }
    }

    interface Traversable<T> {
        void forEach(Consumer<T> c);

        default <R> Traversable<R> map(Function<T, R> f) {
            Traversable<T> self = this;

            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    self.forEach(t -> c.accept(f.apply(t)));
                }
            };
        }

        default <T> List<T> force() {
            Traversable<T> self = (Traversable<T>) this;
            List<T> temp = new ArrayList<>();
            self.forEach(e -> temp.add(e));
            return temp;
        }

        static <T> Traversable<T> from(List<T> l) {

            return new Traversable<T>() {
                @Override
                public void forEach(Consumer<T> c) {
                    l.forEach(t -> c.accept(t));
                }
            };
        }

        default <T> Traversable<T> filter(Predicate<T> p) {
            Traversable<T> self = (Traversable<T>) this;

            return new Traversable<T>() {
                @Override
                public void forEach(Consumer<T> c) {
                    self.forEach(t -> {
                        if (p.test(t)) {
                            c.accept(t);
                        }
                    });
                }
            };
        }

        default <R> Traversable<R> flatMap(Function<T, List<R>> f) {
            Traversable<T> self = this;

            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    self.forEach(t ->
                            f.apply(t).forEach(c));
                }
            };
        }

        //TODO: flatMap, force, filter, test, from
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
                        .map(e -> e.withJobHistory(qaToUppercase(e.getJobHistory())))
                        .force();

        final List<Employee> mappedEmployeesTraversable =
                Traversable.from(employees)
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                        .map(e -> e.withJobHistory(qaToUppercase(e.getJobHistory())))
                        .force();

                /*
                .map(TODO) // change name to John
                .map(TODO) // add 1 year to experience duration
                .map(TODO) // replace qa with QA
                * */

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
        assertEquals(mappedEmployeesTraversable, expectedResult);
    }
}
