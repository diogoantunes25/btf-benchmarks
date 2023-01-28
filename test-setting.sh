#! /bin/bash

# =======================================================
# =======================================================
# test-setting.sh mode
#
# Launch a node (master, replica or client) running to test
# the settings provided.
# File are assumed to be at /alea.
# =======================================================
# =======================================================

function help {
	echo "usage: test-setting mode"
	echo "Required files:"
	echo "	- setting.json: experiment settings"
	echo "	- master.jar: master class files"
	echo "	- pcs.jar: pcs class files"
	echo "	- replica.jar: replica class files"
	echo "	- client.jar: client class files"
	echo "	- script-gen.py: python script to produce alea script from settings"
}

function verify {
	ls $1 &> /dev/null;

	if [[ $? -ne 0 ]]; then
		echo "$1 not found";
		exit 1;
	fi
}

if [[ $# -ne 1 ]]; then
	echo "Invalid arguments";
	help;
	exit 1;
fi

verify "/alea/setting.json";

if [[ "$1" == "master" ]]; then
	echo "master";
	verify "/alea/master.jar";
	python3 script-gen.py /alea/setting.json /alea/main.alea

	# Produce main.alea
	java -jar /alea/master.jar < main.alea
else
	masterIP=$(cat setting.json | cut -d \, -f 6 | cut -d \:  -f 2 | sed -E 's/\ *\"\ *//g')
	echo "The master is at $masterIP"

	if [[ "$1" == "replica" ]]; then
		verify "/alea/pcs.jar";
		verify "/alea/replica.jar";
		mkdir /alea/logs

		cd /alea
		java -jar /alea/pcs.jar http://$masterIP:15000
	else
		if [[ "$1" == "client" ]]; then
			verify "/alea/client.jar";

			java -jar /alea/client.jar $masterIP
		else
			echo "Invalid mode - $1";
			help;
		fi
	fi
fi
