package application

import domain.entity.{Bin, CapturedRequest}
import domain.repository.{BinRepository, CapturedRequestRepository}
import domain.shared.{Clock, TxManager}
import domain.policy.BinPolicy

class RequestCollector(
    transactionManager: TxManager,
    binRepository: BinRepository,
    capturedRequestRepository: CapturedRequestRepository,
    systemClock: Clock,
    binPolicy: BinPolicy
) {
    def collect(binId: String, capturedRequest: CapturedRequest): Boolean = {
        transactionManager.withTx { implicit ctx =>
            binRepository.findByBinId(binId) match {
                case Some(bin) =>
                    if (bin.canAcceptRequest(capturedRequest, systemClock.now(), binPolicy.ttl)) {
                        capturedRequestRepository.save(bin, capturedRequest.copy(createdAt = systemClock.now()))
                        binRepository.save(
                            bin.markLastUsed(
                                systemClock.now()
                            )
                        )

                        true
                    } else false
                
                case _ => false
            }
        }
    }
}
