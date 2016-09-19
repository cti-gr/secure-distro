#!/bin/bash

# This file is part of the PROTOS project: http://protos.cti.gr
# Contact e-mail: protosproject@gmail.com
#
# Firelog Linux client installation script
# Version: 0.1 Beta
# Updated: 2013-04-23


installDir=/usr/local/share/firelog
versionFile=$installDir/version.txt


# We need root privileges to perform some operations
if [ $(id -u) -ne 0 ]; then
    echo "[-] You need to be root to run this script"
    exit 1
fi

# Check if sqlite3 is installed in the system
which sqlite3 > /dev/null
if [ $? -ne 0 ]; then
    echo "[-] sqlite3 not found. Please install sqlite3 and try again"
    exit 1
else
    echo "[*] Looking for sqlite3... OK!"    
fi

# Check if the firelog data directory exists
if [ ! -d $installDir ]; then
    echo "[*] Firelog directory $installDir not found, creating..."
    mkdir -m 755 $installDir # Set permissions to 755
fi

workingDir=$(pwd)
echo "[*] Copying required files to $installDir/. ..."
cp $workingDir/firelog $installDir/.
chmod 744 $installDir/firelog
cp $workingDir/update_firelog $installDir/.
chmod 744 $installDir/update_firelog
cp $workingDir/version.txt $installDir/.
chmod 644 $installDir/version.txt
cp $workingDir/firelog.conf /etc/init/.
chmod 644 /etc/init/firelog.conf

echo "[*] Adding crontab entries..."
# The following entries will be added:
# @reboot $installDir/update_firelog
# @daily $installDir/update_firelog
crontab -l | awk '{print} END {print "@reboot '$installDir'/update_firelog"}' | crontab
crontab -l | awk '{print} END {print "@daily '$installDir'/update_firelog"}' | crontab

echo "[*] Starting firelog service..."
start firelog

echo "[*] Installation script finished!"
