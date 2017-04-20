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
        final Function<Person, Integer> getAge = t -> t.getAge();

        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {
        final BiPredicate<Person, Person> compareAges = (t1, t2) -> t1.getAge() == t2.getAge();

        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        final Function<Person, String> getFullName = t -> t.getFirstName() + " " + t.getLastName();
        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName =
                (t1, t2) -> {
                    if (getFullName.apply(t1).length() > getFullName.apply(t2).length())
                        return t1.getAge();
                    else return t2.getAge();
                };

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
