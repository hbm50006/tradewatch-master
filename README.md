# tradewatch

Path of Exile Trade Indexer - This was private and now its public. It use poe.trade to find lowest prices on items and notices you immediately so you can flip the item.

You can change the IRC channel under \tradewatch-master\src\main\scala\org\trade\TraderMain.scala

    var ircChannel = "#tradewatch" 
    

This file also is where you can change the IRC server and bot name if you wish.



An Example of this bot running on irc:

This is what you will see:

[01:19] <tradehunter> Alpha's Howl Sinner Tricorne | Boomzilla4 | 1.5 exalted | Alpha's Howl | http://poe.trade/search/okotabokuhubas | @Boomzilla Hey, I'll buy your Alpha's Howl Sinner Tricorne for your buyout of 1.5 exalted on Warbands

[01:29] <tradehunter> Maelström Staff | geraldsummers | 1 fusing | 6 sockets | http://poe.trade/search/abaunikutawihu | @SaylightDave Hey, I'll buy your Maelström Staff for your buyout of 1 fusing on Warbands

[01:29] <tradehunter> Rathpith Globe Titanium Spirit Shield | TheRisenOne | 5 exalted | Rathpith Globe | http://poe.trade/search/imosikotinahig | @RizensAwakening Hey, I'll buy your Rathpith Globe Titanium Spirit Shield for your buyout of 5 exalted on Warbands

[01:35] <tradehunter> Rathpith Globe Titanium Spirit Shield | icexxx | 4.2 exalted | Rathpith Globe | http://poe.trade/search/imosikotinahig | @GGrambo Hey, I'll buy your Rathpith Globe Titanium Spirit Shield for your buyout of 4.2 exalted on Warbands

[01:35] <tradehunter> Rathpith Globe Titanium Spirit Shield | downhilldemon | 4 exalted | Rathpith Globe | http://poe.trade/search/imosikotinahig | @tradingonly Hey, I'll buy your Rathpith Globe Titanium Spirit Shield for your buyout of 4 exalted on Warbands

[01:35] <tradehunter> Rathpith Globe Titanium Spirit Shield | TheRisenOne | 5 exalted | Rathpith Globe | http://poe.trade/search/imosikotinahig | @RizensAwakening Hey, I'll buy your Rathpith Globe Titanium Spirit Shield for your buyout of 5 exalted on Warbands





Basically it's finding the lowest price from poe.trade and notifies you in the specified IRC channel of the price.

This is a picture of the Logs from sales made using this bot:
http://i.imgur.com/tC41yCy.png


Basic Tutorial:

I will try to give a better tutorial or 'HOW TO' guide here.

So you're goal here is to take the tradewatch-master Github and clone it to your PC. Then compile it into a .JAR file and run it.

You can use ScalaIDE to make sure your project is built how you want it - ScalaIDE from Eclipse is mostly used to manage the build files.

~~~~~~~GUIDE~~~~~~~ (for windows users)

You will need to download the 'Simple Build Tool' http://www.scala-sbt.org/download.html

On windows its an msi file and you will open sbt.bat which opens sbt in a command prompt window.
From here you can run sbt commands like 'sbt compile'

You should read over these pages in order to understand sbt compiling as it might be different for each person depending on where you installed sbt and where you saved tradewatch.

Page 1:
https://www.safaribooksonline.com/library/view/scala-cookbook/9781449340292/ch18s01.html

Page 2:
https://www.safaribooksonline.com/library/view/scala-cookbook/9781449340292/ch18s02.html

Page 3: (most important)
https://www.safaribooksonline.com/library/view/scala-cookbook/9781449340292/ch18s03.html


When you open sbt.bat and you see that its pointing to the correct directory /tradewatch-master/
Next:
You can type 'sbt compile' which compiles the main sources (in src/main/scala and src/main/java directories).
Next:
You can type 'sbt package' which creates a jar file containing the files in src/main/resources and the classes compiled from src/main/scala and src/main/java.
Next:
You can type 'sbt run packagename.jar'

If all the dependencies have been downloaded by sbt correctly, then your project will run through the .JAR specified from the sbt run command.

