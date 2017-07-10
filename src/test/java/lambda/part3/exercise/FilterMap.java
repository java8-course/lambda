package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
            List<R> newList = new ArrayList<>();
            for (T element : list) {
                newList.add(function.apply(element));
            }
            newActions.add(new Container<>((Function<Object, Object>) function));
            return new LazyCollectionHelper<R>(newList, newActions);
        }

        public List<T> force() {
            List<T> result = new ArrayList<>();
            list.forEach(result::add);
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
        final List<Employee> mappedEmployees = Traversable.from(employees)
                .map(e -> e.withPerson(e.getPerson().withFirstName("John"))).toList();


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
                                )),
                        new Employee(
                                new Person("John", "White", 50),
                                Collections.singletonList(
                                        new JobHistoryEntry(5, "qa", "epam")
                                ))
                );

        assertEquals(mappedEmployees, expectedResult);
    }

    @Test
    public void filtering() {
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

        final List<Employee> filteredEmployees = Traversable.from(employees)
                .filter(e -> e.getPerson().getFirstName().equals("b")).toList();

        final List<Employee> expectedResult =
                Arrays.asList(
                        new Employee(
                                new Person("b", "Doe", 40),
                                Arrays.asList(
                                        new JobHistoryEntry(3, "qa", "yandex"),
                                        new JobHistoryEntry(1, "qa", "epam"),
                                        new JobHistoryEntry(1, "dev", "abc")
                                ))
                );

        assertEquals(filteredEmployees, expectedResult);
    }

    @Test
    public void flatMap() {
        List<JobHistoryEntry> jobHistory2 = Arrays.asList(
                new JobHistoryEntry(3, "qa", "yandex"),
                new JobHistoryEntry(1, "qa", "epam"),
                new JobHistoryEntry(1, "dev", "abc")
        );
        List<JobHistoryEntry> jobHistory1 = Arrays.asList(
                new JobHistoryEntry(2, "dev", "epam"),
                new JobHistoryEntry(1, "dev", "google")
        );
        List<JobHistoryEntry> jobHistory3 = Collections.singletonList(
                new JobHistoryEntry(5, "qa", "epam")
        );

        final List<Employee> employees =
                Arrays.asList(
                        new Employee(
                                new Person("a", "Galt", 30),
                                jobHistory1),
                        new Employee(
                                new Person("b", "Doe", 40),
                                jobHistory2),
                        new Employee(
                                new Person("c", "White", 50),
                                jobHistory3)
                );

        final List<JobHistoryEntry> employeeJobHistories = Traversable.from(employees)
                .flatMap(Employee::getJobHistory).toList();

        for(JobHistoryEntry e : jobHistory2) {
            assertTrue(employeeJobHistories.contains(e));
        }

        for(JobHistoryEntry e : jobHistory1) {
            assertTrue(employeeJobHistories.contains(e));
        }

        for(JobHistoryEntry e : jobHistory3) {
            assertTrue(employeeJobHistories.contains(e));
        }
    }
}
interface Traversable<T> {
    void forEach(Consumer<T> c);

    default <R> Traversable<R> map(Function<T,R> f) {
        final Traversable<T> self = this;
        return new Traversable<R>() {
            @Override
            public void forEach(Consumer<R> c) {
                self.forEach(t -> c.accept(f.apply(t)));
            }
        };
    }

    default Traversable<T> filter(Predicate<T> p) {
        final Traversable<T> self = this;
        return new Traversable<T>() {
            @Override
            public void forEach(Consumer<T> c) {
                self.forEach(t -> {
                    if (p.test(t)) {
                        c.accept(t);
                    }
                });
            }
        };
    }

    default List<T> toList() {
        final List<T> result = new ArrayList<>();
        forEach(result::add);
        return result;
    }

    static <T> Traversable<T> from(List<T> list) {
        return new Traversable<T>() {
            @Override
            public void forEach(Consumer<T> c) {
                list.forEach(c);
            }
        };
    }

    default <R> Traversable<R> flatMap(Function<T,List<R>> f) {
        final Traversable<T> self = this;

        return new Traversable<R>() {
            final List<R> result = new ArrayList<R>();
            @Override
            public void forEach(Consumer<R> c) {
                self.forEach((T t) -> f.apply(t).forEach(c));
            }
        };
    }
}







