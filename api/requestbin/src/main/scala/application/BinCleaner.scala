package application

import domain.shared.TxManager
import domain.repository.BinRepository
import domain.shared.Clock
import domain.policy.SchedulerPolicy
import java.util.concurrent.atomic.AtomicLong

class BinCleaner(
    transactionManager: TxManager,
    binRepository: BinRepository,
    clock: Clock,
    policy: SchedulerPolicy
) {
    private val lastCleanedUnixTimeSeconds = new AtomicLong(clock.currentUnixTimeSeconds)

    def run: Unit = {
        val currentUnixTimeSeconds = clock.currentUnixTimeSeconds
        if (
            policy.isCertainTimeHour(currentUnixTimeSeconds) &&
            policy.isFirstCleanTime(currentUnixTimeSeconds, lastCleanedUnixTimeSeconds.get())
        ) {
            transactionManager.withTx { implicit ctx =>
                binRepository.deleteAllExpiredBins(currentUnixTimeSeconds - policy.ttlSeconds)
            }
            lastCleanedUnixTimeSeconds.set(currentUnixTimeSeconds)
        }
    }
}
