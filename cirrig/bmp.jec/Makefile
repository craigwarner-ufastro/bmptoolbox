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

JPKG := bmpjec
JSRC := src
JDATA := data
JAVAC = javac
JFLAGS = -classpath ../javaUFProtocol/javaUFProtocol.jar:../javaUFLib/javaUFLib.jar:../javaMMTLib/javaMMTLib.jar  -d ./ -deprecation

RCSLIST = Makefile ufstartbmpjec

JDDSRC := $(JSRC)/mjec.java		\
	$(JSRC)/mjecError.java		\
	$(JSRC)/AeSimpleSHA1.java	\
	$(JSRC)/AgentConfig.java	\
	$(JSRC)/BmpJecButtonPanel.java	\
	$(JSRC)/JPanelPLC.java		\
	$(JSRC)/JPanelLogin.java	\
	$(JSRC)/MJECApplyButton.java	\
	$(JSRC)/MJECButton.java		\
	$(JSRC)/MJECPlot.java		\
	$(JSRC)/MJECPlotPanel.java	\
	$(JSRC)/MjecFrame.java		\
        $(JSRC)/StatusThread.java

$(JPKG)/%.class: $(JSRC)/%.java
	$(JAVAC) $(JFLAGS) $<

#targets:

install: initpub bmpjec.jar newparams
	cp -f -p bmpjec.jar $(BMPINSTALL)/bmpjec
	cp -f -p ufstartbmpjec $(BMPINSTALL)/bin
	cd $(BMPINSTALL)/bin; rm ufbmpjec; ln -s ufstartbmpjec ufbmpjec
	-@echo installed bmpjec.jar and param files

newparams:
	cp -f -p $(JDATA)/*.* $(BMPINSTALL)/bmpjec

.ufmjec: $(JDDSRC)
	$(JAVAC) $(JFLAGS) $(JDDSRC)

bmpjec.jar: .ufmjec
	echo 'Main-Class: bmpjec.mjec' >! manifest
	echo 'Class-Path: bmpjec.jar ../lib/javaUFProtocol.jar ../lib/javaUFLib.jar ../lib/javaMMTLib.jar' >> manifest
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
	mkdir -p $(BMPINSTALL)/bmpjec

cleanpub:
	source ../.ufcshrc; $(RM) -r $(BMPINSTALL)/bmpjec

pubclean: cleanpub initpub
	@echo cleaned and re-initialized $(BMPINSTALL)/bmpjec

clean:
	-$(RM) -rf $(JPKG)  *.jar  *.class  *~ 
