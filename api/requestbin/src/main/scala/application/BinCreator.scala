package application

import domain.entity.Bin
import domain.repository.BinRepository
import domain.shared.{Clock, TxManager}

class BinCreator(txManager: TxManager, binRepository: BinRepository, clock: Clock) {
    private def generateBinId(): String =
        LazyList
            .continually(scala.util.Random.nextInt(26))
            .map(i => ('a' + i).toChar)
            .take(10)
            .mkString

    def create(): String = {
        val binId = generateBinId()
        val bin = Bin(None, binId, clock.currentUnixTimeSeconds)
        txManager.withTx { implicit ctx => binRepository.save(bin) }
        binId
    }
}
