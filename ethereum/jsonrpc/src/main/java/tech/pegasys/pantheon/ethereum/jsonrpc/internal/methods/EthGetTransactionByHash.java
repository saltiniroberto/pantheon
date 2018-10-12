package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.PendingTransactions;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.TransactionCompleteResult;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.TransactionPendingResult;

import java.util.Optional;

public class EthGetTransactionByHash implements JsonRpcMethod {

  private final BlockchainQueries blockchain;
  private final PendingTransactions pendingTransactions;
  private final JsonRpcParameter parameters;

  public EthGetTransactionByHash(
      final BlockchainQueries blockchain,
      final PendingTransactions pendingTransactions,
      final JsonRpcParameter parameters) {
    this.blockchain = blockchain;
    this.pendingTransactions = pendingTransactions;
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return "eth_getTransactionByHash";
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    if (request.getParamLength() != 1) {
      return new JsonRpcErrorResponse(request.getId(), JsonRpcError.INVALID_PARAMS);
    }
    final Hash hash = parameters.required(request.getParams(), 0, Hash.class);
    final JsonRpcSuccessResponse jsonRpcSuccessResponse =
        new JsonRpcSuccessResponse(request.getId(), getResult(hash));
    return jsonRpcSuccessResponse;
  }

  private Object getResult(final Hash hash) {
    final Optional<Object> transactionCompleteResult =
        blockchain.transactionByHash(hash).map(TransactionCompleteResult::new);
    return transactionCompleteResult.orElseGet(
        () ->
            pendingTransactions
                .getTransactionByHash(hash)
                .map(TransactionPendingResult::new)
                .orElse(null));
  }
}
