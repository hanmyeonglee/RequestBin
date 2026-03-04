package application

import domain.CapturedRequest

class RequestCollector(
    transactionManager: TxManager,
    binRepository: BinRepository,
    capturedRequestRepository: CapturedRequestRepository
) {
    def collect(binId: String, capturedRequest: CapturedRequest): Boolean = {
        transactionManager.withTx { implicit ctx =>
            binRepository.findByBinId(binId) match {
                case Some(bin) =>
                    if (bin.canAcceptRequest(capturedRequest)) {
                        capturedRequestRepository.save(bin.id, capturedRequest)
                        binRepository.updateLastUsedAt(bin.id)

                        true
                    } else false
                case None => false
            }
        }
    }
}
