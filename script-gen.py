import argparse, json

parser = argparse.ArgumentParser(description='Generate alea script from experiment settings')
parser.add_argument("source")
parser.add_argument("dest")

args = vars(parser.parse_args())

src = open(args["source"], "r")
dest = open(args["dest"], "w")

setting = json.loads(src.read())

# High-level check of syntax
for prop in ["batch", "load", "protocol", "fault-mode", "benchmarking-mode", "replicas", "clients", "duration"]:
    try:
        assert(prop in setting)
    except:
        print(f"{prop} not in settings")
        exit(1)

SLEEP_TIME = 1000

for i, ip in enumerate(setting["replicas"]):
    dest.write(f"pcs {i} {ip}\n")
    dest.write(f"sleep {SLEEP_TIME}\n")
    dest.write(f"replica {i}\n")
    dest.write(f"sleep {SLEEP_TIME}\n")

for ip in setting["clients"]:
    dest.write(f"client {ip}\n")
    dest.write(f"sleep {SLEEP_TIME}\n")

dest.write(f"topology{''.join([' ' + str(i) for i in range(len(setting['replicas']))])}\n")
dest.write(f"sleep {SLEEP_TIME}\n")

dest.write(f"protocol {setting['protocol']} {setting['batch']} {setting['benchmarking-mode']} {setting['fault-mode']} {setting['load']}\n")
dest.write(f"sleep {SLEEP_TIME}\n")

dest.write("start\n")
dest.write(f"sleep {setting['duration']}\n")

dest.write("stop\n")
dest.write(f"sleep {SLEEP_TIME}\n")

dest.write("shutdown 1000\n")

