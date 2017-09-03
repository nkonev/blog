package com.github.nikit.cpp.dto;

import java.util.Collection;

public class Wrapper<T> {

    /**
     * total count
     */
    private long count;

    private Collection<T> data;

    public Wrapper() { }

    public Wrapper(Collection<T> data, long count) {
        this.count = count;
        this.data = data;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Collection<T> getData() {
        return data;
    }

    public void setData(Collection<T> data) {
        this.data = data;
    }
}
