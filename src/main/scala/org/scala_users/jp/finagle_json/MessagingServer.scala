package org.scala_users.jp.finagle_json

import org.scala_users.jp.finagle_json.codec.JSONCodec
import org.scala_users.jp.finagle_json.db.{ClientInfo, ClientInfoDbSchema}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.customtypes.DateField
import java.util.Date
import com.twitter.concurrent.Broker
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.conversions.time._
import java.net.InetSocketAddress
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.util.CharsetUtil
import scala.collection.mutable
import com.twitter.util.Future
import net.liftweb.json.{JsonParser, JsonAST}
import com.twitter.finagle.{ServiceFactory, ClientConnection, Service}
import org.jboss.netty.channel.{Channels, ChannelHandlerContext}
import org.jboss.netty.buffer.ChannelBuffers._
import net.liftweb.json.Printer._
import net.liftweb.json.JsonAST._
import org.scala_users.jp.finagle_json.db.{ClientInfo, ClientInfoDbSchema}
import org.scala_users.jp.finagle_json.codec.JSONCodec

object MessagingServer {
  def string(value: JsonAST.JValue): String = value.asInstanceOf[JString].values

  def integer(value: JsonAST.JValue): Int = value.asInstanceOf[JInt].values.toInt

  class EchoService(clientConnection: ClientConnection) extends Service[JsonAST.JValue, JsonAST.JValue] {
    self =>

    def makeAck(success: Boolean): String = <t>{{ "type":"ack", "success":
      {success}
      }}</t>.text

    def processEcho(request: JsonAST.JValue): JsonAST.JValue = {
      val message = string(request \\ "message")
      JsonParser.parse(<t>{{ "type":"echoResult", "message":{message} }}</t>.text)
    }

    def apply(request: JsonAST.JValue): Future[JsonAST.JValue] = {
      Future.value {
        val messageType = string(request \\ "type")
        messageType match {
          case "echo" => processEcho(request)
        }
      }
    }
  }

  object MessageProxyServiceFactory extends ServiceFactory[JsonAST.JValue, JsonAST.JValue] {
    def apply(conn: ClientConnection): Future[Service[JsonAST.JValue, JsonAST.JValue]] = Future.value {
      new EchoService(conn)
    }

    def close() {}
  }

  def main(args: Array[String]) {
    db.initialize()
    // Bind the service to port 10000
    val server: Server = ServerBuilder()
      .codec(JSONCodec)
      .bindTo(new InetSocketAddress(10000))
      .name("finagle_json")
      .maxConcurrentRequests(1000000)
      .keepAlive(true)
      .readTimeout(1000.seconds)
      .build(MessageProxyServiceFactory)
  }
}

