package com.yu.stu_join;

import com.yu.Reduce_Join.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class stuDriver {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Job job = Job.getInstance(new Configuration());
        job.setJarByClass(R_Driver.class);

        job.setMapperClass(stuMapper.class);
        job.setReducerClass(stuReduce.class);
        job.setGroupingComparatorClass(stuComparator.class);

        job.setMapOutputKeyClass(stuBean.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setOutputKeyClass(stuBean.class);
        job.setOutputValueClass(NullWritable.class);

        FileInputFormat.setInputPaths(job, new Path("F:\\BaiduNetdiskDownload\\大数据\\面试准备\\数据倾斜\\input"));
        FileOutputFormat.setOutputPath(job, new Path("F:\\BaiduNetdiskDownload\\大数据\\面试准备\\数据倾斜\\output"));

        boolean b = job.waitForCompletion(true);

        System.exit(b ? 0 : 1);
    }
}
