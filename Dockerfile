FROM maven:latest

WORKDIR /alea

COPY ./alea-benchmarks/master/target/master-1.0-SNAPSHOT.jar /alea/master.jar

COPY ./alea-benchmarks/pcs/target/pcs-1.0-SNAPSHOT.jar /alea/pcs.jar

COPY ./alea-benchmarks/replica/target/replica-1.0-SNAPSHOT.jar /alea/replica.jar

COPY ./run_benchmarks.sh /alea/run.sh

RUN chmod +x /alea/run.sh
RUN apt update

RUN apt install ifstat -y

RUN mkdir /alea/results

RUN mkdir /alea/logs

ENTRYPOINT [ "/alea/run.sh" ]

