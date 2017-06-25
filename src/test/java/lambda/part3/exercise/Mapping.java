package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
            List<R> mappedList = new ArrayList<>();
            list.forEach(item -> mappedList.add(f.apply(item)));

            return new MapHelper<R>(mappedList);
        }

        // [T] -> (T -> [R]) -> [R]

        // map: [T, T, T], T -> [R] => [[], [R1, R2], [R3, R4, R5]]
        // flatMap: [T, T, T], T -> [R] => [R1, R2, R3, R4, R5]
        public <R> MapHelper<R> flatMap(Function<T, List<R>> f) {
            final List<R> result = new ArrayList<>();
            list.forEach(t -> result.addAll(f.apply(t)));

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
                        .map(e -> e.withJobHistory(replaceQA(e.getJobHistory())))
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

    private List<JobHistoryEntry> replaceQA(List<JobHistoryEntry> jobHistory) {
        return new MapHelper<>(jobHistory)
                .map(j -> {
                    if (j.getPosition().equals("qa")) {
                        return j.withPosition("QA");
                    }
                    return j;
                })
                .getList();
    }

    private List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> jobHistory) {
        return new MapHelper<>(jobHistory)
                .map(j -> j.withDuration(j.getDuration() + 1))
                .getList();
    }

    private static class LazyMapHelper<T, R> {

        private Function<T, R> function;
        private List<T> list;

        public LazyMapHelper(List<T> list, Function<T, R> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyMapHelper<T, T> from(List<T> list) {
            return new LazyMapHelper<>(list, Function.identity());
        }

        public List<R> force() {
            List<R> result = new ArrayList<>();
            list.forEach(item ->
                    result.add(function.apply(item))
            );
            return result;
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            return new LazyMapHelper<>(list, function.andThen(f));
        }

    }

    private static class LazyFlatMapHelper<T, R> {

        private Function<T, List<R>> function;
        private List<T> list;

        public LazyFlatMapHelper(List<T> list, Function<T, List<R>> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyFlatMapHelper<T, T> from(List<T> list) {
            return new LazyFlatMapHelper<>(list, Arrays::asList);
        }

        public List<R> force() {
            List<R> result = new ArrayList<>();
            list.forEach(item ->
                    result.addAll(function.apply(item))
            );

            return result;
        }

        // (T -> boolean) -> (T -> [T])
        // filter: [T1, T2] -> (T -> boolean) -> [T2]
        // flatMap": [T1, T2] -> (T -> [T]) -> [T2]

        public LazyFlatMapHelper<T, R> filter(Predicate<R> p) {
            return flatMap(r ->
                    p.test(r) ? Collections.singletonList(r) : Collections.emptyList());
        }

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            final Function<R, List<R2>> listFunction = rR2TorListR2(f);
            return flatMap(listFunction);
        }

        // (R -> R2) -> (R -> [R2])
        private <R2> Function<R, List<R2>> rR2TorListR2(Function<R, R2> f) {
            return item -> Collections.singletonList(f.apply(item));
        }

        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
            return new LazyFlatMapHelper<>(list, item -> {
                List<R2> r2List = new ArrayList<>();
                function.apply(item)
                        .forEach((R r) -> r2List.addAll(f.apply(r)));

                return r2List;
            });
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
                        .map(e -> e.withJobHistory(addOneYearLazy(e.getJobHistory())))
                        .map(e -> e.withJobHistory(replaceQAlazy(e.getJobHistory())))
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

    private List<JobHistoryEntry> replaceQAlazy(List<JobHistoryEntry> jobHistory) {
        return LazyMapHelper.from(jobHistory)
                .map(j -> {
                    if (j.getPosition().equals("qa")) {
                        return j.withPosition("QA");
                    }
                    return j;
                })
                .force();
    }

    private List<JobHistoryEntry> addOneYearLazy(List<JobHistoryEntry> jobHistory) {
        return LazyMapHelper.from(jobHistory)
                .map(j -> j.withDuration(j.getDuration() + 1))
                .force();
    }

    interface Traversable<T> {
        void forEach(Consumer<T> c);

        default List<T> force() {
            List<T> result = new ArrayList<>();
            forEach(result::add);

            return result;
        }

        default <R> Traversable<R> map(Function<T,R> f) {
            Traversable<T> self = this;

            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    self.forEach(t -> c.accept(f.apply(t)));
                }
            };
        }

        default Traversable<T> filter(Predicate<T> p) {
            Traversable<T> self = this;

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
                    self.forEach(t -> f.apply(t).forEach(c));
                }
            };
        }

        static <T> Traversable<T> from(List<T> l) {
            return new Traversable<T>() {
                @Override
                public void forEach(Consumer<T> c) {
                    l.forEach(c);
                }
            };
        }

    }

    @Test
    public void testTraversable() {
        List<Integer> dataList = Arrays.asList(1, 2, 3, 4, 5);

        assertEquals(
                Arrays.asList("1", "2", "3", "4", "5"),
                Traversable.from(dataList).map(String::valueOf).force()
        );

        assertEquals(
                Arrays.asList(1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5),
                Traversable.from(dataList).flatMap(i -> Collections.nCopies(i, i)).force()
        );

        assertEquals(
                Arrays.asList(4, 5),
                Traversable.from(dataList).filter(i -> i > 3).force()
        );
    }

    interface ReachIterable<T> {

        boolean forNext(Consumer<T> c);

        default ReachIterable<T> filter(Predicate<T> p) {
            ReachIterable<T> self = this;

            return new ReachIterable<T>() {

                @Override
                public boolean forNext(Consumer<T> c) {
                    boolean[] isTested = {false};
                    boolean isNotEnd = true;
                    while (!isTested[0] && isNotEnd) {
                        isNotEnd = self.forNext(t -> {
                            if (p.test(t)) {
                                isTested[0] = true;
                                c.accept(t);
                            }
                        });
                    }

                    return isTested[0];
                }
            };
        }

        default <R> ReachIterable<R> map(Function<T,R> f) {
            ReachIterable<T> self = this;

            return new ReachIterable<R>() {

                @Override
                public boolean forNext(Consumer<R> c) {
                    return self.forNext(t ->
                            c.accept(f.apply(t))
                    );
                }
            };
        }

        default <R> ReachIterable<R> flatMap(Function<T,List<R>> f) {
            ReachIterable<T> self = this;

            return new ReachIterable<R>() {

                private ReachIterable<R> currentIterable =
                        ReachIterable.from(Collections.emptyList());
                private Consumer<T> getNewIterable =
                        (T t) -> currentIterable = ReachIterable.from(f.apply(t));

                @Override
                public boolean forNext(Consumer<R> c) {
                    boolean result = currentIterable.forNext(c);

                    if (!result) {
                        boolean isNotEnd = self.forNext(getNewIterable);
                        if (isNotEnd) {
                            result = currentIterable.forNext(c);
                        }
                    }

                    return result;
                }
            };
        }

        default boolean anyMatch(Predicate<T> p) {
            final boolean[] found = {false};
            boolean isNotEnd = true;
            while (!found[0] && isNotEnd) {
                isNotEnd = forNext(t -> {
                    if (p.test(t)) {
                        found[0] = true;
                    }
                });
            }

            return found[0];
        }

        default boolean allMatch(Predicate<T> p) {
            return !anyMatch(p.negate());
        }

        default boolean nonMatch(Predicate<T> p) {
            return allMatch(p.negate());
        }

        default Optional<T> firstMatch(Predicate<T> p) {
            final Object[] object = new Object[1];
            final boolean[] isMatch = {false};
            boolean isNotEnd = true;
            while (!isMatch[0] && isNotEnd) {
                isNotEnd = forNext(t -> {
                    if (p.test(t)) {
                        isMatch[0] = true;
                        object[0] = t;
                    }
                });
            }

            return Optional.ofNullable((T) object[0]);
        }

        default List<T> force() {
            List<T> result = new ArrayList<>();
            while (forNext(result::add));

            return result;
        }

        static <T> ReachIterable<T> from(List<T> l) {
            return new ReachIterable<T>() {

                private ListIterator<T> listIterator = l.listIterator();

                @Override
                public boolean forNext(Consumer<T> c) {
                    if (listIterator.hasNext()) {
                        c.accept(listIterator.next());
                        return true;
                    } else {
                        return false;
                    }
                }
            };
        }

    }

    @Test
    public void testReachIterableMethodFilter() {
        List<Integer> dataList = Arrays.asList(1, 2, 3, 4, 5);

        assertEquals(
                Arrays.asList(4, 5),
                ReachIterable.from(dataList).filter(i -> i > 3).force()
        );
    }

    @Test
    public void testReachIterableMethodFlatMap() {
        List<Integer> dataList = Arrays.asList(1, 2, 3, 4, 5);

        assertEquals(
                Arrays.asList(1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5),
                ReachIterable.from(dataList).flatMap(i -> Collections.nCopies(i, i)).force()
        );
    }

    @Test
    public void testReachIterableMethodMap() {
        List<Integer> dataList = Arrays.asList(1, 2, 3, 4, 5);

        assertEquals(
                Arrays.asList("1", "2", "3", "4", "5"),
                ReachIterable.from(dataList).map(String::valueOf).force()
        );
    }

    @Test
    public void testReachIterableMethodAnyMatch() {
        assertTrue(ReachIterable.from(Arrays.asList(1, 2, 3, 4, 5))
                .anyMatch(i -> i == 3));

        assertTrue(ReachIterable.from(Arrays.asList(1, 2, 3, 3, 5))
                .anyMatch(i -> i == 3));

        assertFalse(ReachIterable.from(Arrays.asList(1, 2, 4, 4, 5))
                .anyMatch(i -> i == 3));
    }

    @Test
    public void testReachIterableMethodAllMatch() {
        assertTrue(ReachIterable.from(Arrays.asList(1, 1, 1, 1, 1))
                .allMatch(i -> i == 1));

        assertTrue(ReachIterable.from(Collections.singletonList(1))
                .allMatch(i -> i == 1));

        assertFalse(ReachIterable.from(Arrays.asList(1, 2))
                .allMatch(i -> i == 1));
    }

    @Test
    public void testReachIterableMethodNonMatch() {
        assertTrue(ReachIterable.from(Arrays.asList(2, 2, 2, 2, 2))
                .nonMatch(i -> i == 1));

        assertFalse(ReachIterable.from(Arrays.asList(1, 1, 1, 1, 1))
                .nonMatch(i -> i == 1));

        assertFalse(ReachIterable.from(Collections.singletonList(1))
                .nonMatch(i -> i == 1));

        assertFalse(ReachIterable.from(Arrays.asList(1, 2))
                .nonMatch(i -> i == 1));
    }

    @Test
    public void testReachIterableFirstMatch() {
        String actual =
                ReachIterable.from(Arrays.asList("a", "b", "c", "dd", "ee"))
                .firstMatch(s -> s.length() > 1)
                .orElseThrow(AssertionError::new);

        assertEquals("dd", actual);

        Optional<String> optional =
                ReachIterable.from(Arrays.asList("a", "b", "c", "d", "e"))
                .firstMatch(s -> s.length() > 1);

        assertFalse(optional.isPresent());
    }

}
