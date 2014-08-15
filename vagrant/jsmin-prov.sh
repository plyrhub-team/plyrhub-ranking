#!/bin/sh

cd /usr/local/src

sudo git clone https://github.com/douglascrockford/JSMin.git

cd JSMin

sudo /usr/bin/gcc -o jsmin jsmin.c

sudo mv jsmin /usr/local/bin/jsmin

sudo rm -rf /usr/local/src/JSMin
