package com.yu.stu_join;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

import java.util.Comparator;

public class stuComparator extends WritableComparator {
    protected stuComparator() {
        super(stuBean.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        stuBean sa= (stuBean)a;
        stuBean sb= (stuBean)b;
        return sa.getS_id().compareTo(sb.getS_id());
    }
}
