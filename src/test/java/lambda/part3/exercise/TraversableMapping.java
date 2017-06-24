package lambda.part3.exercise;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class TraversableMapping {


    interface Traversable<T> {

        void forEach(Consumer<T> c);

        default <R> lambda.part3.exercise.Mapping.Traversable<R> map(Function<T, R> f) {
            lambda.part3.exercise.TraversableMapping.Traversable<T> self = this;

            return new lambda.part3.exercise.Mapping.Traversable<R>() {
                @Override
                public void forEach(Consumer<R> c) {
                    self.forEach(t -> c.accept(f.apply(t)));
                }
            };
        }

        default <R> lambda.part3.exercise.Mapping.Traversable<R> flatMap(Function<T, lambda.part3.exercise.Mapping.Traversable<R>> mapper){

            return consumer -> this.forEach(t -> mapper.apply(t).forEach(consumer));

        }

        default lambda.part3.exercise.Mapping.Traversable<T> filter(Predicate<T> predicate){
            return consumer -> this.forEach(item -> {
                if (predicate.test(item)) consumer.accept(item);
            });
        }
        default List<T> force (){
            List<T> list = new ArrayList<>();
            this.forEach(list::add);
            return list;
        }


        static <T> lambda.part3.exercise.Mapping.Traversable<T> from (List<T> list){
            return list::forEach;
        }
    }
}
