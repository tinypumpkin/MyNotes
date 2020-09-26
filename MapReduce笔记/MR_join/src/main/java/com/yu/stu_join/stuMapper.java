package com.yu.stu_join;

import com.yu.Reduce_Join.InfoBean;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

import java.io.IOException;

public class stuMapper extends Mapper<LongWritable, Text, stuBean, NullWritable> {
    private String filename;
    private stuBean stu = new stuBean();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        FileSplit fs = (FileSplit)context.getInputSplit();
        //获取当前文件名
        filename=fs.getPath().getName();

    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] files = value.toString().split("\t");
        System.out.println("验证不同时期files是否不同==>"+filename+"====="+files.length);
        if (filename.equals("stu.txt")){
            stu.setS_id(files[0]);
            stu.setS_name(files[1]);
            stu.setS_age(Integer.parseInt(files[2]));
            stu.setC_name("");
            stu.setC_id("");
            stu.setC_score(0.0);
        }
        if (filename.equals("sco.txt")){
            stu.setS_id(files[0]);
            stu.setS_name("");
            stu.setS_age(0);
            stu.setC_name("");
            stu.setC_id(files[1]);
            stu.setC_score(Double.parseDouble(files[2]));
        }
        else {
            stu.setS_id("");
            stu.setS_name("");
            stu.setS_age(0);
            stu.setC_name(files[1]);
            stu.setC_id(files[0]);
            stu.setC_score(0);
        }
        context.write(stu,NullWritable.get());
    }
}
