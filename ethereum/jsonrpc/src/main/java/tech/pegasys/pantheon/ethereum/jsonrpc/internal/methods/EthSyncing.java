package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods;

import tech.pegasys.pantheon.ethereum.core.Synchronizer;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.SyncingResult;

/*
 * SyncProgress retrieves the current progress of the syncing algorithm. If there's no sync
 * currently running, it returns false.
 */
public class EthSyncing implements JsonRpcMethod {

  private final Synchronizer synchronizer;

  public EthSyncing(final Synchronizer synchronizer) {
    this.synchronizer = synchronizer;
  }

  @Override
  public String getName() {
    return "eth_syncing";
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest req) {
    // Returns false when not synchronizing.
    final Object result =
        synchronizer.getSyncStatus().map(s -> (Object) new SyncingResult(s)).orElse(false);
    return new JsonRpcSuccessResponse(req.getId(), result);
  }
}
