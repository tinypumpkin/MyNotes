package com.yu;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class jobDriver {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        // 1 获取配置信息以及job实例
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration);

        // 2 设置jar包
        job.setJarByClass(jobDriver.class);

        // 3 设置map和reduce类
        job.setMapperClass(wcMapper.class);
        job.setReducerClass(wcReducer.class);

        // 4 设置Map和Reduce输出类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // 5 设置输入输出文件
        FileInputFormat.setInputPaths(job, new Path("hdfs://hadoop100:8020/wordcount"));
        FileOutputFormat.setOutputPath(job, new Path("hdfs://hadoop100:8020/wordcount_out"));

        // 7 提交
        boolean result = job.waitForCompletion(true);

        System.exit(result ? 0 : 1);
    }

}
