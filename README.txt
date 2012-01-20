To build TorrentScout (release 2.0)  yourself and run it, you will need:

Java 1.6 or higher (Java Download)
NetBeans 7.0 or higher (netbeans.org)
To compile it for the first time:

Open Project TorrentScout in NetBeans 7
Open TorrentScout/important files/Netbeams Platform Config in the Project explorer
(the file TorrentScout/nbproject/platform.properties)
Change the line with harness.dir to the project path/harness
Build and run it!
 
If your developer machine has less than 4 GB of memory available 
and you want to run TorrentScout via NetBeans:
Edit the file harness/run.xml
Go to the line with
<property name="run.args.common" value='--userdir "${test.user.dir}" -J-Dnetbeans.logger.console=true -J-ea -J-Xms512m -J-Xmx4096m'/>
Change the memory setting Xmx4096m to a lower value (such as Xmx2048m)
 
To give NetBeans itself more or less memory:

Open the folder where Netbeans 7 is installed
Go to etc and open the file netbeans.conf
Edit the line with:
netbeans_default_options="-J-client -J-Xss2m -J-Xms512m -J-Xmx3900m -J-XX:PermSize=32m -J-XX:MaxPermSize=384m -J-Dapple.laf.useScreenMenuBar=true -J-Dapple.awt.graphics.UseQuartz=true -J-Dsun.java2d.noddraw=true"
Here again you can change the memory you want to give NetBeans (here: Xmx3900m)
In that same file you can also specify the default location for your jdk
 
If you ever want to deploy it yourself as a web start app, you will also need additional files 
(a modified jnlp-servlet.jar, for instance), which are now in the java folder (inside the TorrentScout project folder)