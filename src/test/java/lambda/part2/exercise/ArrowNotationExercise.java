package lambda.part2.exercise;

import data.Person;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ArrowNotationExercise {

    @Test
    public void getAge() {
        final Function<Person, Integer> getAge = Person::getAge;

        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {
        final BiPredicate<Person, Person> compareAges = (p1, p2) -> Integer.valueOf(p1.getAge()).compareTo(p2.getAge()) == 0;

        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    private static String getFullName(Person person) {
        return person.getFirstName() + " " + person.getLastName();
    }

    private static BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName (Function<Person, String> getFullName) {
        return (p1, p2) -> {
            if (getFullName.apply(p1).length() > getFullName.apply(p2).length()) {
                return p1.getAge();
            }
            return p2.getAge();
        };
    }

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        final Function<Person, String> getFullName = ArrowNotationExercise::getFullName;

        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName = ageOfPersonWithTheLongestFullName(getFullName);

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
