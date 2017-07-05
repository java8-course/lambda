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
        final Function<Person, Integer> getAge = Person::getAge; // TODO

        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {
        // TODO use BiPredicate
        // compareAges: (Person, Person) -> boolean
        final BiFunction<Person, Person, Boolean> compareAges = (person1, person2) -> person1.getAge() == person2.getAge();

        assertEquals(true, compareAges.apply(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // TODO
    // getFullName: Person -> String
    private Function<Person, String> getFullNameFunc(){
        return person -> String.format("%1%s %2$s", person.getFirstName(), person.getLastName());
    }

    // TODO
    // ageOfPersonWithTheLongestFullName: (Person -> String) -> (Person, Person) -> int
    private BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullNameFunc(Function<Person, String> getFullNameFunc){
        return (person1, person2) -> getFullNameFunc.apply(person1).length() > getFullNameFunc.apply(person2).length()?
                person1.getAge():person2.getAge();
    }
    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String
        final Function<Person, String> getFullName = getFullNameFunc(); // TODO

        // (Person, Person) -> Integer
        // TODO use ageOfPersonWithTheLongestFullName(getFullName)
        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName =
                ageOfPersonWithTheLongestFullNameFunc(getFullName);

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
