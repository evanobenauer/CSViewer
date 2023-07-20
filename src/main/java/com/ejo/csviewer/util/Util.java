package com.ejo.csviewer.util;

import com.ejo.csviewer.element.Cell;

import java.util.ArrayList;

public class Util {

    public static <T> int getMaxRowSize(ArrayList<T[]> list) {
        int maxSize = list.get(0).length;
        for (T[] row : list) {
            if (row.length > maxSize) maxSize = row.length;
        }
        return maxSize;
    }

}
