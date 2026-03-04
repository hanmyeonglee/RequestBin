package application

import domain.entity.CapturedRequest
import config.Env
import domain.repository.{BinRepository, CapturedRequestRepository}

class RequestCollector(
    transactionManager: TxManager,
    binRepository: BinRepository,
    capturedRequestRepository: CapturedRequestRepository
) {
    def collect(binId: String, capturedRequest: CapturedRequest): Boolean = {
        transactionManager.withTx { implicit ctx =>
            binRepository.findByBinId(binId) match {
                case Some(bin) =>
                    if (
                        !bin.isExpired(System.currentTimeMillis(), Env.BIN_TTL_SECONDS) &&
                        bin.canAcceptRequest(capturedRequest)
                    ) {
                        capturedRequestRepository.save(bin.id, capturedRequest)
                        binRepository.save(bin.markLastUsedTime(System.currentTimeMillis()))

                        true
                    } else false
                
                case None => false
            }
        }
    }
}
