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
            final List<R> result = new ArrayList<R>();
            list.forEach(t -> result.add(f.apply(t)));
            return new MapHelper<R>(result);
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

    static List<JobHistoryEntry> addOneYear (List<JobHistoryEntry> historyEntries) {
        return new MapHelper<JobHistoryEntry>(historyEntries).map(h -> h.withDuration(h.getDuration() + 1)).getList() ;
    }

    static List<JobHistoryEntry> changeQA (List<JobHistoryEntry> historyEntries) {
        return new MapHelper<JobHistoryEntry>(historyEntries)
                .map(h -> h.getPosition().equals("qa") ? new JobHistoryEntry(h.getDuration(), "QA", h.getEmployer()) : h).getList() ;
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
                LazyMapHelper.from(employees)
                .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                .map(e -> e.withJobHistory(changeQA(e.getJobHistory())))
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

        public LazyFlatMapHelper(List<T> list, Function<T, R> function) {
        }

        public static <T> LazyFlatMapHelper<T, T> from(List<T> list) {
            return new LazyFlatMapHelper<>(list, Function.identity());
        }

        public List<R> force() {
            // TODO
            throw new UnsupportedOperationException();
        }

        public <R2> LazyFlatMapHelper<T, R2> map(Function<R, R2> f) {
            // TODO





            // return flatMap(rR2TorListR2(f))
            throw new UnsupportedOperationException();
        }

        // TODO *
        // public <R> LazyMapHelper<R> flatMap(Function<T, List<R>> f)
    }






    private static class LazyMapHelper<T, R> {

        private final Function<T, R> function;
        private final List<T> list;

        public LazyMapHelper(List<T> list, Function<T, R> function) {
            this.function = function;
            this.list = list;
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
                .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                .map(e -> e.withJobHistory(changeQA(e.getJobHistory())))
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
            final LazyCollection<T> self = this;
            return new LazyCollection<T> () {
                @Override
                public void forEach(Consumer<T> c) {
                    self.forEach(t -> condition.test(t) ? c.accept(t):);
                }




        }

        //filter, flatmap
     }

     public class LazyCollectionImp<T> implements LazyCollection<T> {
        private final List<T> list;

        public LazyCollectionImp(List<T> list) {
            this.list = list;
        }

        @Override
         public void forEach(Consumer<T> c) {
            list.forEach(t ->c.accept(t));
        }



     }
}
