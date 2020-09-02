# 	自定义WordCount
>创建文件
```bash
vim wordcount.txt
```
>写入数据
```txt
hadoop hadoop kafka 
ss ss scala 
cls cls
giao
banzhang
xue
hadoop
dick assp
spark
```
>上传到HDFS中
```bash
hdfs dfs -mkdir /wordcount/

hdfs dfs -put wordcount.txt /wordcount/
```
>导入依赖
```xml
<dependencies>
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.12</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j-impl</artifactId>
        <version>2.12.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.hadoop</groupId>
        <artifactId>hadoop-client</artifactId>
        <version>3.1.3</version>
    </dependency>
</dependencies>
```
>resources下写入log4j2.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="error" strict="true" name="XMLConfig">
    <Appenders>
        <!-- 类型名为Console，名称为必须属性 -->
        <Appender type="Console" name="STDOUT">
            <!-- 布局为PatternLayout的方式，
            输出样式为[INFO] [2018-01-22 17:34:01][org.test.Console]I'm here -->
            <Layout type="PatternLayout"
                    pattern="[%p] [%d{yyyy-MM-dd HH:mm:ss}][%c{10}]%m%n" />
        </Appender>

    </Appenders>

    <Loggers>
        <!-- 可加性为false -->
        <Logger name="test" level="info" additivity="false">
            <AppenderRef ref="STDOUT" />
        </Logger>

        <!-- root loggerConfig设置 -->
        <Root level="info">
            <AppenderRef ref="STDOUT" />
        </Root>
    </Loggers>

</Configuration>
```
>自定义Mapper
```java
import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class wcMapper extends Mapper<LongWritable, Text, Text, IntWritable>{

    Text k = new Text();
    IntWritable v = new IntWritable(1);

    @Override
    protected void map(LongWritable key, Text value, Context context)	throws IOException, InterruptedException {

        // 1 获取一行
        String line = value.toString();

        // 2 拆分一行（分割）
        String[] words = line.split(" ");

        // 3 输出（将k,v写入上下文中）
        for (String word : words) {
            k.set(word);
            context.write(k, v);
        }
    }
}
```
>自定义Reducer
```java
import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


public class wcReducer extends Reducer<Text, IntWritable, Text, IntWritable>{

    int sum;
    IntWritable v = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values,Context context) throws IOException, InterruptedException {

        // 1 累加求和
        sum = 0;
        for (IntWritable count : values) {
            sum += count.get();
        }

        // 2 输出（将新的k,v对写入上下文中）
        v.set(sum);
        context.write(key,v);
    }
}
```
>自定义Driver
```java
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
```
>测试执行
+ 上传打包后的jar包
+ 执行jar
```bash
hadoop jar 包名.jar 主类Main方法路径

hadoop jar My_MR-1.0-SNAPSHOT.jar com.yu.jobDriver
```