package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.Module

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class ModuleRepository @Inject() (dbConfigProvider: DatabaseConfigProvider, repoLog: LogEntryRepository)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._

  private class ModulesTable(tag: Tag) extends Table[Module](tag, "module") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def president = column[String]("president")
    def description = column[String]("description")
    def associationId = column[Long]("associationId")
    def associationName = column[String]("associationName")
    def * = (id, name, president, description, associationId, associationName) <> ((Module.apply _).tupled, Module.unapply)
  }

  private val tableQ = TableQuery[ModulesTable]

  def create(name: String, president: String, description: String, associationId: Long, associationName: String,
            userId: Long, userName: String): Future[Module] = db.run {
    repoLog.createLogEntry(repoLog.CREATE, repoLog.MODULE, userId, userName, name)
    (tableQ.map(p => (p.name, p.president, p.description, p.associationId, p.associationName))
      returning tableQ.map(_.id)
      into ((nameAge, id) => Module(id, nameAge._1, nameAge._2, nameAge._3, nameAge._4, nameAge._5))
    ) += (name, president, description, associationId, associationName)
  }

  def list(): Future[Seq[Module]] = db.run {
    tableQ.result
  }

  // to cpy
  def getById(id: Long): Future[Seq[Module]] = db.run {
    tableQ.filter(_.id === id).result
  }

  // update required to copy
  def update(id: Long, name: String, president: String, description: String, associationId: Long, 
             associationName: String, userId: Long, userName: String): Future[Seq[Module]] = db.run {
    repoLog.createLogEntry(repoLog.UPDATE, repoLog.MODULE, userId, userName, name);
    val q = for { c <- tableQ if c.id === id } yield c.name
    db.run(q.update(name))
    val q2 = for { c <- tableQ if c.id === id } yield c.president
    db.run(q2.update(president))
    val q3 = for { c <- tableQ if c.id === id } yield c.description
    db.run(q3.update(description))
    val q4 = for { c <- tableQ if c.id === id } yield c.associationId
    db.run(q4.update(associationId))
    val q5 = for { c <- tableQ if c.id === id } yield c.associationName
    db.run(q5.update(associationName))
    tableQ.filter(_.id === id).result
  }

  // delete required
  def delete(id: Long): Future[Seq[Module]] = db.run {
    val q = tableQ.filter(_.id === id)
    val action = q.delete
    val affectedRowsCount: Future[Int] = db.run(action)
    
    tableQ.result
  }

  def getListNames(): Future[Seq[(Long, String)]] = db.run {
    tableQ.take(200).map(s => (s.id, s.name)).result
  }

}
