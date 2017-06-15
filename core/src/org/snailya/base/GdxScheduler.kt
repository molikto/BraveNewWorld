package org.snailya.base

import com.badlogic.gdx.Gdx
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import java.util.concurrent.TimeUnit
import io.reactivex.plugins.RxJavaPlugins
import com.sun.javafx.animation.TickCalculation.toMillis
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.functions.Function
import ktx.log.info


/**
 * Created by molikto on 14/06/2017.
 */

object GdxScheduler : Scheduler() {

    override fun scheduleDirect(run: Runnable): Disposable {
        val scheduled = ScheduledRunnable(run)
        Gdx.app.postRunnable(scheduled)
        return scheduled
    }

    override fun scheduleDirect(run: Runnable, delay: Long, unit: TimeUnit?): Disposable {
        if (delay != 0L) throw Error("Not supported")
        return scheduleDirect(run)
    }

    override fun createWorker(): Worker {
        return object : Worker() {
            @Volatile var disposed = false
            override fun isDisposed(): Boolean = disposed

            override fun schedule(run: Runnable, delay: Long, unit: TimeUnit?): Disposable {
                if (disposed) return Disposables.disposed()
                return scheduleDirect(run)
            }

            override fun dispose() {
                disposed = true
            }

        }
    }

    class ScheduledRunnable(private val delegate: Runnable) : Runnable, Disposable {

        @Volatile private var disposed: Boolean = false

        override fun run() {
            try {
                if (!disposed) delegate.run()
            } catch (t: Throwable) {
                val ie = IllegalStateException("Fatal Exception thrown on Scheduler.", t)
                RxJavaPlugins.onError(ie)
                val thread = Thread.currentThread()
                thread.uncaughtExceptionHandler.uncaughtException(thread, ie)
            }

        }

        override fun dispose() { disposed = true }
        override fun isDisposed(): Boolean = disposed
    }


}
