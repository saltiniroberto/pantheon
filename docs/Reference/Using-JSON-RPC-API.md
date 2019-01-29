description: How to use Pantheon JSON-RPC API
<!--- END of page meta data -->

# Using the JSON-RPC API

!!!attention "Breaking Change in v0.8.3"

    From v0.8.3, to prevent an issue known as DNS rebinding incoming HTTP requests are only accepted from hostnames specified using the [`--host-whitelist`](Pantheon-CLI-Syntax.md#host-whitelist) option.
    The default value for [`--host-whitelist`](Pantheon-CLI-Syntax.md#host-whitelist) is `localhost`. 

    If using the URL `http://127.0.0.1` to make JSON-RPC calls, use [`--host-whitelist`](Pantheon-CLI-Syntax.md#host-whitelist) to specify the hostname `127.0.0.1` or update the hostname in the JSON-RPC call to `localhost`. 

    If your application publishes RPC ports, specify the hostnames when starting Pantheon. For example:  
    ```bash
    pantheon --host-whitelist=example.com
    ```
 
    Specify `*` or `all` for [`--host-whitelist`](Pantheon-CLI-Syntax.md#host-whitelist) to effectively disable host protection and replicate pre-v0.8.3 behavior.
    
    **This is not recommended for production code.**

## Using the Pantheon JSON-RPC API

### Postman

Use the button to import our collection of examples to [Postman](https://www.getpostman.com/). 

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/cffe1bc034b3ab139fa7)

### Endpoint Host and Port

The placeholder
`<JSON-RPC-http-endpoint:port>` and `<JSON-RPC-ws-endpoint:port>` represents an endpoint (IP address and port) 
of the JSON-RPC service of a Pantheon node for HTTP and WebSocket requests.

To enable JSON-RPC over HTTP or WebSockets, use the [`--rpc-http-enabled`](../Reference/Pantheon-CLI-Syntax.md#rpc-http-enabled) 
and [`--rpc-ws-enabled`](../Reference/Pantheon-CLI-Syntax.md#rpc-ws-enabled) options.

Use the [--rpc-http-host](../Reference/Pantheon-CLI-Syntax.md#rpc-http-host) and [--rpc-ws-host](../Reference/Pantheon-CLI-Syntax.md#rpc-ws-host) 
options to specify the host on which the JSON-RPC listens. The default host is 127.0.0.1 for HTTP and WebSockets.  

Set the host to `0.0.0.0` to allow remote connections. 

!!! caution 
    Setting the host to 0.0.0.0 exposes the RPC connection on your node to any remote connection. In a 
    production environment, ensure you are using a firewall to avoid exposing your node to the internet.  

Use the [--rpc-http-port](../Reference/Pantheon-CLI-Syntax.md#rpc-http-port) and [--rpc-ws-port](../Reference/Pantheon-CLI-Syntax.md#rpc-ws-port)
options to specify the port on which the JSON-RPC listens. The default ports are: 

* 8545 for HTTP
* 8546 for WebSockets

### HTTP and WebSocket Requests

#### HTTP

To make RPC over HTTP requests, you can use the `curl` tool, available in many systems using [curl source code or pre-compiled packages](https://curl.haxx.se/download.html).

```bash
$ curl -X POST --data '{"jsonrpc":"2.0","method":"web3_clientVersion","params":[],"id":53}' <JSON-RPC-http-endpoint:port>
```

#### WebSockets

To make requests over WebSockets, this reference uses [wscat](https://github.com/websockets/wscat), a Node.js based command-line tool.

First connect to the WebSockets server using `wscat` (you only need to connect once per session):

```bash
$ wscat -c ws://<JSON-RPC-ws-endpoint:port>
```

After the connection is established, the terminal will display a '>' prompt.
Send individual requests as a JSON data package at each prompt:

```bash
> {"jsonrpc":"2.0","method":"web3_clientVersion","params":[],"id":53}
```

The [RPC Pub/Sub methods](../Using-Pantheon/RPC-PubSub.md) can also be used over WebSockets.

### API Methods Enabled by Default

The `ETH`, `NET`, and `WEB3` API methods are enabled by default. 

Use the [`--rpc-http-api`](Pantheon-CLI-Syntax.md#rpc-http-api) or [`--rpc-ws-api`](Pantheon-CLI-Syntax.md#rpc-ws-api) 
options to enable the `ADMIN` ,`CLIQUE`,`DEBUG`, `IBFT` and `MINER` API methods.

!!! note
    IBFT 2.0 is under development and will be available in v1.0. 

### Block Parameter

When you make requests that might have different results depending on the block accessed, 
the block parameter specifies the block. 
Several methods, such as [eth_getTransactionByBlockNumberAndIndex](JSON-RPC-API-Methods.md#eth_gettransactionbyblocknumberandindex), have a block parameter.

The block parameter can have the following values:

* `blockNumber` : `quantity` - Block number. Can be specified in hexadecimal or decimal. 0 represents the genesis block.
* `earliest` : `tag` - Earliest (genesis) block. 
* `latest` : `tag` - Last block mined.
* `pending` : `tag` - Last block mined plus pending transactions. Use only with [eth_getTransactionCount](JSON-RPC-API-Methods.md#eth_gettransactioncount).  

## Not Supported by Pantheon

### Account Management 

Account management relies on private key management in the client which is not implemented by Pantheon. 

Use [`eth_sendRawTransaction`](JSON-RPC-API-Methods.md#eth_sendrawtransaction) to send signed transactions; `eth_sendTransaction` is not implemented. 

Use third-party wallets for [account management](../Using-Pantheon/Account-Management.md). 

### Protocols

Pantheon does not implement the Whisper and Swarm protocols.