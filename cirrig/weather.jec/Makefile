# RCS: 
# $Id: Makefile,v 1.3 2011/01/20 23:08:51 warner Exp $
#
# Macros:

SHELL := /bin/tcsh -f

ifndef GCCID
  GCCID = $(shell ../.gccv)
endif

ifndef RCSMASTER
  RCSMASTER := $(shell /bin/ls -l .. | grep RCS | awk '{print $$11}' | sed 's^/RCS*^^')
endif

#include ../.makerules

JPKG := wthjec
JSRC := src
JDATA := data
JAVAC = javac
JFLAGS = -classpath ../lib/Scheduler.jar:../javaUFProtocol/javaUFProtocol.jar:../javaUFLib/javaUFLib.jar:../javaMMTLib/javaMMTLib.jar  -d ./ -deprecation

RCSLIST = Makefile ufstartwthjec

JDDSRC := $(JSRC)/wthjec.java		\
	$(JSRC)/mjecError.java		\
	$(JSRC)/AeSimpleSHA1.java	\
	$(JSRC)/AgentConfig.java	\
	$(JSRC)/HourlyIterator.java	\
	$(JSRC)/JPanelLogin.java	\
	$(JSRC)/JPanelWeather.java	\
	$(JSRC)/MJECPlot.java		\
	$(JSRC)/MJECPlotPanel.java	\
	$(JSRC)/StatusThread.java	\
	$(JSRC)/WeatherFileConfig.java	\
	$(JSRC)/WeatherRealTime.java	\
	$(JSRC)/WthjecFrame.java	

$(JPKG)/%.class: $(JSRC)/%.java
	$(JAVAC) $(JFLAGS) $<

#targets:

install: initpub wthjec.jar newparams
	cp -f -p wthjec.jar $(BMPINSTALL)/wthjec
	cp -f -p ufstartwthjec $(BMPINSTALL)/bin
	cd $(BMPINSTALL)/bin; rm ufwthjec; ln -s ufstartwthjec ufwthjec
	-@echo installed wthjec.jar and param files

newparams:
	cp -f -p $(JDATA)/*.* $(BMPINSTALL)/wthjec

.ufwthjec: $(JDDSRC)
	$(JAVAC) $(JFLAGS) $(JDDSRC)

wthjec.jar: .ufwthjec
	echo 'Main-Class: wthjec.wthjec' >! manifest
	echo 'Class-Path: wthjec.jar ../lib/Scheduler.jar ../lib/javaUFProtocol.jar ../lib/javaUFLib.jar ../lib/javaMMTLib.jar' >> manifest
	jar cmf manifest $@ $(JPKG) 

show:
	-@echo OS= $(OS)
	-@echo OS_VERSION= $(OS_VERSION)
	-@echo JAVAC = $(JAVAC)
	-@echo JFLAGS =$(JFLAGS)
	-@echo RCSLIST = $(RCSLIST)
	-@echo JDDSRC= $(JDDSRC)
	-@echo JPKG= $(JPKG)
	-@echo RCSMASTER= $(RCSMASTER)

init:
	mkdir -p $(JSRC) $(JDATA);

initpub:
	source ../.ufcshrc
	mkdir -p $(BMPINSTALL)/wthjec

cleanpub:
	source ../.ufcshrc; $(RM) -r $(BMPINSTALL)/wthjec

pubclean: cleanpub initpub
	@echo cleaned and re-initialized $(BMPINSTALL)/wthjec

clean:
	-$(RM) -rf $(JPKG)  *.jar  *.class  *~ 
