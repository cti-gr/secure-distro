#!/bin/bash

username=$1
userid=`id $username -u`
tmpfile=/tmp/iptablestmpfile
permfile=/etc/iptables.conf

searchString1="\-A OUTPUT \-p tcp \-m owner \-\-uid\-owner $userid \-m multiport \-\-dports 80,8080 \-j REDIRECT \-\-to\-ports 8080"
searchString2="\-A OUTPUT \-p tcp \-m owner \-\-uid\-owner $userid \-m tcp \-\-dport 8118 \-j DROP"


# export iptables rules to a tmp_file
iptables-save > $tmpfile

sed -i '/'"$searchString1"'/d' $tmpfile
sed -i '/'"$searchString2"'/d' $tmpfile
sed -i '/'"$searchString1"'/d' $permfile
sed -i '/'"$searchString2"'/d' $permfile

# DELETE those lines from the tmp file
# DELETE those lines from /etc/iptables.conf
iptables-restore < $tmpfile

userdel -r $username

# removing entry from sudoers
if [ -e /etc/sudoers.tmp -o "$(pidof visudo)" ]; then 
  echo "/etc/sudoers busy, try again later"
  exit 1
fi

cp /etc/sudoers /etc/sudoers.bak
cp /etc/sudoers /etc/sudoers.tmp

chmod 0640 /etc/sudoers.tmp
sed -i /"${username}"/d /etc/sudoers.tmp
chmod 0440 /etc/sudoers.tmp

mv /etc/sudoers.tmp /etc/sudoers
