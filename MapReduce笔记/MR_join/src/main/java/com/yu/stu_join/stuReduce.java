package com.yu.stu_join;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Iterator;

public class stuReduce extends Reducer<stuBean, NullWritable,stuBean,NullWritable> {
    @Override
    protected void reduce(stuBean key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
        Iterator<NullWritable> its = values.iterator();
        String s_name = key.getS_name();
        String c_name = key.getC_name();
        while (its.hasNext()){
            its.next();
            key.setS_name(s_name);
            key.setC_name(c_name);
            context.write(key,NullWritable.get());
        }
    }
}
