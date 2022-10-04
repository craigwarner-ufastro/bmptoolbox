# RCS: 
# $Id: Makefile,v 1.7 2011/01/20 23:07:37 warner Exp $
#
# Macros:

SHELL := /bin/tcsh -f

ifndef GCCID
  GCCID = $(shell ../.gccv)
endif

#include ../.makerules

JPKG := javaMMTLib
JAVAC = javac
JFLAG = -classpath $(BMPINSTALL)/lib/javaUFProtocol.jar:$(BMPINSTALL)/lib/javaUFLib.jar:$(BMPINSTALL)/lib/jamod.jar  -d ./ -deprecation
JFLAGS = -classpath $(BMPINSTALL)/lib/javaUFProtocol.jar:$(BMPINSTALL)/lib/javaUFLib.jar:$(BMPINSTALL)/lib/jamod.jar:javaMMTLib.jar  -d ./ -deprecation

JSRC =	MMTHelper.java			\
	UFGUIRecord.java		\
	UFMMTBaytech.java		\
	UFMMTCheckBox.java		\
	UFMMTClientThread.java		\
	UFMMTComboBox.java		\
	UFMMTComponent.java		\
	UFMMTDitherPattern.java		\
	UFMMTImgHeader.java		\
	UFMMTImg.java			\
	UFMMTIntegratedComboBox.java	\
	UFMMTIntegratedTextField.java	\
	UFMMTLabel.java			\
	UFMMTLock.java			\
	UFMMTMotor.java			\
	UFMMTPasswordField.java		\
	UFMMTProgressBar.java		\
	UFMMTRadioButton.java		\
	UFMMTTextField.java		\
	UFMMTThreadedAgent.java		\
	UFRecord.java

JCLASS := $(patsubst %.java, $(JPKG)/%.class, $(JSRC))
JPDIR = $(shell /bin/ls -d $(JPKG))
JPFILE = $(shell /bin/ls $(JPKG).jar)
RCSLIST = Makefile ReadMe $(JSRC)

#targets:

all: install

$(JPKG)/%.class: %.java
	$(JAVAC) $(JFLAGS) $<

ifeq ($(JPFILE),$(JPKG).jar)

install: initpub build $(JSRC) $(JPKG).jar
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
