package lambda.part2.exercise;

import data.Person;
import org.junit.Test;

import java.util.function.*;

import static org.junit.Assert.assertEquals;

public class ArrowNotationExercise {

    @Test
    public void getAge() {
        // Person -> Integer
        final Function<Person, Integer> getAge = Person::getAge; // TODO
        //Or
        //final Function<Person, Integer> getAge = p -> p.getAge();

        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {
        // TODO use BiPredicate
        // compareAges: (Person, Person) -> boolean
        BiPredicate<Person, Person> compareAges = (p1, p2) -> p1.getAge() == p2.getAge();

        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // TODO
    // getFullName: Person -> String
    private Function<Person, String> getFullName = p -> p.getFirstName() + " " + p.getLastName();

    // TODO
    // ageOfPersonWithTheLongestFullName: (Person -> String) -> (Person, Person) -> int
    private BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName(Function<Person, String> f) {
        return (p1, p2) -> f.apply(p1).length() > f.apply(p2).length() ? p1.getAge() : p2.getAge();
    }
    //Or
    //private ToIntBiFunction<Person, Person> ageOfPersonWithTheLongestFullName(Function<Person, String> f) {..}



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
