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

        // [T] -> (T -> [R]) -> [R]
        // [T1, T2, T3] -> (T -> R) -> [R1, R2, R3]
        public <R> MapHelper<R> map (Function<T, R> f) {
            // TODO
            final List<R> result = new ArrayList<>();
            list.forEach(t -> result.add(f.apply(t)));

            return new MapHelper<R>(result);
//            throw new UnsupportedOperationException();
        }

        // [T] -> (T -> [R]) -> [R]

        // map: [T, T, T], T -> [R] => [[], [R1, R2], [R3, R4, R5]]
        // flatMap: [T, T, T], T -> [R] => [R1, R2, R3, R4, R5]
        public <R> MapHelper<R> flatMap(Function<T, List<R>> f) {
            final List<R> result = new ArrayList<R>();
            list.forEach(t ->
                    f.apply(t).forEach(result::add)
            );

            return new MapHelper<R>(result);
        }
    }

    private List<JobHistoryEntry> addOneYear (List<JobHistoryEntry> jobHistory) {
        return new MapHelper<>(jobHistory)
                .map(e -> e.withDuration(e.getDuration() + 1))
                .getList();
    }

    private List<JobHistoryEntry> replaceqaQA (List<JobHistoryEntry> jobHistory) {
        return new MapHelper<>(jobHistory)
                .map(e -> e.withPosition(e.getPosition().equals("qa") ? "QA" : e.getPosition()))
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

//        private List<JobHistoryEntry> addOneYear

        final List<Employee> mappedEmployees =
                new MapHelper<>(employees)
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                        .map(e -> e.withJobHistory(replaceqaQA(e.getJobHistory())))
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


    private static class LazyMapHelper<T, R> {
        List<T> list;
        Function<T,R> function;

        public LazyMapHelper(List<T> list, Function<T, R> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyMapHelper<T, T> from(List<T> list) {

            return new LazyMapHelper<>(list, Function.identity());
        }

        public List<R> force() {
            // TODO
            return new MapHelper<>(list).map(function).getList();
//            throw new UnsupportedOperationException();
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            // TODO
//            throw new UnsupportedOperationException();
            return new LazyMapHelper<>(list, function.andThen(f));
        }

    }

    private static class LazyFlatMapHelper<T, R> {

        public LazyFlatMapHelper(List<T> list, Function<T, List<R>> function) {
        }

        public static <T> LazyFlatMapHelper<T, T> from(List<T> list) {
            throw new UnsupportedOperationException();
        }

        public List<R> force() {
            // TODO
            throw new UnsupportedOperationException();
        }

        // TODO filter
        // (T -> boolean) -> (T -> [T])
        // filter: [T1, T2] -> (T -> boolean) -> [T2]
        // flatMap": [T1, T2] -> (T -> [T]) -> [T2]

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            final Function<R, List<R2>> listFunction = rR2TorListR2(f);
            return flatMap(listFunction);
        }

        // (R -> R2) -> (R -> [R2])
        private <R2> Function<R, List<R2>> rR2TorListR2(Function<R, R2> f) {
            throw new UnsupportedOperationException();
        }

        // TODO *
        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
            throw new UnsupportedOperationException();
        }
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
                        .map(e -> e.withJobHistory(LazyMapHelper.from(e.getJobHistory()).map(
                                j -> new JobHistoryEntry(
                                        j.getDuration() + 1,
                                        j.getPosition().equals("qa") ? "QA" : j.getPosition(),
                                        j.getEmployer()))
                                .force()
                        ))
                /*
                .map(TODO) // change name to John
                .map(TODO) // add 1 year to experience duration
                .map(TODO) // replace qa with QA
                * */
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

    private static class LazyFlatMapHelper<T, R> {
        List<T> list;
        Function<T, R> function;

        public LazyFlatMapHelper(List<T> list, Function<T, R> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyFlatMapHelper<T, T> from(List<T> list) {
            return new LazyFlatMapHelper<>(list, Function.identity());
        }

        public List<R> force() {
            // TODO
            return new MapHelper<>(list).map(function).getList();
//            throw new UnsupportedOperationException();
        }

        // TODO: filter
        // (T -> boolean) -> (T -> [T])
        // filter: [T1, T2] -> (T -> boolean) -> [T2]
        // flatMap: [T1, T2] -> (T -> [T]) -> [T2]
        public LazyFlatMapHelper<T,R> filter(Predicate<R> condition) {
            return flatMap(r -> condition.test(r) ? Collections.singletonList(r) : Collections.emptyList());
        }

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            // TODO
            return flatMap(f.andThen(Collections::singletonList));
        }

        // TODO *
        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
            return new LazyFlatMapHelper<>(list, function.andThen(f).andThen(c -> c.get(0)));
        }
    }

    @Test
    public void lazy_flat_mapping() {
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
                LazyFlatMapHelper.from(employees)
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        .map(e -> e.withJobHistory(LazyFlatMapHelper.from(e.getJobHistory()).map(
                                j -> new JobHistoryEntry(
                                        j.getDuration() + 1,
                                        j.getPosition().equals("qa") ? "QA" : j.getPosition(),
                                        j.getEmployer()))
                                .force()
                        ))
                /*
                .map(TODO) // change name to John
                .map(TODO) // add 1 year to experience duration
                .map(TODO) // replace qa with QA
                * */
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

    interface LazyCollection<T> {
        void forEach(Consumer<T> c);

        default List<T> toList() {
            final ArrayList<T> res = new ArrayList<>();
            forEach(res::add);
            return res;
        }

        default <R> LazyCollection<R> map(Function<T, R> f) {
            final LazyCollection<T> self = this;
            return new LazyCollection<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    self.forEach(t -> c.accept(f.apply(t)));
                }
            };
        }

        default LazyCollection<T> filter(Predicate<T> condition) {
            return c -> forEach(t -> {
                if (condition.test(t)) {
                    c.accept(t);
                }
            });
        }

        default <R> LazyCollection<R> flatMap(Function<T, List<R>> f) {
            return c -> forEach(t -> {
                for (R r : f.apply(t)) {
                    c.accept(r);
                }
            });
        }
    }



    public static class LazyCollectionImp<T> implements LazyCollection<T> {
        private final List<T> list;

        public LazyCollectionImp(List<T> list) {
            this.list = list;
        }

        @Override
        public void forEach(Consumer<T> c) {
            list.forEach(t ->c.accept(t));
        }


        public static <R> LazyCollectionImp<R> from(List<R> list) {
            return new LazyCollectionImp<R>(list);
        }

    }

    @Test
    public void lazyCollection() {
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


        final List<Employee> mappedEmployees = LazyCollectionImp.from(employees)
                .filter(e -> e.getPerson().getLastName().equals("Doe"))
                .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                .map(e -> e.withJobHistory((replaceqaQA(e.getJobHistory()))))
                .toList();


        final List<Employee> expectedResult =
                Arrays.asList(
                        new Employee(
                                new Person("John", "Doe", 40),
                                Arrays.asList(
                                        new JobHistoryEntry(4, "QA", "yandex"),
                                        new JobHistoryEntry(2, "QA", "epam"),
                                        new JobHistoryEntry(2, "dev", "abc")
                                ))
                );

        assertEquals(mappedEmployees, expectedResult);
    }
}
