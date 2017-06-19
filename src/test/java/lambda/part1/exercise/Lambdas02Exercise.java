package lambda.part1.exercise;

import com.google.common.collect.ImmutableList;
import data.Person;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Lambdas02Exercise {
    @Test
    public void sortPersonsByAge() {
        Person[] persons = {
                new Person("name 3", "lastName 3", 20),
                new Person("name 1", "lastName 2", 40),
                new Person("name 2", "lastName 1", 30)
        };

        // TODO use Arrays.sort
        Arrays.sort(persons, (o1,o2) -> {
                if (o1.getAge() < o2.getAge())
                    return -1;
                else if (o1.getAge() > o2.getAge())
                    return 1;
                else
                    return 0;

            }
        );
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

        // TODO use FluentIterable
        final Optional<Person> personOptional =
                FluentIterable.from(persons)
                        .firstMatch(p ->  p.getAge() == 30
                   );
        assertEquals(new Person("name 1", "lastName 2", 30), personOptional.get());

    }
}
