package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class MyMapping {

    private static class MapHelper<T> {
        private final List<T> list;

        @java.beans.ConstructorProperties({"list"})
        public MapHelper(List<T> list) {
            this.list = list;
        }

        public <R> MapHelper<R> map(Function<T, R> f) {
            List<R> newList = new ArrayList<>(list.size());
            list.forEach(t -> newList.add(f.apply(t)));

            return new MapHelper<>(newList);
        }

        // TODO *
        public <R> MapHelper<R> flatMap(Function<T, List<R>> f) {
            List<R> newList = new ArrayList<>();
            list.forEach(t -> newList.addAll(f.apply(t)));

            return new MapHelper<>(newList);
        }

        public List<T> getList() {
            return this.list;
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
//                .map(TODO) // change name to John
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
//                .map(TODO) // add 1 year to experience duration
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
//                .map(TODO) // replace qa with QA
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

    private List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> jobHistory) {
        List<JobHistoryEntry> newHistory = new ArrayList<>(jobHistory.size());
        jobHistory.forEach(jhe -> newHistory.add(jhe.withDuration(jhe.getDuration() + 1)));

        return newHistory;
    }

    private List<JobHistoryEntry> replaceQA(List<JobHistoryEntry> jobHistory) {
        List<JobHistoryEntry> newHistory = new ArrayList<>(jobHistory.size());
        jobHistory.forEach(jhe -> newHistory.add("qa".equals(jhe.getPosition()) ? jhe.withPosition("QA") : jhe));

        return newHistory;
    }

    private static class LazyMapHelperNext<T, R> {
        private final Function<T, R> function;
        private final List<T> list;

        public LazyMapHelperNext(List<T> list, Function<T, R> function) {
            this.list = list;
            this.function = function;
        }

        public List<R> force() {
            return new MapHelper<>(list).map(function).getList();
        }

        public <R2> LazyMapHelperNext<T, R2> map(Function<R, R2> f) {
            // Function<T, R2> newFunction = t -> f.apply(function.apply(t));
            Function<T, R2> newFunction = function.andThen(f);
            return new LazyMapHelperNext<>(list, newFunction);
        }

        public <R> LazyMapHelper<R> flatMap(Function<T, List<R>> f) {
            List<R> newList = new ArrayList<>();
            list.forEach(t -> newList.addAll(f.apply(t)));
            return new LazyMapHelper<>(newList);


            // Non-lazy flatMap
            /*
            List<R> newList = new ArrayList<>();
            list.forEach(t -> newList.addAll(f.apply(t)));

            return new MapHelper<>(newList);*/
        }
    }

    private static class LazyMapHelper<T> {
        private final List<T> list;

        public LazyMapHelper(List<T> list) {
            this.list = list;
        }

        public List<T> force() {
            return list;
        }

        public <R> LazyMapHelperNext<T, R> map(Function<T, R> f) {
            return new LazyMapHelperNext<>(list, f);
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
                new LazyMapHelper<>(employees)
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                        .map(e -> e.withJobHistory(replaceQA(e.getJobHistory())))
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
