/*     / \____  _    ______   _____ / \____   ____  _____
 *    /  \__  \/ \  / \__  \ /  __//  \__  \ /    \/ __  \   Javaslang
 *  _/  // _\  \  \/  / _\  \\_  \/  // _\  \  /\  \__/  /   Copyright 2014-2015 Daniel Dietrich
 * /___/ \_____/\____/\_____/____/\___\_____/_/  \_/____/    Licensed under the Apache License, Version 2.0
 */
package javaslang.collection;

import javaslang.Lazy;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.control.None;
import javaslang.control.Option;
import javaslang.control.Some;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * An immutable {@code HashSet} implementation.
 *
 * @param <T> Component type
 * @since 2.0.0
 */
public final class HashSet<T> implements Set<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final HashSet<?> EMPTY = new HashSet<>(HashArrayMappedTrie.empty());

    private final HashArrayMappedTrie<T, T> tree;
    private final transient Lazy<Integer> hash;

    private HashSet(HashArrayMappedTrie<T, T> tree) {
        this.tree = tree;
        this.hash = Lazy.of(() -> Traversable.hash(tree::iterator));
    }

    @SuppressWarnings("unchecked")
    public static <T> HashSet<T> empty() {
        return (HashSet<T>) EMPTY;
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link javaslang.collection.HashSet}.
     *
     * @param <T> Component type of the HashSet.
     * @return A javaslang.collection.HashSet Collector.
     */
    public static <T> Collector<T, ArrayList<T>, HashSet<T>> collector() {
        final Supplier<ArrayList<T>> supplier = ArrayList::new;
        final BiConsumer<ArrayList<T>, T> accumulator = ArrayList::add;
        final BinaryOperator<ArrayList<T>> combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        final Function<ArrayList<T>, HashSet<T>> finisher = HashSet::ofAll;
        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    /**
     * Returns a singleton {@code HashSet}, i.e. a {@code HashSet} of one element.
     *
     * @param element An element.
     * @param <T>     The component type
     * @return A new HashSet instance containing the given element
     */
    static <T> HashSet<T> of(T element) {
        return HashSet.<T> empty().add(element);
    }

    /**
     * <p>
     * Creates a HashSet of the given elements.
     * </p>
     *
     * <pre>
     * <code>  HashSet.of(1, 2, 3, 4)
     * </pre>
     *
     * @param <T>      Component type of the HashSet.
     * @param elements Zero or more elements.
     * @return A set containing the given elements.
     * @throws NullPointerException if {@code elements} is null
     */
    @SafeVarargs
    static <T> HashSet<T> of(T... elements) {
        Objects.requireNonNull(elements, "elements is null");
        HashArrayMappedTrie<T, T> tree = HashArrayMappedTrie.empty();
        for (T element : elements) {
            tree = tree.put(element, element);
        }
        return new HashSet<>(tree);
    }

    /**
     * Creates a HashSet of the given elements.
     *
     * @param elements Set elements
     * @param <T>      The value type
     * @return A new HashSet containing the given entries
     */
    @SuppressWarnings("unchecked")
    public static <T> HashSet<T> ofAll(java.lang.Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof HashSet) {
            return (HashSet<T>) elements;
        } else {
            final HashArrayMappedTrie<T, T> tree = addAll(HashArrayMappedTrie.empty(), elements);
            return tree.isEmpty() ? empty() : new HashSet<>(tree);
        }
    }

    /**
     * Creates a HashSet based on the elements of a boolean array.
     *
     * @param array a boolean array
     * @return A new HashSet of Boolean values
     */
    static HashSet<Boolean> ofAll(boolean[] array) {
        Objects.requireNonNull(array, "array is null");
        return HashSet.ofAll(Iterator.ofAll(array));
    }

    /**
     * Creates a HashSet based on the elements of a byte array.
     *
     * @param array a byte array
     * @return A new HashSet of Byte values
     */
    static HashSet<Byte> ofAll(byte[] array) {
        Objects.requireNonNull(array, "array is null");
        return HashSet.ofAll(Iterator.ofAll(array));
    }

    /**
     * Creates a HashSet based on the elements of a char array.
     *
     * @param array a char array
     * @return A new HashSet of Character values
     */
    static HashSet<Character> ofAll(char[] array) {
        Objects.requireNonNull(array, "array is null");
        return HashSet.ofAll(Iterator.ofAll(array));
    }

    /**
     * Creates a HashSet based on the elements of a double array.
     *
     * @param array a double array
     * @return A new HashSet of Double values
     */
    static HashSet<Double> ofAll(double[] array) {
        Objects.requireNonNull(array, "array is null");
        return HashSet.ofAll(Iterator.ofAll(array));
    }

    /**
     * Creates a HashSet based on the elements of a float array.
     *
     * @param array a float array
     * @return A new HashSet of Float values
     */
    static HashSet<Float> ofAll(float[] array) {
        Objects.requireNonNull(array, "array is null");
        return HashSet.ofAll(Iterator.ofAll(array));
    }

    /**
     * Creates a HashSet based on the elements of an int array.
     *
     * @param array an int array
     * @return A new HashSet of Integer values
     */
    static HashSet<Integer> ofAll(int[] array) {
        Objects.requireNonNull(array, "array is null");
        return HashSet.ofAll(Iterator.ofAll(array));
    }

    /**
     * Creates a HashSet based on the elements of a long array.
     *
     * @param array a long array
     * @return A new HashSet of Long values
     */
    static HashSet<Long> ofAll(long[] array) {
        Objects.requireNonNull(array, "array is null");
        return HashSet.ofAll(Iterator.ofAll(array));
    }

    /**
     * Creates a HashSet based on the elements of a short array.
     *
     * @param array a short array
     * @return A new HashSet of Short values
     */
    static HashSet<Short> ofAll(short[] array) {
        Objects.requireNonNull(array, "array is null");
        return HashSet.ofAll(Iterator.ofAll(array));
    }

    @Override
    public HashSet<T> add(T element) {
        return new HashSet<>(tree.put(element, element));
    }

    @Override
    public HashSet<T> addAll(java.lang.Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        final HashArrayMappedTrie<T, T> that = addAll(tree, elements);
        if (that.size() == tree.size()) {
            return this;
        } else {
            return new HashSet<>(that);
        }
    }

    @Override
    public HashSet<T> clear() {
        return empty();
    }

    @Override
    public boolean contains(T element) {
        return tree.get(element).isDefined();
    }

    @Override
    public HashSet<T> diff(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (isEmpty() || elements.isEmpty()) {
            return this;
        } else {
            return removeAll(elements);
        }
    }

    @Override
    public HashSet<T> distinct() {
        return this;
    }

    @Override
    public HashSet<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return HashSet.ofAll(iterator().distinctBy(comparator));
    }

    @Override
    public <U> HashSet<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        return HashSet.ofAll(iterator().distinctBy(keyExtractor));
    }

    @Override
    public HashSet<T> drop(int n) {
        if (n <= 0) {
            return this;
        } else {
            return HashSet.ofAll(iterator().drop(n));
        }
    }

    @Override
    public HashSet<T> dropRight(int n) {
        return drop(n);
    }

    @Override
    public HashSet<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final HashSet<T> dropped = HashSet.ofAll(iterator().dropWhile(predicate));
        return dropped.length() == length() ? this : dropped;
    }

    @Override
    public HashSet<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final HashSet<T> filtered = HashSet.ofAll(iterator().filter(predicate));
        return filtered.length() == length() ? this : filtered;
    }

    @Override
    public Option<T> findLast(Predicate<? super T> predicate) {
        return findFirst(predicate);
    }

    @Override
    public <U> HashSet<U> flatMap(Function<? super T, ? extends java.lang.Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isEmpty()) {
            return empty();
        } else {
            final HashArrayMappedTrie<U, U> that = foldLeft(HashArrayMappedTrie.empty(), (tree, t) -> addAll(tree, mapper.apply(t)));
            return new HashSet<>(that);
        }
    }

    @Override
    public HashSet<Object> flatten() {
        return flatMap(t -> (t instanceof java.lang.Iterable) ? HashSet.ofAll((java.lang.Iterable<?>) t).flatten() : HashSet.of(t));
    }

    @Override
    public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        return foldLeft(zero, (u, t) -> f.apply(t, u));
    }

    @Override
    public <C> Map<C, HashSet<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return foldLeft(HashMap.empty(), (map, t) -> {
            final C key = classifier.apply(t);
            final HashSet<T> values = map.get(key).map(ts -> ts.add(t)).orElse(HashSet.of(t));
            return map.put(key, values);
        });
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public T head() {
        if (tree.isEmpty()) {
            throw new NoSuchElementException("head of empty set");
        }
        return iterator().next();
    }

    @Override
    public Option<T> headOption() {
        return iterator().headOption();
    }

    @Override
    public HashSet<T> init() {
        return tail();
    }

    @Override
    public Option<HashSet<T>> initOption() {
        return tailOption();
    }

    @Override
    public HashSet<T> intersect(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (isEmpty() || elements.isEmpty()) {
            return empty();
        } else {
            return retainAll(elements);
        }
    }

    @Override
    public boolean isEmpty() {
        return tree.isEmpty();
    }

    @Override
    public boolean isTraversableAgain() {
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return tree.iterator().map(t -> t._1);
    }

    @Override
    public int length() {
        return tree.size();
    }

    @Override
    public <U> HashSet<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isEmpty()) {
            return empty();
        } else {
            final HashArrayMappedTrie<U, U> that = foldLeft(HashArrayMappedTrie.empty(), (tree, t) -> {
                final U u = mapper.apply(t);
                return tree.put(u, u);
            });
            return new HashSet<>(that);
        }
    }

    @Override
    public String mkString(CharSequence delimiter,
                           CharSequence prefix,
                           CharSequence suffix) {
        final StringBuilder builder = new StringBuilder(prefix);
        forEach(t -> builder.append(String.valueOf(t)).append(String.valueOf(delimiter)));
        if (!isEmpty()) {
            builder.delete(builder.length() - delimiter.length(), builder.length());
        }
        return builder.append(suffix).toString();
    }

    @Override
    public Tuple2<HashSet<T>, HashSet<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final Tuple2<Iterator<T>, Iterator<T>> p = iterator().partition(predicate);
        return Tuple.of(HashSet.ofAll(p._1), HashSet.ofAll(p._2));
    }

    @Override
    public HashSet<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!isEmpty()) {
            action.accept(iterator().head());
        }
        return this;
    }

    @Override
    public T reduceRight(BiFunction<? super T, ? super T, ? extends T> op) {
        return reduceLeft(op);
    }

    @Override
    public HashSet<T> remove(T element) {
        final HashArrayMappedTrie<T, T> newTree = tree.remove(element);
        return newTree == tree ? this : new HashSet<>(newTree);
    }

    @Override
    public HashSet<T> removeAll(java.lang.Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        HashArrayMappedTrie<T, T> trie = tree;
        for (T element : elements) {
            trie = trie.remove(element);
        }
        return (trie == tree) ? this : new HashSet<>(trie);
    }

    @Override
    public HashSet<T> replace(T currentElement, T newElement) {
        if (tree.containsKey(currentElement)) {
            return remove(currentElement).add(newElement);
        } else {
            return this;
        }
    }

    @Override
    public HashSet<T> replaceAll(T currentElement, T newElement) {
        return replace(currentElement, newElement);
    }

    @Override
    public HashSet<T> replaceAll(UnaryOperator<T> operator) {
        Objects.requireNonNull(operator, "operator is null");
        HashArrayMappedTrie<T, T> that = HashArrayMappedTrie.empty();
        for (T currElem : this) {
            T newElem = operator.apply(currElem);
            that = that.put(newElem, newElem);
        }
        return new HashSet<>(that);
    }

    @Override
    public HashSet<T> retainAll(java.lang.Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        final HashArrayMappedTrie<T, T> keeped = addAll(HashArrayMappedTrie.empty(), elements);
        HashArrayMappedTrie<T, T> that = HashArrayMappedTrie.empty();
        for (T element : this) {
            if (keeped.containsKey(element)) {
                that = that.put(element, element);
            }
        }
        return new HashSet<>(that);
    }

    @Override
    public Tuple2<HashSet<T>, HashSet<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final Tuple2<Iterator<T>, Iterator<T>> t = iterator().span(predicate);
        return Tuple.of(HashSet.ofAll(t._1), HashSet.ofAll(t._2));
    }

    @Override
    public HashSet<T> tail() {
        if (tree.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty set");
        }
        return remove(head());
    }

    @Override
    public Option<HashSet<T>> tailOption() {
        if (tree.isEmpty()) {
            return None.instance();
        } else {
            return new Some<>(tail());
        }
    }

    @Override
    public HashSet<T> take(int n) {
        if (tree.size() <= n) {
            return this;
        }
        return HashSet.ofAll(() -> iterator().take(n));
    }

    @Override
    public HashSet<T> takeRight(int n) {
        return take(n);
    }

    @Override
    public HashSet<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        HashSet<T> taken = HashSet.ofAll(iterator().takeWhile(predicate));
        return taken.length() == length() ? this : taken;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HashSet<T> union(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (isEmpty()) {
            if (elements instanceof HashSet) {
                return (HashSet<T>) elements;
            } else {
                return HashSet.ofAll(elements);
            }
        } else if (elements.isEmpty()) {
            return this;
        } else {
            final HashArrayMappedTrie<T, T> that = addAll(tree, elements);
            if (that.size() == tree.size()) {
                return this;
            } else {
                return new HashSet<>(that);
            }
        }
    }

    @Override
    public <T1, T2> Tuple2<HashSet<T1>, HashSet<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        Tuple2<Iterator<T1>, Iterator<T2>> t = iterator().unzip(unzipper);
        return Tuple.of(HashSet.ofAll(t._1), HashSet.ofAll(t._2));
    }

    @Override
    public <U> HashSet<Tuple2<T, U>> zip(java.lang.Iterable<U> that) {
        Objects.requireNonNull(that, "that is null");
        return HashSet.ofAll(iterator().zip(that));
    }

    @Override
    public <U> HashSet<Tuple2<T, U>> zipAll(java.lang.Iterable<U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return HashSet.ofAll(iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    public HashSet<Tuple2<T, Integer>> zipWithIndex() {
        return HashSet.ofAll(iterator().zipWithIndex());
    }

    // -- Object

    @Override
    public int hashCode() {
        return hash.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof HashSet) {
            final HashSet<?> that = (HashSet<?>) o;
            return this.length() == that.length() && ((HashSet<Object>) this).containsAll(that);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return mkString(", ", "HashSet(", ")");
    }

    private static <T> HashArrayMappedTrie<T, T> addAll(HashArrayMappedTrie<T, T> initial, Iterable<? extends T> additional) {
        HashArrayMappedTrie<T, T> that = initial;
        for (T t : additional) {
            that = that.put(t, t);
        }
        return that;
    }

    // -- Serialization

    /**
     * <p>
     * {@code writeReplace} method for the serialization proxy pattern.
     * </p>
     * <p>
     * The presence of this method causes the serialization system to emit a SerializationProxy instance instead of
     * an instance of the enclosing class.
     * </p>
     *
     * @return A SerialiationProxy for this enclosing class.
     */
    private Object writeReplace() {
        return new SerializationProxy<>(this.tree);
    }

    /**
     * <p>
     * {@code readObject} method for the serialization proxy pattern.
     * </p>
     * Guarantees that the serialization system will never generate a serialized instance of the enclosing class.
     *
     * @param stream An object serialization stream.
     * @throws java.io.InvalidObjectException This method will throw with the message "Proxy required".
     */
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    /**
     * A serialization proxy which, in this context, is used to deserialize immutable, linked Lists with final
     * instance fields.
     *
     * @param <T> The component type of the underlying list.
     */
    // DEV NOTE: The serialization proxy pattern is not compatible with non-final, i.e. extendable,
    // classes. Also, it may not be compatible with circular object graphs.
    private static final class SerializationProxy<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        // the instance to be serialized/deserialized
        private transient HashArrayMappedTrie<T, T> tree;

        /**
         * Constructor for the case of serialization, called by {@link HashSet#writeReplace()}.
         * <p/>
         * The constructor of a SerializationProxy takes an argument that concisely represents the logical state of
         * an instance of the enclosing class.
         *
         * @param tree a Cons
         */
        SerializationProxy(HashArrayMappedTrie<T, T> tree) {
            this.tree = tree;
        }

        /**
         * Write an object to a serialization stream.
         *
         * @param s An object serialization stream.
         * @throws java.io.IOException If an error occurs writing to the stream.
         */
        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeInt(tree.size());
            for (Tuple2<T, T> e : tree) {
                s.writeObject(e._1);
            }
        }

        /**
         * Read an object from a deserialization stream.
         *
         * @param s An object deserialization stream.
         * @throws ClassNotFoundException If the object's class read from the stream cannot be found.
         * @throws InvalidObjectException If the stream contains no list elements.
         * @throws IOException            If an error occurs reading from the stream.
         */
        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            final int size = s.readInt();
            if (size < 0) {
                throw new InvalidObjectException("No elements");
            }
            HashArrayMappedTrie<T, T> temp = HashArrayMappedTrie.empty();
            for (int i = 0; i < size; i++) {
                @SuppressWarnings("unchecked")
                final T element = (T) s.readObject();
                temp = temp.put(element, element);
            }
            tree = temp;
        }

        /**
         * <p>
         * {@code readResolve} method for the serialization proxy pattern.
         * </p>
         * Returns a logically equivalent instance of the enclosing class. The presence of this method causes the
         * serialization system to translate the serialization proxy back into an instance of the enclosing class
         * upon deserialization.
         *
         * @return A deserialized instance of the enclosing class.
         */
        private Object readResolve() {
            return tree.isEmpty() ? HashSet.empty() : new HashSet<>(tree);
        }
    }
}