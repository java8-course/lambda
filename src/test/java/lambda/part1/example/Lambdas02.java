package lambda.part1.example;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import data.Person;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Lambdas02 {

    @Test
    public void sortPersons() {
        Person[] persons = {
                new Person("name 3", "lastName 3", 20),
                new Person("name 1", "lastName 2", 40),
                new Person("name 2", "lastName 1", 30)
        };

        Arrays.sort(persons, (o1, o2) -> o1.getLastName().compareTo(o2.getFirstName()));

        assertArrayEquals(persons, new Person[]{
                new Person("name 2", "lastName 1", 30),
                new Person("name 1", "lastName 2", 40),
                new Person("name 3", "lastName 3", 20),
        });
    }

    @Test
    public void findFirstByName_guava() {
        List<Person> persons = ImmutableList.of(
                new Person("name 3", "lastName 3", 20),
                new Person("name 1", "lastName 2", 40),
                new Person("name 2", "lastName 1", 30)
        );

        final Optional<Person> personOptional =
                FluentIterable.from(persons)
                        .firstMatch(p -> p.getFirstName().equals("name 1"));

        if (personOptional.isPresent()) {
            personOptional.get().print();
        }
    }

    @Test
    public void lastNamesSet() {
        List<Person> persons = ImmutableList.of(
                new Person("name 3", "lastName 3", 20),
                new Person("name 1", "lastName 2", 40),
                new Person("name 2", "lastName 1", 30)
        );

        final ImmutableMap<String, Person> personByLastName =
                FluentIterable.from(persons)
                        .uniqueIndex(person -> person.getLastName());

        assertEquals(personByLastName.get("lastName 2"), new Person("name 1", "lastName 2", 40));
    }

}
