package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class Mapping2 {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @SuppressWarnings("WeakerAccess")
    private static class LazyMapHelper<T, R> {
        private final List<T> list;
        private final BiConsumer<List<T>, List<R>> operations;

        public static <T> LazyMapHelper<T, T> ofList(List<T> list) {
            final BiConsumer<List<T>, List<T>> noOps = (inputList, outputList) -> outputList.addAll(inputList);

            return new LazyMapHelper<>(list, noOps);
        }

        public List<R> force() {
            List<R> result = new ArrayList<>();
            operations.accept(list, result);
            return result;
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            final BiConsumer<List<T>, List<R2>> newOps =
                    (ls, rs) -> {
                        List<R> lBefore = force();
                        for (R item : lBefore)
                            rs.add(f.apply(item));
                    };
            return new LazyMapHelper<>(list, newOps);
        }

        public <R2> LazyMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
            final BiConsumer<List<T>, List<R2>> newOps =
                    (ls, rs) -> {
                        List<R> lBefore = force();
                        for (R item : lBefore)
                            rs.addAll(f.apply(item));
                    };
            return new LazyMapHelper<>(list, newOps);
        }

        public LazyMapHelper<T, R> filter(Predicate<R> p) {
            final BiConsumer<List<T>, List<R>> newOps =
                    (ls, rs) -> {
                        List<R> lBefore = force();
                        for (R item : lBefore)
                            if (p.test(item)) rs.add(item);
                    };
            return new LazyMapHelper<>(list, newOps);
        }
    }

    private final List<Employee> employees =
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
    public void lazy_mapping() {
        final List<Employee> mappedEmployees = LazyMapHelper.ofList(employees)
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

    @Test
    public void flatMapping() {
        final List<JobHistoryEntry> teamExperienceInQA = LazyMapHelper.ofList(employees)
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
