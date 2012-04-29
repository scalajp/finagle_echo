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

  val contexts: mutable.Map[(String, String), ChannelHandlerContext] = mutable.Map()

  class LivingClientManagementService(clientConnection: ClientConnection) extends Service[(ChannelHandlerContext, JsonAST.JValue), JsonAST.JValue] {
    self =>
    private var context: ChannelHandlerContext = null

    def makeAck(success: Boolean): String = <t>{{ "type":"ack", "success":
      {success}
      }}</t>.text

    def processBroadcast(request: JsonAST.JValue): JsonAST.JValue = {
      val channel = string(request \\ "channel")
      val sourceDeviceId = string(request \\ "source_device_id")
      val message = string(request \\ "message")
      for ((ctx, dev) <- contexts.collect {
        case ((ch, dev), ctx) if ch == channel => (ctx, dev)
      }) {
        val broadcastMessage = copiedBuffer(compact(render(request)) + "\n\n", CharsetUtil.UTF_8)
        val channelFuture = Channels.succeededFuture(ctx.getChannel())
        Channels.write(ctx, channelFuture, broadcastMessage)
      }
      val responseJson = <t>{{"type":"ack", "success":"true"}}</t>.text
      JsonParser.parse(responseJson)
    }

    def processHeartbeat(request: JsonAST.JValue): JsonAST.JValue = {
      import ClientInfoDbSchema._
      val channel = string(request \\ "channel")
      val deviceId = string(request \\ "source_device_id")
      val timestamp = new DateField(new Date())
      try {
        transaction {
          val cls = from(clients)(c =>
            where(c.channel === channel and c.deviceId === deviceId)
              select (c)
          ).toList.sortBy {
            c => -c.timestamp.value.getTime()
          }
          cls match {
            case Nil =>
              clients.insert(new ClientInfo(timestamp, channel, deviceId))
              contexts((channel, deviceId)) = context
            case client :: rest =>
              client.timestamp = timestamp
              client.update
              rest.foreach(c => clients.delete(c.id))
          }
          ClientInfoDbSchema.save
        }
        JsonParser.parse(makeAck(true))
      } catch {
        case e: Exception =>
          JsonParser.parse(makeAck(false))
      }
    }

    def processRequest(request: JsonAST.JValue): JsonAST.JValue = {
      val channel = string(request \\ "channel")
      val sourceDeviceId = string(request \\ "source_device_id")
      val destinationDeviceId = string(request \\ "destination_device_id")
      val message = string(request \\ "message")
      (for (ctx <- contexts.get((channel, destinationDeviceId))) yield {
        val msgJson = <t>{{"type":"response", "message":"
          {message}
          ", "channel":"
          {channel}
          ", "source_device_id":"
          {sourceDeviceId}
          ", "destination_device_id":"
          {destinationDeviceId}
          "}}</t>.text
        val channelFuture = Channels.succeededFuture(ctx.getChannel())
        Channels.write(ctx, channelFuture, copiedBuffer(msgJson + "\n\n", CharsetUtil.UTF_8))
        JsonParser.parse(makeAck(true))
      }).get
    }

    def apply(req: (ChannelHandlerContext, JsonAST.JValue)): Future[JsonAST.JValue] = {
      val (ctx, request) = req
      context = ctx
      Future.value {
        val messageType = string(request \\ "type")
        val jsonValue = messageType match {
          case "response" => request
          case "heartbeat" => processHeartbeat(request)
          case "broadcast" => processBroadcast(request)
          case "request" => processRequest(request)
        }
        jsonValue
      }
    }

  }

  object MessageProxyServiceFactory extends ServiceFactory[(ChannelHandlerContext, JsonAST.JValue), JsonAST.JValue] {
    def apply(conn: ClientConnection): Future[Service[(ChannelHandlerContext, JValue), JsonAST.JValue]] = Future.value {
      new LivingClientManagementService(conn)
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

