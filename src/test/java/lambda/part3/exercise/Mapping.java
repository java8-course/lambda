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
            list.forEach(t ->
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
            return new MapHelper<>(list).map(function).getList();
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            return new LazyMapHelper<T, R2>(list, function.andThen(f));
        }

        // TODO *
        // public <R> LazyMapHelper<R> flatMap(Function<T, List<R>> f)
    }

    private static class LazyFlatMapHelper<T, R> {
        private final BiConsumer<List<T>, Consumer<R>> forEachR;
        private final List<T> list;

        public LazyFlatMapHelper(List<T> list, BiConsumer<List<T>, Consumer<R>> forEachR) {

            this.list = list;
            this.forEachR = forEachR;
        }

        public static <T> LazyFlatMapHelper<T, T> from(List<T> list) {
            final BiConsumer<List<T>, Consumer<T>> traverse = (ts, c) -> {
                for (T t : ts) {
                    c.accept(t);
                }
            };
            return new LazyFlatMapHelper<>(list, traverse);
        }

        public List<R> force() {
            final List<R> result = new ArrayList<R>();

            forEachR.accept(list, result::add);

            return result;
        }

        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {

            BiConsumer<List<T>, Consumer<R2>> forEachR2 = (ts, consR2) ->
                    forEachR.accept(ts, r ->
                            f.apply(r).forEach(consR2)
                    );
            return new LazyFlatMapHelper<T, R2>(list, forEachR2);
        }

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            return flatMap(f.andThen(Collections::singletonList));
        }

        public LazyFlatMapHelper<T, R> filter(Predicate<R> p) {
            return flatMap(r ->
                    p.test(r)
                            ? Collections.singletonList(r)
                            : Collections.emptyList());
        }
    }

    private static interface Collection<T> {
        void forEach(Consumer<T> c);

        default List<T> toList() {
            final List<T> result = new ArrayList<T>();

            forEach(result::add);

            return result;
        }

        default Collection<T> filter(Predicate<T> p) {
            final Collection<T> self = this;
            return new Collection<T>() {
                @Override
                public void forEach(Consumer<T> c) {
                    self.forEach(t -> {
                        if(p.test(t)) {
                            c.accept(t);
                        }
                    });
                }
            };
        }

        default <R> Collection<R> map(Function<T, R> f) {
            final Collection<T> self = this;
            return new Collection<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    self.forEach(t -> c.accept(f.apply(t)));
                }
            };
        }

        default <R> Collection<R> flatMap(Function<T, Collection<R>> f) {
            final Collection<T> self = this;
            return new Collection<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    self.forEach(t -> {
                        f.apply(t).forEach(c);
                    });
                }
            };
        }
    }

    private static class CollectionImpl<T> implements Collection<T> {


        private final List<T> list;

        public CollectionImpl(List<T> list) {

            this.list = list;
        }

        @Override
        public void forEach(Consumer<T> c) {
            list.forEach(c);
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
                        .map(e -> e.withJobHistory(
                                new LazyMapHelper<>(e.getJobHistory(),
                                j -> new JobHistoryEntry(
                                        j.getDuration() + 1,
                                        j.getPosition().equals("qa") ? "QA" : j.getPosition(),
                                        j.getEmployer()
                                )).force()))

                                e.getJobHistory())
                                .map(j -> new JobHistoryEntry(
                                        j.getDuration() + 1,
                                        j.getPosition().equals("qa") ? "QA" : j.getPosition(),
                                        j.getEmployer()))
                                .getList()
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
