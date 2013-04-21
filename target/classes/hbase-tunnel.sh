#!/bin/sh

#lsof -i | grep LISTEN

##kill `ps ax | grep 'ssh \-f' | awk '{print $1}'`
#xmolnarm2
ssh -f -L 2181:147.175.146.112:2181 molnar@147.175.146.112 -N 
ssh -f -L 59848:147.175.146.112:59848 molnar@147.175.146.112 -N 
ssh -f -L 34063:147.175.146.112:34063 molnar@147.175.146.112 -N 
ssh -f -L 60030:147.175.146.112:60030 molnar@147.175.146.112 -N 
ssh -f -L 60010:147.175.146.112:60010 molnar@147.175.146.112 -N 

###
ssh -f -L 60010:147.175.159.131:60010 molnar@147.175.159.131 -N


java -cp news-recommender-0.0.1-SNAPSHOT-jar-with-dependencies.jar -Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl com.fiit.lusinda.rss.RssReader feed-settings-sme.xml
R

	ssh molnar@147.175.159.131 server
ssh molnar@147.175.146.112 cluster

 
 600 5302
sudo cp reccomender.war /home/valencik/apache-tomcat-7.0.27/webapps/
sudo cp -r /tmp/deps/* /home/valencik/apache-tomcat-7.0.27/webapps/reccomender/WEB-INF/lib/
sudo vim /home/valencik/apache-tomcat-7.0.27/webapps/reccomender/WEB-INF/config/database.yml

1625

INSERT INTO evaluated_articles(article_id) SELECT id FROM sme_sk.articles where category like 'Zahran%ie' order by published_at LIMIT 600
truncate evaluated_articles
 SELECT MAX(`evaluated_articles`.`id`) AS max_id FROM `evaluated_articles` WHERE (rating_id is null)
 
 insert into ratings(topic_chain_value,updated_at,created_at) values(-1,'2012-05-03-13:04:16','2012-05-03-13:04:16')
 
 Buildovanie
 cd /Users/teo/Documents/workspace-new/news-recommender 
 mvn package
 
 deploy lokalne
 cp -r ~/Documents/workspace-new/news-recommender/target/deps /tmp
 cp -R ~/DP/resources/* /tmp
 cp ~/Documents/workspace-new/news-recommender/target/news-recommender-0.0.1-SNAPSHOT.jar /tmp
 
 sk_sme_áŽf_misie_OSN_priznal_udrìiavanie_tajnùch_kontaktov_s_Talibanom
 
 
 Nasadzovanie>
 
 jruby-1.6.5.1 -S warble
 
 copy to server
 
 copy newsRecommender to server
 cp /home/molnar/news-recommender-0.0.1-SNAPSHOT.jar /tmp
 
 v adresari ~/lib su libky ktore treba nakopirovat
 
 
 copy structure.sql
 
 mysql -u molnar -phesielko -h localhost sme_sk < ~/sql/structure.sql
 mysql -u molnar -phesielko -h localhost sme_sk 
 INSERT INTO evaluated_articles(article_id) SELECT id FROM sme_sk.articles where category like 'Zahran%ie' order by published_at LIMIT 600;
 
 undeploy reccomender.war
 #sudo rm -rf /home/valencik/apache-tomcat-7.0.27/webapps/reccomender*
 
 sudo cp /home/molnar/reccomender.war /home/valencik/apache-tomcat-7.0.27/webapps/
 ls /home/valencik/apache-tomcat-7.0.27/webapps/
 sudo rm -rf /home/valencik/apache-tomcat-7.0.27/webapps/reccomender/WEB-INF/lib/*.jar
 
sudo cp -r ~/lib/* /home/valencik/apache-tomcat-7.0.27/webapps/reccomender/WEB-INF/lib/
		#sudo vim /home/valencik/apache-tomcat-7.0.27/webapps/reccomender/WEB-INF/config/database.yml
sudo rm -rf /home/valencik/apache-tomcat-7.0.27/webapps/reccomender/WEB-INF/config/database.yml
sudo cp /home/molnar/database.yml /home/valencik/apache-tomcat-7.0.27/webapps/reccomender/WEB-INF/config

sudo /etc/init.d/tomcat stop

sudo /etc/init.d/tomcat start






Statistky:

nazvy zhlukov	:
pocet vsetkych ohodnotenych nazvov
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='clusters';

pocet vsetkych dobre ohodnotenych zhlukov
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='clusters'; and k.rate=0;
mojich
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='clusters'; and k.rate=0 and k.type='my';
ich
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='clusters'; and k.rate=0 and k.type='theirs';

pocet vsetkych zle ohodnotenych zhlukov
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='clusters'; and k.rate=1;
mojich
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='clusters'; and k.rate=1 and k.type='my';
ich
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='clusters'; and k.rate=1 and k.type='theirs';




klucove slova:
pocet vsetkych klucovych slov
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='keywords';

pocet vsetkych dobre 
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='keywords' and k.rate=0;
mojich
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='keywords' and k.rate=0 and k.type='my';
ich
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='keywords' and k.rate=0 and k.type='theirs';

pocet vsetkych zle
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='keywords' and k.rate=1;
mojich
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='keywords' and k.rate=1 and k.type='my';
ich
select count(name) from keywords as k join keyword_ratings kr on(k.keywordRating_id=kr.id) where kr.type='keywords' and k.rate=1 and k.type='theirs';


