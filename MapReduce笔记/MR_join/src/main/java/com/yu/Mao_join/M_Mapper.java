package com.yu.Mao_join;

import com.ctc.wstx.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class M_Mapper extends Mapper<LongWritable, Text,Text, NullWritable>{

    private Map<String,String> pm = new HashMap();
    private Text val = new Text();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        /** 读取小表中的数据到 pm
                获取分布式缓存存入的小表
                开流
         */
        URI[] cf = context.getCacheFiles();
        FileSystem fs = FileSystem.get(context.getConfiguration());
        FSDataInputStream streams = fs.open(new Path(cf[0]));
        /** 将文件按行处理,读取到 Map ==> pm中 */
        BufferedReader buf = new BufferedReader(new InputStreamReader(streams));
        String line;
        while (StringUtils.isNotEmpty(line=buf.readLine())){
            String[] files = line.split("\t");
            pm.put(files[0],files[1]);
        }
        IOUtils.closeStream(buf);
    }

    /** 处理info.txt（大表）数据
        用小表进行映射替换*/
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] sp = value.toString().split("\t");
        val.set(sp[0]+"\t"+pm.get(sp[1])+"\t"+sp[2]);
        context.write(val,NullWritable.get());
    }
}
