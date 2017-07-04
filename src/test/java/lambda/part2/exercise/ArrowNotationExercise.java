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
        final Function<Person, Integer> getAge = Person::getAge;

        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {
        // compareAges: (Person, Person) -> boolean
        final BiPredicate<Person, Person> compareAges = ((person1, person2) -> person1.getAge() == person2.getAge());

        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // getFullName: Person -> String
    private static String getFullName(final Person person) {
        return person.getFirstName() + " " + person.getLastName();
    }

    // ageOfPersonWithTheLongestFullName: (Person -> String) -> (Person, Person) -> int
    private static BiFunction<Person, Person, Integer> getAgeOfPersonWithTheLongestFullName(Function<Person, String> getFullName) {
        return (person1, person2) -> getFullName.apply(person1).length() > getFullName.apply(person2).length() ?
                person1.getAge() : person2.getAge();
    }

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String
        final Function<Person, String> getFullName = ArrowNotationExercise::getFullName;
        // (Person, Person) -> Integer
        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName = getAgeOfPersonWithTheLongestFullName(getFullName);

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
