package com.yu.Reduce_Join;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

public class R_Maper extends Mapper<LongWritable, Text,InfoBean, NullWritable>{
    private String filename;

    private InfoBean Info = new InfoBean();


    @Override
    protected void setup(Context context) throws IOException, InterruptedException {

        //获取切片文件名
        FileSplit fs = (FileSplit) context.getInputSplit();
        filename = fs.getPath().getName();
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] fields = value.toString().split("\t");

        //对不同数据来源分开处理
        if ("Info.txt".equals(filename)) {
            Info.setId(fields[0]);
            Info.setPid(fields[1]);
            Info.setAmount(Integer.parseInt(fields[2]));
            Info.setPname("");
        } else {
            Info.setPid(fields[0]);
            Info.setPname(fields[1]);
            Info.setAmount(0);
            Info.setId("");
        }

        context.write(Info, NullWritable.get());
    }

}
