package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.*;
import java.util.function.BiConsumer;
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
            return new LazyFlatMapHelper<>(list, Traversable.from(list));
        }

        public List<R> force() {
            return traversable.force();
        }

        // TODO filter
        // (T -> boolean) -> (T -> [T])

        // filter: [T1, T2] -> (T -> boolean) -> [T2]
        public LazyFlatMapHelper<T, R> filter(Predicate<R> filter) {
            return new LazyFlatMapHelper<>(list, traversable.filter(filter));
        }
        // flatMap": [T1, T2] -> (T -> [T]) -> [T2]

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            return new LazyFlatMapHelper<>(list, traversable.map(f));
        }

        // TODO *
        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
            return new LazyFlatMapHelper<>(list, traversable.flatMap(f));
        }
    }

    interface Traversable<T> {
        void forEach(Consumer<T> c);

        default <R> Traversable<R> map(Function<T, R> f) {
            return (c) -> this.forEach(t -> c.accept(f.apply(t)));
        }

        default <R> Traversable<R> flatMap(Function<T, List<R>> f) {
            return (c) -> this.forEach(t -> f.apply(t).forEach(c));
        }

        default Traversable<T> filter(Predicate<T> p) {
            return (c) -> this.forEach(t -> {
                if (p.test(t)) c.accept(t);
            });
        }

        default List<T> force() {
            List<T> result = new ArrayList<>();
            forEach(result::add);
            return result;
        }

        static <T> Traversable<T> from(List<T> l) {
            return l::forEach;
        }
    }

    interface ReachIterable<T> {
        boolean forNext(Consumer<T> c);

        static <T> ReachIterable<T> from(List<T> l) {
            final Iterator iterator = l.iterator();
            return c -> {
                if (iterator.hasNext()) {
                    c.accept((T) iterator.next());
                    return true;
                } else {
                    return false;
                }
            };
        }

        default List<T> force() {
            List<T> result = new ArrayList<T>();
            while (forNext(result::add)) ;
            return result;
        }

        // filter
        default ReachIterable<T> filter(Predicate<T> p) {
            final ReachIterable<T> prev = this;
            return c -> {
                final boolean[] found = {false};
                boolean hasNext = true;
                while (!found[0] && hasNext) {
                    hasNext = prev.forNext(t -> {
                        if (p.test(t)) {
                            c.accept(t);
                            found[0] = true;
                        }
                    });
                }
                return hasNext;
            };
        }

        // map
        default <R> ReachIterable<R> map(Function<T, R> f) {
            final ReachIterable<T> prev = this;
            return c -> prev.forNext(t -> {
                c.accept(f.apply(t));
            });
        }

        // flatMap
        default <R> ReachIterable<R> flatMap(Function<T, List<R>> f) {
            final ReachIterable<T> prev = this;
            ReachIterable<R>[] newOne = new ReachIterable[1];
            prev.forNext(t -> {
                List<R> list = f.apply(t);
                newOne[0] = ReachIterable.from(list);
            });
            return newOne[0];
        }

        // boolean anyMatch(Predicate<T>)
        default boolean anyMatch(Predicate<T> p) {
            final boolean[] found = {false};
            while (!found[0] &&
                    forNext(t -> found[0] = p.test(t)));
            return found[0];
        }

        // boolean allMatch(Predicate<T>)
        default boolean allMatch(Predicate<T> p) {
            final boolean[] allMatch = {true};
            while (allMatch[0] &&
                    forNext(t -> allMatch[0] = allMatch[0] && p.test(t)));
            return allMatch[0];
        }

        // boolean nonMatch(Predicate<T>)
        default boolean nonMatch(Predicate<T> p) {
            return allMatch(p.negate());
        }

        // Optional<T> firstMatch(Predicate<T>)
        default Optional<T> firstMatch(Predicate<T> p){
            Object[] result = new Object[1];
            final boolean[] found = {false};
            while (!found[0] &&
                    forNext(t -> {
                        found[0] = p.test(t);
                        if(found[0]) result[0] = t;
                    }));
            return Optional.ofNullable((T) result[0]);
        }
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


    @Test
    public void test() {
        List<List<Integer>> lists = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5, 6)
        );

        System.out.println(LazyFlatMapHelper
                .from(lists)
                .flatMap(l -> l)
                .filter(t -> t % 2 == 0)
                .map(t -> "ok" + t)
                .force());
    }

    @Test
    public void reach_iter() {
        List<List<Integer>> lists = Arrays.asList(
                Arrays.asList(0, 1, 2, 3, 4, 5),
                Arrays.asList(6, 7, 8, 9, 10));

        ReachIterable<String> r = ReachIterable
                .from(lists)
                .flatMap(l -> l)
                .filter(t -> t % 2 == 0)
                .map(i -> "ok" + i);

//        assertFalse(r.nonMatch(s -> s.contains("2")));
//        assertEquals(r.firstMatch(s -> s.contains("2")).get(), "ok2");
//        assertEquals(r.force(), Arrays.asList("ok2","ok4","ok6","ok8","ok10"));
        System.out.println(r.force());
        System.out.println(r.force());
    }
}
