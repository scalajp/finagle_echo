package org.scala_users.jp.finagle_json

import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.conversions.time._
import java.net.InetSocketAddress
import com.twitter.util.Future
import net.liftweb.json.{JsonParser, JsonAST}
import com.twitter.finagle.{ServiceFactory, ClientConnection, Service}
import net.liftweb.json.JsonAST._
import org.scala_users.jp.finagle_json.codec.JSONCodec
import org.onion_lang.jsonic.Jsonic._

object MessagingServer {
  def string(value: JsonAST.JValue): String = value.asInstanceOf[JString].values

  def integer(value: JsonAST.JValue): Int = value.asInstanceOf[JInt].values.toInt

  class EchoService(clientConnection: ClientConnection) extends Service[JsonAST.JValue, JsonAST.JValue] {
    self =>

    def processEcho(request: JsonAST.JValue): JsonAST.JValue = {
      val message = string(request \\ "message")
      %{ 'type :- "echoResult"; 'message :- message }
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
    // Bind the service to port 10000
    val server: Server = ServerBuilder()
      .codec(JSONCodec)
      .bindTo(new InetSocketAddress(10000))
      .name("finagle_json")
      .maxConcurrentRequests(10000)
      .keepAlive(true)
      .readTimeout(1000.seconds)
      .build(MessageProxyServiceFactory)
  }
}

