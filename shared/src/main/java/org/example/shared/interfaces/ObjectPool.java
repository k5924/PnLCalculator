package org.example.shared.interfaces;

public interface ObjectPool<T extends Worker> extends Worker{

    ObjectPool<T> setNumberOfWorkers(int numberOfWorkers);

    T get();

    T get(int hashCode);
}
