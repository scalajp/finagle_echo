package org.scala_users.jp.finagle_echo

import org.scala_users.jp.finagle_echo.codec.JSONCodec
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.util.TimeConversions._
import org.onion_lang.jsonic.Jsonic._
import com.twitter.util.Future
import net.liftweb.json.{JsonAST, JsonParser}

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
      val helloWorld: Future[JsonAST.JValue] = for {
        h <- client(%{ 'type :- "echo"; 'message :- "Hello, " })
        w <- client(%{ 'type :- "echo"; 'message :- "World" })
      } yield ("" + (h \\ "message").values +  (w \\ "message").values)
      println(helloWorld.get())
      client.release()
    }
}
