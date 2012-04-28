package org.onion_lang.finagle_json

import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._
import java.sql.DriverManager


/**
 * Created by IntelliJ IDEA.
 * User: mizushima
 * Date: 12/02/23
 * Time: 18:21
 * To change this template use File | Settings | File Templates.
 */

package object db {
  private var session: Session = _
  def initialize(): Unit = {
    Class.forName("org.h2.Driver")
    SessionFactory.concreteFactory = Some{() =>
      val connection = DriverManager.getConnection("jdbc:h2:mem:testdb")
      Session.create(connection, new H2Adapter)
    }
    session = SessionFactory.concreteFactory.get()
    transaction {
      ClientInfoDbSchema.create
    }
  }
}
