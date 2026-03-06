package application

import munit.FunSuite
import domain.entity.Bin
import domain.repository.BinRepository
import domain.shared.{Clock, Generator, TxContext, TxManager}

class BinCreatorSuite extends FunSuite {
    private val fixedTime = 5000L
    private val fixedId   = "abcdefghij"

    private val stubTx = new TxManager {
        def withTx[T](block: TxContext => T): T = block(new TxContext {})
    }

    private val fixedClock = new Clock {
        def currentUnixTimeSeconds: Long = fixedTime
    }

    private val fixedGenerator = new Generator[String] {
        def generate: String = fixedId
    }

    private def makeRepo(onSave: Bin => Unit = _ => ()): BinRepository = new BinRepository {
        def findByBinId(id: String)(implicit ctx: TxContext): Option[Bin] = None
        def deleteAllExpiredBins(t: Long)(implicit ctx: TxContext): Unit  = ()
        def save(bin: Bin)(implicit ctx: TxContext): Unit                 = onSave(bin)
    }

    test("create returns the generated binId") {
        val creator = new BinCreator(stubTx, makeRepo(), fixedClock, fixedGenerator)
        assertEquals(creator.create(), fixedId)
    }

    test("create saves Bin with the generated binId") {
        var saved: Option[Bin] = None
        val creator = new BinCreator(stubTx, makeRepo(b => saved = Some(b)), fixedClock, fixedGenerator)
        creator.create()
        assertEquals(saved.map(_.binId), Some(fixedId))
    }

    test("create saves Bin with current clock timestamp") {
        var saved: Option[Bin] = None
        val creator = new BinCreator(stubTx, makeRepo(b => saved = Some(b)), fixedClock, fixedGenerator)
        creator.create()
        assertEquals(saved.map(_.lastUsedAtUnixTimeSeconds), Some(fixedTime))
    }
}
