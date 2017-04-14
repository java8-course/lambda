package lambda.part2.exercise;

import static org.junit.Assert.assertEquals;

import data.Person;
import org.junit.Test;

import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class ArrowNotationExercise {

    @Test
    public void getAge() {
        // Person -> Integer
        final Function<Person, Integer> getAge = Person::getAge;

        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {

        // compareAges: (Person, Person) -> boolean
        BiPredicate<Person, Person> compareAges = (p1, p2) -> p1.getAge() == p2.getAge();
        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String
        final Function<Person, String> getFullName =
            p -> new StringJoiner(p.getFirstName())
                .add(p.getLastName())
                .toString();

        // (Person, Person) -> Integer
        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName =
            (p1, p2) -> getFullName.apply(p1).length()>getFullName.apply(p2).length()
                        ?p1.getAge()
                        :p2.getAge();

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
