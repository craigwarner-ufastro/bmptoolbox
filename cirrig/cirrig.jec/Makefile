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

JPKG := cjec
JSRC := src
JDATA := data
JAVAC = javac
JFLAGS = -classpath ../javaUFProtocol/javaUFProtocol.jar:../javaUFLib/javaUFLib.jar:../javaMMTLib/javaMMTLib.jar  -d ./ -deprecation

RCSLIST = Makefile ufstartcjec

JDDSRC := $(JSRC)/cjec.java		\
	$(JSRC)/cjecError.java		\
	$(JSRC)/AeSimpleSHA1.java	\
	$(JSRC)/AgentConfig.java	\
	$(JSRC)/CirrigJecButtonPanel.java	\
        $(JSRC)/JPanelCounters.java     \
        $(JSRC)/JPanelIndividualCounter.java    \
	$(JSRC)/JPanelIrrigator.java	\
	$(JSRC)/JPanelIrrigatorOptions.java	\
	$(JSRC)/JPanelLogin.java	\
	$(JSRC)/JPanelOutlet.java	\
	$(JSRC)/JPanelZoneGroup.java	\
	$(JSRC)/MJECApplyButton.java	\
	$(JSRC)/MJECButton.java		\
	$(JSRC)/MJECPlot.java		\
	$(JSRC)/MJECPlotPanel.java	\
	$(JSRC)/CjecFrame.java		\
        $(JSRC)/StatusThread.java

$(JPKG)/%.class: $(JSRC)/%.java
	$(JAVAC) $(JFLAGS) $<

#targets:

install: initpub cjec.jar 
	cp -f -p cjec.jar $(BMPINSTALL)/cjec
	cp -f -p ufstartcjec $(BMPINSTALL)/bin
	cd $(BMPINSTALL)/bin; rm ufcjec; ln -s ufstartcjec ufcjec
	-@echo installed cjec.jar and param files

newparams:
	cp -f -p $(JDATA)/*.* $(BMPINSTALL)/cjec

.ufcjec: $(JDDSRC)
	$(JAVAC) $(JFLAGS) $(JDDSRC)

cjec.jar: .ufcjec
	echo 'Main-Class: cjec.cjec' >! manifest
	echo 'Class-Path: cjec.jar ../lib/javaUFProtocol.jar ../lib/javaUFLib.jar ../lib/javaMMTLib.jar' >> manifest
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
	mkdir -p $(BMPINSTALL)/cjec

cleanpub:
	source ../.ufcshrc; $(RM) -r $(BMPINSTALL)/cjec

pubclean: cleanpub initpub
	@echo cleaned and re-initialized $(BMPINSTALL)/cjec

clean:
	-$(RM) -rf $(JPKG)  *.jar  *.class  *~ 
