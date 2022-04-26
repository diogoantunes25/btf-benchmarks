# Alea-BFT - Benchmark Service

## Overview
There are two roles to play by processes: master or replica.
The replicas are the processes that will run Alea-BFT and the master is the process responsible for coordinating the replicas.
## Setup
## Use
You start by setting up the master. Then you can launch the replicas and they will register in the master and wait for instructions.

Before running the code make sure everythings is okay by running `mvn verify`. Then make sure the CLASSPATH is set properly (if not run `export CLASSPATH=$(pwd)/master/target/master-1.0-SNAPSHOT.jar:$(pwd)/pcs/target/pcs-1.0-SNAPSHOT.jar:$(pwd)/replica/target/replica-1.0-SNAPSHOT.jar:$(pwd)/contract/target/contract-1.0-SNAPSHOT.jar` in the root directory of the project). 

### Run locally
Running locally is easier since the default IP hard coded is the localhost.
To start the master run `java -jar ./master/target/master-1.0-SNAPSHOT.jar` and to start the replicas run `java -jar ./replica/target/original-replica-1.0-SNAPSHOT.jar`
### Available commands

- `pcs <id> <node>`: information about the pcs to spawn
- `replica <pcs> <replica_id>`: pcs that will spawn the replica and the replica_id
- `exit`: terminates the master
- `list`: Lists the PCSs and the replicas
- `topology <replicas_ids>`: sets the topology 
- `protocol <protocol> <batch_size> <mode> <fault>`: sets the protocol to use (can be HoneyBadgerBFT(`hb`), Dumbo1(`dumbo`), Dumbo2(`dumbo2`) or Alea-BFT(`alea`)), the batch size for the protocol, the metric to measure (can be `latency` or `throughput`) and the fault mode (can be `free`, `crash` or `byzantine`)
- `start`: 
- `stop`: 
- `benchmark <num_requests> <payload>`: 
- `shutdown`: shutsdown all the replicas and the master
- `nop`: nop command (no operation)
- `sleep <duration>`: sleeps for the specified duration
- `script <script>`: runs a script
- `aws <region>`: 
- `terminate [id]`: 