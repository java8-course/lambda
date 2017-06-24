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
            list.forEach(a -> result.add(f.apply(a)));
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
                        .map(e -> e.withJobHistory(changeAllQAToUppercase(e.getJobHistory())))
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
        return new MapHelper<>(jobHistory)
                .map((JobHistoryEntry job) -> job.withDuration(job.getDuration() + 1)).getList();
    }

    private List<JobHistoryEntry> changeAllQAToUppercase(List<JobHistoryEntry> jobHistory) {
        return new MapHelper<>(jobHistory)
                .map((JobHistoryEntry job) -> {
                    String currentPosition = job.getPosition();
                    return currentPosition.equals("qa") ? job.withPosition("QA") : job;
                })
                .getList();
    }

    private static class LazyMapHelper<T, R> {
        private final List<T> list;
        private Function<T, R> function;

        public LazyMapHelper(List<T> list, Function<T, R> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyMapHelper<T, T> from(List<T> list) {
            return new LazyMapHelper<>(list, Function.identity());
        }

        public List<R> force() {
            List<R> result = new ArrayList<>();
            list.forEach(a -> result.add(function.apply(a)));
            return result;
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            return new LazyMapHelper<>(list, function.andThen(f));
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
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                        .map(e -> e.withJobHistory(changeAllQAToUppercase(e.getJobHistory())))
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

        private final List<T> list;
        private Function<T, List<R>> function;

        public LazyFlatMapHelper(List<T> list, Function<T, List<R>> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyFlatMapHelper<T, T> from(List<T> list) {
            return new LazyFlatMapHelper<>(list, Arrays::asList);
        }

        public List<R> force() {
            List<R> result = new ArrayList<>();
            for (T element : list) {
                result.addAll(function.apply(element));
            }
            return result;
        }

        public LazyFlatMapHelper<T, R> filter(Predicate<T> predicate) {
            return new LazyFlatMapHelper<>(
                    list,
                    element -> predicate.test(element) ?
                            function.apply(element) :
                            Collections.emptyList()
            );
        }

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            final Function<R, List<R2>> listFunction = rR2TorListR2(f);
            return flatMap(listFunction);
        }

        // (R -> R2) -> (R -> [R2])
        private <R2> Function<R, List<R2>> rR2TorListR2(Function<R, R2> mapper) {
            return element -> Collections.singletonList(mapper.apply(element));
        }

        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
            return new LazyFlatMapHelper<>(
                    list,
                    element -> {
                        List<R2> results = new ArrayList<>();
                        function.apply(element).forEach(result -> results.addAll(f.apply(result)));
                        return results;
                    });
        }
    }

    private interface Traversable<T> {
        void forEach(Consumer<T> consumer);

        static <T> Traversable<T> from(final List<T> list) {
            return list::forEach;
        }

        default Traversable<T> filter(final Predicate<T> predicate) {
            final Traversable<T> self = this;
            return consumer -> self.forEach(
                    element -> {
                        if (predicate.test(element)) consumer.accept(element);
                    }
            );
        }

        default <R> Traversable<R> map(final Function<T, R> mapper) {
            final Traversable<T> self = this;
            return consumer -> self.forEach(
                    element -> consumer.accept(mapper.apply(element))
            );
        }

        default <R> Traversable<R> flatMap(final Function<T, Traversable<R>> function) {
            final Traversable<T> self = this;
            return consumer -> self.forEach(
                    collection -> function.apply(collection).forEach(consumer)
            );
        }

        default List<T> force() {
            final List<T> result = new ArrayList<>();
            forEach(result::add);
            return result;
        }
    }
}
