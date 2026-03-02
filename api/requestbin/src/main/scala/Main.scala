import scalikejdbc._
import scalikejdbc.config._

@main def hello(): Unit = {
  DBs.setupAll()
  implicit val session: DBSession = AutoSession

  val result: Int = DB autoCommit { implicit session => 
    sql"SELECT 1 as val".map(rs => rs.int("val")).single.apply().getOrElse(0)
  }
  println(s"Hello, world! Result: $result")
}
