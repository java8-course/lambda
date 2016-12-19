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

        final Function<Person, Integer> getAge = person -> person.getAge();
        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {

        final BiPredicate<Person, Person> compareAges = (person, person2) -> person.equals(person2);
        assertEquals(false, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    private Function<Person, String> getFullName = person -> person.getFirstName() + " " + person.getLastName();

    private Function<Function<Person, String>, BiFunction<Person, Person, Integer>> ageOfPersonWithTheLongestFullName = personFullName -> (person, person2) -> (personFullName.apply(person).length() >= personFullName.apply(person2).length())
            ? person.getAge() : person2.getAge();

    @Test

    public void getAgeOfPersonWithTheLongestFullName() {

        final Function<Person, String> getFullName = this.getFullName;
        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName = this.ageOfPersonWithTheLongestFullName.apply(getFullName);

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
