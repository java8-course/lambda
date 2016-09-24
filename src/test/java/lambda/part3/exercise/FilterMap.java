package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("WeakerAccess")
public class FilterMap {

    public static class Container {
        private final Predicate predicate;
        private final Function function;

        public Container(Predicate predicate) {
            this.predicate = predicate;
            this.function = null;
        }

        public Container(Function function) {
            this.function = function;
            this.predicate = null;
        }

        public Predicate getPredicate() {
            return predicate;
        }

        public Function getFunction() {
            return function;
        }
    }

    public static class LazyCollectionHelper<T> {
        private final List<Container> actions;
        private final List list;

        public LazyCollectionHelper(List list, List<Container> actions) {
            this.actions = actions;
            this.list = list;
        }

        public LazyCollectionHelper(List<T> list) {
            this(list, new ArrayList<>());
        }

        public LazyCollectionHelper<T> filter(Predicate<T> condition) {
            List<Container> newActions = new ArrayList<>(actions);
            newActions.add(new Container(condition));
            return new LazyCollectionHelper<>(list, newActions);
        }

        public <R> LazyCollectionHelper<R> map(Function<T, R> function) {
            List<Container> newActions = new ArrayList<>(actions);
            newActions.add(new Container(function));
            return new LazyCollectionHelper<>(list, newActions);
        }

        @SuppressWarnings("unchecked")
        public List<T> force() {
            final List<T> result = new ArrayList<>();
            for (Object o : list) {
                boolean pass = true;
                final Iterator<Container> acterator = actions.iterator();
                while (pass && acterator.hasNext()) {
                    Container action = acterator.next();
                    Predicate aPred = action.getPredicate();
                    if (aPred != null)
                        pass = aPred.test(o);
                    else
                        o = action.getFunction().apply(o);
                }
                if (pass) result.add((T) o);
            }
            return result;
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
                new LazyCollectionHelper<>(employees)
                        // change name to John
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        // add 1 year to experience duration
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                        // Filter out persons older than 40
                        .filter(e -> e.getPerson().getAge() <= 40)
                        // replace qa with QA
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
}