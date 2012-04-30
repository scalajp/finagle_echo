package org.scala_users.jp.finagle_echo.codec

import java.nio.charset.Charset
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.channel.{Channel, ChannelHandlerContext}
import net.liftweb.json.JsonParser
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder
import org.jboss.netty.util.CharsetUtil

/**
 * Created by IntelliJ IDEA.
 * User: mizushima
 * Date: 12/02/23
 * Time: 16:35
 * To change this template use File | Settings | File Templates.
 */
object JSONDecoder extends JSONDecoder

class JSONDecoder(val charset: Charset) extends OneToOneDecoder {
  /**
   * Creates a new instance with the current system character set.
   */
  def this() {
    this(Charset.defaultCharset)
  }

  def this(charsetName: String) {
    this(Charset.forName(charsetName))
  }

  def decode(ctx: ChannelHandlerContext, channel: Channel, msg: AnyRef): AnyRef = {
    if(!msg.isInstanceOf[ChannelBuffer]) return msg
    val jsonString = msg.asInstanceOf[ChannelBuffer].toString(CharsetUtil.UTF_8)
    JsonParser.parse(jsonString)
  }
}
