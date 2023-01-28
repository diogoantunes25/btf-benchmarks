#! /bin/bash

# ====================================================
# Runs the benchmarks for a single setting
# Usage: run.sh master|client|replica run.json
# ====================================================

# master run.json
function master {
	cd /alea;

	# TODO: Produce input file from run.json
	java -jar ./master.jar < input_file > output_file 2> log_file
	# TODO: do something with results, output and logs
}

# replica run.json
function replica {
	cd /alea;

	java -jar ./pcs.jar http://$1:15000 > output_file 2> log_file
	# TODO: do something with results, output and logs
}

# client run.json
function client {
	cd /alea;

	java -jar ./client.jar > output_file 2> log_file
	# TODO: do something with results, output and logs
}

function help {
	echo "Usage: run.sh master|client|replica run.json";
}

if [[ "$1" = "master" ]]; then
	master $2;
else
	if [[ "$1" = "replica" ]]; then
		replica $2;
	else
		if [[ "$1" = "client" ]]; then
			client $2;
		else
			help;
		fi
	fi
fi


