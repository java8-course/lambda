package lambda.part2.exercise;

import data.Person;
import org.junit.Test;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;

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
        BiPredicate<Person, Person> compareAges = (person, person2) -> person.getAge() == person2.getAge();

        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // getFullName: Person -> String
    public String getFullName(Person p) {
        return String.format("%s %s", p.getFirstName(), p.getLastName());
    }

    // ageOfPersonWithTheLongestFullName: (Person -> String) -> (Person, Person) -> int
    public ToIntBiFunction<Person, Person> ageOfPersonWithTheLongestFullName(Function<Person, String> fullName) {
        return (p1, p2) -> fullName.apply(p1).length() > fullName.apply(p2).length() ? p1.getAge() : p2.getAge();
    }

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String
        final Function<Person, String> getFullName = this::getFullName;

        // (Person, Person) -> Integer
        final ToIntBiFunction<Person, Person> ageOfPersonWithTheLongestFullName =
                ageOfPersonWithTheLongestFullName(getFullName);

        assertEquals(
                1,
                ageOfPersonWithTheLongestFullName.applyAsInt(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
