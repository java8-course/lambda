package lambda.part1.exercise;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import data.Person;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Lambdas01Exercise {

  @Test
  public void sortPersonsByAge() {
    Person[] persons = {
            new Person("name 3", "lastName 3", 20),
            new Person("name 1", "lastName 2", 40),
            new Person("name 2", "lastName 1", 30)
    };
    // TODO use Arrays.sort
    Arrays.sort(persons, Comparator.comparingInt(Person::getAge));

    assertArrayEquals(persons, new Person[]{
            new Person("name 3", "lastName 3", 20),
            new Person("name 2", "lastName 1", 30),
            new Person("name 1", "lastName 2", 40),
    });
  }

  @Test
  public void findFirstWithAge30() {
    List<Person> persons = ImmutableList.of(
            new Person("name 3", "lastName 3", 20),
            new Person("name 1", "lastName 2", 30),
            new Person("name 2", "lastName 1", 30)
    );

    // TODO use FluentIterable
    final Map<String, Person> personByLastName = FluentIterable.from(persons).uniqueIndex(Person::getLastName);

    assertEquals(personByLastName.get("lastName 2"), new Person("name 1", "lastName 2", 30));
  }
}
