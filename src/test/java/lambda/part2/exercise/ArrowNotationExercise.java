package lambda.part2.exercise;

import data.Person;
import org.junit.Test;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ArrowNotationExercise {

    @Test
    public void getAge() {
        // Person -> Integer
        final Function<Person, Integer> getAge = person -> person.getAge(); // TODO - done

        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {
        // TODO use BiPredicate  - done
        // compareAges: (Person, Person) -> boolean
        BiPredicate<Person, Person> compareAges = (x,y) -> x.getAge() == y.getAge();


//        throw new UnsupportedOperationException("Not implemented");
        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // TODO - done
    // getFullName: Person -> String
    private static String getFullName(Person p){
        return String.format("%s %s",p.getFirstName(),p.getLastName());
    }

    // TODO - done
    //lambda BiFunction
    // ageOfPersonWithTheLongestFullName: (Person -> String) -> (Person, Person) -> int
    private static BiFunction<Person,Person,Integer> ageOfPersonWithTheLongestFullName(Function<Person,String> function) {
        return (person, person2) -> function.apply(person).length()<=function.apply(person2).length() ? person2.getAge() : person.getAge();
    }

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String
        final Function<Person, String> getFullName = ArrowNotationExercise::getFullName; // TODO - done

        // (Person, Person) -> Integer
        // TODO use ageOfPersonWithTheLongestFullName(getFullName) - done
        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName = ageOfPersonWithTheLongestFullName(getFullName);

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
