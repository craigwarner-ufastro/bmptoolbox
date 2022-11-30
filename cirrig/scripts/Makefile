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

CONFIG := .cshrc

SCRIPTS :=  archiveIrrigHis.py checkInternetConnection  checkWeewxDaemon  checkWeewxStatus.py  checkDST  db_backup.py

RCSLIST := Makefile $(SCRIPTS) $(CONFIG) 

# targets:

install: $(RCSLIST)
	chmod u+x $(SCRIPTS)
	cp -f -p  $(SCRIPTS)  $(BMPINSTALL)/bin
	@echo installed scripts

init:  $(RCSLIST)

slib:
	@echo no static libs

dynlib:
	@echo no dynamic shared libs

#include ../../.maketargets
