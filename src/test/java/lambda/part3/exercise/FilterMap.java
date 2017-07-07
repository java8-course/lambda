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
import java.util.function.Predicate;

import static lambda.part3.exercise.Mapping.addOneYear;
import static lambda.part3.exercise.Mapping.changeToQa;
import static org.junit.Assert.assertEquals;

public class FilterMap {

    public static class Container<T, R> {
        private final Predicate<T> predicate;
        private final Function<T, R> function;

        public Container(Predicate<T> predicate) {
            this.predicate = predicate;
            this.function = null;
        }

        public Container(Function<T, R> function) {
            this.function = function;
            this.predicate = null;
        }

        public Predicate<T> getPredicate() {
            return predicate;
        }

        public Function<T, R> getFunction() {
            return function;
        }
    }

    public static class LazyCollectionHelper<T> {
        private final List<Container<Object, Object>> actions;
        private final List list;

        private LazyCollectionHelper(List list, List<Container<Object, Object>> actions) {
            this.actions = actions;
            this.list = list;
        }

        public LazyCollectionHelper(List<T> list) {
            this( list, new ArrayList<>());
        }

        public LazyCollectionHelper<T> filter(Predicate<T> condition) {
            actions.add(new Container<>((Predicate<Object>) condition));
            return this;
        }

        public <R> LazyCollectionHelper<R> map(Function<T, R> function) {
            actions.add(new Container<>((Function<Object, Object>) function));
            return new LazyCollectionHelper<R>(list, actions);
        }

        public List<T> force() {
            List<Object> result = new ArrayList<>(list);
            for (Container<Object, Object> action : actions) {
                List<Object> tmp = new ArrayList<>();
                if (action.getFunction() != null)
                    result.forEach(element ->
                        tmp.add(action.getFunction().apply(element)));
                else result.forEach(element -> {
                            if (action.getPredicate().test(element))
                                tmp.add(element);
                        });
                result = tmp;
            }
            final ArrayList<T> ret = new ArrayList<>();
            result.forEach(e -> ret.add((T) e));
            return ret;
        }
    }

    @Test
    public void createEmployeesAndGetEpamEmployeesHistory() {
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
                                        new JobHistoryEntry(5, "qa", "JoshkinaLolo")
                                ))
                );

        final List<Employee> mappedEmployees =
                new LazyCollectionHelper(employees)
                        .map(e -> ((Employee)e).withPerson(((Employee)e).getPerson().withFirstName("John")))
                        .map(e -> ((Employee)e).withJobHistory(addOneYear(((Employee)e).getJobHistory())))
                        .map(e -> ((Employee)e).withJobHistory(changeToQa(((Employee)e).getJobHistory())))
                        .filter(e -> ((Employee)e).getJobHistory().stream().anyMatch(j -> j.getEmployer().equals("epam")))
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
                                ))
                );

        assertEquals(mappedEmployees, expectedResult);
    }
}
