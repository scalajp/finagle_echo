package org.onion_lang.finagle_json.db
import org.squeryl._

/**
 * Created by IntelliJ IDEA.
 * User: mizushima
 * Date: 12/02/23
 * Time: 17:49
 * To change this template use File | Settings | File Templates.
 */
object ClientInfoDbSchema extends Schema {
  val clients = table[ClientInfo]("CLIENTINFO")
}