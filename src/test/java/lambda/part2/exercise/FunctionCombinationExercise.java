package lambda.part2.exercise;

import data.Person;
import org.junit.Test;

import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class FunctionCombinationExercise {

    @Test
    public void personHasNotEmptyLastNameAndFirstName0() {
        // Person -> boolean
        final Predicate<Person> validate = p -> !p.getFirstName().isEmpty() && !p.getLastName().isEmpty();

        assertEquals(true, validate.test(new Person("a", "b", 0)));
        assertEquals(false, validate.test(new Person("", "b", 0)));
        assertEquals(false, validate.test(new Person("a", "", 0)));
    }

    // TODO
    // negate1: (Person -> boolean) -> (Person -> boolean)
    private Predicate<Person> negate1(Predicate<Person> test) {
        return p -> {
            // TODO
            throw new UnsupportedOperationException();
        };
    }

    // TODO
    // validateFirstNameAndLastName: (Person -> boolean, Person -> boolean) -> (Person -> boolean)
    private Predicate<Person> validateFirstNameAndLastName(Predicate<Person> t1, Predicate<Person> t2) {
        return p -> {
            // TODO
            throw new UnsupportedOperationException();
        };
    }

    @Test
    public void personHasNotEmptyLastNameAndFirstName1() {
        final Predicate<Person> hasEmptyFirstName = p -> p.getFirstName().isEmpty();
        final Predicate<Person> hasEmptyLastName = p -> p.getLastName().isEmpty();

        final Predicate<Person> validateFirstName = negate1(hasEmptyFirstName);
        final Predicate<Person> validateLastName = negate1(hasEmptyLastName);

        final Predicate<Person> validate = validateFirstNameAndLastName(validateFirstName, validateLastName);

        assertEquals(true, validate.test(new Person("a", "b", 0)));
        assertEquals(false, validate.test(new Person("", "b", 0)));
        assertEquals(false, validate.test(new Person("a", "", 0)));
    }

    // TODO
    // negate: (T -> boolean) -> (T -> boolean)
    private <T> Predicate<T> negate(Predicate<T> test) {
        // TODO
        throw new UnsupportedOperationException();
    }

    // TODO
    // and: (T -> boolean, T -> boolean) -> (T -> boolean)
    private <T> Predicate<T> and(Predicate<T> t1, Predicate<T> t2) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Test
    public void personHasNotEmptyLastNameAndFirstName2() {
        final Predicate<Person> hasEmptyFirstName = p -> p.getFirstName().isEmpty();
        final Predicate<Person> hasEmptyLastName = p -> p.getLastName().isEmpty();

        final Predicate<Person> validateFirstName = null; // TODO use negate
        final Predicate<Person> validateLastName = null; // TODO use negate

        final Predicate<Person> validate = null; // TODO use and

        assertEquals(true, validate.test(new Person("a", "b", 0)));
        assertEquals(false, validate.test(new Person("", "b", 0)));
        assertEquals(false, validate.test(new Person("a", "", 0)));
    }

    @Test
    public void personHasNotEmptyLastNameAndFirstName3() {
        final Predicate<Person> hasEmptyFirstName = p -> p.getFirstName().isEmpty();
        final Predicate<Person> hasEmptyLastName = p -> p.getLastName().isEmpty();

        final Predicate<Person> validateFirstName = null; // TODO use Predicate::negate
        final Predicate<Person> validateLastName = null; // TODO use Predicate::negate

        final Predicate<Person> validate = null; // TODO use Predicate::and

        assertEquals(true, validate.test(new Person("a", "b", 0)));
        assertEquals(false, validate.test(new Person("", "b", 0)));
        assertEquals(false, validate.test(new Person("a", "", 0)));
    }

}
