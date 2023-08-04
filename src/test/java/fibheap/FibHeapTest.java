package fibheap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class FibHeapTest {
    @Test
    public void testCtorInit() {
        FibHeap<Integer> heap = new FibHeap<>();
        assertEquals(0, heap.size());
        assertTrue(heap.isEmpty());
        assertEquals(null, heap.peek());
        assertEquals(null, heap.poll());
    }

    @Test
    public void testMinElement() {
        FibHeap<Integer> heap = new FibHeap<>();
        heap.add(3);
        heap.add(5);
        heap.add(2);
        heap.add(7);
        heap.add(6);
        assertEquals(5, heap.size());
        assertEquals(2, heap.peek().intValue());
        heap.add(1);
        assertEquals(6, heap.size());
        assertEquals(1, heap.peek().intValue());
    }

    @Test
    public void testNullElements() {
        assertThrows(NullPointerException.class, () -> {
            FibHeap<Integer> heap = new FibHeap<>();
            heap.add(null);
        });
    }

    @Test
    public void testInsertRemove() {
        FibHeap<Integer> heap = new FibHeap<>();
        heap.add(3);
        heap.add(5);
        heap.add(2);
        heap.add(10);
        heap.add(11);
        heap.add(12);
        heap.add(13);
        assertEquals(2, heap.remove().intValue());
        assertEquals(3, heap.remove().intValue());
        assertEquals(5, heap.remove().intValue());
        assertEquals(10, heap.remove().intValue());
        assertEquals(3, heap.size());
        heap.add(10);
        assertEquals(10, heap.remove().intValue());
    }

    @Test
    public void testIterator() {
        FibHeap<Integer> heap = new FibHeap<>(List.of(7, 6, 88, 33, 5, 21, 44, 1, 4, 3));
        heap.remove();
        int count = 0;
        for (Iterator<Integer> iterator = heap.iterator(); iterator.hasNext(); iterator.next()) {
            count++;
        }
        assertEquals(heap.size(), count);
    }

    @Test
    public void testCollections() {
        List<Integer> list = List.of(7, 6, 88, 33, 5, 21, 44, 1, 4, 3);
        SortedSet<Integer> sortedSet = new TreeSet<>(list);

        FibHeap<Integer> listHeap = new FibHeap<>(list);
        FibHeap<Integer> collectionHeapHeap = new FibHeap<>((Collection<Integer>) listHeap);
        FibHeap<Integer> heapHeap = new FibHeap<>(listHeap);
        FibHeap<Integer> collectionSortedSetHeap = new FibHeap<>((Collection<Integer>) sortedSet);
        FibHeap<Integer> sortedSetHeap = new FibHeap<>(sortedSet);

        for (Integer element : list.stream().sorted().toList()) {
            assertEquals(element, listHeap.remove());
            assertEquals(element, heapHeap.remove());
            assertEquals(element, collectionHeapHeap.remove());
            assertEquals(element, sortedSetHeap.remove());
            assertEquals(element, collectionSortedSetHeap.remove());
        }
    }

    @Test
    public void testComparators() {
        class Pair<T extends Comparable<T>, U extends Comparable<U>> {
            T x;
            U y;

            public Pair(T x, U y) {
                this.x = x;
                this.y = y;
            }

            @Override
            public String toString() {
                return "{" + x + ", " + y + "}";
            }
        }

        class PairComparator<T extends Comparable<T>, U extends Comparable<U>> implements Comparator<Pair<T, U>> {
            @Override
            public int compare(Pair<T, U> o1, Pair<T, U> o2) {
                int resx = ((Comparable<? super T>) o1.x).compareTo(o2.x);
                return resx != 0 ? resx : ((Comparable<? super U>) o1.y).compareTo(o2.y);
            }
        }

        FibHeap<Pair<Integer, String>> heap = new FibHeap<>(new PairComparator<>());
        List<Pair<Integer, String>> list = List.of(
                new Pair<Integer, String>(3, "The World"),
                new Pair<Integer, String>(3, "Star Platinum"),
                new Pair<Integer, String>(4, "Killer Queen"),
                new Pair<Integer, String>(5, "Gold Experience"),
                new Pair<Integer, String>(7, "Scary Monsters"),
                new Pair<Integer, String>(5, "King Crimson"),
                new Pair<Integer, String>(7, "Tusk"),
                new Pair<Integer, String>(6, "Stone Free"),
                new Pair<Integer, String>(4, "Crazy Diamond"),
                new Pair<Integer, String>(6, "Made In Heaven"));
        heap.addAll(list);

        for (Pair<Integer, String> element : list.stream().sorted(new PairComparator<>()).toList()) {
            assertEquals(element, heap.remove());
        }
    }

    @Test
    public void testContainsRemoveParticular() {
        FibHeap<Integer> heap = new FibHeap<>();
        List<Integer> list = IntStream.range(1, 64).boxed().toList();
        heap.addAll(list);
        for (Integer i : list) {
            assertTrue(heap.contains(i));
        }
        assertFalse(heap.contains(Integer.valueOf(432141)));
        assertFalse(heap.remove(Integer.valueOf(432141)));
        assertTrue(heap.remove(list.get(0)));
        assertNotEquals(list.get(0), heap.peek());
        assertFalse(heap.contains(list.get(0)));
        heap.remove();
        assertFalse(heap.contains(list.get(1)));
        assertTrue(heap.remove(list.get(26)));
        assertFalse(heap.contains(list.get(26)));
        assertTrue(heap.remove(list.get(27)));
        assertFalse(heap.contains(list.get(27)));
        assertTrue(heap.contains(list.get(25)));
    }

    @Test
    public void randomTestRemoveParticular() {
        Random random = new Random(0xDEAFDEED);
        List<Integer> list = random.ints(0, 0x80).limit(0x8000).boxed().collect(Collectors.toList());
        FibHeap<Integer> heap = new FibHeap<>(list);

        for (int i = 0; i < 0x2000; i++) {
            Integer target = random.nextInt(0x80);
            heap.remove(target);
            list.remove(target);
            assertEquals(list.contains(target), heap.contains(target));
        }
    }

    @Test
    public void testMerging() {
        FibHeap<Integer> firstHeap = new FibHeap<>(List.of(
            2, 3, 5, 8, 10
        ));
        FibHeap<Integer> secondHeap = new FibHeap<>(List.of(
            1, 4, 6, 7, 9, 10
        ));
        firstHeap.merge(secondHeap);
        assertEquals(11, firstHeap.size());
        for (int i : IntStream.range(1, 11).toArray()) {
            assertEquals(i, firstHeap.poll().intValue());
        }

        firstHeap = new FibHeap<>(List.of(
            1
        ));
        secondHeap = new FibHeap<>();
        firstHeap.merge(secondHeap);
        assertEquals(1, firstHeap.size());
        assertEquals(1, firstHeap.poll());

        firstHeap = new FibHeap<>();
        secondHeap = new FibHeap<>(List.of(
            2
        ));
        firstHeap.merge(secondHeap);
        assertEquals(1, firstHeap.size());
        assertEquals(2, firstHeap.poll());

        firstHeap = new FibHeap<>();
        secondHeap = new FibHeap<>();
        firstHeap.merge(secondHeap);
        assertEquals(0, firstHeap.size());

        assertThrows(IllegalArgumentException.class, () -> {
            FibHeap<Integer> heap = new FibHeap<>();
            heap.merge(heap);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            FibHeap<Integer> heap = new FibHeap<>();
            FibHeap<Integer> anotherHeap = new FibHeap<>(new Comparator<Integer>(){
                @Override
                public int compare(Integer arg0, Integer arg1) {
                    return -arg0.compareTo(arg1);
                }
            });
            heap.merge(anotherHeap);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            FibHeap<Integer> heap = new FibHeap<>(new Comparator<Integer>(){
                @Override
                public int compare(Integer arg0, Integer arg1) {
                    return -arg0.compareTo(arg1);
                }
            });
            FibHeap<Integer> anotherHeap = new FibHeap<>();
            heap.merge(anotherHeap);
        });
    }
}
