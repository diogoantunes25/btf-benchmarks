#! /bin/bash

if [[ "$1" = "m" ]]
then

	if [[ $* == *--auto-run* ]]
	then
		java -jar ./master.jar < /main.alea
	else
		echo "Running master..."
		java -jar ./master.jar
	fi
else
	echo "Running PCS..."
	# <master URI>
	java -jar ./pcs.jar $2
fi


