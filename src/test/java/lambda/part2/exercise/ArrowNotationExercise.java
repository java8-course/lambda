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
        final Function<Person, Integer> getAge = p -> p.getAge(); // TODO
        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {
        // TODO use BiPredicate
        // compareAges: (Person, Person) -> boolean
        final BiPredicate<Person,Person> compareAges = (p1, p2) -> p1.getAge() == p2.getAge();
        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // TODO
    // getFullName: Person -> String
    private final Function<Person,String> getFullName = (p) -> p.getFirstName() + " " + p.getLastName();

    // TODO
    // ageOfPersonWithTheLongestFullName: (Person -> String) -> (Person, Person) -> int
    private final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName (Function<Person,String> getFullName) {
        return ((person, person2) -> getFullName.apply(person).length() > getFullName.apply(person2).length() ? person.getAge() : person2.getAge());
    }

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String
        final Function<Person, String> getFullName = this.getFullName; // TODO

        // (Person, Person) -> Integer
        // TODO use ageOfPersonWithTheLongestFullName(getFullName)
        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName = this.ageOfPersonWithTheLongestFullName(getFullName);

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
