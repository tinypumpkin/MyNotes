package com.yu.Reduce_Join;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

//自定义比较器用来分组
public class R_Comparator extends WritableComparator {
    protected R_Comparator() {
        super(InfoBean.class,true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        InfoBean ia =(InfoBean)a;
        InfoBean ib =(InfoBean)b;
        return  ia.getPid().compareTo(ib.getPid());
    }
}
