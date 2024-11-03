package com.seeyou.dieselpoint.norm;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class Page<T> extends ArrayList<T> {
    private static final long serialVersionUID = 1L;
    private int               pageNum;
    private int               pageSize;
    private int               totalSize;

    public Page(Collection<T> elements, int pageNum, int pageSize, int totalSize) {
        super(elements);
        this.pageNum                           = pageNum;
        this.pageSize                          = pageSize;
        this.totalSize                         = totalSize;
    }

    public int pageNumber() {
        return pageNum;
    }

    public int pageSize() {
        return pageSize;
    }

    public int totalSize() {
        return totalSize;
    }
}
