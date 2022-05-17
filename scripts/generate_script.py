# input: [pcs_nodes] [number of replicas] [protocols] [batch sizes] [benchmark modes] [security modes]
# output: scripts for the all the possible configurations in the cartesian product defined by the input

import sys

RUNNING_TIME = 1000 # duration of the tests (time master waits before shutting down)

def parse_cl_args():
    if len(sys.argv) < 7:
        raise Exception("insufficient number of arguments")

    pcs_nodes = sys.argv[1][1:-1].split(",")
    replica_counts = [int(count) for count in sys.argv[2][1:-1].split(",")]
    protocols = sys.argv[3][1:-1].split(",")
    batch_sizes = [int(size) for size in sys.argv[4][1:-1].split(",")]
    benchmark_modes = sys.argv[5][1:-1].split(",")
    security_modes = sys.argv[6][1:-1].split(",")

    args = {}
    args["pcs_nodes"] = pcs_nodes
    args["replica_counts"] = replica_counts
    args["protocols"] = protocols
    args["batch_sizes"] = batch_sizes
    args["benchmark_modes"] = benchmark_modes
    args["security_modes"] = security_modes

    return args

def generate_script(args):

    script = open("generated_scripts/script", "w")

    # Initial set up
    for i, node in enumerate(args["pcs_nodes"]):
        script.write(f'pcs {i} {node}\n')

    n_max = max(args["replica_counts"])

    pcs_count = len(args["pcs_nodes"])
    pcs_index = 0
    for n in range(n_max):
        script.write(f'replica {pcs_index} {n}\n') # Distribute replicas evenly between nodes (I assume all pcs run on different nodes)
        pcs_index = (pcs_index + 1) % pcs_count

    # For each count, set up topology
    for n in args["replica_counts"]:
        script.write(f'topology {" ".join([str(i) for i in range(n)])}\n')
        for protocol in args["protocols"]:
            for batch_size in args["batch_sizes"]:
                for benchmark_mode in args["benchmark_modes"]:
                    for security_mode in args["security_modes"]:
                        script.write(f'protocol {protocol} {batch_size} {benchmark_mode} {security_mode}\n')
                        script.write("start\n")
                        script.write(f'sleep {RUNNING_TIME}\n')
                        script.write("stop\n")

    # Final shutdown
    script.write("shutdown\n")

    script.close()

if __name__ == '__main__':
    generate_script(parse_cl_args())