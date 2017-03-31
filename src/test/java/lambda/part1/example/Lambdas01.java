package lambda.part1.example;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import data.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

// JSR-335 Lambda Expressions for the Java Programming Language

// https://github.com/Vyacheslav-Lapin/lambda
//@ExtendWith()
class Lambdas01{

    @Test
    void sortPersons() {
        Person[] persons = {
                new Person("name 3", "lastName 3", 20),
                new Person("name 1", "lastName 2", 40),
                new Person("name 2", "lastName 1", 30)
        };

        Arrays.sort(persons, new Comparator<Person>() {
            public int compare(Person o1, Person o2) {
                return o1.getLastName().compareTo(o2.getLastName());
            }
        });

        assertArrayEquals(persons, new Person[]{
                new Person("name 2", "lastName 1", 30),
                new Person("name 1", "lastName 2", 40),
                new Person("name 3", "lastName 3", 20)
        });
    }

    @Test
    void findFirstByName_foreach() {
        List<Person> persons = ImmutableList.of(
                new Person("name 3", "lastName 3", 20),
                new Person("name 1", "lastName 2", 40),
                new Person("name 2", "lastName 1", 30)
        );

        Person person = null;

        for (Person p : persons) {
            if (p.getFirstName().equals("name 1")) {
                person = p;
                break;
            }
        }

        if (person != null) {
            person.print();
        }
    }

    @Test
    void findFirstByName_guava() {
        List<Person> persons = ImmutableList.of(
                new Person("name 3", "lastName 3", 20),
                new Person("name 1", "lastName 2", 40),
                new Person("name 2", "lastName 1", 30)
        );

        final Optional<Person> personOptional =
                FluentIterable.from(persons)
                        .firstMatch(new Predicate<Person>() {
                            public boolean apply(Person p) {
                                return p.getFirstName().equals("name 1");
                            }
                        });

        if (personOptional.isPresent()) {
            personOptional.get().print();
        }
    }

    @Test
    void lastNamesSet() {
        List<Person> persons = ImmutableList.of(
                new Person("name 3", "lastName 3", 20),
                new Person("name 1", "lastName 2", 40),
                new Person("name 2", "lastName 1", 30)
        );

        final Map<String, Person> personByLastName =
                FluentIterable.from(persons)
                        .uniqueIndex(new Function<Person, String>() {
                            @Override
                            public String apply(Person person) {
                                return person.getLastName();
                            }
                        });

        assertEquals(personByLastName.get("lastName 3"), new Person("name 3", "lastName 3", 20));
    }

}
