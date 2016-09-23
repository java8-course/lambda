package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

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
            throw new UnsupportedOperationException();
        }

        // TODO *
        // public <R> MapHelper<R> flatMap(Function<T, List<R>> f)
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


    private static class LazyMapHelperNext<T, R> {
        private final Function<T, R> funtion;
        private final List<T> list;

        public LazyMapHelperNext(List<T> list, Function<T, R> funtion) {
            // TODO
            throw new UnsupportedOperationException();
        }

        public List<R> force() {
            // TODO
            throw new UnsupportedOperationException();
        }

        public <R2> LazyMapHelperNext<T, R2> map(Function<R, R2> f) {
            // TODO
            throw new UnsupportedOperationException();
        }

        // TODO *
        // public <R> LazyMapHelper<R> flatMap(Function<T, List<R>> f)
    }

    private static class LazyMapHelper<T> {
        public LazyMapHelper(List<T> list) {
            // TODO
            throw new UnsupportedOperationException();
        }

        public List<T> force() {
            // TODO
            throw new UnsupportedOperationException();
        }

        public <R> LazyMapHelperNext<T, R> map(Function<T, R> f) {
            // TODO
            throw new UnsupportedOperationException();
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
