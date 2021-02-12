'''Script for running integration tests in docker container'''
#!/usr/bin/env python
import subprocess
import os
import argparse
import socket

###Gets the IP of the machine in which it is running
def get_local_ip():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.connect(("8.8.8.8", 80))
    return sock.getsockname()[0]

testHost=get_local_ip()

if __name__ == '__main__':
    parser=argparse.ArgumentParser()
    parser.add_argument('--cluster', type=str, help="name of the deployed cluster")
    parser.add_argument('--tlsPort', type=str, help="TLS Port", default="11501")
    parser.add_argument('--udpPort', type=str, help="UDP Port", default="11500")
    parser.add_argument('--tag', type=str, help="Tag of the docker image containers.cisco.com/edge_group/dhruva-integration-tests")
    args = parser.parse_args()
    cluster_name=args.cluster
    tlsPort=args.tlsPort
    udpPort=args.udpPort
    tag=args.tag
    dhruvaHost="dhruva."+cluster_name+".int.meetapi.webex.com"
    helloWorldPublicUrl="https://dhruva."+cluster_name+".int.meetapi.webex.com/dhruva/api/v1"
    ##Print statements added to verify via jenkins console that correct values are stored
    print("testHost - "+testHost)
    print("dhruvaHost - "+dhruvaHost)
    print("helloWorldPublicUrl - "+helloWorldPublicUrl)
    print("TLSPort - "+tlsPort)
    print("UDPPort - "+udpPort)
    print("Tag - "+tag)
    #To get current working directory
    pwd=os.path.abspath(os.getcwd())
    if not os.path.exists(str(pwd)+"/integration-tests"):
        os.mkdir(str(pwd)+"/integration-tests")

    subprocess.call(['docker', 'run', '-i', '--privileged', \
                       '--mount', 'type=bind,src='+str(pwd)+'/docker/local/run-test-group-1.sh,dst=/usr/local/run-test.sh', \
                       '--mount', 'type=bind,src='+str(pwd)+'/integration-tests,dst=/usr/local/test-output/junitreports', \
                       '--network', 'host', \
                       '-e', 'APP_DYNAMICS_ENABLED=FALSE', \
                       '-e', 'buildTime=$(date +%s)', \
                       '-e', 'helloWorldPublicUrl='+helloWorldPublicUrl, \
                       '-e', 'dhruvaSipTlsPort='+tlsPort, \
                       '-e', 'dhruvaSipUdpPort='+udpPort, \
                       '-e', 'testHost='+testHost, \
                       '-e', 'dhruvaHost='+dhruvaHost, \
                       '-e', 'ciscojEnabled=false', \
                       '-e', 'fedRampEnabled=false', \
                       '--name',  'dhruva-integration-test-'+tag, \
                       'containers.cisco.com/edge_group/dhruva-integration-tests:'+tag, \
                       'sh', '-c', 'date; ls -lrt /usr/local; chmod 777 /usr/local/*.sh; cd /usr/local; ./run-test.sh; ls -lrt' \
                       ])