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
[01:39] <tradehunter> Rathpith Globe Titanium Spirit Shield | xXFINNXx | 4 exalted | Rathpith Globe | http://poe.trade/search/imosikotinahig | @BreequinnII Hey, I'll buy your Rathpith Globe Titanium Spirit Shield for your buyout of 4 exalted on Warbands
[02:09] <tradehunter> Rathpith Globe Titanium Spirit Shield | xXFINNXx | 4 exalted | Rathpith Globe | http://poe.trade/search/imosikotinahig | @BreequinnII Hey, I'll buy your Rathpith Globe Titanium Spirit Shield for your buyout of 4 exalted on Warbands
[02:29] <tradehunter> Rathpith Globe Titanium Spirit Shield | Minihunter | 4.1 exalted | Rathpith Globe | http://poe.trade/search/imosikotinahig | @AVentorsMadeMeDoThis Hey, I'll buy your Rathpith Globe Titanium Spirit Shield for your buyout of 4.1 exalted on Warbands
[02:29] <tradehunter> The Whispering Ice Vile Staff | GegMal | 2 exalted | The Whispering Ice | http://poe.trade/search/nokomohakoorit | @Sleepyfollow Hey, I'll buy your The Whispering Ice Vile Staff for your buyout of 2 exalted on Warbands
[02:39] <tradehunter> Soul Taker Siege Axe | Lithandus | 3 exalted | Soul Taker | http://poe.trade/search/uzumahoteuruna | @Lithandus Hey, I'll buy your Soul Taker Siege Axe for your buyout of 3 exalted on Warbands
[02:39] <tradehunter> Rathpith Globe Titanium Spirit Shield | Minihunter | 4.1 exalted | Rathpith Globe | http://poe.trade/search/imosikotinahig | @KevinKerryMe Hey, I'll buy your Rathpith Globe Titanium Spirit Shield for your buyout of 4.1 exalted on Warbands
[02:49] <tradehunter> Rathpith Globe Titanium Spirit Shield | Minihunter | 4.1 exalted | Rathpith Globe | http://poe.trade/search/imosikotinahig | @AVentorsMadeMeDoThis Hey, I'll buy your Rathpith Globe Titanium Spirit Shield for your buyout of 4.1 exalted on Warbands
[02:49] <tradehunter> Maelström Staff | geraldsummers | 1 fusing | 6 sockets | http://poe.trade/search/abaunikutawihu | @DaylightSavings Hey, I'll buy your Maelström Staff for your buyout of 1 fusing on Warbands
[02:50] <tradehunter> Rathpith Globe Titanium Spirit Shield | iLackSkill89 | 4 exalted | Rathpith Globe | http://poe.trade/search/imosikotinahig | @iLackSkillLuvBro Hey, I'll buy your Rathpith Globe Titanium Spirit Shield for your buyout of 4 exalted on Warbands




Basically it's finding the lowest price from poe.trade and notifies you in the specified IRC channel of the price.

This is a picture of the Logs from sales made using this bot:
http://i.imgur.com/tC41yCy.png



