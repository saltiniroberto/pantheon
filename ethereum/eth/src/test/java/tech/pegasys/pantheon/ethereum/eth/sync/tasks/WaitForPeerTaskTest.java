package tech.pegasys.pantheon.ethereum.eth.sync.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthProtocolManager;
import tech.pegasys.pantheon.ethereum.eth.manager.EthProtocolManagerTestUtil;
import tech.pegasys.pantheon.ethereum.eth.manager.EthTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

public class WaitForPeerTaskTest {
  private EthProtocolManager ethProtocolManager;
  private EthContext ethContext;

  @Before
  public void setupTest() {
    ethProtocolManager = EthProtocolManagerTestUtil.create();
    ethContext = ethProtocolManager.ethContext();
  }

  @Test
  public void completesWhenPeerConnects() throws ExecutionException, InterruptedException {
    // Execute task and wait for response
    final AtomicBoolean successful = new AtomicBoolean(false);
    final EthTask<Void> task = WaitForPeerTask.create(ethContext);
    final CompletableFuture<Void> future = task.run();
    future.whenComplete(
        (result, error) -> {
          if (error == null) {
            successful.compareAndSet(false, true);
          }
        });
    EthProtocolManagerTestUtil.createPeer(ethProtocolManager);
    assertThat(successful).isTrue();
  }

  @Test
  public void doesNotCompleteWhenNoPeerConnects() throws ExecutionException, InterruptedException {
    final AtomicBoolean successful = new AtomicBoolean(false);
    final EthTask<Void> task = WaitForPeerTask.create(ethContext);
    final CompletableFuture<Void> future = task.run();
    future.whenComplete(
        (result, error) -> {
          if (error == null) {
            successful.compareAndSet(false, true);
          }
        });

    assertThat(successful).isFalse();
  }

  @Test
  public void cancel() throws ExecutionException, InterruptedException {
    // Execute task
    final EthTask<Void> task = WaitForPeerTask.create(ethContext);
    final CompletableFuture<Void> future = task.run();

    assertThat(future.isDone()).isFalse();
    task.cancel();
    assertThat(future.isDone()).isTrue();
    assertThat(future.isCancelled()).isTrue();
    assertThat(task.run().isCancelled()).isTrue();
  }
}
