package lambda.part2.exercise;

import data.Person;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ArrowNotationExercise {

    @Test
    public void getAge() {
        final Function<Person, Integer> getAge = Person::getAge;
        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {
        BiPredicate<Person,Person> compareAges = (Person p1, Person p2) -> p1.getAge() == p2.getAge();
        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    @Test
    public void getFullName(){
        final Function<Person, String> getFullName = (Person p) -> p.getFirstName().concat(" ").concat(p.getLastName());
        assertEquals("John White", getFullName.apply(new Person("John", "White", 33)));
    }

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {

        final Function<Person, String> getFullName = (Person p) -> p.getFirstName().concat(" ").concat(p.getLastName());
        final BiFunction<Person,Person, Integer> ageOfPersonWithTheLongestFullName =
                (p1,p2) -> getFullName.apply(p1).length()>getFullName.apply(p2).length()?p1.getAge():p2.getAge();

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
