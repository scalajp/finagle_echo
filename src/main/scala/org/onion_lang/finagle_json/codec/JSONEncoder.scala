package org.onion_lang.finagle_json.codec

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST._
import net.liftweb.json.Printer._

/**
 * Created by IntelliJ IDEA.
 * User: mizushima
 * Date: 12/02/23
 * Time: 16:56
 * To change this template use File | Settings | File Templates.
 */
object JSONEncoder extends JSONEncoder

class JSONEncoder(val charset: Charset) extends OneToOneEncoder {

  /**
   * Creates a new instance with the current system character set.
   */
  def this() {
    this(Charset.defaultCharset())
  }

  def this(charsetName: String) {
    this(Charset.forName(charsetName))
  }

  def encode(ctx: ChannelHandlerContext, channel: Channel, msg: AnyRef) :AnyRef = {
    msg match {
      case (_, json:JsonAST.JValue) =>
        val buffer = copiedBuffer(compact(render(json)) + "\n\n", charset)
        buffer
      case json:JsonAST.JValue =>
        val buffer = copiedBuffer(compact(render(json)) + "\n\n", charset)
        buffer
      case _ =>
        msg
    }
  }
}

