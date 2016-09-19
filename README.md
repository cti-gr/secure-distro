The Security Distribution is a debian meta-package developed by Computer Technology Institute, targeting Ubuntu 14.04.
The purpose of the Security Distribution is to create a safe environment for children to surf and browse the web.
When installed the Security Distribution package will install a list of tools and applications (some of which have been developed by CTI) and will proceed to specific configurations that are all related to safe usage of home and school computer.

**Installation Instructions**

Installation Instructions
The Security Distribution has been tested on Ubuntu 14.04. This does not exclude other Ubuntu versions, but no testing has been performed on releases other than 14.04.

The version which is available through the CTI repository is still in testing process.

In order to install the Security Distribution Release Candidate, we need to perform the following steps in the command line:

1. sudo add-apt-repository ppa:webupd8team/java

This will add the above repository which is necessary for the installation of Oracle Java 8 package (dependency of Security Distribution)

 2. sudo add-apt-repository 'deb http://secure-distro.cti.gr/debs binary/'

This command will add the security distribution repository set up and maintained by CTI, from which the security-distribution package and some other dependencies will be downloaded and installed

 3. sudo apt-get update

To download / update the package lists 

 4. sudo apt-get install secure-distro

To perform the actual installation of the Security Distribution package.

 

During the installation, the user will be prompted to accept the Oracle Java Licence agreement. 

After the installation has finished, the user should perform a system reboot and the Security Distribution will be ready to use.

For more information please visit [http://secure-distro.cti.gr]()