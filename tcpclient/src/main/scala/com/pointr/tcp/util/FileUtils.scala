/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pointr.tcp.util

import java.io._
import java.util.concurrent.{Callable, Executors, Future}

import com.pointr.tcp.xfer.{DataPtr, RawData}

import scala.collection.mutable.ArrayBuffer
import Logger._

object FileUtils {
  def mv(src: String, dest: String) = {
    debug(s"Moving $src to $dest ..")
    val res = new File(src).renameTo(new File(dest))
    if (!res) {
      throw new FileNotFoundException(s"Unable to rename $src to $dest")
    }
  }

  def removeExt(path: String) = if (path.indexOf(".") > 0) path.substring(0,path.lastIndexOf(".")) else path

  def fileName(fp: String) = if (fp.indexOf("/") >= 0) fp.substring(fp.lastIndexOf("/")+1) else fp

  def fileExt(fp: String) = if (!fp.contains(".")) "" else { fp.substring(fp.lastIndexOf(".")+1).toLowerCase }

  def filePath(fp: String) = if (fp.indexOf("/") >= 0) fp.substring(0, fp.lastIndexOf("/")) else ""

  def cleanDir(dir: String, extensions: Seq[String]) = {
    assert(dir.length >= 8)
    val toDelete = new File(dir).listFiles(new FileFilter {
      override def accept(pathname: File) = {
        pathname.getName.indexOf(".") <= 0 ||
          !extensions.contains(pathname.getName.substring(pathname.getName.lastIndexOf(".") + 1).toLowerCase())
      }
    })
    toDelete.foreach{ f=> info(s"Deleting ${f.getAbsolutePath} .."); f.delete }
  }


  //  def checkMd5(packed: PackedData): Unit = checkMd5(packed._1, packed._2, packed._3)
  //  def checkMd5(packed: PackedData): Unit = checkMd5(packed._1, packed._2, packed._3)

  def checkMd5(path: DataPtr, data: Array[Byte], md5In: RawData) = {
    if (!compareBytes(md5(data), md5In)) {
      throw new IllegalStateException(s"writeNio: output md5 not matching input on $path")
    }
  }

  def compareBytes(_md5: Array[Byte], md5: Array[Byte]) = {
    if (_md5.length != md5.length) {
      false
    } else {
      md5.sameElements(_md5)
    }
  }

  import Logger._

  type TaskResult = Array[Byte]

  def mkdirs(dir: String) = {
    val fdir = new File(dir)
    if (!fdir.exists()) {
      info(s"Creating directory ${fdir.getPath}")
      fdir.mkdirs
    }
  }

  def rmdirs(dir: String): Array[(String, Boolean)] = {
    //    if (fdir.exists()) {
    //      debug(s"Removing directory ${fdir.getPath}")
    Option(new File(dir).listFiles)
      .map(_.flatMap(f => rmdirs(f.getPath))).getOrElse(Array()) :+ (dir -> new File(dir).delete)
  }

  def delete(path: String) = {
    val del = new File(path).delete
    if (!del) throw new IllegalStateException(s"Unable to delete $path")
    del
  }

  def write(path: String, data: String, silent: Boolean = false): Unit = {
    if (!silent)
      info(s"Writing to $path with datalen=${data.length}")
    tools.nsc.io.File(path).writeAll(data)
  }

  def writeBytes(fpath: String, data: Array[Byte]) = {
    info(s"Writing ${data.length} bytes to $fpath ..")
    val fos = new FileOutputStream(fpath)
    fos.write(data)
    fos.flush
    fos.close
//    debug(s"Writing ${data.length} bytes to $fpath .. done ..")
  }

  def readPath(path: String, recursive: Boolean = true, multiThreaded: Boolean = true): TaskResult = {
    val nThreads = if (multiThreaded) {
      Runtime.getRuntime.availableProcessors * 2
    } else {
      1
    }
    val tpool = Executors.newFixedThreadPool(nThreads)
    class ReadTask(path: String) extends Callable[TaskResult] {
      override def call(): TaskResult = {
        readFileBytes(path)
      }
    }
    //    val sb = new StringBuffer // Make sure StringBUFFER not BUILDER because of multithreaded!!
    val taskResult = new ArrayBuffer[Byte](1024 * 128) // Make sure StringBUFFER not BUILDER because of multithreaded!!

    import collection.mutable
    val tasksBuf = mutable.ArrayBuffer[Future[TaskResult]]()

    def readPath0(fpath: String): TaskResult = {
      val paths = new File(fpath).listFiles.filter { f => !f.getName.startsWith(".") }
      paths.foreach { f =>
        if (f.isDirectory) {
          if (recursive) {
            debug(s"Descending into ${f.getPath} ..")
            readPath0(f.getPath)
          } else {
            debug(s"Recursive is false so NOT descending into ${f.getPath}")
          }
        } else {
          tasksBuf += tpool.submit(new ReadTask(f.getPath))
        }
      }
      tasksBuf.foreach { t => taskResult ++= t.get }
      taskResult.toArray
    }

    readPath0(path)
  }

  //  def readFileBytes(fpath: String): Array[Byte] = readFileAsString(fpath).getBytes("ISO-8859-1")
  def readFileBytes(fpath: String): Array[Byte] = {
    try {
      val file = new File(fpath).getCanonicalFile
      val bytes = new Array[Byte](file.length.toInt)
      val dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fpath)))
      dis.readFully(bytes)
      dis.close()
      bytes
    } catch {
      case fnfe: FileNotFoundException =>
        throw new FileNotFoundException(s"readFileBytes: unable to find $fpath")
    }
  }

  def readFileAsString(fpath: String) = new String(readFileBytes(fpath), "ISO-8859-1")

  import java.security.MessageDigest

  def readFileOption(fpath: String) = try {
    Some(readFileAsString(fpath))
  } catch {
    case fne: FileNotFoundException => /* debug(s"File $fpath not found"); */ None
    case e: Exception => warn(s"Error reading file $fpath ${Logger.toString(e)}"); None
  }

  def md5(arr: Array[Byte]) = {
    val md = MessageDigest.getInstance("MD5")
    md.update(arr)
    md.digest
  }

  def main(args: Array[String]): Unit = {
    writeBytes("/tmp/abc.txt", " here is stuff".getBytes("ISO-8859-1"))
  }
}
