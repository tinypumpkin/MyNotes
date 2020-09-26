package com.yu.Reduce_Join;
/**
Map端的主要工作：
 为来自不同表或文件的key/value对，打标签以区别不同来源的记录。
 然后用连接字段作为key，其余部分和新加的标志作为value，最后进行输出。

 Reduce端的主要工作：
 在Reduce端以连接字段作为key的分组已经完成，我们只需要在每一个分组当中
 将那些来源于不同文件的记录(在Map阶段已经打标志)分开，最后进行合并就ok了。

 通过将关联条件作为Map输出的key，将两表满足Join条件的数据并携带数据所来源的文件信息，
 发往同一个ReduceTask，在Reduce中进行数据的串联
 **/

import org.apache.hadoop.conf.Configuration;
        import org.apache.hadoop.fs.Path;
        import org.apache.hadoop.io.NullWritable;
        import org.apache.hadoop.mapreduce.Job;
        import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
        import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

        import java.io.IOException;

public class R_Driver {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Job job = Job.getInstance(new Configuration());
        job.setJarByClass(R_Driver.class);

        job.setMapperClass(R_Maper.class);
        job.setReducerClass(R_Reduce.class);
        job.setGroupingComparatorClass(R_Comparator.class);

        job.setMapOutputKeyClass(InfoBean.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setOutputKeyClass(InfoBean.class);
        job.setOutputValueClass(NullWritable.class);

        FileInputFormat.setInputPaths(job, new Path("F:\\Mywork\\大数据\\MapReduce基础\\input\\join"));
        FileOutputFormat.setOutputPath(job, new Path("F:\\Mywork\\大数据\\MapReduce基础\\output\\join"));

        boolean b = job.waitForCompletion(true);

        System.exit(b ? 0 : 1);

    }
}
