package lambda.part2.example;

import data.Person;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class OtherClasses {

    @Test
    // Supplier: () -> T
    // Arity 0
    public void suppliers() {
        final Supplier<String> strSupplier = () -> new String("a");

        assertEquals("a", strSupplier.get());
        assert strSupplier.get() != strSupplier.get();

        final BooleanSupplier booleanSupplier = () -> true;
        assertEquals(true, booleanSupplier.getAsBoolean());

        final IntSupplier intSupplier = () -> ThreadLocalRandom.current().nextInt();
        assertNotEquals(intSupplier.getAsInt(), intSupplier.getAsInt());

        final LongSupplier longSupplier = () -> Integer.MAX_VALUE + 100L;
        assert longSupplier.getAsLong() > Integer.MAX_VALUE;

        final DoubleSupplier doubleSupplier = () -> 0.1;
        assertEquals(0.1, doubleSupplier.getAsDouble(), 0.0001);
    }

    @Test
    // Consumer: T -> void
    // Arity 1
    public void consumers() {
        final Consumer<String> stringConsumer = System.out::println;
        stringConsumer.accept("Some string");

        final Consumer<Integer> intConsumer = System.out::println;
        intConsumer.accept(666);

        final Consumer<Long> longConsumer = System.out::println;
        longConsumer.accept(666L);

        final Consumer<Double> doubleConsumer = System.out::println;
        doubleConsumer.accept(666D);

    }

    @Test
    // UnaryOperator: T -> T
    // Arity 1
    public void unaryOperator() {
        final UnaryOperator<String> reverse = s -> new StringBuilder(s).reverse().toString();
        assertEquals("abc", reverse.apply("cba"));

        final IntUnaryOperator negateI = i -> -i;
        assertEquals(-1, negateI.applyAsInt(1));

        final LongUnaryOperator negateL = i -> -i;
        assertEquals(-1L, negateL.applyAsLong(1L));

        final DoubleUnaryOperator negateD = i -> -i;
        assertEquals(-1D, negateD.applyAsDouble(1D), 0.01D);
    }

    @Test
    // Function: T -> R
    // Arity 1
    public void functions() {
        final Function<Person, String> getFirstName = Person::getFirstName;
        assertEquals("a", getFirstName.apply(new Person("a", "b", 33)));

        final ToIntFunction<Person> getAge = Person::getAge;
        assertEquals(33, getAge.applyAsInt(new Person("a", "b", 33)));

        final IntFunction<Person> withAge = i -> new Person("a", "b", i);
        assertEquals(new Person("a", "b", 666), withAge.apply(666));

        final DoubleToLongFunction doubleToLong = d -> (long)d;
        assertEquals(666, doubleToLong.applyAsLong(666.66));
    }

    @Test
    // Predicate: T -> boolean
    // Arity 1
    public void predicates() {
        final Predicate<String> isEmpty = String::isEmpty;
        assertEquals(true, isEmpty.test(""));

        final IntPredicate positive = i -> i > 0;
        assertEquals(false, positive.test(-1));

        // LongPredicate
        // DoublePredicate
    }

    @Test
    // BinaryOperator: (T, T) -> T
    // Arity 2
    public void binaryOperators() {
        final BinaryOperator<String> concat = String::concat;
        assertEquals("ab", concat.apply("a", "b"));

        final IntBinaryOperator sumI = (i1, i2) -> i1 + i2;
        assertEquals(3, sumI.applyAsInt(1, 2));

        final LongBinaryOperator sumL = (i1, i2) -> i1 + i2;
        assertEquals(3L, sumL.applyAsLong(1L, 2L));

        final DoubleBinaryOperator sumD = (i1, i2) -> i1 + i2;
        assertEquals(3D, sumD.applyAsDouble(1D, 2D), 0.01D);
    }

    @Test
    // BiFunction: (A, B) -> R
    // Arity 2
    public void biFunction() {
        final BiFunction<Person, String, Person> changeFirstName = Person::withFirstName;
        assertEquals(new Person("c", "b", 0), changeFirstName.apply(new Person("a", "b", 0), "c"));

        final ToIntBiFunction<Person, String> toIntBiFunction = (p, s) -> p.getAge() + s.length();
        assertEquals(10, toIntBiFunction.applyAsInt(new Person("", "", 8), "ab"));

        // ToLongBiFunction
        // ToDoubleBiFunction

        // no IntLongToDouble, etc
    }

    @Test
    // BiPredicate: (A, B) -> boolean
    // Arity 2
    public void biPredicate() {
        final BiPredicate<String, Person> checkFirstName = (s, p) -> s.equals(p.getFirstName());
        assertEquals(true, checkFirstName.test("c", new Person("c", "b", 0)));
    }

    @Test
    // BiConsumer: (A, B) -> void
    // Arity 2
    public void biConsumers() {
        final BiConsumer<Person, String> biConsumer = (p, s) -> System.out.println(p.getFirstName().replace("a", s));
        biConsumer.accept(new Person("Vasia", "Pupkin", 0), "i");

        final ObjIntConsumer<String> checkLength = (s, i) -> System.out.println(s.length()==i);
        checkLength.accept("John", 4);
    }


    private interface PersonFactory {
        Person create(String name, String lastName, int age);
    }

    // ((String, String, int) -> Person, String) -> (String, Int) -> Person
    private BiFunction<String, Integer, Person> partiallyApply(
            PersonFactory pf,
            String lastName) {
        return (name, age) -> pf.create(name, lastName, age);
    }

    // ((String, String, int) -> Person) -> String -> String -> Int -> Person
    private Function<String, Function<String, IntFunction<Person>>> curry(
            PersonFactory pf) {
        return name -> lastName -> age -> pf.create(name, lastName, age);
    }

    public void currying() {
        // (String, String, int) -> Person
        final PersonFactory factory = (n, ln, a) -> new Person(n, ln, a);


        final BiFunction<String, Integer, Person> doe =
                (name, age) -> factory.create(name, "Doe", age);

        final Person mother = doe.apply("Samanta", 33);
        final Person father = doe.apply("Bob", 33);
        final Person son = doe.apply("John", 33);


        // String -> String -> int -> Person
        final Function<String, Function<String, IntFunction<Person>>> curried =
                name -> (lastName -> age -> factory.create(name, lastName, age));

        final Function<String, IntFunction<Person>> john =
                curried.apply("John");

        final IntFunction<Person> johnDoeWithoutAge =
                john.apply("Doe");

        assertEquals(new Person("John", "Doe", 22), johnDoeWithoutAge.apply(22));
        assertEquals(new Person("John", "Doe", 33), johnDoeWithoutAge.apply(33));
    }










}
