package lambda.part3.exercise;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class TraversableMapping {


    interface Traversable<T> {

        void forEach(Consumer<T> c);

        default <R> Traversable<R> map(Function<T, R> mapper) {
            return consumer -> this.forEach(item -> consumer.accept(mapper.apply(item)));
            };
        }

        default <R> Traversable<R> flatMap(Function<T, Traversable<R>> mapper){

            return consumer -> this.forEach(element -> mapper.apply(element).forEach(consumer));
            //return consumer -> this.forEach(consumer);

        }

        default Traversable<T> filter(Predicate<T> predicate){
            return consumer -> this.forEach(item -> {
                if (predicate.test(item)) con
                    sumer.accept(item);
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
