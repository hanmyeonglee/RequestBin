package application

import domain.entity.Bin
import domain.repository.BinRepository
import domain.shared.{Clock, TxManager, Generator}

class BinCreator(
    txManager: TxManager,
    binRepository: BinRepository,
    clock: Clock,
    binIdGenerator: Generator[String]
) {
    def create(): String = {
        val binId = binIdGenerator.generate
        val bin = Bin(binId, clock.currentUnixTimeSeconds)
        txManager.withTx { implicit ctx => binRepository.save(bin) }
        binId
    }
}
