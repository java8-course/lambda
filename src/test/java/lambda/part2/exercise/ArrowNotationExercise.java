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
        final Function<Person, Integer> getAge =
                Person::getAge; // TODO

        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {
        final BiPredicate<Person, Person> compareAges =
                (person, person2) -> person.getAge() == person2.getAge();
        // TODO use BiPredicate
        // compareAges: (Person, Person) -> boolean

        //throw new UnsupportedOperationException("Not implemented");
        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // TODO
    // getFullName: Person -> String


    // TODO
    // ageOfPersonWithTheLongestFullName: (Person -> String) -> ((Person, Person) -> int)
    //

    Function<Function<Person, String>, BiFunction<Person, Person, Integer>> ageOfPersonWithTheLongestWhatever =
         personStringFunction ->
                 (person, person2) ->
                         personStringFunction.apply(person).length() > personStringFunction.apply(person2).length() ?
                                 person.getAge() : person2.getAge();

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String
       // final Function<Person, String> getFullName = null; // TODO
        Function <Person, String> getFullName = person ->
                person.getFirstName() + " " + person.getLastName();

        // (Person, Person) -> Integer
        // TODO use ageOfPersonWithTheLongestFullName(getFullName)
//        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName =
//                ageOfPersonWithTheLongestWhatever.apply(getFullName);

        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName =(person, person2) ->
                getFullName.apply(person).length() > getFullName.apply(person2).length() ?
                        person.getAge() : person2.getAge();

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
