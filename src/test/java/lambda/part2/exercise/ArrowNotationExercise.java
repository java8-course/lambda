package lambda.part2.exercise;

import data.Person;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class ArrowNotationExercise {

    @Test
    public void getAge() {
        // Person -> Integer
        final Function<Person, Integer> getAge = p -> p.getAge(); // TODO

        assertEquals(Integer.valueOf(33), getAge.apply(new Person("", "", 33)));
    }

    @Test
    public void compareAges() {
        // TODO use BiPredicate
        // compareAges: (Person, Person) -> boolean
        final BiPredicate<Person, Person> compareAges = (p, p1) -> p.getAge() == p1.getAge();

        assertEquals(true, compareAges.test(new Person("a", "b", 22), new Person("c", "d", 22)));
    }

    // TODO
    // getFullName: Person -> String
    final Function<Person, String> getFullName = p -> p.getFirstName() + " " + p.getLastName();

    // TODO
    // ageOfPersonWithTheLongestFullName: (Person -> String) -> (Person, Person) -> int
    private BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName
            (Function<Person, String> getFullNameFunction) {
        return (p1, p2) -> {
            String n1 = getFullNameFunction.apply(p1);
            String n2 = getFullNameFunction.apply(p2);
            if (n1.length() == n2.length())
                return 0;
            if (n1.length() > n2.length())
                return p1.getAge();
            else
                return p2.getAge();
        };
    }
    final Function<Function<Person, String>, BiFunction<Person, Person, Integer>> ageOfPersonWithTheLongestFullNameFunction =
            getFullNameFunction -> (p1,p2)-> {
        String n1 = getFullNameFunction.apply(p1);
        String n2 = getFullNameFunction.apply(p2);
        if (n1.length() == n2.length())
            return 0;
        if (n1.length() > n2.length())
            return p1.getAge();
        else
             return p2.getAge();
    };

    @Test
    public void getAgeOfPersonWithTheLongestFullName() {
        // Person -> String
        final Function<Person, String> getFullName = this.getFullName; // TODO

        // (Person, Person) -> Integer
        // TODO use ageOfPersonWithTheLongestFullName(getFullName)
        final BiFunction<Person, Person, Integer> ageOfPersonWithTheLongestFullName =
                (p1,p2)->this.ageOfPersonWithTheLongestFullNameFunction.apply(getFullName).apply(p1,p2);
            // (p1,p2)->this.ageOfPersonWithTheLongestFullName(getFullName).apply(p1,p2);

        assertEquals(
                Integer.valueOf(1),
                ageOfPersonWithTheLongestFullName.apply(
                        new Person("a", "b", 2),
                        new Person("aa", "b", 1)));
    }
}
