#!/bin/bash
########### error codes ###################
# 1: repo not added
# 2: dependency/ies missing
# 3: secure distro package missing
# 4: cannot update sudoers file
CHILD=$1
installDirectory=/opt/secure-distro/bin

# sqlite3 commands needed for avgui database file
CREATE_TBL_MALWARE="CREATE TABLE tblMalware(
ID INTEGER PRIMARY KEY AUTOINCREMENT,
Name TEXT NOT NULL UNIQUE
)"

CREATE_TBL_VIRUSDBS="CREATE TABLE tblVirusDBs (
ID INTEGER PRIMARY KEY AUTOINCREMENT,
DBVersion TEXT NOT NULL,
DBrDate TEXT NOT NULL)"

CREATE_TABLE_SCANEVENT="CREATE TABLE tblScanEvent (
ID INTEGER PRIMARY KEY AUTOINCREMENT,
User TEXT NOT NULL,
NoOfInfections INTEGER NOT NULL,
NoOfHealings INTEGER NOT NULL,
DateTime TEXT NOT NULL,
VirusDBID INTEGER NOT NULL,
FOREIGN KEY(VirusDBID) REFERENCES tblVirusDBs(ID)
)"

CREATE_TBL_INF="CREATE TABLE tblInfections (
ID INTEGER PRIMARY KEY AUTOINCREMENT,
FilePath TEXT NOT NULL,
Inode INTEGER NOT NULL UNIQUE,
ScanEventID INTEGER NOT NULL,
MalwareID INTEGER NOT NULL,
FOREIGN KEY(ScanEventID) REFERENCES tblScanEvent(ID),
FOREIGN KEY(MalwareID) REFERENCES tblMalware(ID)
)"

# Step 2: Checking Dependencies
# exiting with errcode 2 if any dependency is missing;

dpkg -s python3-pip >/dev/null || exit 2
dpkg -s python3.4 >/dev/null || exit 2
dpkg -s sqlite3 >/dev/null || exit 2
dpkg -s gksu >/dev/null  || exit 2
dpkg -s libc6:i386 >/dev/null || exit 2
dpkg -s flashplugin-installer >/dev/null || exit 2


# Step 3: Checking Secure Distro packages 
# exiting with errcode 3 if any dependency is missing;

dpkg -s avg2013flx >/dev/null || exit 3
dpkg -s avgui >/dev/null || exit 3
dpkg -s privoxy >/dev/null || exit 3
dpkg -s dansguardian >/dev/null || exit 3
dpkg -s bleachbit >/dev/null  || exit 3
dpkg -s gufw >/dev/null || exit 3
dpkg -s keepass2 >/dev/null || exit 3


################################################################################
############################## AVGUI CONFIGURATION #############################
################################################################################

mkdir -p /home/${CHILD}/.avgui/

# Creating sqlite3 file


# echo "Creating tables needed to keep avg history"
sqlite3 /home/${CHILD}/.avgui/avghistory.sqlite "${CREATE_TBL_MALWARE}; ${CREATE_TBL_VIRUSDBS}; ${CREATE_TABLE_SCANEVENT}; ${CREATE_TBL_INF}"
# Assigning avghistory.sqlite file to user " "${SUDO_USER}"

cp /home/${SUDO_USER}/.avgui/config.ini /home/${CHILD}/.avgui/
#cp /home/${SUDO_USER}/.avgui/avghistory.sqlite /home/${CHILD}/.avgui/


mkdir -p /home/${CHILD}/.avgui/log

if [ -f /home/${CHILD}/.avgui/log/hellodaemon.conf ]
then
    rm /home/${CHILD}/.avgui/log/hellodaemon.conf
    touch /home/${CHILD}/.avgui/log/hellodaemon.conf
fi

if [ -f /home/${CHILD}/.avgui/log/hellodaemon.log ]
then
    rm /home/${CHILD}/.avgui/log/hellodaemon.log
    touch /home/${CHILD}/.avgui/log/hellodaemon.log
fi

echo "[hello]" >> /home/${CHILD}/.avgui/log/hellodaemon.conf
echo "pidfile =  /home/${CHILD}/.avgui/log/hellodaemon.pid" >> /home/${CHILD}/.avgui/log/hellodaemon.conf
echo "logfile =  /home/${CHILD}/.avgui/log/hellodaemon.log" >> /home/${CHILD}/.avgui/log/hellodaemon.conf
echo "loglevel = 1" >> /home/${CHILD}/.avgui/log/hellodaemon.conf

chown -R ${CHILD}:${CHILD} /home/${CHILD}/.avgui

################################################################################
########################### SUDOERS FILE CONFIGURATION #########################
################################################################################

if [ -f "/etc/sudoers.tmp" ]; then
	exit 4
fi

echo "${CHILD} ALL = NOPASSWD: /usr/bin/avgupdate , /usr/bin/avgupdate -c , /usr/bin/avgctl" >> /etc/sudoers

SD_DIR=/home/${CHILD}/.securedistro
if [ ! -d $SD_DIR ]
then
	mkdir -p $SD_DIR
	touch $SD_DIR/application.properties
	echo "country=EN" >> $SD_DIR/application.properties
	echo "language=en" >> $SD_DIR/application.properties
	echo "protectionStatus=true" >> $SD_DIR/application.properties	
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )" && echo "Current dir is $DIR"
#ls -al
cp /opt/secure-distro/bin/sd-wallpaper.png $SD_DIR

cp /opt/secure-distro/bin/addShortcutsNewUser.sh $SD_DIR

chown ${CHILD}:${CHILD} ${SD_DIR}/addShortcutsNewUser.sh
chmod 700 ${SD_DIR}/addShortcutsNewUser.sh

chown -R ${CHILD}:${CHILD} $SD_DIR

################################################################################
############ CONFIGURE IPTABLES TO MAKE DG THE TRRANSPARENT PROXY ##############
################################################################################

# the new user's tcp traffic will go through port 8080
child_id=`id $CHILD -u`
iptables -t nat -A OUTPUT -p tcp -m owner --uid-owner $child_id -m multiport --dports 80,8080 -j REDIRECT --to-port 8080

# disallow new user to directly connect to Privoxy, thus bypassing dansguardian
iptables  -A OUTPUT -p tcp -m owner --uid-owner $child_id --dport 8118 -j DROP

# saving the rule to an iptables conf file
bash -c "iptables-save > /etc/iptables.conf"

# removing potential duplicate lines from iptables conf file
#bash -c "cat /etc/iptables.conf | uniq > /etc/iptables2.conf"
#bash -c "mv /etc/iptables2.conf /etc/iptables.conf"


# ensuring the rules are restored on each system initialization
# insert the iptables-restore command one line before the last of /etc/rc.local
myString="iptables-restore < /etc/iptables.conf"
grep -q "$myString" /etc/rc.local || 
{
rclocalines=`wc -l < /etc/rc.local`
linetoinsert=$((rclocalines-1))
sed -i "${linetoinsert}i \iptables-restore < /etc/iptables.conf" /etc/rc.local
}

################################################################################
####################### FIREFOX CUSTOMIZATION PROCESS ##########################
################################################################################

sudo cp -r ${installDirectory}/firefox/.mozilla/ /home/${CHILD}/


#########################################
###           Home Page               ###
#########################################
HP_FILE=/home/${CHILD}/.mozilla/firefox/*.default/prefs.js
HP_PATTERN='"browser.startup.homepage"'
HP_LINE='user_pref("browser.startup.homepage", "http://secure-distro.cti.gr/");'

if grep -q $HP_PATTERN $HP_FILE;
 then
     sed -i 's|\("browser.startup.homepage",\) "\(.*\)"|\1 "http://secure-distro.cti.gr/"|' $HP_FILE
     echo "Homepage changed."
 else
     echo $HP_LINE >> $HP_FILE
     echo "" >> $HP_FILE
     echo "Homepage inserted."
fi


#########################################
###           Bookmarks               ###
#########################################
DATABASE=/home/${CHILD}/.mozilla/firefox/*.default/places.sqlite

sqlite3 $DATABASE "INSERT INTO moz_bookmarks(type, parent, title) VALUES (2, 3, 'Διανομή Ασφάλειας')"

sqlite3 $DATABASE "INSERT INTO moz_places(url) VALUES ('http://www.sch.gr/')"
sqlite3 $DATABASE "INSERT INTO moz_bookmarks(type, fk, parent, title) Values (1, (SELECT id FROM moz_places WHERE url='http://www.sch.gr/'), (SELECT MAX(id) FROM moz_Bookmarks WHERE type='2'), 'Πανελλήνιο Σχολικό Δίκτυο')"

sqlite3 $DATABASE "INSERT INTO moz_places(url) VALUES ('http://repository.edulll.gr/edulll/?locale=el')"
sqlite3 $DATABASE "INSERT INTO moz_bookmarks(type, fk, parent, title) Values (1, (SELECT id FROM moz_places WHERE url='http://repository.edulll.gr/edulll/?locale=el'), (SELECT MAX(id) FROM moz_Bookmarks WHERE type='2'), 'Ψηφιακή βιβλιοθήκη')"

sqlite3 $DATABASE "INSERT INTO moz_places(url) VALUES ('http://www.study4exams.gr/')"
sqlite3 $DATABASE "INSERT INTO moz_bookmarks(type, fk, parent, title) Values (1, (SELECT id FROM moz_places WHERE url='http://www.study4exams.gr/'), (SELECT MAX(id) FROM moz_Bookmarks WHERE type='2'), 'Ψηφιακά εκπαιδευτικά βοηθήματα')"

sqlite3 $DATABASE "INSERT INTO moz_places(url) VALUES ('http://www.0-18.gr/')"
sqlite3 $DATABASE "INSERT INTO moz_bookmarks(type, fk, parent, title) Values (1, (SELECT id FROM moz_places WHERE url='http://www.0-18.gr/'), (SELECT MAX(id) FROM moz_Bookmarks WHERE type='2'), 'Ο συνήγορος του παιδιού')"

sqlite3 $DATABASE "INSERT INTO moz_places(url) VALUES ('http://internet-safety.sch.gr/')"
sqlite3 $DATABASE "INSERT INTO moz_bookmarks(type, fk, parent, title) Values (1, (SELECT id FROM moz_places WHERE url='http://internet-safety.sch.gr/'), (SELECT MAX(id) FROM moz_Bookmarks WHERE type='2'), 'Ασφάλεια στο Διαδίκτυο')"

sqlite3 $DATABASE "INSERT INTO moz_places(url) VALUES ('http://www.edutv.gr/')"
sqlite3 $DATABASE "INSERT INTO moz_bookmarks(type, fk, parent, title) Values (1, (SELECT id FROM moz_places WHERE url='http://www.edutv.gr/'), (SELECT MAX(id) FROM moz_Bookmarks WHERE type='2'), 'Εκπαιδευτική τηλεόραση')"

echo "Bookmarks inserted."


#########################################
###      Enable Bookmarks Toolbar     ###
#########################################

sed -i 's|</RDF:RDF>|<RDF:Description RDF:about="chrome://browser/content/browser.xul#PersonalToolbar" collapsed="false" />\n&|' /home/${CHILD}/.mozilla/firefox/*.default/localstore.rdf

sed -i 's|<RDF:Description RDF:about="chrome://browser/content/browser.xul">|&\n<NC:persist RDF:resource="chrome://browser/content/browser.xul#PersonalToolbar"/>|' /home/${CHILD}/.mozilla/firefox/*.default/localstore.rdf

echo "Bookmarks Toolbar enabled."



#########################################
###           Extensions              ###
#########################################

mkdir -p /usr/lib/firefox-addons/extensions/ 
cp -rf ${installDirectory}/firefox/extensions /usr/lib/firefox-addons
echo "Extensions inserted."


#########################################
###        Change Credentials         ###
#########################################
sudo chown -R ${CHILD} /home/${CHILD}/.mozilla


################################################################################
################# MODIFYING .profile FOR SD INSTALLATION ###################
################################################################################

if [ -f /home/${CHILD}/.profile ]
then
    PROFILE=/home/${CHILD}/.profile
    echo "# The following snippet is to enable the Security Distrubution"  >> $PROFILE
    echo "# It will run only on the first user login after installation of Security Distribution"  >> $PROFILE
    echo "if [ ! -f /home/${CHILD}/.securedistro/installed.lock ]" >> $PROFILE
    echo "then" >> $PROFILE
    echo "if [ -f /home/${CHILD}/.securedistro/addShortcutsNewUser.sh ]" >> $PROFILE
    echo "then" >> $PROFILE
    echo "/bin/bash /home/${CHILD}/.securedistro/addShortcutsNewUser.sh && rm /home/${CHILD}/.securedistro/addShortcutsNewUser.sh" >> $PROFILE
    echo "touch /home/${CHILD}/.securedistro/installed.lock" >> $PROFILE
    echo "fi" >> $PROFILE
    echo "fi" >> $PROFILE
else
    exit 1
fi
