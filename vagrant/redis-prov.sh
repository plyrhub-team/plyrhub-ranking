#!/bin/sh

sudo apt-get -y update

sudo apt-get -y install build-essential

sudo apt-get -y install tcl8.5

cd /tmp

# Redis repo / redis tar / redis version 
redis_repo=$1
redis_tar=$2
redis_version=$3

wget $redis_repo/$redis_tar 

tar xzf $redis_tar  

cd $redis_version 

make

#make test

sudo make install

cd utils

sudo ./install_server.sh




