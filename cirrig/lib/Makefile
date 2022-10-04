# RCS: 
# $Id: Makefile,v 1.5 2009/09/12 09:50:24 varosi Exp $
#
# Macros:

SHELL := /bin/tcsh -f

RCSLIST = Makefile  jamod.jar  README.txt  LICENSE.txt

#targets:

install: initpub
	cp -pf jamod.jar $(BMPINSTALL)/lib
	cp -pf mysql-connector-java-5.1.15-bin.jar $(BMPINSTALL)/lib
	cp -pf sqlite-jdbc-3.8.10.1.jar $(BMPINSTALL)/lib
	cp -pf Scheduler.jar $(BMPINSTALL)/lib
	cp -pf mail.jar $(BMPINSTALL)/lib
	rm -f javaUFProtocol.jar;  ln -s ../javaUFProtocol/javaUFProtocol.jar
	rm -f javaUFLib.jar;  ln -s ../javaUFLib/javaUFLib.jar
	rm -f javaUFCORBA.jar;  ln -s ../javaUFCORBALib/javaUFCORBA.jar
	rm -f javaJCILib.jar;  ln -s ../javaJCILib/javaJCILib.jar
	rm -f javaJDDLib.jar;  ln -s ../javaJDDLib/javaJDDLib.jar
	-@echo installed jamod.jar

init: $(RCSLIST)
	source ../.ufcshrc

initpub:
	source ../.ufcshrc
	cp -pf ../.ufcshrc $(BMPINSTALL)
	mkdir -p $(BMPINSTALL)/lib

clean:

