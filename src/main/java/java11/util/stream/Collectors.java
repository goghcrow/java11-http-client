package java11.util.stream;

import java11.util.Lists;
import java11.util.Maps;
import java11.util.Sets;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

/**
 * 提取自 java.util.stream.Collectors
 * @author chuxiaofeng
 */
public class Collectors {
    static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();
    static final Set<Collector.Characteristics> CH_UNORDERED_NOID
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED));

    /**
     * Returns a {@code Collector} that accumulates the input elements into an
     * <a href="../List.html#unmodifiable">unmodifiable List</a> in encounter
     * order. The returned Collector disallows null values and will throw
     * {@code NullPointerException} if it is presented with a null value.
     *
     * @param <T> the type of the input elements
     * @return a {@code Collector} that accumulates the input elements into an
     * <a href="../List.html#unmodifiable">unmodifiable List</a> in encounter order
     * @since 10
     */
    @SuppressWarnings("unchecked")
    public static <T>
    Collector<T, ?, List<T>> toUnmodifiableList() {
        return new CollectorImpl<>((Supplier<List<T>>) ArrayList::new, List::add,
                (left, right) -> { left.addAll(right); return left; },
                list -> (List<T>) Lists.of(list.toArray()),
                CH_NOID);
    }

    /**
     * Returns a {@code Collector} that accumulates the input elements into an
     * <a href="../Set.html#unmodifiable">unmodifiable Set</a>. The returned
     * Collector disallows null values and will throw {@code NullPointerException}
     * if it is presented with a null value. If the input contains duplicate elements,
     * an arbitrary element of the duplicates is preserved.
     *
     * <p>This is an {@link Collector.Characteristics#UNORDERED unordered}
     * Collector.
     *
     * @param <T> the type of the input elements
     * @return a {@code Collector} that accumulates the input elements into an
     * <a href="../Set.html#unmodifiable">unmodifiable Set</a>
     * @since 10
     */
    @SuppressWarnings("unchecked")
    public static <T>
    Collector<T, ?, Set<T>> toUnmodifiableSet() {
        return new CollectorImpl<>((Supplier<Set<T>>) HashSet::new, Set::add,
                (left, right) -> {
                    if (left.size() < right.size()) {
                        right.addAll(left); return right;
                    } else {
                        left.addAll(right); return left;
                    }
                },
                set -> (Set<T>) Sets.of(set.toArray()),
                CH_UNORDERED_NOID);
    }

    /**
     * Returns a {@code Collector} that accumulates the input elements into an
     * <a href="../Map.html#unmodifiable">unmodifiable Map</a>,
     * whose keys and values are the result of applying the provided
     * mapping functions to the input elements.
     *
     * <p>If the mapped keys contain duplicates (according to
     * {@link Object#equals(Object)}), an {@code IllegalStateException} is
     * thrown when the collection operation is performed.  If the mapped keys
     * might have duplicates, use {@link #toUnmodifiableMap(Function, Function, BinaryOperator)}
     * to handle merging of the values.
     *
     * <p>The returned Collector disallows null keys and values. If either mapping function
     * returns null, {@code NullPointerException} will be thrown.
     *
     * @param <T> the type of the input elements
     * @param <K> the output type of the key mapping function
     * @param <U> the output type of the value mapping function
     * @param keyMapper a mapping function to produce keys, must be non-null
     * @param valueMapper a mapping function to produce values, must be non-null
     * @return a {@code Collector} that accumulates the input elements into an
     * <a href="../Map.html#unmodifiable">unmodifiable Map</a>, whose keys and values
     * are the result of applying the provided mapping functions to the input elements
     * @throws NullPointerException if either keyMapper or valueMapper is null
     *
     * @see #toUnmodifiableMap(Function, Function, BinaryOperator)
     * @since 10
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T, K, U>
    Collector<T, ?, Map<K,U>> toUnmodifiableMap(Function<? super T, ? extends K> keyMapper,
                                                Function<? super T, ? extends U> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper");
        Objects.requireNonNull(valueMapper, "valueMapper");
        return collectingAndThen(
                toMap(keyMapper, valueMapper),
                map -> (Map<K,U>) Maps.ofEntries(map.entrySet().toArray(new Map.Entry[0])));
    }

    /**
     * Returns a {@code Collector} that accumulates the input elements into an
     * <a href="../Map.html#unmodifiable">unmodifiable Map</a>,
     * whose keys and values are the result of applying the provided
     * mapping functions to the input elements.
     *
     * <p>If the mapped
     * keys contain duplicates (according to {@link Object#equals(Object)}),
     * the value mapping function is applied to each equal element, and the
     * results are merged using the provided merging function.
     *
     * <p>The returned Collector disallows null keys and values. If either mapping function
     * returns null, {@code NullPointerException} will be thrown.
     *
     * @param <T> the type of the input elements
     * @param <K> the output type of the key mapping function
     * @param <U> the output type of the value mapping function
     * @param keyMapper a mapping function to produce keys, must be non-null
     * @param valueMapper a mapping function to produce values, must be non-null
     * @param mergeFunction a merge function, used to resolve collisions between
     *                      values associated with the same key, as supplied
     *                      to {@link Map#merge(Object, Object, BiFunction)},
     *                      must be non-null
     * @return a {@code Collector} that accumulates the input elements into an
     * <a href="../Map.html#unmodifiable">unmodifiable Map</a>, whose keys and values
     * are the result of applying the provided mapping functions to the input elements
     * @throws NullPointerException if the keyMapper, valueMapper, or mergeFunction is null
     *
     * @see #toUnmodifiableMap(Function, Function)
     * @since 10
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T, K, U>
    Collector<T, ?, Map<K,U>> toUnmodifiableMap(Function<? super T, ? extends K> keyMapper,
                                                Function<? super T, ? extends U> valueMapper,
                                                BinaryOperator<U> mergeFunction) {
        Objects.requireNonNull(keyMapper, "keyMapper");
        Objects.requireNonNull(valueMapper, "valueMapper");
        Objects.requireNonNull(mergeFunction, "mergeFunction");
        return collectingAndThen(
                toMap(keyMapper, valueMapper, mergeFunction, HashMap::new),
                map -> (Map<K,U>)Maps.ofEntries(map.entrySet().toArray(new Map.Entry[0])));
    }

    @SuppressWarnings("unchecked")
    private static <I, R> Function<I, R> castingIdentity() {
        return i -> (R) i;
    }


    /**
     * Simple implementation class for {@code Collector}.
     *
     * @param <T> the type of elements to be collected
     * @param <R> the type of the result
     */
    static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Set<Characteristics> characteristics;

        CollectorImpl(Supplier<A> supplier,
                      BiConsumer<A, T> accumulator,
                      BinaryOperator<A> combiner,
                      Function<A,R> finisher,
                      Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }

        CollectorImpl(Supplier<A> supplier,
                      BiConsumer<A, T> accumulator,
                      BinaryOperator<A> combiner,
                      Set<Characteristics> characteristics) {
            this(supplier, accumulator, combiner, castingIdentity(), characteristics);
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return accumulator;
        }

        @Override
        public Supplier<A> supplier() {
            return supplier;
        }

        @Override
        public BinaryOperator<A> combiner() {
            return combiner;
        }

        @Override
        public Function<A, R> finisher() {
            return finisher;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return characteristics;
        }
    }
}
