package com.yu.Reduce_Join;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Iterator;

public class R_Reduce extends Reducer<InfoBean, NullWritable, InfoBean, NullWritable>{
    @Override
    protected void reduce(InfoBean key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {

        //第一条数据来自pd，之后全部来自Info
        Iterator<NullWritable> iterator = values.iterator();

        //通过第一条数据获取pname
        iterator.next();
        String pname = key.getPname();

        //遍历剩下的数据，替换并写出
        while (iterator.hasNext()) {
            iterator.next();
            key.setPname(pname);
            context.write(key,NullWritable.get());
        }
    }

}
