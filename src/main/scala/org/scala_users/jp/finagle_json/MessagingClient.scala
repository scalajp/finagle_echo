package org.scala_users.jp.finagle_json

import org.scala_users.jp.finagle_json.codec.JSONCodec
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.util.TimeConversions._
import net.liftweb.json.JsonParser
import org.onion_lang.jsonic.Jsonic._

object MessagingClient {
    val client = ClientBuilder()
      .codec(JSONCodec)
      .hosts("localhost:10000")
      .hostConnectionLimit(10000)
      .maxOutstandingConnections(10000)
      .timeout(1000.seconds)
      .tcpConnectTimeout(1000.seconds)
      .build()

    def main(args: Array[String]) {
      val hello = %{ 'type :- "echo"; 'message :- "Hello" }
      println(client(hello).get())
      println(client(hello).get())
      client.release()
    }
}
