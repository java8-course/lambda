package lambda.part2.example;

import com.sun.org.apache.xpath.internal.operations.Bool;
import data.Person;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class ArrowNotation {

    // String -> int
    private static int strLength(String s) {
        return s.length();
    }

    @Test
    public void stringToInt() {
        // String -> Integer
        final Function<String, Integer> strLength = ArrowNotation::strLength;

        assertEquals(Integer.valueOf(5), strLength.apply("12345"));
    }

    @Test
    public void personToString() {
        // Person -> String
        final Function<Person, String> lastName = Person::getLastName;

        assertEquals("lastName", lastName.apply(new Person("f", "lastName", 0)));
    }

    @Test
    public void personToInt() {
        // Person -> Integer
        final Function<Person, Integer> lastNameLength = p -> p.getLastName().length();

        assertEquals(Integer.valueOf(5), lastNameLength.apply(new Person("a", "abcde", 0)));
    }

    // (Person, String) -> boolean
    private static boolean sameLastName(Person p, String lastName) {
        return p.getLastName().equals(lastName);
    }

    @Test
    public void checkLastName() {
        // (Person, String) -> Boolean
        final BiFunction<Person, String, Boolean> sameLastName = ArrowNotation::sameLastName;

        assertEquals(Boolean.TRUE, sameLastName.apply(new Person("a", "b", 0), "b"));
    }

    // (Person, Person -> String) -> (String -> boolean)
    private static Predicate<String> propertyChecker(Person p, Function<Person, String> getProperty) {
        return s -> {
            final String propertyValue = getProperty.apply(p);
            return s.equals(propertyValue);
        };
    }

    // (Person -> String) -> Person -> String -> boolean
    private static Function<Person, Predicate<String>> propertyChecker2(Function<Person, String> getProperty) {
        return p -> expectedPropValue -> getProperty.apply(p).equals(expectedPropValue);
    }

    @Test
    public void checkProperty2() {
        final Function<Person, Predicate<String>> lastNameChecker = propertyChecker2(Person::getLastName);
        final Function<Person, Predicate<String>> ageChecker = propertyChecker2((person1) -> person1.getAge() + "");

        final Person person = new Person("a", "b", 33);
        assertEquals(Boolean.TRUE, lastNameChecker.apply(person).test("b"));
        assertEquals(Boolean.TRUE, ageChecker.apply(person).test("33"));
    }

    @Test
    public void checkProperty() {
        // String -> boolean
        Person person = new Person("a", "b", 0);
        // Person -> String
        Function<Person, String> getFirstName = Person::getFirstName;
        // String -> boolean
        final Predicate<String> checkFirstName = propertyChecker(person, getFirstName);

        assertEquals(Boolean.TRUE, checkFirstName.test("a"));
    }
}
