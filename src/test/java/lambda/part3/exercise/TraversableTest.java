package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import lambda.part3.example.Filtering;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by msid on 25.06.17.
 */
public class TraversableTest {
    @Test
    public void map() throws Exception {
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
                Traversable.from(employees)
                        .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                        .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                        .map(e -> e.withJobHistory(qaToUppercase(e.getJobHistory())))
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
    public void filter() throws Exception {
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

        List<Employee> filteredList = Traversable.from(employees)
                .filter(e -> e.getPerson().getFirstName().equals("John"))
                .filter(Filtering::hasDevExperience)
                .filter(Filtering::workedInEpamMoreThenOneYear)
                .force();

        assertEquals(filteredList.size(), 1);
        assertEquals(filteredList.get(0).getPerson(), new Person("John", "Galt", 30));

    }

    @Test
    public void flatMap() throws Exception {
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(2);
        integers.add(3);
        integers.add(4);

        List<Integer> force = Traversable
                .from(integers)
                .flatMap(i -> i > 2 ?
                        Collections.singletonList(i) :
                        Collections.emptyList()).force();


        System.out.println(force);

    }

    private List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> jobHistory) {
        return Traversable.from(jobHistory).map(j -> j.withDuration(j.getDuration() + 1)).force();
    }

    private List<JobHistoryEntry> qaToUppercase(List<JobHistoryEntry> jobHistory) {
        return Traversable.from(jobHistory)
                .map(j ->
                        j.getPosition().equals("qa") ?
                                j.withPosition("QA") :
                                j).force();
    }

}