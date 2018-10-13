package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.consensus.ibft.ibftevent.BlockTimerExpiry;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.util.time.Clock;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Class for starting and keeping organised block timers */
public class BlockTimer {
  private final ScheduledExecutorService timerExecutor;
  private Optional<ScheduledFuture<?>> currentTimerTask;
  private final IbftEventQueue queue;
  private final long minimumTimeBetweenBlocksMillis;
  private final Clock clock;

  /**
   * Construct a BlockTimer with primed executor service ready to start timers
   *
   * @param queue The queue in which to put round expiry events
   * @param minimumTimeBetweenBlocksMillis Minimum timestamp difference between blocks
   * @param timerExecutor Executor service that timers can be scheduled with
   * @param clock System clock
   */
  public BlockTimer(
      final IbftEventQueue queue,
      final long minimumTimeBetweenBlocksMillis,
      final ScheduledExecutorService timerExecutor,
      Clock clock) {
    this.queue = queue;
    this.timerExecutor = timerExecutor;
    this.currentTimerTask = Optional.empty();
    this.minimumTimeBetweenBlocksMillis = minimumTimeBetweenBlocksMillis;
    this.clock = clock;
  }

  /** Cancels the current running round timer if there is one */
  public synchronized void cancelTimer() {
    currentTimerTask.ifPresent(t -> t.cancel(false));
    currentTimerTask = Optional.empty();
  }

  /**
   * Whether there is a timer currently running or not
   *
   * @return boolean of whether a timer is ticking or not
   */
  public synchronized boolean isRunning() {
    return currentTimerTask.map(t -> !t.isDone()).orElse(false);
  }

  /**
   * Starts a timer for the supplied round cancelling any previously active block timer
   *
   * @param round The round identifier which this timer is tracking
   * @param chainHeadHeader The header of the chain head
   */
  public synchronized void startTimer(
      final ConsensusRoundIdentifier round, final BlockHeader chainHeadHeader) {
    cancelTimer();

    final long now = clock.millisecondsSinceEpoch();

    // absolute time when the timer is supposed to expire
    final long expiryTime = chainHeadHeader.getTimestamp() * 1_000 + minimumTimeBetweenBlocksMillis;

    if (expiryTime > now) {
      final long delay = expiryTime - now;

      final Runnable newTimerRunnable = () -> queue.add(new BlockTimerExpiry(round));

      final ScheduledFuture<?> newTimerTask =
          timerExecutor.schedule(newTimerRunnable, delay, TimeUnit.MILLISECONDS);
      currentTimerTask = Optional.of(newTimerTask);
    } else {
      queue.add(new BlockTimerExpiry(round));
    }
  }
}