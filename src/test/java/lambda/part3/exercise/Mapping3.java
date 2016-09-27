package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class Mapping3 {

    @FunctionalInterface
    interface LazyMapHelper<T> {
        Consumer<List<T>> operations();

        static <T> LazyMapHelper<T> ofList(List<T> list) {
            return withOps(outputList -> outputList.addAll(list));
        }

        // In Java 9, this method will be private.
        static <T> LazyMapHelper<T> withOps(Consumer<List<T>> ops) {
            return () -> ops;
        }

        default List<T> force() {
            List<T> result = new ArrayList<>();
            operations().accept(result);
            return result;
        }

        default <R> LazyMapHelper<R> map(Function<T, R> f) {
            return withOps(rs -> force().forEach(item -> rs.add(f.apply(item))));
        }

        default <R> LazyMapHelper<R> flatMap(Function<T, List<R>> f) {
            return withOps(rs -> force().forEach(item -> rs.addAll(f.apply(item))));
        }

        default LazyMapHelper<T> filter(Predicate<T> p) {
            return withOps(rs -> force().forEach(item -> {
                if (p.test(item)) rs.add(item);
            }));
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
