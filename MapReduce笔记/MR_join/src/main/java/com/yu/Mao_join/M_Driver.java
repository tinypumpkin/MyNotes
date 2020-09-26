package com.yu.Mao_join;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import javax.xml.soap.Text;
import java.io.IOException;
import java.net.URI;

public class M_Driver {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Job job = Job.getInstance(new Configuration());
        job.setJarByClass(M_Driver.class);
        job.setMapperClass(M_Mapper.class);
        //不要reduce ==>Map 阶段结束后直接outputformat
        job.setNumReduceTasks(0);

        //若使mapper可读取数据要在driver端设置分布式缓存mapper端读取
        //规模较小的表作为缓冲加载到内存
        job.addCacheFile(URI.create("file:///F:/Mywork/大数据/MapReduce基础/input/join/na.txt"));

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);

        //仅读取规模大的表
        FileInputFormat.setInputPaths(job, new Path("F:\\Mywork\\大数据\\MapReduce基础\\input\\join\\info.txt"));
        FileOutputFormat.setOutputPath(job, new Path("F:\\Mywork\\大数据\\MapReduce基础\\output\\map"));

        boolean b = job.waitForCompletion(true);
        System.exit(b?0:1);
    }
}
