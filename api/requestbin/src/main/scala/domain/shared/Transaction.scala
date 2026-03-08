package domain.shared

trait TxContext

trait TxManager {
   def withTx[T](block: TxContext => T): T
}
