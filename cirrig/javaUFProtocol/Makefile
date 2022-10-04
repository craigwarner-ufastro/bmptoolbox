# RCS: 
# $Id: Makefile,v 1.13 2012/11/14 01:22:03 varosi Exp $
#
# Macros:

SHELL := /bin/tcsh -f

ifndef GCCID
  GCCID = $(shell ../.gccv)
endif

#include ../.makerules

JPKG := javaUFProtocol
JAVAC = javac
JFLAGS = -d ./ -deprecation

JSRC =	UFProtocol.java		\
	UFTimeStamp.java	\
	UFBytes.java		\
	UFStrings.java		\
	UFShorts.java		\
	UFunsignShorts.java	\
	UFInts.java		\
	UFFloats.java		\
	UFImageConfig.java	\
	UFFrameConfig.java	\
	UFObsConfig.java

RCSLIST = Makefile ReadMe $(JSRC)
JCLASS := $(patsubst %.java, $(JPKG)/%.class, $(JSRC))

$(JPKG)/%.class: %.java
	$(JAVAC) $(JFLAGS) $<

#targets:

all: install

install: initpub build $(JPKG).jar
	cp -f -p $(JPKG).jar $(BMPINSTALL)/lib
	-@echo installed $(JPKG)

build: $(JSRC)
	$(JAVAC) $(JFLAGS) $(JSRC)

$(JPKG).jar: $(JCLASS)
	jar cf $@ $(JPKG)

show:
	source ../.ufcshrc
	-@echo BMPINSTALL= $(BMPINSTALL)
	-@echo OS= $(OS)
	-@echo OS_VERSION= $(OS_VERSION)
	-@echo JAVAC = $(JAVAC)
	-@echo JFLAGS =$(JFLAGS)
	-@echo RCSLIST = $(RCSLIST)
	-@echo JCLASS = $(JCLASS)
	-@echo JPKG= $(JPKG)

init: $(RCSLIST)
	source ../.ufcshrc
	if ( ! -e $(JPKG) ) mkdir -p $(JPKG);

initpub:
	source ../.ufcshrc
	if ( ! -e $(BMPINSTALL)/lib ) mkdir -p $(BMPINSTALL)/lib

clean:
	if ( -e $(JPKG) ) $(RM) -r $(JPKG)
