package lambda.part1.example;

import data.Person;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;

class Lambdas05 {
    private <T> void printResult(T t, Function<T, String> f) {
        System.out.println(f.apply(t));
    }

    private final Person person = new Person("John", "Galt", 33);

    @Test
    void printField() {
        printResult(person, Person::getLastName);
        printResult(person, Person::getLastName2);

        BiFunction<Person, String, Person> changeFirstName = Person::withFirstName;

        printResult(changeFirstName.apply(person, "newName"), Person::getFirstName);
    }

    @Nested
    private static class PersonHelper {
        static String stringRepresentation(Person person) {
            return person.toString();
        }
    }


    @Test
    void printStringRepresentation() {
        printResult(person, PersonHelper::stringRepresentation);
    }

    @Test
    void exception() {
        Runnable r = () -> {
            //Thread.sleep(100);
            person.print();
        };

        r.run();
    }

    @FunctionalInterface
    private interface DoSmth {
        void doSmth();
    }

    private void conflict(Runnable r) {
        System.out.println("Runnable");
        r.run();
    }

    private void conflict(DoSmth d) {
        System.out.println("DoSmth");
        d.doSmth();
    }

    private String printAndReturn() {
        person.print();
        return person.toString();
    }

    @Test
    void callConflict() {
        conflict((DoSmth & Serializable) this::printAndReturn);
    }

    private interface PersonFactory {
        Person create(String name, String lastName, int age);
    }

    private void withFactory(PersonFactory pf) {
        pf.create("name", "lastName", 33).print();
    }

    @Test
    void factory() {
        withFactory(Person::new);
        withFactory((firstName, lastName, age) -> new Person(firstName + "!", lastName + "!", age + 633));
    }
}
