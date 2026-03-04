package domain.shared

import domain.entity.{Bin, CapturedRequest}

trait TxContext

trait TxManager {
   def withTx[T](block: TxContext => T): T
}
