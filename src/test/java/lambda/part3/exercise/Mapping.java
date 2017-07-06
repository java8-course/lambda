package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
            throw new UnsupportedOperationException();
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

        public LazyMapHelper(List<T> list, Function<T, R> function) {
        }

        public static <T> LazyMapHelper<T, T> from(List<T> list) {
            return new LazyMapHelper<>(list, Function.identity());
        }

        public List<R> force() {
            // TODO
            throw new UnsupportedOperationException();
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            // TODO
            throw new UnsupportedOperationException();
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

    interface ReachIterable<T> {
        boolean tryGet(Consumer<T> consumer);

        static<T> ReachIterable<T> from(List<T> list) {
            final Iterator<T> iterator = list.iterator();
            return new ReachIterable<T>() {
                @Override
                public boolean tryGet(Consumer<T> consumer) {
                    if (iterator.hasNext()) {
                        consumer.accept(iterator.next());
                        return true;
                    } else {
                        return false;
                    }
                }
            };
        }

        default List<T> force() {
            final List<T> result = new ArrayList<>();
            while (tryGet(result::add));
            return result;
        }

        default ReachIterable<T> filter(Predicate<T> predicate) {
            final ReachIterable<T> self = this;
            return new ReachIterable<T>() {
                @Override
                public boolean tryGet(Consumer<T> consumer) {
                    return self.tryGet(t -> {
                        if (predicate.test(t)) {
                            consumer.accept(t);
                        }
                    });
                }
            };
        }

        default <R> ReachIterable<R> map(final Function<T, R> function) {
            final ReachIterable<T> self = this;
            return new ReachIterable<R>() {
                @Override
                public boolean tryGet(final Consumer<R> consumer) {
                    return self.tryGet(t -> consumer.accept(function.apply(t)));
                }
            };
        }

        default <R> ReachIterable<R> flatMap(Function<T, ReachIterable<R>> function) {
            final ReachIterable<T> self = this;
            return new ReachIterable<R>() {
                private ReachIterable<R> reachIterable;
                @Override
                public boolean tryGet(final Consumer<R> consumer) {
                    boolean res = self.tryGet(t -> reachIterable = function.apply(t));
                    while (reachIterable.tryGet(consumer));
                    return res;
                }
            };
        }

        default boolean anyMatch(Predicate<T> p) {
            return firstMatch(p).isPresent();
        }

        default boolean allMatch(Predicate<T> predicate) {
            return !anyMatch(predicate.negate());
        }

        default boolean noneMatch(Predicate<T> predicate) {
            return !anyMatch(predicate);
        }

        default Optional<T> firstMatch(Predicate<T> predicate) {
            final Object[] objects = new Object[1];
            while (tryGet(t -> {
                if (predicate.test(t)) {
                    objects[0] = t;
                }
            })) {
                if (objects[0] != null) {
                    return Optional.of((T)objects[0]);
                }
            }
            return Optional.empty();
        }
    }

    @Test
    public void reachIterable() {
        final List<Integer> integers = ReachIterable.from(Arrays.asList("1", "2", "3", "4", "5"))
                .map(Integer::valueOf)
                .filter(i -> i % 2 == 0)
                .force();

        assertEquals(integers, Arrays.asList(2,4));

        final Optional<String> stringOptional = ReachIterable.from(Arrays.asList("1", "2", "3", "4", "5"))
                .firstMatch(s -> s.equals("3"));

        assertEquals(stringOptional, Optional.of("3"));

        assertEquals(ReachIterable.from(Collections.singletonList("1")).firstMatch(s -> s.length() == 5), Optional.empty());

        assertTrue(ReachIterable.from(Arrays.asList("1", "2", "3"))
                .noneMatch(s -> s.length() == 3));

        assertTrue(ReachIterable.from(Arrays.asList("1", "2", "3"))
                .allMatch(s -> s.length() == 1));


        final List<Integer> integers2 = ReachIterable.from(Arrays.asList(new String[][]{{"1", "2"}, {"3", "4"}, {"5", "6"}, {}}))
                .flatMap(strings -> ReachIterable.from(Arrays.stream(strings).collect(Collectors.toList())))
                .map(Integer::valueOf)
                .filter(i -> i % 2 == 0)
                .force();

        assertEquals(integers2, Arrays.asList(2,4,6));
    }

    interface Traversable<T> {
        void forEach(Consumer<T> consumer);

        default <R> Traversable<R> flatMap(Function<T, Traversable<R>> function) {
            final Traversable<T> self = this;
            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> consumer) {
                    self.forEach(t -> function.apply(t).forEach(consumer));
                }
            };
        }

        default Traversable<T> filter(Predicate<T> predicate) {
            final Traversable<T> self = this;
            return new Traversable<T>() {
                @Override
                public void forEach(Consumer<T> consumer) {
                    self.forEach(t -> {
                        if (predicate.test(t)) {
                            consumer.accept(t);
                        }
                    });
                }
            };
        }

        default <R> Traversable<R> map(Function<T,R> function) {
            final Traversable<T> self = this;
            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> consumer) {
                    self.forEach(t -> consumer.accept(function.apply(t)));
                }
            };
        }

        default List<T> toList() {
            final List<T> result = new ArrayList<>();
            forEach(result::add);
            return result;
        }

        static <T> Traversable<T> from(List<T> list) {
            return new Traversable<T>() {
                @Override
                public void forEach(Consumer<T> consumer) {
                    list.forEach(consumer);
                }
            };
        }
    }

    @Test
    public void traversable() {
        final List<Integer> integers = Traversable.from(Arrays.asList(new String[][]{{"1", "2"}, {"3", "4"}, {"5", "6"}}))
                .flatMap(strings -> Traversable.from(Arrays.stream(strings).collect(Collectors.toList())))
                .map(Integer::valueOf)
                .filter(i -> i % 2 == 0)
                .toList();

        assertEquals(integers, Arrays.asList(2,4,6));
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
