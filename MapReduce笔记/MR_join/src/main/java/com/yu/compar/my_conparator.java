package com.yu.compar;


import java.util.Comparator;

public interface my_conparator<T> extends Comparator<T> {
    int compare(T o1, T o2);
}
