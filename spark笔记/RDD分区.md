# Rdd分区规则
## 集合创建RDD
```scala
object SetRDD {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkCoreTest1")
    val sc: SparkContext = new SparkContext(conf)

    val rdd: RDD[Int] = sc.makeRDD(Array(1,2,3,4))
    // 查看输出分区数
    rdd.saveAsTextFile("output")
  }
}
```
>源码追溯
+ 查看makeRDD源码
```scala
def makeRDD[T: ClassTag](
    seq: Seq[T],
    numSlices: Int = defaultParallelism): RDD[T] = withScope {
parallelize(seq, numSlices)
}
```
+ 查看默认分区defaultParallelism
```scala
def defaultParallelism: Int = {
assertNotStopped()
taskScheduler.defaultParallelism
}
```
+ 获知defaultParallelism为抽象方法需寻找其实现类
```scala
def defaultParallelism(): Int
```
+ ctrl+shift+h 查找defaultParallelism的实现类
```scala
override def defaultParallelism(): Int = backend.defaultParallelism()
```
+ 获知第二个defaultParallelism同为抽象方法需寻找其实现类
```scala
def defaultParallelism(): Int
```
+ ctrl+shift+h 查找defaultParallelism的实现类
```scala
 override def defaultParallelism(): Int =
    scheduler.conf.getInt("spark.default.parallelism", totalCores)
```
## 从文件中读取后创建RDD 
```scala
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object FileRDD {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkCoreTest1")
    val sc: SparkContext = new SparkContext(conf)

    val rdd: RDD[String] = sc.textFile("F:\\BaiduNetdiskDownload\\大数据\\spark\\input\\input\\wc.txt",3)

    rdd.saveAsTextFile("output")

    sc.stop()
  }
}
```
>源码追溯
+ 查看sc.textFile源码
```scala
def textFile(
    path: String,
    minPartitions: Int = defaultMinPartitions): RDD[String] = withScope {
assertNotStopped()
hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text],
    minPartitions).map(pair => pair._2.toString).setName(path)
}
```
+ 查看hadoopFile
```scala
def hadoopFile[K, V](
    path: String,
    inputFormatClass: Class[_ <: InputFormat[K, V]],
    keyClass: Class[K],
    valueClass: Class[V],
    minPartitions: Int = defaultMinPartitions): RDD[(K, V)] = withScope {
assertNotStopped()
```
+ 查看minPartitions
```scala
val setInputPathsFunc = (jobConf: JobConf) => FileInputFormat.setInputPaths(jobConf, path)
new HadoopRDD(
    this,
    confBroadcast,
    Some(setInputPathsFunc),
    inputFormatClass,
    keyClass,
    valueClass,
    minPartitions).setName(path)
}
```
+ 查看HadoopRDD
```scala
class HadoopRDD[K, V](
    sc: SparkContext,
    broadcastedConf: Broadcast[SerializableConfiguration],
    initLocalJobConfFuncOpt: Option[JobConf => Unit],
    inputFormatClass: Class[_ <: InputFormat[K, V]],
    keyClass: Class[K],
    valueClass: Class[V],
    minPartitions: Int)
{......}
```
+ 在HadoopRDD中查找getPartitions方法 ctrl+f12
```scala
override def getPartitions: Array[Partition] = {
val jobConf = getJobConf()
// add the credentials here as this can be called before SparkContext initialized
SparkHadoopUtil.get.addCredentials(jobConf)
val inputFormat = getInputFormat(jobConf)
val inputSplits = inputFormat.getSplits(jobConf, minPartitions)
val array = new Array[Partition](inputSplits.size)
for (i <- 0 until inputSplits.size) {
    array(i) = new HadoopPartition(id, i, inputSplits(i))
}
array
}
```
+ 调用getSplits方法得到切片规划
```scala
InputSplit[] getSplits(JobConf job, int numSplits) throws IOException;
```
+ ctrl+H 寻找其实现类选择FileInputFormat
```scala
public abstract class FileInputFormat<K, V> implements InputFormat<K, V>{...}
```
+ 在FileInputFormat中查找getSplits
```scala
public InputSplit[] getSplits(JobConf job, int numSplits)
    throws IOException
{......}
```
+ listStatus遍历input目录所有文件
```scala
StopWatch sw = new StopWatch().start();
FileStatus[] stats = listStatus(job);
```
+ 计算大小每个切片放多少字节
```scala
job.setLong(NUM_INPUT_FILES, stats.length);
long totalSize = 0;                           // compute total size
boolean ignoreDirs = !job.getBoolean(INPUT_DIR_RECURSIVE, false)
    && job.getBoolean(INPUT_DIR_NONRECURSIVE_IGNORE_SUBDIRS, false);

List<FileStatus> files = new ArrayList<>(stats.length);
for (FileStatus file: stats) {                // check we have valid files
    if (file.isDirectory()) {
    if (!ignoreDirs) {
        throw new IOException("Not a file: "+ file.getPath());
    }
    } else {
    files.add(file);
    totalSize += file.getLen();
    }
}
```
+ makesplit创建切片规划
```scala
protected FileSplit makeSplit(Path file, long start, long length, 
                            String[] hosts, String[] inMemoryHosts) {
return new FileSplit(file, start, length, hosts, inMemoryHosts);
}
```
+ 切片后字节长度自动缩减
```scala
long blockSize = file.getBlockSize();
long splitSize = computeSplitSize(goalSize, minSize, blockSize);

long bytesRemaining = length;
while (((double) bytesRemaining)/splitSize > SPLIT_SLOP) {
String[][] splitHosts = getSplitHostsAndCachedHosts(blkLocations,
    length-bytesRemaining, splitSize, clusterMap);
splits.add(makeSplit(path, length-bytesRemaining, splitSize,
    splitHosts[0], splitHosts[1]));
bytesRemaining -= splitSize;
}
```
返回切片规划