package fibheap;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;

/**
 * An unbounded priority {@linkplain Queue queue} based on a Fibonacci heap.
 * The elements of the priority queue are ordered according to their
 * {@linkplain Comparable natural ordering}, or by a {@link Comparator}
 * provided at queue construction time, depending on which constructor is
 * used. A priority queue does not permit {@code null} elements.
 * A priority queue relying on natural ordering also does not permit
 * insertion of non-comparable objects (doing so may result in
 * {@code ClassCastException}).
 *
 * <p>
 * The <em>head</em> of this queue is the <em>least</em> element
 * with respect to the specified ordering. If multiple elements are
 * tied for least value, the head is one of those elements -- ties are
 * broken arbitrarily. The queue retrieval operations {@code poll},
 * {@code remove}, {@code peek}, and {@code element} access the
 * element at the head of the queue.
 *
 * <p>
 * This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces. The Iterator provided in method {@link
 * #iterator()} is <em>not</em> guaranteed to traverse the elements of
 * the priority queue in any particular order. If you need ordered
 * traversal, consider using {@code Arrays.sort(fh.toArray())}.
 *
 * <p>
 * Implementation note: this implementation provides
 * constant time for enqueuing and retrieval methods
 * ({@code offer}, {@code peek}, {@code element}, {@code size} and {@code add});
 * amortized constant time for {@code contains(Object)}; amortized O(log(n))
 * time for dequeuing methods ({@code remove(Object)}, {@code poll} and
 * {@code remove()}).
 * 
 * This implementation was heavily inspired by {@code PriorityQueue}
 * made by Josh Bloch and Doug Lea
 *
 * @author Daniel Kulakovskiy
 * @see PriorityQueue
 * @see AbstractQueue
 * @param <E> the type of elements held in this queue
 */
@SuppressWarnings("unchecked")
public class FibHeap<E> extends AbstractQueue<E> {
    private static class Node<E> {
        E item;
        Node<E> parent;
        Node<E> child;
        Node<E> left;
        Node<E> right;
        int degree;
        boolean mark;

        public Node(final E item, final Node<E> parent, final Node<E> child, final Node<E> left, final Node<E> right) {
            this.item = item;
            this.parent = parent;
            this.child = child;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * The natural logarithm of golden ratio.
     */
    private static final double LOG_PHI = 0.48121182505960347;

    private final Map<E, List<Node<E>>> nodeMapping;

    /**
     * The number of elements in the Fibonacci heap.
     */
    int size;

    /**
     * The comparator, or null if priority queue uses elements'
     * natural ordering.
     */
    private final Comparator<? super E> comparator;

    /**
     * Priority queue represented as a Fibonacci heap: the children of
     * a node are represented as a linked list. The priority queue is
     * ordered by comparator, or by the elements' natural ordering, if
     * comparator is null: For each node n in the heap and each descendant
     * d of n, n <= d. The element with the lowest value is in minNode,
     * assuming the queue is nonempty. Otherwise minNode is null.
     */
    Node<E> minNode;

    /**
     * Creates a {@code FibHeap} that orders its elements according to their
     * {@linkplain Comparable natural ordering}.
     */
    public FibHeap() {
        this((Comparator<? super E>) null);
    }

    /**
     * Creates a {@code FibHeap} whose elements are ordered according to the
     * specified comparator.
     * 
     * @param comparator the comparator that will be used to order this
     *                   priority queue. If {@code null}, the {@linkplain Comparable
     *                   natural ordering} of the elements will be used.
     */
    public FibHeap(final Comparator<? super E> comparator) {
        this.comparator = comparator;
        this.nodeMapping = new HashMap<>();
    }

    /**
     * Creates a {@code FibHeap} containing the elements in the
     * specified collection. If the specified collection is an instance of
     * a {@link SortedSet} or is another {@code FibHeap}, this
     * priority queue will be ordered according to the same ordering.
     * Otherwise, this priority queue will be ordered according to the
     * {@linkplain Comparable natural ordering} of its elements.
     *
     * @param c the collection whose elements are to be placed
     *          into this priority queue
     * @throws ClassCastException   if elements of the specified collection
     *                              cannot be compared to one another according to
     *                              the priority
     *                              queue's ordering
     * @throws NullPointerException if the specified collection or any
     *                              of its elements are null
     */
    public FibHeap(final Collection<? extends E> c) {
        this.nodeMapping = new HashMap<>();
        if (c instanceof SortedSet<?>) {
            final SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
            this.comparator = (Comparator<? super E>) ss.comparator();
            addAll(ss);
        } else if (c instanceof FibHeap<?>) {
            final FibHeap<? extends E> fh = (FibHeap<? extends E>) c;
            this.comparator = (Comparator<? super E>) fh.comparator();
            addAll(fh);
        } else {
            this.comparator = null;
            addAll(c);
        }
    }

    /**
     * Creates a {@code FibHeap} containing the elements in the
     * specified priority queue. This priority queue will be
     * ordered according to the same ordering as the given priority
     * queue.
     *
     * @param c the priority queue whose elements are to be placed
     *          into this priority queue
     * @throws ClassCastException   if elements of {@code c} cannot be
     *                              compared to one another according to {@code c}'s
     *                              ordering
     * @throws NullPointerException if the specified priority queue or any
     *                              of its elements are null
     */
    public FibHeap(final FibHeap<? extends E> c) {
        this.comparator = (Comparator<? super E>) c.comparator();
        this.nodeMapping = new HashMap<>();
        addAll(c);
    }

    /**
     * Creates a {@code FibHeap} containing the elements in the
     * specified sorted set. This priority queue will be ordered
     * according to the same ordering as the given sorted set.
     *
     * @param c the sorted set whose elements are to be placed
     *          into this priority queue
     * @throws ClassCastException   if elements of the specified sorted
     *                              set cannot be compared to one another according
     *                              to the
     *                              sorted set's ordering
     * @throws NullPointerException if the specified sorted set or any
     *                              of its elements are null
     */
    public FibHeap(final SortedSet<? extends E> c) {
        this.comparator = (Comparator<? super E>) c.comparator();
        this.nodeMapping = new HashMap<>();
        addAll(c);
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws ClassCastException   if the specified element cannot be
     *                              compared with elements currently in this
     *                              priority queue
     *                              according to the priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(final E e) {
        return offer(e);
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws ClassCastException   if the specified element cannot be
     *                              compared with elements currently in this
     *                              priority queue
     *                              according to the priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(final E e) {
        if (e == null) {
            throw new NullPointerException("Null elements are not supported");
        }
        final Node<E> newNode = linkRight(e, minNode);
        if (minNode == null || compareElements(minNode.item, newNode.item) > 0) {
            minNode = newNode;
        }
        size++;
        nodeMapping.putIfAbsent(e, new ArrayList<Node<E>>());
        nodeMapping.get(e).add(newNode);
        return true;
    }

    public E peek() {
        return minNode == null ? null : minNode.item;
    }

    public boolean contains(final Object o) {
        return nodeMapping.get(o) != null;
    }

    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present. More formally, removes an element {@code e} such
     * that {@code o.equals(e)}, if this queue contains one or more such
     * elements. Returns {@code true} if and only if this queue contained
     * the specified element (or equivalently, if this queue changed as a
     * result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if this queue changed as a result of the call
     */
    public boolean remove(final Object o) {
        final List<Node<E>> nodeList = nodeMapping.get(o);
        final Node<E> node = nodeList != null ? nodeList.get(0) : null;
        if (node == null)
            return false;
        else {
            if (node.parent != null) {
                unlink(node);
                cascadingCut(node.parent);
                node.parent = null;
                unionLists(minNode, node);
            }
            minNode = node;
            poll();
            return true;
        }
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

    private final class Itr implements Iterator<E> {
        private final Stack<Boolean> childStack = new Stack<>();
        private Node<E> cursor = minNode;

        Itr() {
            childStack.push(false);
        }

        public boolean hasNext() {
            return cursor != null;
        }

        public E next() {
            final E currentElement = cursor.item;

            if (cursor.child != null) {
                cursor = cursor.child;
                childStack.push(cursor == cursor.right);
            } else {
                while (!childStack.empty() && childStack.peek()) {
                    cursor = cursor.parent;
                    childStack.pop();
                }
                if (!childStack.empty()) {
                    cursor = cursor.right;
                    if (cursor.right == minNode || cursor.parent != null &&
                            cursor.right == cursor.parent.child) {
                        childStack.set(childStack.size() - 1, true);
                    }
                }
            }

            return currentElement;
        }
    }

    /**
     * Merges this Fibonacci heap with another one.
     * <strong>Note that comparator of this Fibonacci heap
     * stays the same</strong>
     * 
     * @param other another instance of a Fibonacci heap
     * @throws IllegalArgumentException if {@code comparator}
     *                                  is not equal to {@code other.comparator} or
     *                                  if {@code other}
     *                                  is equal to {@code this}
     */
    public void merge(final FibHeap<E> other) {
        if (other.equals(this)) {
            throw new IllegalArgumentException("Merging with itself is not allowed");
        }
        if (other.comparator == null && comparator != null || other.comparator != null && !other.comparator.equals(comparator)) {
            throw new IllegalArgumentException("Comparators should be equal");
        }
        if (other.isEmpty()) {
            return;
        }
        for (var i : other.nodeMapping.entrySet()) {
            if (nodeMapping.containsKey(i.getKey())) {
                nodeMapping.get(i.getKey()).addAll(i.getValue());
            } else {
                nodeMapping.put(i.getKey(), i.getValue());
            }
        }
        if (isEmpty()) {
            minNode = other.minNode;
            size = other.size;
            return;
        }
        unionLists(minNode, other.minNode);
        size += other.size;
        if (compareElements(minNode.item, other.minNode.item) > 0) {
            minNode = other.minNode;
        }
    }

    public int size() {
        return size;
    }

    /**
     * Returns the comparator used to order the elements in this
     * queue, or {@code null} if this queue is sorted according to
     * the {@linkplain Comparable natural ordering} of its elements.
     *
     * @return the comparator used to order this queue, or
     *         {@code null} if this queue is sorted according to the
     *         natural ordering of its elements
     */
    public Comparator<? super E> comparator() {
        return comparator;
    }

    public E poll() {
        if (minNode == null) {
            return null;
        }
        final Node<E> prevChild = minNode.child;
        if (prevChild != null) {
            minNode.child = null;
            for (Node<E> currentChild = prevChild; currentChild.parent != null; currentChild = currentChild.right) {
                currentChild.parent = null;
            }
        }
        unionLists(minNode, prevChild);
        final Node<E> prevMin = minNode;
        if (minNode.right == minNode) {
            minNode = null;
        } else {
            minNode = minNode.right;
            unlink(prevMin);
            consolidate();
        }
        size--;
        
        final List<Node<E>> nodeList = nodeMapping.get(prevMin.item);

        // this removal is constant time as long as all elements of
        // a priority queue are unique. else it is can be as bad as
        // linear time of all nodes

        nodeList.remove(prevMin);
        if (nodeList.isEmpty()) {
            nodeMapping.remove(prevMin.item);
        }
        return prevMin.item;
    }

    private void consolidate() {
        final Node<E>[] A = new Node[((int) Math.ceil(Math.log(size) / LOG_PHI)) + 1];
        Node<E> current = minNode;
        while (A[current.degree] != current) {
            if (A[current.degree] == null) {
                A[current.degree] = current;
                current = current.right;
            } else {
                final Node<E> conflict = A[current.degree];
                A[current.degree] = null;
                Node<E> addTo, adding;
                if (compareElements(conflict.item, current.item) < 0) {
                    addTo = conflict;
                    adding = current;
                } else {
                    addTo = current;
                    adding = conflict;
                }
                unlink(adding);
                if (addTo.child != null) {
                    unionLists(addTo.child, adding);
                }
                adding.parent = addTo;
                addTo.child = adding;
                addTo.degree++;
                current = addTo;
            }
            if (compareElements(minNode.item, current.item) > 0) {
                minNode = current;
            }
        }
    }

    private void unionLists(final Node<E> first, final Node<E> second) {
        if (first == null || second == null || first == second) {
            return;
        }
        final Node<E> L = second.left;
        final Node<E> R = first.right;
        first.right = second;
        second.left = first;
        L.right = R;
        R.left = L;
    }

    private Node<E> linkRight(final E element, final Node<E> right) {
        final Node<E> newNode;
        if (right == null) {
            newNode = new Node<>(element, null, null, null, null);
            return newNode.left = (newNode.right = newNode);
        }
        final Node<E> left = right.left;
        newNode = new Node<>(element, right.parent, null, left, right);
        return right.left = (left.right = newNode);
    }

    private void cascadingCut(Node<E> node) {
        while (node != null && node.mark) {
            unlink(node);
            final Node<E> parent = node.parent;
            node.parent = null;
            node = parent;
        }
        if (node != null)
            node.mark = true;
    }

    private Node<E> unlink(final Node<E> node) {
        // assert node != null;
        node.mark = false;
        if (node.parent != null) {
            node.parent.degree--;
            if (node.parent.child == node) {
                if (node.right != node) {
                    node.parent.child = node.right;
                } else {
                    node.parent.child = null;
                }
            }
        }
        if (node.right != node) {
            final Node<E> L = node.left;
            final Node<E> R = node.right;
            node.left = (node.right = node);
            L.right = R;
            R.left = L;
        }
        return node;
    }

    private int compareElements(final E e1, final E e2) {
        if (comparator != null) {
            final Comparator<? super E> cmp = (Comparator<? super E>) comparator;
            return cmp.compare(e1, e2);
        } else {
            final Comparable<? super E> key = (Comparable<? super E>) e1;
            return key.compareTo(e2);
        }
    }

}
