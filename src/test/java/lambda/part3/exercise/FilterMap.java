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

        public <R> LazyCollectionHelper<T> map(Function<T, R> function) {
            // TODO
            List<Container<Object, Object>> newActions = new ArrayList<>(actions);
            newActions.add(new Container<>((Function<Object, Object>) function));
            return new LazyCollectionHelper<>(list, newActions);
        }

        public List<T> force() {
            // TODO
            List<T> result = new ArrayList<>();

            for (T t: list) {
                Object temp = t;
                for (Object action: actions) {
                    if (((Container)action).getFunction() != null) {
                        temp = ((Container) action).getFunction().apply(temp);
                    } else {
                        if ( !((Container) action).getPredicate().test(t)) {
                            temp = null;
                        }
                    }
                }
                if (temp != null) {
                    result.add((T) temp);
                }
            }
            return result;
        }
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
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        .filter(e -> workedAsDev(e))
                        .force();

        final List<Employee> expectedResult =
                Arrays.asList(
                        new Employee(
                                new Person("John", "Galt", 30),
                                Arrays.asList(
                                        new JobHistoryEntry(2, "dev", "epam"),
                                        new JobHistoryEntry(1, "dev", "google")
                                )),
                        new Employee(
                                new Person("John", "Doe", 40),
                                Arrays.asList(
                                        new JobHistoryEntry(3, "qa", "yandex"),
                                        new JobHistoryEntry(1, "qa", "epam"),
                                        new JobHistoryEntry(1, "dev", "abc")
                                ))
                );

        assertEquals(mappedEmployees, expectedResult);
    }

    private static boolean workedAsDev(Employee e) {
        return ! new LazyCollectionHelper<>(e.getJobHistory())
                .filter(j -> j.getPosition().equals("dev"))
                .force()
                .isEmpty();
    }

    final List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> entries) {
        return new LazyCollectionHelper<>(entries)
                .map(jobHistoryEntry -> jobHistoryEntry.withDuration(jobHistoryEntry.getDuration() + 1))
                .force();
    }

    final List<JobHistoryEntry> replaceQaToQA(List<JobHistoryEntry> entries) {
        return new LazyCollectionHelper<>(entries)
                .map(j -> {
                    if (j.getPosition().equals("qa")) {
                        return j.withPosition(j.getPosition().toUpperCase());
                    } else
                        return j;
                })
                .force();
    }
}
