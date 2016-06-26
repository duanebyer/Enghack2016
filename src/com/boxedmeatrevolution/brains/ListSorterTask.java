package com.boxedmeatrevolution.brains;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by aidan on 2016-06-25.
 */
public class ListSorterTask<T extends Comparable<T>> extends Task {

   public ListSorterTask(List<T> list) {
       super();
       _list = new ArrayList<>(list);
   }

    @Override
    public Serializable call() {
        Collections.sort(_list);
        return _list;
    }

    private ArrayList<T> _list;
}
