package lambda.part1.exercise;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import data.Person;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Lambdas01Exercise {

    @Test
    public void sortPersonsByAge() {
        Person[] persons = {
                new Person("name 3", "lastName 3", 20),
                new Person("name 1", "lastName 2", 40),
                new Person("name 2", "lastName 1", 30)
        };

        //        Java 7
//        Arrays.sort(persons, new Comparator<Person>() {
//            @Override
//            public int compare(Person o1, Person o2) {
//                return o1.getAge() - o2.getAge();
//            }
//        });

//        Java 8
        persons = Stream.of(persons)
            .sorted(Comparator.comparing(Person::getAge))
            .toArray(Person[]::new);

        assertThat(persons, is(new Person[]{
            new Person("name 3", "lastName 3", 20),
            new Person("name 2", "lastName 1", 30),
            new Person("name 1", "lastName 2", 40),
            }));

        assertArrayEquals(persons, new Person[]{
                new Person("name 3", "lastName 3", 20),
                new Person("name 2", "lastName 1", 30),
                new Person("name 1", "lastName 2", 40),
        });
    }

    @Test
    public void findFirstWithAge30() {
        List<Person> persons = ImmutableList.of(
                new Person("name 3", "lastName 3", 20),
                new Person("name 1", "lastName 2", 30),
                new Person("name 2", "lastName 1", 30)
        );

        Person person = null;

        //        Java 7
//        final Optional<Person> personOptional =
//                FluentIterable.from(persons)
//                .firstMatch(new Predicate<Person>() {
//                    @Override
//                    public boolean apply(Person p) {
//                        return p.getAge() == 30;
//                    }
//                });
//
//        if(personOptional.isPresent())
//            person = personOptional.get();

//        Java 8
        person = persons.stream()
            .filter(p -> p.getAge() ==30)
            .findFirst()
            .orElse(new Person("n","ln", 99999));

        assertEquals(person, new Person("name 1", "lastName 2", 30));
    }
}
