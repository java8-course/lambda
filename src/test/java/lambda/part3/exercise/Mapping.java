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

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
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
        private final List<T> list;
        private final Function<T, List<R>> function;

        public LazyFlatMapHelper(List<T> list, Function<T, List<R>> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyFlatMapHelper<T, T> from(List<T> list) {
            return new LazyFlatMapHelper<>(list, Collections::singletonList);
        }

        public List<R> force() {
            return new MapHelper<T>(list).flatMap(function).getList();
        }

        // TODO filter
        // (T -> boolean) -> (T -> [T])
        // filter: [T1, T2] -> (T -> boolean) -> [T2]
        // flatMap": [T1, T2] -> (T -> [T]) -> [T2]

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            final Function<R, List<R2>> listFunction = f.andThen(Collections::singletonList);
            return flatMap(listFunction);
        }

        public LazyFlatMapHelper<T, R> filter(Predicate<R> p) {
            return flatMap(r -> p.test(r) ? Collections.singletonList(r) : Collections.emptyList());
        }

        // TODO *
        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
            Function<T, List<R2>> listFunction = t -> new MapHelper<>(function.apply(t)).flatMap(f).getList();
            return new LazyFlatMapHelper<>(list, listFunction);
        }
    }


    interface ReachIterable<T> {
        boolean tryGet(Consumer<T> c);

        static <T> ReachIterable<T> from(List<T> list) {
            final Iterator<T> it = list.iterator();
            return c -> {
                if (it.hasNext()) {
                    c.accept(it.next());
                    return true;
                } else {
                    return false;
                }
            };
        }


        default List<T> toList() {
            List<T> result = new ArrayList<>();
            while (tryGet(result::add)) ;
            return result;
        }

        default ReachIterable<T> filter(Predicate<T> p) {
            return c -> tryGet(t -> {
                if (p.test(t))
                    c.accept(t);
            });
        }

        default <R> ReachIterable<R> flatMap(Function<T, ReachIterable<R>> f) {
            return c -> tryGet(t -> f.apply(t).toList().forEach(r -> c.accept(r)));
        }

        default <R> ReachIterable<R> map(Function<T, R> f) {
            return c -> tryGet(t -> c.accept(f.apply(t)));
        }


        default boolean anyMatch(Predicate<T> p) {
            return firstMatch(p).isPresent();
        }

        default boolean allMatch(Predicate<T> p) {
            return !anyMatch(p.negate());
        }

        default boolean noneMatch(Predicate<T> p) {
            return !firstMatch(p).isPresent();
        }

        default Optional<T> firstMatch(Predicate<T> p) {
            final Object[] res = new Object[1];
            final boolean[] b = {true};
            while (b[0]) {
                b[0] = tryGet(t -> {
                    if (p.test(t)) {
                        res[0] = t;
                        b[0] = false;
                    }
                });
            }

            return Optional.ofNullable((T) res[0]);
        }

    }

    interface Traversable<T> {
        void forEach(Consumer<T> c);

        default <R> Traversable<R> map(Function<T, R> f) {
            Traversable<T> self = this;
            return c -> self.forEach(t->c.accept(f.apply(t)));
        }

        static <T> Traversable<T> from (List<T> l){
            return l::forEach;
        }

        default Traversable<T> filter(Predicate<T> p) {
            return c -> forEach(t -> {
                if (p.test(t))
                    c.accept(t);
            });
        }

        default <R> Traversable<R> flatMap(Function<T, Traversable<R>> f) {
            return c -> forEach(t -> f.apply(t).forEach(c::accept));
        }


        default List<T> toList() {
            List<T> result = new ArrayList<>();
            forEach(result::add);
            return result;
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

    @Test
    public void reachIterableTest1() {
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

        final List<Employee> testList1 = Arrays.asList(
                new Employee(
                        new Person("c", "White", 50),
                        Collections.singletonList(
                                new JobHistoryEntry(5, "qa", "epam")
                        )),
                new Employee(
                        new Person("c", "Black", 50),
                        Collections.singletonList(
                                new JobHistoryEntry(5, "qa", "epam")
                        ))
        );

        final List<Employee> testList2 = Arrays.asList(
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
                        )),
                new Employee(
                        new Person("d", "Some", 60),
                        Collections.singletonList(
                                new JobHistoryEntry(5, "qa", "epam")
                        ))
        );


        final boolean anyMatch1 =
                ReachIterable.from(employees)
                        .anyMatch(e -> e.equals(testList1.get(0)));

        final boolean anyMatch2 =
                ReachIterable.from(employees)
                        .anyMatch(e -> e.equals(testList1.get(1)));

        final boolean allMatch1 =
                ReachIterable.from(employees)
                        .allMatch(e -> testList1.contains(e));

        final boolean allMatch2 =
                ReachIterable.from(employees)
                        .allMatch(e -> testList2.contains(e));

        final boolean noneMatch =
                ReachIterable.from(employees)
                        .noneMatch(e -> e.equals(testList1.get(1)));

        final Optional<Employee> firstMatch = ReachIterable.from(employees)
                .firstMatch(e -> e.equals(testList1.get(0)));

        assertTrue(anyMatch1);
        assertFalse(anyMatch2);
        assertFalse(allMatch1);
        assertTrue(allMatch2);
        assertTrue(noneMatch);
        assertTrue(firstMatch.isPresent());
    }


    @Test
    public void reachIterableTest2() {
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


        final List<Employee> result = ReachIterable.from(employees)
                .filter(e -> e.getPerson().getAge() >= 40)
                .map(e -> e.withPerson(e.getPerson().withFirstName("Adam")))
                .toList();

        final List<Employee> expectedEmployees =
                Arrays.asList(
                        new Employee(
                                new Person("Adam", "Doe", 40),
                                Arrays.asList(
                                        new JobHistoryEntry(3, "qa", "yandex"),
                                        new JobHistoryEntry(1, "qa", "epam"),
                                        new JobHistoryEntry(1, "dev", "abc")
                                )),
                        new Employee(
                                new Person("Adam", "White", 50),
                                Collections.singletonList(
                                        new JobHistoryEntry(5, "qa", "epam")
                                ))
                );

        final List<JobHistoryEntry> historyEntries =
                ReachIterable.from(employees)
                        .flatMap(e -> ReachIterable.from(e.getJobHistory()))
                        .toList();

        final List<JobHistoryEntry> expectedHistoryEntries =
                Arrays.asList(
                        new JobHistoryEntry(2, "dev", "epam"),
                        new JobHistoryEntry(1, "dev", "google"),
                        new JobHistoryEntry(3, "qa", "yandex"),
                        new JobHistoryEntry(1, "qa", "epam"),
                        new JobHistoryEntry(1, "dev", "abc"),
                        new JobHistoryEntry(5, "qa", "epam")
                );

        assertEquals(expectedEmployees, result);
        assertEquals(expectedHistoryEntries, historyEntries);
    }



    @Test
    public void traversableTest() {
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
                                )),
                        new Employee(
                                new Person("d", "John", 60),
                                Arrays.asList(
                                        new JobHistoryEntry(5, "qa", "epam"),
                                        new JobHistoryEntry(7, "dev", "apple")
                                ))
                );

        final List<Employee> mappedEmployees =
                Traversable.from(employees)
                        .filter(employee -> employee.getPerson().getAge() > 40)
                        .map(e -> e.withPerson(e.getPerson().withFirstName("Donald")))
                        .toList();

        final List<Employee> expectedResult =
                Arrays.asList(
                        new Employee(
                                new Person("Donald", "White", 50),
                                Collections.singletonList(
                                        new JobHistoryEntry(5, "qa", "epam")
                                )),
                        new Employee(
                                new Person("Donald", "John", 60),
                                Arrays.asList(
                                        new JobHistoryEntry(5, "qa", "epam"),
                                        new JobHistoryEntry(7, "dev", "apple")
                                ))
                );

        final List<JobHistoryEntry> historyEntries =
                Traversable.from(employees)
                        .flatMap(e -> Traversable.from(e.getJobHistory()))
                        .toList();

        final List<JobHistoryEntry> expectedHistoryEntries =
                Arrays.asList(
                        new JobHistoryEntry(2, "dev", "epam"),
                        new JobHistoryEntry(1, "dev", "google"),
                        new JobHistoryEntry(3, "qa", "yandex"),
                        new JobHistoryEntry(1, "qa", "epam"),
                        new JobHistoryEntry(1, "dev", "abc"),
                        new JobHistoryEntry(5, "qa", "epam"),
                        new JobHistoryEntry(5, "qa", "epam"),
                        new JobHistoryEntry(7, "dev", "apple")
                );

        assertEquals(mappedEmployees, expectedResult);
        assertEquals(historyEntries, expectedHistoryEntries);
    }
}
