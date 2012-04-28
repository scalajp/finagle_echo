package org.onion_lang.finagle_json.db
import org.squeryl.customtypes.DateField
import org.squeryl.KeyedEntity

/**
 * Created by IntelliJ IDEA.
 * User: mizushima
 * Date: 12/02/24
 * Time: 11:39
 * To change this template use File | Settings | File Templates.
 */
class ClientInfo(val id: Long = 0L, var timestamp: DateField, var channel: String, var deviceId: String) extends KeyedEntity[Long] {
  def this() {
    this(0L, null, null, null)
  }
  def this(timestamp: DateField, channel: String, deviceId: String) {
    this(0L, timestamp, channel, deviceId)
  }
}
