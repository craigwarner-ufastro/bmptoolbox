# RCS: 
# $Id: Makefile,v 1.1 2010/04/22 20:48:12 warner Exp $
#
# Macros:
#.IGNORE:
#	@true

ifndef GCCID
  GCCID := $(shell ../.gccv)
endif

#include ../../.makerules

SCRIPTS :=  ufbmpstart  ufbmpstop  ufbmpsim  ufbmprestart checkCCrop checkBmpPlc checkWeather checkCirrigPlc checkWeewxAgent

RCSLIST := Makefile $(SCRIPTS) 

# targets:

install: $(RCSLIST)
	cp -f -p  $(SCRIPTS)  $(BMPINSTALL)/bin
	@echo installed scripts

init:  $(RCSLIST)

slib:
	@echo no static libs

dynlib:
	@echo no dynamic shared libs

#include ../../.maketargets
