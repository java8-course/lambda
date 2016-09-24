package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static lambda.part3.exercise.FilterMapWithFlatmap.Container.Contype.*;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("WeakerAccess")
public class FilterMapWithFlatmap {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class Container {
        enum Contype {
            WITH_PREDICATE,
            WITH_FUNCTION,
            WITH_FLAT_FUNCTION
        }

        private final Predicate predicate;
        private final Function function;
        private final Contype type;

        public static Container withPredicate(Predicate predicate) {
            return new Container(predicate, null, WITH_PREDICATE);
        }

        public static Container withFunction(Function function) {
            return new Container(null, function, WITH_FUNCTION);
        }

        public static Container withFlatFunction(Function function) {
            return new Container(null, function, WITH_FLAT_FUNCTION);
        }
    }

    @SuppressWarnings("unchecked")
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
            newActions.add(Container.withPredicate((Predicate) condition));
            return new LazyCollectionHelper(list, newActions);
        }

        public <R> LazyCollectionHelper<R> map(Function<T, R> function) {
            List<Container> newActions = new ArrayList<>(actions);
            newActions.add(Container.withFunction((Function) function));
            return new LazyCollectionHelper(list, newActions);
        }

        public <R> LazyCollectionHelper<R> flatMap(Function<T, List<R>> function) {
            List<Container> newActions = new ArrayList<>(actions);
            newActions.add(Container.withFlatFunction((Function) function));
            return new LazyCollectionHelper(list, newActions);
        }

        public List<T> force() {
            List currentResult = list;
            for (Container action : actions) {
                List nextResult = new ArrayList(currentResult.size());
                for (Object o : currentResult)
                    switch (action.getType()) {
                        case WITH_PREDICATE:
                            if (action.getPredicate().test(o))
                                nextResult.add(o);
                            break;
                        case WITH_FUNCTION:
                            nextResult.add(action.getFunction().apply(o));
                            break;
                        case WITH_FLAT_FUNCTION:
                            nextResult.addAll((List) action.getFunction().apply(o));
                            break;
                    }
                currentResult = nextResult;
            }
            return (List<T>) currentResult;
        }
    }

    List<Employee> employees =
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

    @Test
    public void mapping() {
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

    @Test
    public void flatMapping() {
        final List<JobHistoryEntry> teamExperienceInQA =
                new LazyCollectionHelper<>(employees)
                        .flatMap(Employee::getJobHistory)
                        .filter(jhe -> jhe.getPosition().equals("qa"))
                        .force();
        assertEquals(3, teamExperienceInQA.size());
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