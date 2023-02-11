#! /bin/bash

sites="grenoble lille luxembourg lyon nancy nantes rennes sophia"
for site in $sites;
do
	cd $site;
	rm -rf benchmarks;
	cd ..;
done
