# OCspark 
Spark related contributions from OpenChai

Note: The <b>TensorFlow doc/README</b> may be found in [tf/README.md](the tensorflow dir)

Note: The <b>TensorFlow DMA Client doc/README</b> may be found in [tfdma/README.md](the tensorflow DMA dir)

There are two modules:  <b>P2pRdd</b> and <b>TcpClient</b> 

Within the <b><font size="+1">TcpClient</font></b> there is a <b>Controller Channel "XferCon"</b> and a <b>Data Transfer ("XferIf") channel</b> . The XferCon provides coordination of activities between a client and a server.  The XferIf provides data transfers: initially over (slow) TCP but pluggable for use by high performance direct memory access or shared memory approaches.

Within the <b><font size="+1">P2pRdd</font></b> there are two primary contributions:  

<b>Locality Sensitive RDD - LsSinkRDD and LsSourceRDD</b>
The LsSourceRDD reads from local filesystem directories - allowing individual slices/partitions of data to run locally. The intended use case is iterative processing in machine learning algorithms.
The LsSinkRDD sets up that capability by writing data from other RDD's into the correct local filesystem structure as expected by LsSourceRDD's.

<b>Point to Point communication RDD - P2pRDD</b>
P2pRDD sets up a TcP channel between the Driver and each of the Workers. This channel can be used to provide instructions and/or data updates from Driver to Workers without incurring the I/O of reading RDD data afresh. 

The use case is allow repeatedly processing the data in a tight loop but potentially with different Machine Learning or Deep Learning parameters. The Workers would send back results - possibly in the form of updated Weights - to the Driver -who in turn sends updated paramet``ers (and possibly weights) to the Worker.  

Two advantages can be derived from this:

(a) The Workers can run many loops on the in-memory RDD data
(b) Individual workers may complete and get updated parameters then continue to do additional processing - while other workers may not yet have completed.


<h3> How to build</h3>
mvn -DskipTests=true package

<h3> How to test </h3>

<h4> Testing P2pRDD:</h4>

spark-submit --master spark://\<host\>:7077 --jars $(pwd)/libs/spark_p2prdd-1.0.0.jar --class com.pointr.spark.rdd.P2pRDDTest $(pwd)/libs/spark_p2prdd-1.0.0-tests.jar spark://\<host\>:7077

<h4>Testing LsSinkRDD and LsSourceRDD:</h4>

spark-submit --master spark://\<host\>:7077 --jars $(pwd)/libs/spark_p2prdd-1.0.0.jar --class com.pointr.spark.rdd.P2pRDDTest $(pwd)/libs/spark_p2prdd-1.0.0-tests.jar spark://\<host\>:7077

<h4>Testing Rexec:</h4>

a. Check out the codebase from github to the remote server. On the remote server:

mvn -pl tcpclient exec:java -Dexec.mainClass="com.pointr.tcp.rexec.Rexec" -Dexec.args="--server 192.168.0.4 9191"

b. On the client side:

mvn -pl tcpclient exec:java -Dexec.mainClass="com.pointr.tcp.rexec.Rexec" -Dexec.args="--client 192.168.0.4 9191 ls /etc/pam.d"


Additional documentation -including details on the RDD capabilities of LSSink/LsSource and P2p - are  in the <b>doc</b> directory


Credits: conception, design, and development effort for this open source project have been supplied by [OpenChai](http://openchai.org/).


