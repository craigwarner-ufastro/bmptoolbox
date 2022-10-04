# RCS: 
# $Id: Makefile,v 1.8 2011/01/20 23:10:04 warner Exp $
#
# Macros:
#.IGNORE:
#       @true

ifndef GCCID
  GCCID := $(shell ../.gccv)
endif

#include ../.makerules

ETC :=  databaseStartupValues.xml

RCSLIST := Makefile $(ETC)

# targets:

install: $(RCSLIST)
	mkdir -p $(BMPINSTALL)/etc 
	cp -f -p -r $(ETC)  $(BMPINSTALL)/etc
	@echo installed config files 

init:  $(RCSLIST)

slib:
	@echo no static libs

dynlib:
	@echo no dynamic shared libs

#include ../.maketargets
