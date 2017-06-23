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
        // Person -> Integer
        final Function<Person, Integer> getAge = p -> p.getAge();

        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {
        // TODO use BiPredicate
        // compareAges: (Person, Person) -> boolean
        final BiPredicate<Person, Person> compareAges = (p1, p2) -> {
            if (Integer.compare(p1.getAge(), p2.getAge()) == 0) {
                return true;
            } else {
                return false;
            }
        };

//        throw new UnsupportedOperationException("Not implemented");
        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // TODO
    // getFullName: Person -> String
    private static String getFullName(Person person) {
        return person.getFirstName() + " " + person.getLastName();
    }


    // TODO
    // ageOfPersonWithTheLongestFullName: (Person -> String) -> (Person, Person) -> int
    private static BiFunction<Person, Person, Integer>
    ageOfPersonWithTheLongestFullName(Function<Person, String> getProperty) {
        return (p1, p2) -> {
            int p1FullNameLength = getProperty.apply(p1).length();
            int p2FullNameLength = getProperty.apply(p2).length();
            if (Integer.compare(p1FullNameLength, p2FullNameLength) > 0) {
                return p1.getAge();
            } else {
                return p2.getAge();
            }
        };
    }

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String
        final Function<Person, String> getFullName = ArrowNotationExercise::getFullName;

        // (Person, Person) -> Integer
        // TODO use ageOfPersonWithTheLongestFullName(getFullName)
        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName =
                ageOfPersonWithTheLongestFullName(getFullName);

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
