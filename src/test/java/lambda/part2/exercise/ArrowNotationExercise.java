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
        // use BiPredicate

        BiPredicate<Person, Person> compareAges = (p1, p2) -> p1.getAge() == p2.getAge();

        //compareAges: (Person, Person) -> boolean;

        //throw new UnsupportedOperationException("Not implemented");

        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // getFullName: Person -> String
    final Function<Person, String> getFullName = p -> p.getFirstName() + p.getLastName();


    // ageOfPersonWithTheLongestFullName: (Person -> String) -> (Person, Person) -> int
    private BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName(Function<Person, String> getProperty){
        return (p1, p2) -> {
            if (getProperty.apply(p1).length() > getProperty.apply(p2).length()) {
                return p1.getAge();
            }
            return p2.getAge();
        };
    }

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String


        // (Person, Person) -> Integer
        // ageOfPersonWithTheLongestFullName(getFullName)
        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName =
                ageOfPersonWithTheLongestFullName(getFullName);


        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
