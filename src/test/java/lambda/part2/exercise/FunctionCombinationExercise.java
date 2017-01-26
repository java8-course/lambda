package lambda.part2.exercise;

import data.Person;
import org.junit.Test;

import java.util.function.Function;
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

    private Predicate<Person> negate1(Predicate<Person> test) {
        return (Person p) -> !test.test(p);
    }

    // validateFirstNameAndLastName: (Person -> boolean, Person -> boolean) -> (Person -> boolean)
    private Predicate<Person> validateFirstNameAndLastName(Predicate<Person> t1, Predicate<Person> t2) {
        return (Person p) -> t1.test(p) && t2.test(p);
    }

    @Test
    public void validateFirstNameAndLastName(){
        final String namePattern = "^\\p{L}+$";
        Predicate<Person> validateFirstNameAndLastName =  (Person p) -> p.getFirstName().matches(namePattern)&&p.getLastName().matches(namePattern);

        assertEquals(true, validateFirstNameAndLastName.test(new Person("a", "b", 0)));
        assertEquals(false, validateFirstNameAndLastName.test(new Person("", "b", 0)));
        assertEquals(false, validateFirstNameAndLastName.test(new Person("a", "", 0)));
        assertEquals(false, validateFirstNameAndLastName.test(new Person("a", "%", 0)));
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

    // negate: (T -> boolean) -> (T -> boolean)
    private <T> Predicate<T> negate(Predicate<T> test) {
        return (T o) -> !test.test(o);
    }

    // and: (T -> boolean, T -> boolean) -> (T -> boolean)
    private <T> Predicate<T> and(Predicate<T> t1, Predicate<T> t2) {
        return (T o) -> t1.test(o)&&t2.test(o);
    }

    @Test
    public void personHasNotEmptyLastNameAndFirstName2() {
        final Predicate<Person> hasEmptyFirstName = p -> p.getFirstName().isEmpty();
        final Predicate<Person> hasEmptyLastName = p -> p.getLastName().isEmpty();

        final Predicate<Person> validateFirstName = negate(hasEmptyFirstName);
        final Predicate<Person> validateLastName = negate(hasEmptyLastName);

        final Predicate<Person> validate = and(validateFirstName,validateLastName);

        assertEquals(true, validate.test(new Person("a", "b", 0)));
        assertEquals(false, validate.test(new Person("", "b", 0)));
        assertEquals(false, validate.test(new Person("a", "", 0)));
    }

    @Test
    public void personHasNotEmptyLastNameAndFirstName3() {
        final Predicate<Person> hasEmptyFirstName = p -> p.getFirstName().isEmpty();
        final Predicate<Person> hasEmptyLastName = p -> p.getLastName().isEmpty();

        final Predicate<Person> validateFirstName = hasEmptyFirstName.negate();
        final Predicate<Person> validateLastName = hasEmptyLastName.negate();

        final Predicate<Person> validate = validateFirstName.and(validateLastName);

        assertEquals(true, validate.test(new Person("a", "b", 0)));
        assertEquals(false, validate.test(new Person("", "b", 0)));
        assertEquals(false, validate.test(new Person("a", "", 0)));
    }

}
