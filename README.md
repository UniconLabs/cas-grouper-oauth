# CAS Grouper OAuth

## Mac OS X 10.9

### Installation and Initial Testing

*Note:* If you check out the git repository, run `./gradlew downloadGrouper` from the repository root and skip to step 5. You will also have to change some paths to match the directory you cloned into.

1. Install JDK 7 from [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Follow the instructions at that site to download and install Java SE Development Kit 7u45.
1. Download the tarball from [Google Drive](https://drive.google.com/file/d/0B20vWe3mpgaYTWRDRFhGUkRUblE/edit?usp=sharing).
1. Choose a location to install the package. For this demo, `~/Applications` will be used.
1. Unpack the tarball

	```shell
	mkdir ~/Applications
	cd ~/Applications
	tar xvfj ~/Downloads/cas-grouper.tar.bz2
	```
Note: If the directory `~/Applications` already exists, you might get an error
1. set `APP_HOME` and `JAVA_HOME`

	```shell
	cd ~/Applications/cas-grouper
	export APP_HOME=`pwd`
	export JAVA_HOME=`/usr/libexec/java_home`
	```
1. Install Grouper

	```shell
	cd $APP_HOME/grouper
	java -jar grouperInstaller.jar
	```

	Accept the defaults, when available.

	Notes:

	* You will get exceptions if you choose to run dos2unix and it's not installed; You can ignore these.
	* When asked, "Do you want to init the database (delete all existing grouper tables, add new ones)," choose `t`.
	* When prompted, "Enter the GrouperSystem password," use `letmein7`.

1. Check to make sure everything installed properly by visiting the (Grouper Site)[http://localhost:8080/grouper]. You should be able to log in with username `GrouperSystem` and password `letmein7`.
1. Build the application

	```shell
	cd $APP_HOME
	./gradlew clean build
	```

1. Load permissions sample data into Grouper.

	```shell
	cd $APP_HOME/grouper
	export GROUPER_HOME=$APP_HOME/grouper/grouper.apiBinary-2.1.5
	$GROUPER_HOME/bin/gsh sample.gsh
	```

1. Open 2 terminals to run the CAS server and the sample application

	* For CAS:

		```shell
		cd $APP_HOME/cas-grouper
		./gradlew :cas:run
		```

	* For sample resource:

		```shell
		cd $APP_HOME/cas-grouper
		./gradlew :resource:run
		```

1. Test initial configuration of App by going to (resource)[http://localhost:5050/resources/bill]. Click the link to get key. Log in with username `jj` and password `jj`. Click allow. You should be redirected back to the application with a message saying the now logged in user doesn't have permission.

1. Open a new terminal and add permissions to the resource to the user

	```shell
	cd ~/Applications/cas-grouper/grouper
	export GROUPER_HOME=~/Applications/cas-grouper/grouper/grouper.apiBinary-2.1.5
	$GROUPER_HOME/bin/gsh sample-read.gsh
	```

1. Test read permission of App by going to (resource)[http://localhost:5050/resources/bill]. Click the link to get key. Log in with username `jj` and password `jj`. Click allow. You should be redirected back to the application with a message saying the now logged in user has the read permission.

### Stopping and starting

#### Database

Start:
```sh
$GROUPER_HOME/db.sh start
```

Stop:
```sh
$GROUPER_HOME/db.sh stop
```

#### Grouper Web Application

Start:
```sh
$GROUPER_HOME/apache-tomcat-6.0.35/bin/catalina.sh start
```

Stop:
```sh
$GROUPER_HOME/apache-tomcat-6.0.35/bin/catalina.sh stop
```

#### CAS Server

This should be run in its own terminal

Start:
```sh
cd $APP_HOME
./gradlew -q :cas:run
```

Stop:
`ctrl-c` in the terminal

#### Resource

This should be run in its own termial

Start:
```sh
cd $APP_HOME
./gradlew -q :resource:run
```

Stop:
`ctrl-c` in the terminal



## Windows

To be written

## Linux

To be written
