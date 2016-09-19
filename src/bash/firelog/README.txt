This file is part of the PROTOS project: http://protos.cti.gr
Contact e-mail: protosproject@gmail.com


1. Requirements
===============

1.1 SQLite
----------

The Firelog client (part of the PROTOS Project) requires sqlite3.
You can install sqlite3 by executing:

$ sudo apt-get install sqlite3 (Note that the leading '$' is meant to
denote the Bash shell command prompt and is not part of the actual
command to be issued).


1.2 Uncomplicated Firewall (ufw)
--------------------------------

Check firewall status by executing:

$ sudo ufw status

If the firewall is disabled you must enable it:

$ sudo ufw enable
$ sudo ufw logging on



2. Installation
===============

$ wget http://protos.cti.gr/firelog/linux/ubuntu/firelog-linux-0.3-beta.tar.gz

Decompress and install:

$ tar xvfz firelog-linux-0.3-beta.tar.gz
$ cd firelog-linux-0.3-beta
$ sudo ./install_firelog.sh



3. Uninstalling Firelog
=======================

a) Stop firelog service
$ sudo service firelog stop

b) Remove firelog.conf file
$ sudo rm /etc/init/firelog.conf

c) Remove firelog directory
$ sudo rm -rf /usr/local/share/firelog

d) Remove crontabs
$ sudo crontab -e

Find and delete the two lines below, and then save.
@reboot /usr/local/share/firelog/update_firelog
@daily /usr/local/share/firelog/update_firelog
