package lambda.part3.exercise;

import data.JobHistoryEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Traversable<T> {

    void forEach(Consumer<T> consumer);

    default <R> Traversable<R> map(Function<T, R> function) {
        Traversable<T> self = this;

        return new Traversable<R>() {

            @Override
            public void forEach(Consumer<R> consumer) {
                self.forEach(t -> consumer.accept(function.apply(t)));
            }
        };
    }

    //filter
    default Traversable<T> filter(Predicate<T> predicate) {
        Traversable<T> self = this;


        return new Traversable<T>() {

            @Override
            public void forEach(Consumer<T> consumer) {
                self.forEach((T p) -> {
                    if (predicate.test(p))
                        consumer.accept(p);
                });
            }
        };
    }

    //flatMap
    default <R> Traversable<R> flatMap(Function<T, List<R>> function) {
        Traversable<T> self = this;

        return new Traversable<R>() {
            @Override
            public void forEach(Consumer<R> c) {
                self.forEach(l -> function.apply(l).forEach(c));
            }
        };
    }

    static <T> Traversable<T> from(List<T> list) {
        return new Traversable<T>() {
            @Override
            public void forEach(Consumer<T> consumer) {
                list.forEach(consumer);
            }
        };
    }

    default List<T> force(){
        List<T> result = new ArrayList<>();

        forEach(result::add);

        return result;
    }
}
