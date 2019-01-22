*[Byzantine fault tolerant]: Ability to function correctly and reach consensus despite nodes failing or propagating incorrect information to peers.

# Comparing Proof of Authority Consensus Protocols  

Pantheon implements the Clique and IBFT 2.0 Proof of Authority consensus protocols. Proof of Authority 
consensus protocols are used when participants are known to each other and there is a level of trust between them. 
For example, in a permissioned consortium network. 

Proof of Authority consensus protocols allow faster block times and have a much greater throughput of transactions 
than the Ethash Proof of Work consensus protocol used on the Ethereum MainNet. 

In Clique and IBFT 2.0, a group of nodes in the network act as signers (Clique) or validators (IBFT 2.0). These nodes propose, validate, 
and add blocks to the blockchain. Nodes are added to or removed from the signer/validator pool by the existing group of nodes voting. 

!!! note
     For the rest of this page, the term validator is used to refer to signers and validators. 

## Properties 
   
Properties to consider when comparing Clique and IBFT 2.0 are: 

* Immediate finality 
* Validators availability
* Speed 
* Type of Byzantine-fault-tolerance

### Immediate Finality 

IBFT 2.0 has immediate finality. When using IBFT 2.0 there are no forks and all valid blocks are included in the main chain.

Clique does not have immediate finality. Implementations using Clique must be aware of forks and chain reorganizations occurring. 

### Validators availability

IBFT 2.0 requires at least 2/3 of the validators (rounded up to the next integer) to be always online for the network to
be able to create new blocks and progress.
Clique only requires more then 1/2 of the validators (rounded up to the next integer) to be always online for the
network to be able to mine new blocks.

### Speed 

Reaching consensus and adding blocks is faster in Clique networks. For Clique, the probability of a fork 
increases number as the of validators increases. 

For IBFT 2.0, the time to add new blocks increases as the number of validators increases.   

### Type of Byzantine-fault-tolerance

IBFT 2.0 features a classical BFT consensus protocol that ensures safety (no fork is possible) provided that no more than
(n-1)/3 of the validators (truncated to the integer value) are Byzantine.
For example in an IBFT 2.0 network of:
* 3, no Byzantine nodes are tolerated
* 4-6, 1 Byzantine node is tolerated
* 7-9, 2 Byzantine nodes are tolerated

Clique features a probabilistic consensus protocol (à la Nakamoto) where forks are possible but the deeper a block is
in the chain, the more probable is that the block is final (i.e. it will not be part of a reorganization ever).
Also, the higher the network latency is, the more depth is required for a block to be considered stable.







