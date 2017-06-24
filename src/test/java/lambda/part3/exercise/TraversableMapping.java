package lambda.part3.exercise;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class TraversableMapping {


    interface Traversable<T> {

        void forEach(Consumer<T> c);

        default <R> Traversable<R> map(Function<T, R> f) {
            Traversable<T> self = this;

            return new Traversable<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    self.forEach(t -> c.accept(f.apply(t)));
                }
            };
        }

        default <R> Traversable<R> flatMap(Function<T, Traversable<R>> mapper){

            return consumer -> this.forEach(t -> mapper.apply(t).forEach(consumer));

        }

        default Traversable<T> filter(Predicate<T> predicate){
            return consumer -> this.forEach(item -> {
                if (predicate.test(item)) consumer.accept(item);
            });
        }
        default List<T> force(){
            List<T> list = new ArrayList<>();
            this.forEach(list::add);
            return list;
        }


        static <T> Traversable<T> from(List<T> list){
            return list::forEach;
        }
    }
}
