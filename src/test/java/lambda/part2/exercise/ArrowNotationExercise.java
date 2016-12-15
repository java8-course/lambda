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
        final BiPredicate<Person,Person> compareAges = (p1,p2) -> p1.getAge() == p2.getAge();
        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // getFullName: Person -> String
    private static final Function<Person,String> getFullName = p -> p.getFirstName() + " " + p.getLastName();

    // ageOfPersonWithTheLongestFullName: (Person -> String) -> (Person, Person) -> int
    private static final Function<Function<Person,String>,BiFunction<Person,Person,Integer>>
    ageOfPersonWithTheLongestFullName = (getFullName) -> (person1, person2) ->
            getFullName.apply(person1).compareTo(getFullName.apply(person2)) > 0
            ? person1.getAge() : person2.getAge();


    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String
        final Function<Person, String> getFullName = ArrowNotationExercise.getFullName;

        // (Person, Person) -> Integer
        final BiFunction<Person, Person, Integer> getAgeOfPersonWithTheLongestFullName =
                ageOfPersonWithTheLongestFullName.apply(getFullName);

        assertEquals(
                Integer.valueOf(1),
                getAgeOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
