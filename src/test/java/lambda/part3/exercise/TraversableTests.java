package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class TraversableTests {

    private List<Employee> employees;

    private interface Traversable<T>{
        void forEach(final Consumer<T> consumer);

        static <T> Traversable from(final List<T> list){
            return null;
        }

        default <R> Traversable map(final Function<T, R> mapper){
            return null;
        }

        default Traversable<T> filter(final Predicate<T> predicate){
            return null;
        }

        default <R> Traversable<R> flatMap(final Function<T, Traversable<R>> function){
            return null;
        }

        default List<T> toList(){
            return null;
        }
    }

    @Before
    public void init(){
        employees =
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
    }

    @Test
    public void testThatWeCanCreateTraversableAndGetAListFromIt() {

    }
}
