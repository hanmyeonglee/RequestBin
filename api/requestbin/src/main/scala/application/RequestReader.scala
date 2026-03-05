package application

import domain.entity.CapturedRequest
import domain.repository.{BinRepository, CapturedRequestRepository}
import domain.shared.TxManager

class RequestReader(
    txManager: TxManager,
    binRepository: BinRepository,
    capturedRequestRepository: CapturedRequestRepository
) {
    // Returns None if the bin does not exist, Some(requests) otherwise
    def read(binId: String, num: Int): Option[Seq[CapturedRequest]] =
        txManager.withTx { implicit ctx =>
            binRepository.findByBinId(binId).map { bin =>
                capturedRequestRepository.read(bin, num)
            }
        }
}
