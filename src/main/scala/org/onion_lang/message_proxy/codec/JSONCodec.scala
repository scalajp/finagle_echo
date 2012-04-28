package org.onion_lang.message_proxy.codec
import com.twitter.finagle.{Codec, CodecFactory}
import org.jboss.netty.util.CharsetUtil
import net.liftweb.json.JsonAST
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder
import org.jboss.netty.buffer.{ChannelBuffer, ChannelBuffers}
import org.jboss.netty.channel.{ChannelHandlerContext, Channels, ChannelPipelineFactory}

/**
 * Created by IntelliJ IDEA.
 * User: mizushima
 * Date: 12/02/23
 * Time: 16:25
 * To change this template use File | Settings | File Templates.
 */

object JSONCodec extends JSONCodec

class JSONCodec extends CodecFactory[(ChannelHandlerContext, JsonAST.JValue), JsonAST.JValue] {
  def jsonDelimiter = Array[ChannelBuffer](ChannelBuffers.wrappedBuffer(Array[Byte]('\n', '\n')))
  private final val KILLO = 1024

  def server = Function.const {
    new Codec[(ChannelHandlerContext, JsonAST.JValue), JsonAST.JValue] {
      def pipelineFactory = new ChannelPipelineFactory {
        def getPipeline = {
          val pipeline = Channels.pipeline()
          pipeline.addLast("framingDecoder", new DelimiterBasedFrameDecoder(100 * KILLO, jsonDelimiter:_*))
          pipeline.addLast("jsonDecoder", new JSONDecoder(CharsetUtil.UTF_8))
          pipeline.addLast("jsonEncoder", new JSONEncoder(CharsetUtil.UTF_8))
          pipeline
        }
      }
    }
  }

  def client = Function.const {
    new Codec[(ChannelHandlerContext, JsonAST.JValue), JsonAST.JValue] {
      def pipelineFactory = new ChannelPipelineFactory {
        def getPipeline = {
          val pipeline = Channels.pipeline()
          pipeline.addLast("jsonEncoder", new JSONEncoder(CharsetUtil.UTF_8))
          pipeline.addLast("framingDecoder", new DelimiterBasedFrameDecoder(100 * KILLO, jsonDelimiter:_*))
          pipeline.addLast("jsonDecoder", new JSONDecoder(CharsetUtil.UTF_8))
          pipeline
        }
      }
    }
  }
}