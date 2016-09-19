#!/bin/bash

list=`gsettings get com.canonical.Unity.Launcher favorites`
dtapps="application:\/\/firefox.desktop', 'application:\/\/gufw.desktop', 'application:\/\/avgui.desktop', 'application:\/\/bleachbit-root.desktop', 'application:\/\/keepass2.desktop', 'application:\/\/SecureDistro-sudo.desktop"
newlist=`echo $list | sed s/]/", '${dtapps}']"/`
gsettings set com.canonical.Unity.Launcher favorites "$newlist"
gsettings set org.gnome.desktop.background picture-uri file:///home/${USER}/.securedistro/sd-wallpaper.png
