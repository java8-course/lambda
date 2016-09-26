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
        private final List<T> list;

        public LazyCollectionHelper(List<T> list, List<Container<Object, Object>> actions) {
            this.actions = actions;
            this.list = list;
        }

        public LazyCollectionHelper(List<T> list) {
            this(list, new ArrayList<>());
        }

        public LazyCollectionHelper<T> filter(Predicate<T> condition) {
            List<Container<Object, Object>> newActions = new ArrayList<>(actions);
            newActions.add(new Container<>((Predicate<Object>) condition));
            return new LazyCollectionHelper<>(list, newActions);
        }

        public <R> LazyCollectionHelper<R> map(Function<T, R> function) {
            List<Container<Object, Object>> newActions = new ArrayList<>(actions);
            newActions.add(new Container<>((Function<Object, Object>) function));
            return new LazyCollectionHelper<>((List<R>) list, newActions);
        }

        public List<T> force() {
            final List<T> result = new ArrayList<>();
            for (T t : list) {
                boolean present = true;
                for (Container<Object, Object> c : actions) {
                    Function function = c.getFunction();
                    if (function != null) {
                        t = (T) function.apply(t);
                    } else {
                        present = c.getPredicate().test(t);
                        if (!present) break;
                    }
                }
                if (present) {
                    result.add(t);
                }
            }
            return result;
        }
    }

    private static List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> list) {
        Mapping.MapHelper<JobHistoryEntry> helper = new Mapping.MapHelper<>(list);
        return helper.map(j -> j.withDuration(j.getDuration() + 1)).getList();
    }

    private static List<JobHistoryEntry> replace(List<JobHistoryEntry> list) {
        Mapping.MapHelper<JobHistoryEntry> helper = new Mapping.MapHelper<>(list);
        return helper.map(j -> {
            String position = j.getPosition();
            String changed = position.equals("qa") ? position.toUpperCase() : position;
            return j.withPosition(changed);
        }).getList();
    }

    @Test
    public void lazyFilterMap() {
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
                new LazyCollectionHelper<>(employees)
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John"))) // change name to John
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory()))) // add 1 year to experience duration
                        .map(e -> e.withJobHistory(replace(e.getJobHistory())))
                        .filter(e -> e.getPerson().getAge() > 30)// replace qa with QA
                        .force();

        final List<Employee> expectedResult =
                Arrays.asList(
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
