# NIO Server

NIO Chat Server
https://gist.github.com/Botffy/3860641

Сетевое программирование с Сокетами и Каналами
http://javatutor.net/books/tiej/socket#_Toc39472939

Сокеты в Java
https://www.youtube.com/watch?v=fFekJ7myksk

Как установить Java на CentOS
https://www.youtube.com/watch?v=CSxCJlOivuo

# Установка Java на Linux

uname -r						        // Find out the system bit

3.10.0-1062.1.1.el7.x86_64

cd /usr

mkdir java						      // Create /java in /usr

(Download from oracle.com JDK archive jdk-11.0.5_linux-x64_bin.rpm)

(Download through FileZilla to the server in the folder /usr/java archive jdk-11.0.5_linux-x64_bin.rpm)

ls

jdk-11.0.5_linux-x64_bin.rpm

rpm -ivh jdk-11.0.5_linux-x64_bin.rpm

################################# [100%]
  
java -version

java version "11.0.5" 2019-10-15 LTS

javac -version

javac 11.0.5

tmux new -s s3					    // Create new Session (name s3)

tmux attach -t s3				    // In to Session s3

cd /home/java

javac NioChatServer.java		// Compile

java NioChatServer 3000			// Run Server port 3000

Run NIO Server 192.168.0.5:4041
