# Use another base image
FROM maven:3.8.6-amazoncorretto-11 as builder
RUN yum update -y && yum install -y git 

# setup the repository read-only ssh key
RUN mkdir -p /root/.ssh/ 

# create a read only deployment key and add it to github 
COPY id_alea_protocols* /root/.ssh/
RUN mv /root/.ssh/id_alea_protocols /root/.ssh/id_rsa; \
    chmod -R 600 /root/.ssh/ ; \
    ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts

WORKDIR /code
RUN git clone git@github.com:diogoantunes25/alea-protocols.git .
RUN mvn -Dmaven.repo.local=/alea/.m2 clean install

WORKDIR /alea
COPY . .
RUN mvn -Dmaven.repo.local=/alea/.m2 clean install

# Use another base image
FROM amazoncorretto:11.0.17 as runner 

COPY --from=builder ./alea/master/target/master-1.0-SNAPSHOT.jar /alea/master.jar
COPY --from=builder ./alea/pcs/target/pcs-1.0-SNAPSHOT.jar /alea/pcs.jar
COPY --from=builder ./alea/replica/target/replica-1.0-SNAPSHOT.jar /alea/replica.jar
COPY --from=builder ./alea/client/target/client-1.0-SNAPSHOT.jar /alea/client.jar
COPY ./test-setting.sh /alea/run.sh
COPY ./script-gen.py /alea/script-gen.py

RUN chmod +x /alea/run.sh && \
	yum update -y && \
	amazon-linux-extras install -y epel && \
	yum install -y ifstat && \
	yum install -y python3 && \
	yum -y clean all && \
	mkdir /alea/results && \
	mkdir /alea/logs

WORKDIR /alea
ENTRYPOINT [ "/alea/run.sh" ]
