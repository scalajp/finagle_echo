package org.scala_users.jp.finagle_json

import org.scala_users.jp.finagle_json.codec.JSONCodec

object MessageProxyClient {
  /*
    val clientFactory = ClientBuilder()
      .codec(JSONSingleCodec)
      .hosts("localhost:10000")
      .hostConnectionLimit(10000)
      .maxOutstandingConnections(10000)
      .timeout(1000.seconds)
      .tcpConnectTimeout(1000.seconds)
      .buildFactory()

    def main(args: Array[String]) {
      val jsonData = JsonParser.parse("""{"type": "heartbeat", "channel": "1", "device_id":"iPad"}""")
      for(client <- clientFactory()) {
        println(client(null, jsonData).get())
        println(client(null, jsonData).get())
      }
      for(client <- clientFactory()) {
        println(client(null, jsonData).get())
        println(client(null, jsonData).get())
        client.release()
      }
    }
  */
}
