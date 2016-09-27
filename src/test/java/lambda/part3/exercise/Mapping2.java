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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class Mapping2 {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @SuppressWarnings("WeakerAccess")
    private static class LazyMapHelper<T> {
        private final Consumer<List<T>> operations;

        public static <T> LazyMapHelper<T> ofList(List<T> list) {
            final Consumer<List<T>> noOps = (outputList) -> outputList.addAll(list);

            return new LazyMapHelper<>(noOps);
        }

        public List<T> force() {
            List<T> result = new ArrayList<>();
            operations.accept(result);
            return result;
        }

        public <R> LazyMapHelper<R> map(Function<T, R> f) {
            final Consumer<List<R>> newOps =
                    (rs) -> {
                        List<T> lBefore = force();
                        for (T item : lBefore)
                            rs.add(f.apply(item));
                    };
            return new LazyMapHelper<>(newOps);
        }

        public <R> LazyMapHelper<R> flatMap(Function<T, List<R>> f) {
            final Consumer<List<R>> newOps =
                    (rs) -> {
                        List<T> lBefore = force();
                        for (T item : lBefore)
                            rs.addAll(f.apply(item));
                    };
            return new LazyMapHelper<>(newOps);
        }

        public LazyMapHelper<T> filter(Predicate<T> p) {
            final Consumer<List<T>> newOps =
                    (rs) -> {
                        List<T> lBefore = force();
                        for (T item : lBefore)
                            if (p.test(item)) rs.add(item);
                    };
            return new LazyMapHelper<>(newOps);
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
