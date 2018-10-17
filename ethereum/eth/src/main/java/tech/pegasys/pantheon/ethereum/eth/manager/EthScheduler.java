/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.eth.manager;

import tech.pegasys.pantheon.util.ExceptionUtils;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EthScheduler {
  private static final Logger LOG = LogManager.getLogger();

  private final Duration defaultTimeout = Duration.ofSeconds(5);

  private final AtomicBoolean stopped = new AtomicBoolean(false);
  private final CountDownLatch shutdown = new CountDownLatch(1);

  protected final ExecutorService workerExecutor;
  protected final ScheduledExecutorService scheduler;

  EthScheduler(final int workerCount) {
    this(
        Executors.newFixedThreadPool(
            workerCount,
            new ThreadFactoryBuilder()
                .setNameFormat(EthScheduler.class.getSimpleName() + "-Workers")
                .build()),
        Executors.newScheduledThreadPool(
            1,
            new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(EthScheduler.class.getSimpleName() + "Timer")
                .build()));
  }

  protected EthScheduler(
      final ExecutorService workerExecutor, final ScheduledExecutorService scheduler) {
    this.workerExecutor = workerExecutor;
    this.scheduler = scheduler;
  }

  public <T> CompletableFuture<T> scheduleWorkerTask(final Supplier<CompletableFuture<T>> future) {
    final CompletableFuture<T> promise = new CompletableFuture<>();
    final Future<?> workerFuture =
        workerExecutor.submit(
            () -> {
              future
                  .get()
                  .whenComplete(
                      (r, t) -> {
                        if (t != null) {
                          promise.completeExceptionally(t);
                        } else {
                          promise.complete(r);
                        }
                      });
            });
    // If returned promise is cancelled, cancel the worker future
    promise.whenComplete(
        (r, t) -> {
          if (t instanceof CancellationException) {
            workerFuture.cancel(false);
          }
        });
    return promise;
  }

  public Future<?> scheduleWorkerTask(final Runnable command) {
    return workerExecutor.submit(command);
  }

  public CompletableFuture<Void> scheduleFutureTask(
      final Runnable command, final Duration duration) {
    final CompletableFuture<Void> promise = new CompletableFuture<>();
    final ScheduledFuture<?> scheduledFuture =
        scheduler.schedule(
            () -> {
              try {
                command.run();
                promise.complete(null);
              } catch (final Exception e) {
                promise.completeExceptionally(e);
              }
            },
            duration.toMillis(),
            TimeUnit.MILLISECONDS);
    // If returned promise is cancelled, cancel scheduled task
    promise.whenComplete(
        (r, t) -> {
          if (t instanceof CancellationException) {
            scheduledFuture.cancel(false);
          }
        });
    return promise;
  }

  public <T> CompletableFuture<T> scheduleFutureTask(
      final Supplier<CompletableFuture<T>> future, final Duration duration) {
    final CompletableFuture<T> promise = new CompletableFuture<>();
    final ScheduledFuture<?> scheduledFuture =
        scheduler.schedule(
            () -> {
              future
                  .get()
                  .whenComplete(
                      (r, t) -> {
                        if (t != null) {
                          promise.completeExceptionally(t);
                        } else {
                          promise.complete(r);
                        }
                      });
            },
            duration.toMillis(),
            TimeUnit.MILLISECONDS);
    // If returned promise is cancelled, cancel scheduled task
    promise.whenComplete(
        (r, t) -> {
          if (t instanceof CancellationException) {
            scheduledFuture.cancel(false);
          }
        });
    return promise;
  }

  public <T> CompletableFuture<T> timeout(final EthTask<T> task) {
    return timeout(task, defaultTimeout);
  }

  public <T> CompletableFuture<T> timeout(final EthTask<T> task, final Duration timeout) {
    final CompletableFuture<T> future = task.run();
    final CompletableFuture<T> result = timeout(future, timeout);
    result.whenComplete(
        (r, error) -> {
          if (errorIsTimeoutOrCancellation(error)) {
            task.cancel();
          }
        });
    return result;
  }

  private boolean errorIsTimeoutOrCancellation(final Throwable error) {
    final Throwable cause = ExceptionUtils.rootCause(error);
    return cause instanceof TimeoutException || cause instanceof CancellationException;
  }

  private <T> CompletableFuture<T> timeout(
      final CompletableFuture<T> future, final Duration delay) {
    final CompletableFuture<T> timeout = failAfterTimeout(delay);
    return future.applyToEither(timeout, Function.identity());
  }

  public void stop() {
    if (stopped.compareAndSet(false, true)) {
      LOG.trace("Stopping " + getClass().getSimpleName());
      workerExecutor.shutdown();
      scheduler.shutdown();
      shutdown.countDown();
    } else {
      LOG.trace("Attempted to stop already stopped " + getClass().getSimpleName());
    }
  }

  public void awaitStop() throws InterruptedException {
    shutdown.await();
    if (!workerExecutor.awaitTermination(2L, TimeUnit.MINUTES)) {
      LOG.error("{} worker executor did not shutdown cleanly.", this.getClass().getSimpleName());
      workerExecutor.shutdownNow();
      workerExecutor.awaitTermination(2L, TimeUnit.MINUTES);
    }
    if (!scheduler.awaitTermination(2L, TimeUnit.MINUTES)) {
      LOG.error("{} scheduler did not shutdown cleanly.", this.getClass().getSimpleName());
      scheduler.shutdownNow();
      scheduler.awaitTermination(2L, TimeUnit.MINUTES);
    }
    LOG.trace("{} stopped.", this.getClass().getSimpleName());
  }

  private <T> CompletableFuture<T> failAfterTimeout(final Duration timeout) {
    final CompletableFuture<T> promise = new CompletableFuture<>();
    failAfterTimeout(promise, timeout);
    return promise;
  }

  public <T> void failAfterTimeout(final CompletableFuture<T> promise) {
    failAfterTimeout(promise, defaultTimeout);
  }

  public <T> void failAfterTimeout(final CompletableFuture<T> promise, final Duration timeout) {
    final long delay = timeout.toMillis();
    final TimeUnit unit = TimeUnit.MILLISECONDS;
    scheduler.schedule(
        () -> {
          final TimeoutException ex =
              new TimeoutException("Timeout after " + delay + " " + unit.name());
          return promise.completeExceptionally(ex);
        },
        delay,
        unit);
  }
}
