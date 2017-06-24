package lambda.part1.exercise;


import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class Lambdas01Exercise {

    interface ReachIterable<T> {
        boolean hasNext();

        T next();

        default <R> ReachIterable map(Function<T, R> mapFunc) {
            ReachIterable<T> self = this;
            return new ReachIterable<R>() {
                @Override
                public boolean hasNext() {
                    return self.hasNext();
                }

                @Override
                public R next() {
                    return mapFunc.apply(self.next());
                }
            };
        }

        default <R> ReachIterable flatMap(Function<T, ReachIterable<R>> flatMapFunc) {
            ReachIterable<T> self = this;
            Deque<R> deque = new LinkedList<>();

            return new ReachIterable<R>() {
                @Override
                public boolean hasNext() {
                    while (deque.isEmpty() && self.hasNext()) {
                        ReachIterable<R> curEl = flatMapFunc.apply(self.next());
                        while (curEl.hasNext())
                            deque.add(curEl.next());
                    }
                    return !deque.isEmpty();
                }

                @Override
                public R next() {
                    return deque.pollFirst();
                }
            };
        }

        default boolean anyMath(Predicate<T> predicate) {
            boolean res = false;
            for (; this.hasNext() && !res; res = predicate.test(this.next())) {
            }
            return res;
        }

        default boolean allMath(Predicate<T> predicate) {
            boolean res = true;
            for (; this.hasNext(); res = predicate.test(this.next())) {
                if (!res)
                    break;
            }
            return res;
        }

        default ReachIterable<T> filter(Predicate<T> predicate) {
            List<T> list = new ArrayList<>();
            T curEl;
            while (this.hasNext()) {
                curEl = this.next();
                if (predicate.test(curEl))
                    list.add(curEl);
            }
            return ReachIterable.from(list);
        }

        static <T> ReachIterable<T> from(List<T> list) {
            Objects.requireNonNull(list);
            final List<Integer> curIndex = new ArrayList<>(Arrays.asList(-1));
            return new ReachIterable<T>() {
                @Override
                public boolean hasNext() {
                    return curIndex.get(0) < list.size() - 1;
                }

                @Override
                public T next() {
                    curIndex.set(0, curIndex.get(0) + 1);
                    return list.get(curIndex.get(0));
                }
            };
        }
    }

    //----------------------Tests--------------------

    @Test
    public void reachIterableFrom() {
        List<Integer> list = new ArrayList<>(Arrays.asList(5, 3, 2, 7));
        ReachIterable<Integer> re = ReachIterable.from(list);
        int ind = 0;
        while (re.hasNext())
            assertEquals(list.get(ind++), re.next());
    }

    @Test
    public void reachIterableMap() {
        List<Integer> list = new ArrayList<>(Arrays.asList(5, 3, 2, 7));
        ReachIterable<String> re = ReachIterable.from(list).map(s -> String.valueOf(s + 2));
        int index = 0;
        while (re.hasNext())
            assertEquals(String.valueOf(list.get(index++) + 2), re.next());
    }

    @Test
    public void reachIterableFlatMap() {
        List<List<Integer>> actualList = new ArrayList<>();
        actualList.add(new ArrayList<>());
        actualList.add(Arrays.asList(5, 3, 2, 7));
        actualList.add(Arrays.asList(12, 13, 12, 71));
        actualList.add(Collections.emptyList());
        actualList.add(Arrays.asList(111));

        List<Integer> expectedList = new ArrayList<>(Arrays.asList(5, 3, 2, 7, 12, 13, 12, 71));
        ReachIterable<Integer> re = ReachIterable.from(actualList).flatMap(
                s -> s.size() == 1 ? ReachIterable.from(Arrays.asList()) : ReachIterable.from(s));
        int index = 0;
        while (re.hasNext())
            assertEquals(expectedList.get(index++), re.next());
    }

    @Test
    public void reachIterableAnyMatch() {
        List<Integer> list = new ArrayList<>(Arrays.asList(5, 3, 2, 7));
        boolean expected = ReachIterable.from(list).anyMath(p -> p == 3);
        boolean expected1 = ReachIterable.from(list).anyMath(p -> p == 333);
        boolean expected2 = ReachIterable.from(list).anyMath(p -> p == 5 || p == 3 || p == 2);
        boolean expected3 = ReachIterable.from(Collections.emptyList()).anyMath(p -> p.equals(""));
        assertEquals(expected, true);
        assertEquals(expected1, false);
        assertEquals(expected2, true);
        assertEquals(expected3, false);
    }

    @Test
    public void reachIterableALLMatch() {
        List<Integer> list = new ArrayList<>(Arrays.asList(5, 3, 2, 7));
        boolean expected = ReachIterable.from(list).allMath(p -> p == 5 || p == 3 || p == 2 || p == 7);
        boolean expected1 = ReachIterable.from(list).allMath(p -> p == 5 || p == 3 || p == 2);
        boolean expected2 = ReachIterable.from(Collections.emptyList()).allMath(p -> p.equals(""));
        assertEquals(expected, true);
        assertEquals(expected1, false);
        assertEquals(expected2, true);
    }

    @Test
    public void reachIterableFilter() {
        List<Integer> list = new ArrayList<>(Arrays.asList(5, 3, 2, 7));
        ReachIterable<Integer> re = ReachIterable.from(list).filter(p -> p != 3);
        List<Integer> expectedList = new ArrayList<>(Arrays.asList(5, 2, 7));
        int index = 0;
        while (re.hasNext())
            assertEquals(expectedList.get(index++), re.next());
    }
}
