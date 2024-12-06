package org.example.shared.interfaces;

public interface ObjectPool<T extends Worker> extends Worker{

    T get();
}
