# RCS: 
# $Id: Makefile,v 1.48 2010/04/01 21:18:20 swanee Exp $
#
# Macros:

SHELL := /bin/tcsh -f

ifndef GCCID
  GCCID = $(shell ../.gccv)
endif

#include ../.makerules

JPKG := javaUFLib
JAVAC = javac
JFLAG = -classpath $(BMPINSTALL)/lib/javaUFProtocol.jar:$(BMPINSTALL)/lib/jamod.jar  -d ./ -deprecation
JFLAGS = -classpath $(BMPINSTALL)/lib/javaUFProtocol.jar:$(BMPINSTALL)/lib/jamod.jar:javaUFLib.jar  -d ./ -deprecation

JSRC =	RatioLayout.java	\
	TextFile.java		\
	ExtensionFilter.java	\
	UFSounds.java		\
	UFFITSheader.java	\
	UFMathLib.java		\
	UFArrayOps.java		\
	UFImageOps.java		\
	UFColor.java		\
	UFTextArea.java		\
	UFMessageLog.java	\
	UFLabel.java		\
	UFLabelsPanel.java	\
	UFButton.java		\
	UFRadioButtons.java	\
	UFTextField.java	\
	UFTextMinMax.java	\
	UFTextPanel.java	\
	UFComboBox.java		\
	UFComboPanel.java	\
	UFHostPortPanel.java	\
	UFStatusCAR.java	\
	UFLibPanel.java		\
	UFMonitorPanel.java	\
	UFobsMonitor.java	\
	UFexpMonitor.java	\
	UFBayTech.java		\
	UFAgentsControl.java	\
	UFPlot.java		\
	UFPlotPanel.java	\
	GatorPlot.java		\
	XYFrame.java		\
	UFExecCommand.java	\
	UFTail.java		\
	UFPLCifce.java		\
	UFClientThread.java	\
	UFStatusThread.java	\
	UFServerAgent.java	\
	UFClock.java		\
	UFColorButton.java	\
	UFColorCombo.java	\
	UFFlam2Starter.java	\
	UFHelperPlot.java	\
	NewFlam2Helper.java	\
	UFPidFrame.java		\
	UFPidLoop.java		\
	UFPidAgent.java

JCLASS := $(patsubst %.java, $(JPKG)/%.class, $(JSRC))
JPDIR = $(shell /bin/ls -d $(JPKG))
JPFILE = $(shell /bin/ls $(JPKG).jar)
RCSLIST = Makefile ReadMe $(JSRC)

#targets:

all: install

$(JPKG)/%.class: %.java
	$(JAVAC) $(JFLAGS) $<

ifeq ($(JPFILE),$(JPKG).jar)

install: initpub $(JSRC) $(JPKG).jar
	cp -f -p $(JPKG).jar $(BMPINSTALL)/lib
	-@echo installed $(JPKG).jar

else

install: initpub build $(JPKG).jar
	cp -f -p $(JPKG).jar $(BMPINSTALL)/lib
	-@echo installed $(JPKG).jar

endif

build: $(JSRC)
	$(JAVAC) $(JFLAG) $(JSRC)

$(JPKG).jar: $(JCLASS)
	cp -f -p gator_small.gif $(JPKG)
	jar cf $@ $(JPKG)

show:
	-@echo OS= $(OS)
	-@echo OS_VERSION= $(OS_VERSION)
	-@echo JAVAC = $(JAVAC)
	-@echo JFLAGS =$(JFLAGS)
	-@echo RCSLIST = $(RCSLIST)
	-@echo JCLASS = $(JCLASS)
	-@echo JPKG= $(JPKG)
	-@echo JPDIR= $(JPDIR)
	-@echo JPFILE= $(JPFILE)

init: $(RCSLIST)
	source ../.ufcshrc; if ( ! -e $(JPKG) ) mkdir -p $(JPKG);

initpub:
	source ../.ufcshrc; if ( ! -e $(BMPINSTALL)/lib ) mkdir -p $(BMPINSTALL)/lib

clean:
	if ( -e $(JPKG) ) $(RM) -r $(JPKG);  $(RM) *.jar
