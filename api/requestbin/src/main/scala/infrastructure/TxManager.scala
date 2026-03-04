package infrastructure

import scalikejdbc._
import application.{TxContext, TxManager}

final case class JdbcTxContext(session: DBSession) extends TxContext

class JdbcTxManager extends TxManager {
    override def withTx[T](block: TxContext => T): T = {
        DB localTx { session =>
            block(JdbcTxContext(session))
        }
    }
}

trait JdbcRepository {
    protected def dbSession(implicit ctx: TxContext): DBSession = {
        ctx match {
            case JdbcTxContext(session) => session
            case _ => throw new IllegalArgumentException("JDBC Transaction Context is required")
        }
    }
}
