#!/bin/bash

# This file is part of the PROTOS project: http://protos.cti.gr
# Contact e-mail: protosproject@gmail.com
#
# Firelog Linux client installation script
# Version: 0.1 Beta
# Updated: 2013-04-24


installDir=/usr/local/share/firelog
#versionFile=$installDir/version.txt


# We need root privileges to perform some operations
if [ $(id -u) -ne 0 ]; then
    echo "[-] You need to be root to run this script"
    exit 1
fi

echo "[*] Stopping firelog service..."
service firelog stop
# Check if firelog is still running
while $(ps -u root | grep -q "firelog"); do
    service firelog stop
    sleep 1
done;
echo "[*] Firelog service stopped"


echo "[*] Deleting Installation Directory..."
echo "[*] Firelog directory $installDir  found, removing..."
rm -rf $installDir 

echo "[*] Deleting configuration file..."
rm /etc/init/firelog.conf

#workingDir=$(pwd)
#echo "[*] Copying required files to $installDir/. ..."
#cp $workingDir/firelog $installDir/.
#chmod 744 $installDir/firelog
#cp $workingDir/update_firelog $installDir/.
#chmod 744 $installDir/update_firelog
#cp $workingDir/version.txt $installDir/.
#chmod 644 $installDir/version.txt
#cp $workingDir/firelog.conf /etc/init/.
#chmod 644 /etc/init/firelog.conf

echo "[*] Removing crontab entries..."
# The following entries will be removed:
# @reboot $installDir/update_firelog
# @daily $installDir/update_firelog
crontab -l | sed '\!firelog!d' | crontab

echo "[*] Uninstallation script finished!"
