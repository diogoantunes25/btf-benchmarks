import argparse

parser = argparse.ArgumentParser(description = "Parse run specification json file")
parser.add_argument("--full", action = "store_true")
parser.add_argument("--master", action = "store_true")
args = vars(parser.parse_args())

print(args)

if args["master"]: print("full")

elif args["full"]: print("master")
