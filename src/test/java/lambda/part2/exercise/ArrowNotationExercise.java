package lambda.part2.exercise;

import data.Person;
import org.junit.Test;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

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

        final BiPredicate<Person, Person> compareAges = (p1, p2) -> p1.getAge() == p2.getAge();
        // throw new UnsupportedOperationException("Not implemented");
        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // getFullName: Person -> String
    private static Function<Person, String> getFullName = p -> p.getFirstName() + " " + p.getLastName();
    // ageOfPersonWithTheLongestFullName: (Person -> String) -> (Person, Person) -> int

//    private static Function<Person, Predicate<String>> propertyChecker2(Function<Person, String> getProperty) {
//        return p -> expectedPropValue -> getProperty.apply(p).equals(expectedPropValue);
//    }

    private static BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName(Function<Person, String> getFullName) {
        return (p1, p2) -> Integer.compare(getFullName.apply(p1).length(), getFullName.apply(p2).length()) > 0 ? p1.getAge() : p2.getAge();
    }

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String
        final Function<Person, String> getFullName = ArrowNotationExercise.getFullName;

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
