package lambda.part2.exercise;

import data.Person;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArrowNotationExercise {

    @Test
    void getAge() {
        // Person -> Integer
        final Function<Person, Integer> getAge = p -> p.getAge();

        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    void compareAges() {
        final BiPredicate<Person, Person> compareAges = (p1, p2) -> p1.getAge() == p2.getAge();
        // compareAges: (Person, Person) -> boolean

        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // getFullName: Person -> String
    private static final Function<Person,String> getFullName = person ->
            person.getFirstName() + " " + person.getLastName();

    // ageOfPersonWithTheLongestFullName: (Person -> String) -> (Person, Person) -> int
    private BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName(Function<Person,String> f){
        return (p1, p2) -> f.apply(p1).length() > f.apply(p2).length() ? p1.getAge() : p2.getAge();
    }

    @Test
    void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String
        final Function<Person, String> getFullName = ArrowNotationExercise.getFullName;

        // (Person, Person) -> Integer
        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName =
                ageOfPersonWithTheLongestFullName(getFullName);

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
