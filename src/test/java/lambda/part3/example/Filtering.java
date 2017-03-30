package lambda.part3.example;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Filtering {
    @Test
    void filtering0() {
        final List<Employee> employees =
                Arrays.asList(
                        new Employee(
                                new Person("Bob", "Galt", 30),
                                Arrays.asList(
                                        new JobHistoryEntry(2, "dev", "epam"),
                                        new JobHistoryEntry(1, "dev", "google")
                                )),
                        new Employee(
                                new Person("John", "Galt", 30),
                                Arrays.asList(
                                        new JobHistoryEntry(2, "dev", "epam"),
                                        new JobHistoryEntry(1, "dev", "google")
                                )),
                        new Employee(
                                new Person("John", "Doe", 40),
                                Arrays.asList(
                                        new JobHistoryEntry(3, "QA", "yandex"),
                                        new JobHistoryEntry(1, "QA", "epam"),
                                        new JobHistoryEntry(1, "dev", "abc")
                                )),
                        new Employee(
                                new Person("John", "White", 50),
                                Arrays.asList(
                                        new JobHistoryEntry(5, "QA", "epam")
                                ))
                );

        // Johns with dev experience worked in epam more then 1 year

        final List<Employee> result = new ArrayList<>();
        for (Employee employee : employees) {
            final boolean isJohn = employee.getPerson().getFirstName().equals("John");

        }
    }

    @Nested
    static class FilterUtil<T> {
        private final List<T> list;

        FilterUtil(List<T> list) {
            this.list = list;
        }

        List<T> getList() {
            return list;
        }

        // [T] -> (T -> boolean) -> [T]
        private FilterUtil<T> filter(Predicate<T> condition) {
            final List<T> res = new ArrayList<T>();
            for (T t : list) {
                if (condition.test(t)) {
                    res.add(t);
                }
            }

            return new FilterUtil<T>(res);
        }
    }

    private static boolean hasDevExperience(Employee e) {
        return new FilterUtil<>(e.getJobHistory())
                .filter(j -> j.getPosition().equals("dev"))
                .getList()
                .size() > 0;
    }

    private static boolean workedInEpamMoreThenOneYear(Employee e) {
        return new FilterUtil<>(e.getJobHistory())
                .filter(j -> j.getEmployer().equals("epam"))
                .filter(j -> j.getDuration() > 1)
                .getList()
                .size() > 0;
    }

    @Test
    void filtering() {
        final List<Employee> employees =
                Arrays.asList(
                        new Employee(
                                new Person("Bob", "Galt", 30),
                                Arrays.asList(
                                        new JobHistoryEntry(2, "dev", "epam"),
                                        new JobHistoryEntry(1, "dev", "google")
                                )),
                        new Employee(
                                new Person("John", "Galt", 30),
                                Arrays.asList(
                                        new JobHistoryEntry(2, "dev", "epam"),
                                        new JobHistoryEntry(1, "dev", "google")
                                )),
                        new Employee(
                                new Person("John", "Doe", 40),
                                Arrays.asList(
                                        new JobHistoryEntry(3, "QA", "yandex"),
                                        new JobHistoryEntry(1, "QA", "epam"),
                                        new JobHistoryEntry(1, "dev", "abc")
                                )),
                        new Employee(
                                new Person("John", "White", 50),
                                Arrays.asList(
                                        new JobHistoryEntry(5, "QA", "epam")
                                ))
                );

        final FilterUtil<Employee> johns = new FilterUtil<>(employees)
                .filter(e -> e.getPerson().getFirstName().equals("John"));
        final List<Employee> filteredList = johns
                .filter(Filtering::hasDevExperience)
                .filter(Filtering::workedInEpamMoreThenOneYear)
                .getList();

        assertEquals(filteredList.size(), 1);
        assertEquals(filteredList.get(0).getPerson(), new Person("John", "Galt", 30));
    }

    @Nested
    static class LazyFilterUtil<T> {
        private final List<T> list;
        private final Predicate<T> condition;

        LazyFilterUtil(List<T> list, Predicate<T> condition) {
            this.list = list;
            this.condition = condition;
        }

        LazyFilterUtil(List<T> list) {
            this(list, null);
        }

        List<T> force() {
            if (condition == null) {
                return list;
            }

            return new FilterUtil<>(list).filter(condition).getList();
        }

        private LazyFilterUtil<T> filter(Predicate<T> condition) {
            final Predicate<T> combinedCondition = combine(this.condition, condition);
            return new LazyFilterUtil<T>(list, combinedCondition);
        }

        private Predicate<T> combine(Predicate<T> c1, Predicate<T> c2) {
            if (c1 == null) {
                return c2;
            }

            return c1.and(c2);
        }
    }

    private static boolean workedInEpamMoreThenOneYearLazy(Employee e) {
        return new LazyFilterUtil<>(e.getJobHistory())
                .filter(j -> j.getEmployer().equals("epam"))
                .filter(j -> j.getDuration() > 1)
                .force()
                .size() > 0;
    }


    @Test
    void lazy_filtering() {
        final List<Employee> employees =
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
                                        new JobHistoryEntry(3, "QA", "yandex"),
                                        new JobHistoryEntry(1, "QA", "epam"),
                                        new JobHistoryEntry(1, "dev", "abc")
                                )),
                        new Employee(
                                new Person("John", "White", 50),
                                Collections.singletonList(
                                        new JobHistoryEntry(5, "QA", "epam")
                                ))
                );

        final List<Employee> filteredList = new LazyFilterUtil<>(employees)
                .filter(e -> e.getPerson().getFirstName().equals("John"))
                .filter(Filtering::hasDevExperience)
                .filter(Filtering::workedInEpamMoreThenOneYearLazy)
                .force();

        assertEquals(filteredList.size(), 1);
        assertEquals(filteredList.get(0).getPerson(), new Person("John", "Galt", 30));
    }

}
