#!/bin/sh

echo mysql-server mysql-server/root_password password wowwow | sudo debconf-set-selections
echo mysql-server mysql-server/root_password_again password wowwow | sudo debconf-set-selections

# mysqladmin -u root password mysecretpasswordgoeshere

sudo apt-get -y install mysql-server-$1

