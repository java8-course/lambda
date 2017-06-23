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
            List<R> list = new ArrayList<>();
            for (T t : this.getList())
                list.add(f.apply(t));
            return new MapHelper<R>(list);
        }

        // [T] -> (T -> [R]) -> [R]

        // map: [T, T, T], T -> [R] => [[], [R1, R2], [R3, R4, R5]]
        // flatMap: [T, T, T], T -> [R] => [R1, R2, R3, R4, R5]
        public <R> MapHelper<R> flatMap(Function<T, List<R>> f) {
            final List<R> result = new ArrayList<>();
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
                        .map(e -> e.withJobHistory(changeHistory(e.getJobHistory())))
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

    private List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> jobHistory) {
        List<JobHistoryEntry> list = new ArrayList<>();
        jobHistory.forEach(entry ->
                list.add(new JobHistoryEntry(entry.getDuration() + 1, entry.getPosition(), entry.getEmployer())));

        return list;
    }

    private List<JobHistoryEntry> changeHistory(List<JobHistoryEntry> jobHistory) {
        List<JobHistoryEntry> newHistory = new ArrayList<>();
        jobHistory.forEach(entry -> {
            if (entry.getPosition().equals("qa"))
                newHistory.add(new JobHistoryEntry(entry.getDuration(), "QA", entry.getEmployer()));
            else
                newHistory.add(entry);
        });
        return newHistory;
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
            // TODO
            List<R> result = new ArrayList<>();
            this.list.forEach(t -> result.add(function.apply(t)));
            return result;
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            // TODO

            return new LazyMapHelper(this.list, function.andThen(f));

        }
    }


    private static class LazyFlatMapHelper<T, R> {
        List<T> list;
        Function<T, List<R>> function;

        public LazyFlatMapHelper(List<T> list, Function<T, List<R>> func) {
            this.list = list;
            this.function = func;
        }

        public static <T> LazyFlatMapHelper<T, T> from(List<T> list) {
            return new LazyFlatMapHelper(list, Collections::singletonList);
        }

        public List<R> force() {
            // TODO
            List<R> result = new ArrayList<>();
            list.forEach(t -> function.apply(t).forEach(result::add));
            return result;

        }

        // TODO filter
        // (T -> boolean) -> (T -> [T])
        // filter: [T1, T2] -> (T -> boolean) -> [T2]
        // flatMap": [T1, T2] -> (T -> [T]) -> [T2]

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            Function<R, List<R2>> listFunc = f.andThen((R2 r) -> {
                ArrayList<R2> arrList = new ArrayList<>();
                arrList.add(r);
                return arrList;
            });
            return flatMap(listFunc);
        }

        // TODO *
        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> func) {
            Function<T, List<R2>> listFunc = t -> new MapHelper<>(function.apply(t)).flatMap(func).getList();
            return new LazyFlatMapHelper<>(list, listFunc);
        }
    }


    interface Traversable<T> {
        void forEach(Consumer<T> consumer);

        default <R> Traversable<R> map(Function<T, R> func) {
            Traversable<T> self = this;
            Traversable<R> newTravers = new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> consumer) {
                    self.forEach(t -> consumer.accept(func.apply(t)));
                }
            };
            return newTravers;
        }

        default <R> Traversable<R> flatmap(Function<T, List<R>> f) {
            Traversable<T> self = this;
            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> consumer) {
                    self.forEach((T t) -> f.apply(t).forEach(p -> consumer.accept(p)));
                }
            };
        }

        default <R> List<R> force() {
            List<R> result = new ArrayList<>();
            if (this.getClass().isInstance(this.map(q -> q)) || this.getClass().isInstance(this.flatmap(Collections::singletonList))
             || this.getClass().isInstance(this.filter(p->true)))
                forEach((T t) -> result.add(((R) t)));
            else
                throw new UnsupportedOperationException("None of operations were applied to the origin collection");
            return result;
        }

        default <R> Traversable<R> filter(Predicate<R> condition) {
            Traversable<T> self = this;
            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> consumer) {
                    self.forEach((T t) -> {
                        if (condition.test((R) t))
                            consumer.accept((R) t);
                    });
                }
            };
        }

        static <T> Traversable<T> from(List<T> list) {
            return new Traversable<T>() {
                @Override
                public void forEach(Consumer<T> consumer) {
                    list.forEach(consumer::accept);
                }
            };
        }
    }

    @Test
    public void testTraversableMap() {
        final List<Integer> list = Arrays.asList(5, 3, 2);
        Traversable<Integer> tr = Traversable.from(list);

        List<String> lMap = tr.map(t -> String.valueOf(t * 2)).force();
        assertEquals(new ArrayList<>(Arrays.asList("10", "6", "4")), lMap);
    }

    @Test
    public void testTraversableFlatMap() {
        final List<Integer> list = Arrays.asList(5, 3, 2);
        Traversable<Integer> tr = Traversable.from(list);

        List<String> lFlatMap = tr.flatmap(t -> {
            List<String> temp = new ArrayList<>();
            if (t == 5)
                temp.addAll(Arrays.asList(String.valueOf(t * 2), String.valueOf(t - 3)));
            return temp;
        }).force();

        assertEquals(new ArrayList<>(Arrays.asList("10", "2")), lFlatMap);
    }

    @Test
    public void testTraversableFilter() {
        final List<Integer> list = Arrays.asList(5, 3, 2);
        Traversable<Integer> tr = Traversable.from(list);

        List<String> lMap = tr.map(t -> String.valueOf(t * 2)).filter(f -> f.equals("10")).force();
        assertEquals(new ArrayList<>(Arrays.asList("10")), lMap);
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
                        .map(e -> e.withJobHistory(changeHistory(e.getJobHistory())))
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
