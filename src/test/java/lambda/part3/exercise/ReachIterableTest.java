package lambda.part3.exercise;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Denis Verkhoturov, mod.satyr@gmail.com
 */
public class ReachIterableTest {
    interface ReachIterable<T> {
        // forNext(T -> void) -> boolean
        boolean forNext(Consumer<T> consumer);

        // filter
        default ReachIterable<T> filter(final Predicate<T> predicate) {
            final ReachIterable<T> self = this;
            return consumer -> self.forNext(
                    element -> { if (predicate.test(element)) consumer.accept(element); }
            );
        }

        // map
        default <R> ReachIterable<R> map(final Function<T, R> mapper) {
            final ReachIterable<T> self = this;
            return consumer -> self.forNext(
                    element -> consumer.accept(mapper.apply(element))
            );
        }

        // flatMap
        default <R> ReachIterable<R> flatMap(final Function<T, ReachIterable<R>> function) {
            final ReachIterable<T> self = this;
            return consumer -> self.forNext(
                    element -> function.apply(element).forNext(consumer)
            );
        }

        // anyMatch (T -> boolean) -> boolean
        default boolean anyMatch(final Predicate<T> predicate) {
            boolean isAnyMatched = false;
            while (!this.forNext(predicate::test)) isAnyMatched = true;
            return isAnyMatched;
        }

        // allMatch (T -> boolean) -> boolean
        default boolean allMatch(final Predicate<T> predicate) {
            boolean isAllMatched = false;
            while (this.forNext(predicate::test)) isAllMatched = true;
            return isAllMatched;
        }

        // noneMatch (T -> boolean) -> boolean
        // firstMatch(T -> boolean) -> Optional<T>

        static <T> ReachIterable<T> from(final List<T> list) {
            return consumer -> {
                final Iterator<T> iterator = list.iterator();
                final boolean hasNext = iterator.hasNext();
                if (hasNext) consumer.accept(iterator.next());
                return hasNext;
            };
        }
    }
}
