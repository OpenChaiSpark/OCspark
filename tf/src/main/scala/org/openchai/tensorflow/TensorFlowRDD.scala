package org.openchai.tensorflow

import org.openchai.tcp.rpc.{P2pConnectionParams, SolverServerIf}

import scala.reflect.ClassTag
import org.apache.spark
import org.apache.spark.sql.SparkSession
import org.openchai.tcp.util.{FileUtils, TcpCommon, TcpUtils}

object TensorFlowRDD {
}

case class TFSubmitter() {
}
object TFSubmitter {

  def runSparkJob(master: String, dir: String, nPartitions: Int = 10) = {
    val spark = SparkSession.builder.master(master).appName("TFSubmitter").getOrCreate
    val sc = spark.sparkContext
    val irdd = sc.binaryFiles(dir,nPartitions)
    val out = irdd.mapPartitionsWithIndex{ case (np, part) =>
      val tfClient = TfClient()
      part.map { case (path,contents) =>
      val label = s"${TcpUtils.getLocalHostname}-Part$np-$path"
      val bytes = contents.toArray
      val md5 = FileUtils.md5(bytes)
      val res = tfClient.labelImg(LabelImgStruct(label,path,
        bytes, md5))
        println(s"Received label result: $res")
      res
      }
    }
    val c = out.collect

  }


  def main(args: Array[String]): Unit = {
    val (master,dir,nPartitions) = (args(0), args(1), args(2))
    val dir2 = s"${System.getProperty("user.dir")}/tf/src/main/resources/images/"
    runSparkJob(master,s"file:///$dir2",nPartitions.toString.toInt)
  }
}
