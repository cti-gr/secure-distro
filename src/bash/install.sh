#!/bin/bash

installDirectory=/opt/secure-distro/bin
################ Installation Script for the Security Distribution #############


################################################################################
########################### CHECK IF RUN AS SUDO ###############################
################################################################################
if [ $(id -u) -ne 0 ]; then
	exit 1
fi

if [ -z "$1" ]; then
	INSTALLING=true
else
	INSTALLING=false
fi

echo "INSTALLING: " $INSTALLING



################################################################################
########################### PRIVOXY CONFIGURATION ##############################
################################################################################

# change localhost to 127.0.0. in privoxy configuration file and restart privoxy
sed -i 's/listen-address localhost:8118/listen-address 127.0.0.1:8118/g' /etc/privoxy/config
service privoxy restart


################################################################################
######################### DANSGUARDIAN CONFIGURATION ###########################
################################################################################

# make the necessary changes in dansguardian config file
# so that dansguardian uses privoxy
sed -i '5 s/^/#/' /etc/dansguardian/dansguardian.conf
sed -i 's/proxyport = 3128/proxyport = 8118/' /etc/dansguardian/dansguardian.conf

### allow certain file types that dansguardian bans by default ###
dansfileban=/etc/dansguardian/lists/bannedextensionlist
dansite=/etc/dansguardian/lists/bannedsitelist
dansurl=/etc/dansguardian/lists/bannedurllist

if [ -f $dansfileban ];
	then
	# compressed files
	sed -i 's/\^.gz/#.gz/' $dansfileban
	sed -i 's/\^.tgz/#.tgz/' $dansfileban
	sed -i 's/\^.zip/#.zip/' $dansfileban
	sed -i 's/\^.bz2/#.bz2/' $dansfileban
	sed -i 's/\^.tar/#.tar/' $dansfileban
	# office files
	sed -i 's/\^.doc/#.doc/' $dansfileban
	sed -i 's/\^.docx/#.docx/' $dansfileban
	sed -i 's/\^.xls/#.xls/' $dansfileban
	sed -i 's/\^.xlsx/#.xlsx/' $dansfileban
	sed -i 's/\^.pps/#.pps/' $dansfileban
	sed -i 's/\^.ppt/#.ppt/' $dansfileban
	sed -i 's/\^.pptx/#.pptx/' $dansfileban
	# media files
	sed -i 's/\^.mp3/#.mp3/' $dansfileban
	sed -i 's/\^.avi/#.avi/' $dansfileban
	sed -i 's/\^.rar/#.rar/' $dansfileban
	sed -i 's/\^.asf/#.asf/' $dansfileban
	sed -i 's/\^.asx/#.asx/' $dansfileban
	sed -i 's/\^.ogg/#.ogg/' $dansfileban
	sed -i 's/\^.mpeg/#.mpeg/' $dansfileban
	sed -i 's/\^.mpg/#.mpg/' $dansfileban
else
	echo "Cannot find the bannedextensionlist file, Dansguardian settings will not be updated..."
fi

### move the blacklists to the appropriate folder
cp -rf ${installDirectory}/DG/blacklists/* /etc/dansguardian/lists/blacklists
for dir in /etc/dansguardian/lists/blacklists/*
	do
		if [ -d $dir ];
		then
			echo "changing mode to 755 for $dir"
			chmod 755 $dir
				for file in $dir/*
					do
						if [ -f $file ];
						then
							echo "changing mode to 644 for $file"
							chmod 644 $file
						fi
					done
		fi
	done

### move the greek language folder
cp -rf ${installDirectory}/DG/greek /etc/dansguardian/languages/

## add command to delete blacklists folder

## #changing DG language to greek
sed -i "s/language = 'ukenglish'/language = 'greek'/" /etc/dansguardian/dansguardian.conf

### enable the appropriate blacklists ###
if [ -f $dansite ];
then
	echo "File $dansite found - proceeding to configurations"
	grep -q '^.Include</etc/dansguardian/lists/blacklists/adulteducation/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/adulteducation/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/aggressive/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/aggressive/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/beerliquorinfo/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/beerliquorinfo/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/dating/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/dating/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/downloads/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/downloads/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/drugs/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/drugs/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/fortunetelling/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/fortunetelling/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/gambling/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/gambling/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/hacking/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/hacking/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/lingerie/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/lingerie/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/military/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/military/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/models/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/models/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/movies/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/movies/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/porn/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/porn/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/spyware/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/spyware/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/tracker/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/tracker/domains>' >> $dansite
	grep -q '^.Include</etc/dansguardian/lists/blacklists/violence/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/violence/domains>' >> $dansite	
	grep -q '^.Include</etc/dansguardian/lists/blacklists/warez/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/warez/domains>' >> $dansite	
	grep -q '^.Include</etc/dansguardian/lists/blacklists/weapons/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/weapons/domains>' >> $dansite	
	grep -q '^.Include</etc/dansguardian/lists/blacklists/custom/domains>' $dansite || echo '.Include</etc/dansguardian/lists/blacklists/custom/domains>' >> $dansite
else
	echo "Cannot find the bannedextensionlist file, Dansguardian settings will not be updated..."
fi
if [ -f $dansurl ];
then
	echo "File $dansurl found - proceeding to configurations"
	grep -q '^.Include</etc/dansguardian/lists/blacklists/adulteducation/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/adulteducation/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/aggressive/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/aggressive/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/beerliquorinfo/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/beerliquorinfo/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/dating/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/dating/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/downloads/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/downloads/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/drugs/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/drugs/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/fortunetelling/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/fortunetelling/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/gambling/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/gambling/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/hacking/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/hacking/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/lingerie/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/lingerie/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/military/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/military/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/models/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/models/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/movies/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/movies/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/porn/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/porn/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/spyware/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/spyware/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/tracker/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/tracker/urls>' >> $dansurl
	grep -q '^.Include</etc/dansguardian/lists/blacklists/violence/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/violence/urls>' >> $dansurl	
	grep -q '^.Include</etc/dansguardian/lists/blacklists/warez/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/warez/urls>' >> $dansurl	
	grep -q '^.Include</etc/dansguardian/lists/blacklists/weapons/urls>' $dansurl || echo '.Include</etc/dansguardian/lists/blacklists/weapons/urls>' >> $dansurl
else
	echo "Cannot find the bannedurllist file, Dansguardian settings will not be updated..."
fi

################################################################################
######################### PROTOS SENSOR INSTALLATION ###########################
################################################################################
cd ${installDirectory}/firelog
source ${installDirectory}/firelog/install_firelog.sh

################################################################################
########################## CREATING SD LOCAL FOLDER ############################
################################################################################
if [ "$INSTALLING" = true ]; then
	echo
	for HOME_U in /home/*?; do

		SUDO_USER=$( basename ${HOME_U} )
		awk -F':' '/sudo/{print $4}' /etc/group | grep -q ${SUDO_USER} || continue
		### Creating Security Distribution local hidden folder
		SD_DIR="/home/${SUDO_USER}/.securedistro"

	
		if [ ! -d $SD_DIR ];
			then
			mkdir -p $SD_DIR
			touch $SD_DIR/application.properties
			echo "country=GR" >> $SD_DIR/application.properties
			echo "language=el" >> $SD_DIR/application.properties
			echo "protectionStatus=false" >> $SD_DIR/application.properties
			chown -R ${SUDO_USER}:${SUDO_USER} $SD_DIR
		fi

		### Step 3b: Setting up wallpaper and shortcuts for next reboot
		cp -rf 	${installDirectory}/addShortcuts.sh $SD_DIR/addShortcuts.sh
		chown ${SUDO_USER}:${SUDO_USER} $SD_DIR/addShortcuts.sh
		# moving wallpaper and changing ownership
		cp ${installDirectory}/sd-wallpaper.png /home/${SUDO_USER}/.securedistro/
		chown ${SUDO_USER}:${SUDO_USER} $SD_DIR/sd-wallpaper.png
	
	################################################################################
	######################### FIREFOX CUSTOMIZATION ################################
	################################################################################


		FIREFOX_HOME="/home/${SUDO_USER}/.mozilla/firefox"	
		killall -9 firefox
		if [ $?==0 ];		
		then

			if [ -d $FIREFOX_HOME ]; then 
				# find the prefs.js file where proxy settings are stored
				FIREFOX_HOME="/home/${SUDO_USER}/.mozilla/firefox"
				CURRENT_DIR=$(pwd)
				cd $FIREFOX_HOME
				SETTINGS_DIR=$(ls | grep default)
				PREFS_FILE="${FIREFOX_HOME}/${SETTINGS_DIR}/prefs.js"			
			else
				echo "Firefox dir unavailable - not proceeding with firefox customization"
			fi

		else
			echo "Unable to shut down firefox / configuration not completed"
		fi

		mkdir -p /home/${SUDO_USER}/.config/autostart/

		###########################################
		#    Copy missing files for existing User   #
		###########################################

		sudo yes n | cp -i -r ${installDirectory}/firefox/.mozilla/ /home/${SUDO_USER}/ 2>/dev/null


		#########################################
		###           Home Page               ###
		#########################################
		#HP_FILE=/home/${SUDO_USER}/.mozilla/firefox/*.default/prefs.js
		HP_PATTERN='"browser.startup.homepage"'
		HP_LINE='user_pref("browser.startup.homepage", "http://secure-distro.cti.gr/");'

		# echo "HP FILE IS: " $PREFS_FILE

		if grep -q $HP_PATTERN $PREFS_FILE;
		 then
			 sed -i 's|\("browser.startup.homepage",\) "\(.*\)"|\1 "http://secure-distro.cti.gr/"|' $PREFS_FILE
			 echo "Homepage changed."
		 else
			 echo $HP_LINE >> $PREFS_FILE
			 echo "" >> $PREFS_FILE
			 echo "Homepage inserted."
		fi


		#########################################
		###           Bookmarks               ###
		#########################################
		DATABASE=${SETTINGS_DIR}/places.sqlite

		# echo "DATABASE IS: " $DATABASE

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

		sed -i 's|</RDF:RDF>|<RDF:Description RDF:about="chrome://browser/content/browser.xul#PersonalToolbar" collapsed="false" />\n&|' /home/${SUDO_USER}/.mozilla/firefox/*.default/localstore.rdf

		sed -i 's|<RDF:Description RDF:about="chrome://browser/content/browser.xul">|&\n<NC:persist RDF:resource="chrome://browser/content/browser.xul#PersonalToolbar"/>|' /home/${SUDO_USER}/.mozilla/firefox/*.default/localstore.rdf

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
		sudo chown -R ${SUDO_USER} /home/${SUDO_USER}/.mozilla

		################################################################################

		DESKTOP_FILE="/home/${SUDO_USER}/.config/autostart/dtinstall.desktop"
		touch $DESKTOP_FILE
		echo "[Desktop Entry]" > $DESKTOP_FILE
		echo "Type=Application" >> $DESKTOP_FILE
		echo "Exec=${SD_DIR}/addShortcuts.sh" >> $DESKTOP_FILE
		echo "Version=0.80" >> $DESKTOP_FILE
		echo "Hidden=false" >> $DESKTOP_FILE
		echo "NoDisplay=false" >> $DESKTOP_FILE
		echo "Terminal=false" >> $DESKTOP_FILE
		chown ${SUDO_USER}:${SUDO_USER} $DESKTOP_FILE

	done
fi
