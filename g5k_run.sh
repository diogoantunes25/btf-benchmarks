#! /bin/bash

python3 run.py --setup --settings /home/dantunes/settings.json /home/dantunes/vars && \
	oarsub -l walltime=1:00:00 "source ~/.bashrc && \
								python3 run.py --run /home/dantunes/vars &> /home/dantunes/execution.log"
