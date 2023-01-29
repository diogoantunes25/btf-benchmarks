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

echo "arguments seem ok" >> /alea/log;

if [[ "$1" == "master" ]]; then
	echo "master started" >> /alea/log;

	verify "/alea/master.jar";

	echo "SETTING ===================" >> /alea/log;
	cat /alea/setting.json >> /alea/log

	python3 script-gen.py /alea/setting.json /alea/main.alea

	echo "\nMAIN.ALEA ==================" >> /alea/log;
	cat /alea/main.alea >> /alea/log
	echo ""

	java -jar /alea/master.jar < /alea/main.alea &>> /alea/log
	echo "master done" >> /alea/log
else
	masterIP=$(cat setting.json | cut -d \, -f 6 | cut -d \:  -f 2 | sed -E 's/\ *\"\ *//g')
	echo "The master is at $masterIP"

	if [[ "$1" == "replica" ]]; then
		verify "/alea/pcs.jar";
		verify "/alea/replica.jar";

		cd /alea
		java -jar /alea/pcs.jar http://$masterIP:15000 &> /alea/log
	else
		if [[ "$1" == "client" ]]; then
			verify "/alea/client.jar";

			java -jar /alea/client.jar $masterIP &> /alea/log
		else
			echo "Invalid mode - $1";
			help;
		fi
	fi
fi
