# RCS: 
# $Id: Makefile,v 1.2 2011/01/20 23:06:38 warner Exp $
#
# Macros:

SHELL := /bin/tcsh -f

JAVAC = javac
JFLAGS = -classpath ../lib/Scheduler.jar:../lib/mysql-connector-java-5.1.15-bin.jar:../javaUFProtocol/javaUFProtocol.jar:../javaUFLib/javaUFLib.jar:../javaMMTLib/javaMMTLib.jar  -d ./ -deprecation

JPKG := CCROP
JMAIN:= ufCCrop

JSRC =	$(JMAIN).java  CCropClientThread.java  CCropAgent.java  CCropRealTime.java  Zone.java  ETZone.java  LFZone.java

JCLASS := $(patsubst %.java, $(JPKG)/%.class, $(JSRC))

RCSLIST = Makefile  $(JSRC) $(JMAIN)

#targets:

all: install

$(JPKG)/%.class: $(JSRC)
	$(JAVAC) $(JFLAGS) $<

install: initpub build $(JPKG).jar
	source ../.ufcshrc
	cp -f -p $(JPKG).jar $(BMPINSTALL)/bin
	cp -f -p $(JMAIN) $(BMPINSTALL)/bin
	-@echo installed java CCROP agent.

build: $(JSRC)
	$(JAVAC) $(JFLAGS) $(JSRC)

$(JPKG).jar: $(JCLASS)
	echo 'Main-Class: $(JPKG).$(JMAIN)' >! manifest
	echo 'Class-Path: $(JPKG).jar ../lib/Scheduler.jar ../lib/mysql-connector-java-5.1.15-bin.jar ../lib/javaUFProtocol.jar ../lib/javaUFLib.jar ../lib/javaMMTLib.jar' >> manifest
	jar cmf manifest $@ $(JPKG) 

show:
	-@echo JAVAC = $(JAVAC)
	-@echo JFLAGS =$(JFLAGS)
	-@echo RCSLIST = $(RCSLIST)
	-@echo JSRC = $(JSRC)
	-@echo JCLASS = $(JCLASS)
	-@echo JPKG = $(JPKG)

init: $(RCSLIST)
	source ../.ufcshrc
	if ( ! -e $(JPKG) ) mkdir -p $(JPKG)

initpub:
	source ../.ufcshrc
	cp -pf ../.ufcshrc $(BMPINSTALL)
	mkdir -p $(BMPINSTALL)/bin

clean:
	-$(RM) -r $(JPKG)  *.jar  *.class
