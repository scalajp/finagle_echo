package org.onion_lang.finagle_json.example

import java.net.Socket
import java.io.{InputStreamReader, OutputStreamWriter, PrintWriter}
import collection.mutable.{Buffer, ArrayBuffer}


/**
 * Created by IntelliJ IDEA.
 * User: mizushima
 * Date: 12/02/29
 * Time: 22:15
 * To change this template use File | Settings | File Templates.
 */

object MessageProxyRawClient {
  def fork(block: => Any): Thread = {
    val th = new Thread{
      override def run(): Unit = block
    }
    th.start()
    th
  }

  type Host = String
  type Port = Int
  type Account = String
  type NDeviceId = Int

  def parseCommandLine(args: Array[String]) : Option[(Host, Port, Account, NDeviceId)] = {
    var host = "localhost"
    var port = 10000
    var account = "0"
    var nDeviceId = 3
    def parse(args: List[String]): Unit = args match {
      case "-host" :: hostString :: rest =>
        host = hostString
        parse(rest)
      case "-port" :: portString :: rest => 
        port = portString.toInt
        parse(rest)
      case "-account" :: accountString :: rest =>
        account = accountString
        parse(rest)
      case "-n" :: nDeviceIdString :: rest =>
        nDeviceId = nDeviceIdString.toInt
        parse(rest)
      case other :: rest =>
        parse(rest)
      case Nil =>
    }
    try {
      parse(args.toList)
      Some(host, port, account, nDeviceId)
    } catch {
      case e: Exception => None
    }
  }
  def usage() {
    println("""| Usage: sbt run [options]
               | -host <host-name>
               | -port <port-number>
               | -account <start-id-of-account>
               | -n <number-of-device-ids>""".stripMargin)
  }
  def main(args: Array[String]) {
    val startTime = System.currentTimeMillis()
    def makeDeviceId(n: Int) = startTime.toHexString + n
    val options = parseCommandLine(args)
    if(!options.isDefined) {
      usage()
      return
    }
    val Some((host, port, account, numThreads)) = options
    val start = account.toInt
    val threads = new Array[Thread](numThreads)
    val deviceIds = Buffer[String]()
    val writers = Buffer[PrintWriter]()
    for(i <- 0 until numThreads) {
      deviceIds += makeDeviceId(i)
    }
    for(i <- start until (start + numThreads)) {
      threads(i - start) = fork {
        val channel = i / 10
        val socket = new Socket(host, port)
        socket.setKeepAlive(true)
        socket.setSoTimeout(10000000)
        socket.setTcpNoDelay(true)
        socket.setReuseAddress(true)
        val deviceId = deviceIds(i - start)
        val writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"))
        writers += writer
        writer.print(<t>{{"type": "heartbeat", "channel": "{channel}", "source_device_id":"{deviceId}"}}</t>.text)
        writer.print("\n\n")
        writer.flush()
        val reader = new InputStreamReader(socket.getInputStream(), "UTF-8")
        val buffer = new ArrayBuffer[Char]
        var lfSeen = false
        for(ch <- Iterator.continually(reader.read()).takeWhile(_ != -1).map(_.toChar)) {
          buffer += ch
          if(lfSeen && ch == '\n') {
            printf("device_id:%s %s%n", deviceId, buffer.mkString(""))
            buffer.clear()
            lfSeen = false
          } else if(ch == '\n') {
            lfSeen = true
          }
        }
      }
      Thread.sleep(10)
    }
    Thread.sleep(4000)
    val writer = writers(0)
    val BROADCAST_PATTERN = """b *([0-9]+)""".r
    val REQUEST_PATTERN = """r *([0-9]+) *(.*)""".r
    val command = readLine("command> ")
    command match {
      case BROADCAST_PATTERN(targetChannel) =>
        writer.print(<t>{{"type": "broadcast", "channel": "{targetChannel}", "source_device_id":"{deviceIds(0)}", "message":"Broadcast from account={account}, device_id={deviceIds(0)}"}}</t>.text)
      case REQUEST_PATTERN(targetChannel, targetDeviceId) =>
        writer.print(<t>{{"type": "request", "channel": "{targetChannel}", "source_device_id":"{deviceIds(0)}", "destination_device_id":"{targetDeviceId}", "message":"Request Message"}}</t>.text)
    }
    writer.print("\n\n")
    writer.flush()
  }
}
