#!/bin/bash
# exit codes: 1=username exists 2=unable to create user 3=script not run by sudo user

if [ $(id -u) -eq 0 ]; then
	CHILD=$1
	PASS1=$2
	userexists=`egrep "^$CHILD" /etc/passwd >/dev/null`
	if [ $? == 0 ]; then
	   	exit 1
	fi
	CHILD_PASS=$(perl -e 'print crypt($ARGV[0], "password")' $PASS1)
	useradd -m -p $CHILD_PASS $CHILD
	if [ $? -ne 0 ]; then 
		exit 2
	fi
else
	exit 3
fi
exit 0
