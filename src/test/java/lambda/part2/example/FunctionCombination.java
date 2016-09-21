package lambda.part2.example;

import data.Person;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class FunctionCombination {

    @Test
    public void personToInt0() {
        // Person -> Integer
        final Function<Person, Integer> lastNameLength = p -> p.getLastName().length();

        assertEquals(Integer.valueOf(5), lastNameLength.apply(new Person("a", "abcde", 0)));
    }

    // (Person -> String, String -> Integer) -> (Person, Integer)
    private Function<Person, Integer> personStringPropertyToInt(
            Function<Person, String> personToString,
            Function<String, Integer> stringToInteger) {
        return p -> {
            final String str = personToString.apply(p);
            final Integer result = stringToInteger.apply(str);
            return result;
        };
    }

    @Test
    public void personToInt1() {
        final Function<Person, String> getLastName = Person::getLastName;
        final Function<String, Integer> getLength = String::length;
        final Function<Person, Integer> lastNameLength = personStringPropertyToInt(getLastName, getLength);

        assertEquals(Integer.valueOf(5), lastNameLength.apply(new Person("a", "abcde", 0)));
    }

    // (A -> B, B -> C) -> A -> C
    private <A, B, C> Function<A, C> andThen(Function<A, B> f1, Function<B, C> f2) {
        throw new UnsupportedOperationException();
    }

    @Test
    public void personToInt2() {
        final Function<Person, String> getLastName = Person::getLastName;
        final Function<String, Integer> getLength = String::length;
        final Function<Person, Integer> lastNameLength = andThen(getLastName, getLength);

        assertEquals(Integer.valueOf(5), lastNameLength.apply(new Person("a", "abcde", 0)));
    }

    @Test
    public void personToInt3() {
        final Function<Person, String> getLastName = Person::getLastName;
        final Function<String, Integer> getLength = String::length;
        final Function<Person, Integer> lastNameLength = getLastName.andThen(getLength);

        assertEquals(Integer.valueOf(5), lastNameLength.apply(new Person("a", "abcde", 0)));
    }
}
