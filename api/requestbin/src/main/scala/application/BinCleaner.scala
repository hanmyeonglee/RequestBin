package application

import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import domain.shared.TxManager
import domain.repository.BinRepository
import domain.shared.Clock
import domain.policy.SchedulerPolicy

class BinCleaner(
    transactionManager: TxManager,
    binRepository: BinRepository,
    clock: Clock,
    policy: SchedulerPolicy
) {
    private val lastCleanedAt = new AtomicReference[Instant](clock.now())

    def run: Unit = {
        val now = clock.now()
        if (
            policy.isCertainTimeHour(now) &&
            policy.isFirstCleanTime(now, lastCleanedAt.get())
        ) {
            transactionManager.withTx { implicit ctx =>
                binRepository.deleteAllExpiredBins(now.minus(policy.ttl))
            }
            lastCleanedAt.set(now)
        }
    }
}
