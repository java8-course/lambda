package lambda.part3.exercise;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("WeakerAccess")
public class FilterMap {

    public static class Container<T, R> {
        private final Predicate<T> predicate;
        private final Function<T, R> function;

        public Container(Predicate<T> predicate) {
            this.predicate = predicate;
            this.function = null;
        }

        public Container(Function<T, R> function) {
            this.function = function;
            this.predicate = null;
        }

        public Predicate<T> getPredicate() {
            return predicate;
        }

        public Function<T, R> getFunction() {
            return function;
        }
    }

    @SuppressWarnings("unchecked")
    public static class LazyCollectionHelper<T> {
        private final List<Container<Object, Object>> actions;
        private final List<T> list;

        public LazyCollectionHelper(List<T> list, List<Container<Object, Object>> actions) {
            this.actions = actions;
            this.list = list;
        }

        public LazyCollectionHelper(List<T> list) {
            this(list, new ArrayList<>());
        }

        public LazyCollectionHelper<T> filter(Predicate<T> condition) {
            List<Container<Object, Object>> newActions = new ArrayList<>(actions);
            newActions.add(new Container<>((Predicate<Object>) condition));
            return new LazyCollectionHelper<>(list, newActions);
        }

        public <R> LazyCollectionHelper<R> map(Function<T, R> function) {
            List<Container<Object, Object>> newActions = new ArrayList<>(actions);
            newActions.add(new Container<>((Function<Object, Object>) function));
            return new LazyCollectionHelper<>((List<R>)list, newActions);
        }

        public List<T> force() {
            final List<T> result = new ArrayList<>();
            for (Object o: list) {
                boolean pass = true;
                final Iterator<Container<Object, Object>> acterator = actions.iterator();
                while (pass && acterator.hasNext()) {
                    Container action = acterator.next();
                    Predicate aPred = action.getPredicate();
                    if (aPred != null)
                        pass = aPred.test(o);
                    else
                        o = action.getFunction().apply(o);
                }
                if (pass) result.add((T) o);
            }
            return result;
        }
    }
}
