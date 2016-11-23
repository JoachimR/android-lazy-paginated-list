package de.reiss.lazypaginationlist.generic;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * from
 * https://github.com/DocuSignDev/greenDAO/blob/master/DaoCore/src/de/greenrobot/dao/LazyList.java
 */
public class LazyList<E> implements List<E>, Closeable {

    public interface Converter {
        Object convertFromCursor(Cursor cursor);
    }

    private final Cursor cursor;
    private final Converter converter;
    private final List<E> entities;
    private final int count;
    private final ReentrantLock lock;
    private volatile int loadedCount;

    public LazyList(Cursor cursor, Converter converter, boolean cacheEntities) {
        this.cursor = cursor;
        this.converter = converter;

        count = cursor.getCount();
        if (cacheEntities) {
            entities = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                entities.add(null);
            }
        } else {
            entities = null;
        }
        if (count == 0) {
            cursor.close();
        }

        lock = new ReentrantLock();
    }

    /**
     * Loads the remaining entities (if any) that were not loaded before. Applies to cached lazy lists only.
     */
    private void loadRemaining() {
        checkCached();
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            get(i);
        }
    }

    private void checkCached() {
        if (entities == null) {
            throw new IllegalStateException("This operation only works with cached lazy lists");
        }
    }

    /**
     * Like get but does not load the entity if it was not loaded before.
     */
    public E peak(int location) {
        if (entities != null) {
            return entities.get(location);
        } else {
            return null;
        }
    }

    @Override
    /** Closes the underlying cursor: do not try to get entities not loaded (using get) before. */
    public void close() {
        cursor.close();
    }

    public boolean isClosed() {
        return cursor.isClosed();
    }

    public int getLoadedCount() {
        return loadedCount;
    }

    private boolean isLoadedCompletely() {
        return loadedCount == count;
    }

    @Override
    public boolean add(E item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int location, E item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int arg0, @NonNull Collection<? extends E> arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object item) {
        loadRemaining();
        return entities.contains(item);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        loadRemaining();
        return entities.containsAll(collection);
    }

    @Override
    public E get(int location) {
        if (entities != null) {
            E entity = entities.get(location);
            if (entity == null) {
                lock.lock();
                try {
                    entity = entities.get(location);
                    if (entity == null) {
                        entity = loadItem(location);
                        entities.set(location, entity);
                        loadedCount++;
                        if (isLoadedCompletely()) {
                            cursor.close();
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
            return entity;
        } else {
            return loadItem(location);
        }
    }

    @Nullable
    protected E loadItem(int location) {
        if (cursor.moveToPosition(location)) {
            //noinspection unchecked
            return (E) converter.convertFromCursor(cursor);
        }
        return null;
    }

    @Override
    public int indexOf(Object item) {
        loadRemaining();
        return entities.indexOf(item);
    }

    @Override
    public boolean isEmpty() {
        return count == 0;
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return new LazyIterator(0, false);
    }

    @Override
    public int lastIndexOf(Object item) {
        loadRemaining();
        return entities.lastIndexOf(item);
    }

    @Override
    public CloseableListIterator<E> listIterator() {
        return new LazyIterator(0, false);
    }

    /**
     * Closes this list's cursor once the iterator is fully iterated through.
     */
    public CloseableListIterator<E> listIteratorAutoClose() {
        return new LazyIterator(0, true);
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator(int location) {
        return new LazyIterator(location, false);
    }

    @Override
    public E remove(int location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E set(int location, E item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return count;
    }

    @NonNull
    @Override
    public List<E> subList(int start, int end) {
        checkCached();
        for (int i = start; i < end; i++) {
            entities.get(i);
        }
        return entities.subList(start, end);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        loadRemaining();
        return entities.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] array) {
        loadRemaining();
        return entities.toArray(array);
    }


    interface CloseableListIterator<T> extends ListIterator<T>, Closeable {
    }

    private class LazyIterator implements CloseableListIterator<E> {
        private int index;
        private final boolean closeWhenDone;

        LazyIterator(int startLocation, boolean closeWhenDone) {
            index = startLocation;
            this.closeWhenDone = closeWhenDone;
        }

        @Override
        public void add(E item) {
            throw new UnsupportedOperationException();
        }

        @Override
        /** FIXME: before hasPrevious(), next() must be called. */
        public boolean hasPrevious() {
            return index > 0;
        }

        @Override
        public int nextIndex() {
            return index;
        }

        @Override
        /** FIXME: before previous(), next() must be called. */
        public E previous() {
            if (index <= 0) {
                throw new NoSuchElementException();
            }
            index--;
            // if (index == size && closeWhenDone) {
            // close();
            // }
            return get(index);
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }

        @Override
        public void set(E item) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return index < count;
        }

        @Override
        public E next() {
            if (index >= count) {
                throw new NoSuchElementException();
            }
            E entity = get(index);
            index++;
            if (index == count && closeWhenDone) {
                close();
            }
            return entity;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            LazyList.this.close();
        }

    }

}