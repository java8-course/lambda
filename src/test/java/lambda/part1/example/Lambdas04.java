package lambda.part1.example;

import data.Person;
import org.junit.Test;

public class Lambdas04 {

    private void run(Runnable r) {
        r.run();
    }

    @Test
    public void closure() {
        Person person = new Person("John", "Galt", 33);

        run(new Runnable() {
            @Override
            public void run() {
                person.print();
            }
        });

        //person = new Person("a", "a");
    }

    @Test
    public void closure_lambda() {
        Person person = new Person("John", "Galt", 33);

        // statement lambda
        run(() -> {
            person.print();
        });
        // expression lambda
        run(() -> person.print());
        // method reference
        run(person::print);
    }

    private Person _person = null;

    @Test
    public void closure_this_lambda() {
        _person = new Person("John", "Galt", 33);

        run(() -> /*this.*/_person.print());
        run(/*this.*/_person::print);

        _person = new Person("a", "a", 1);

    }


    private Runnable runLater(Runnable r) {
        return () -> {
            System.out.println("before run");
            r.run();
        };
    }


    @Test
    public void closure_this_lambda2() {
        _person = new Person("John", "Galt", 33);

        //final Person person = _person;
        final Runnable r1 = runLater(() -> _person.print());
        final Runnable r2 = runLater(_person::print);

        _person = new Person("a", "a", 1);

        r1.run();
        r2.run();

    }

}
