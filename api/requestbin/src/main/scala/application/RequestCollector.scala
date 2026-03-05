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
                    if (
                        !bin.isExpired(systemClock.currentUnixTimeSeconds, binPolicy.ttlSeconds) &&
                        bin.canAcceptRequest(capturedRequest)
                    ) {
                        capturedRequestRepository.save(bin, capturedRequest)
                        binRepository.save(
                            bin.markLastUsedUnixTimeSeconds(
                                systemClock.currentUnixTimeSeconds
                            )
                        )

                        true
                    } else false
                
                case _ => false
            }
        }
    }
}
