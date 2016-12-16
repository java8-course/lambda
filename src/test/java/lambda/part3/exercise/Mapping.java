package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
            List<R> newList = new ArrayList<>();
            list.forEach(t -> newList.add(f.apply(t)));
//            for (T t : list) {
//                newList.add(f.apply(t));
//            }
            return new MapHelper<>(newList);
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
                        .map(e -> e.withJobHistory(new MapHelper<>(e.getJobHistory())
                                .map(j -> new JobHistoryEntry(
                                        j.getDuration() + 1,
                                        j.getPosition().equals("qa") ? "QA" : j.getPosition(),
                                        j.getEmployer()))
                                .getList()
                        ))
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
        Function<T, R> function;

        public LazyMapHelper(List<T> list, Function<T, R> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyMapHelper<T, T> from(List<T> list) {
            return new LazyMapHelper<>(list, Function.identity());
        }

        public List<R> force() {
            return new MapHelper<>(list).map(function).getList();
            // TODO
//            throw new UnsupportedOperationException();
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            // TODO
            return new LazyMapHelper<>(list, function.andThen(f));
//            throw new UnsupportedOperationException();
        }
    }

    private static class LazyFlatMapHelper<T, R> {
        List<T> list;
        Function<T, List<R>> function;

        public LazyFlatMapHelper(List<T> list, Function<T, List<R>> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyFlatMapHelper<T, T> from(List<T> list) {
            return new LazyFlatMapHelper<>(list, Collections::singletonList);
        }

        public List<R> force() {
            return new MapHelper<>(list).flatMap(function).getList();
            // TODO
//            throw new UnsupportedOperationException();
        }

        // TODO: filter
        // (T -> boolean) -> (T -> [T])
        // filter: [T1, T2] -> (T -> boolean) -> [T2]
        // flatMap: [T1, T2] -> (T -> [T]) -> [T2]
        public LazyFlatMapHelper<T, R> filter(Predicate<R> f) {
            return flatMap(r -> f.test(r) ? Collections.singletonList(r) : Collections.emptyList());
        }

//        // (R -> R2) -> (R -> [R2])
//        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
//            return flatMap(function.andThen(f).andThen(Collections::singletonList));
//            // TODO
////            throw new UnsupportedOperationException();
//        }
//
//        // TODO *
//        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
//            return new LazyFlatMapHelper<T, R2>(list,
////            throw new NotImplementedException();
//        }

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            return flatMap(f.andThen(Collections::singletonList));
        }

        // [R] -> (R -> [R2]) -> [R2]
        // TODO *
        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
            return new LazyFlatMapHelper<>(list, combine(function, f));
        }

        private <R2> Function<T, List<R2>> combine(Function<T, List<R>> function, Function<R, List<R2>> f) {
            return t -> {
                List<R2> list = new ArrayList<>();
                function.apply(t).forEach(r -> list.addAll(f.apply(r)));
                return list;
            };
        }
    }

    public interface LazyCollection<T> {
        void forEach(Consumer<T> consumer);

        default List<T> toList() {
            List<T> list = new ArrayList<>();
            forEach(list::add);
            return list;
        }

        default <R> LazyCollection<R> map(Function<T, R> f) {
            LazyCollection<T> self = this;
            return new LazyCollection<R>() {
                @Override
                public void forEach(Consumer<R> consumer) {
                    self.forEach(t -> consumer.accept(f.apply(t)));
                }
            };
        }

        default <R> LazyCollection<R> flatMap(Function<T, List<R>> f) {
            LazyCollection<T> self = this;
            return consumer -> self.forEach(t -> f.apply(t).forEach(consumer));
        }

        default LazyCollection<T> filter(Predicate<T> f) {
            LazyCollection<T> self = this;
            return consumer -> self.forEach(t -> {
                if (f.test(t)) {
                    consumer.accept(t);
                }
            });
        }
    }

    public static class MyLazyCollection<T> implements LazyCollection<T> {
        List<T> list;

        private MyLazyCollection(List<T> list) {
            this.list = list;
        }

        public static <T> LazyCollection<T> from(List<T> list) {
            return new MyLazyCollection<>(list);
        }

        @Override
        public void forEach(Consumer<T> consumer) {
            list.forEach(consumer);
        }
    }

    @Test
    public void my_lazy_collection() {
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
                MyLazyCollection.from(employees)
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        .map(e -> e.withJobHistory(MyLazyCollection.from(e.getJobHistory()).map(
                                j -> new JobHistoryEntry(
                                        j.getDuration() + 1,
                                        j.getPosition().equals("qa") ? "QA" : j.getPosition(),
                                        j.getEmployer())
                                ).toList()
                        )).toList();

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
}
