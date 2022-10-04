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

PAGES := CalendarPopup.js calendar.css driver.css irrigHistory.php counterHistory.php

RCSLIST := Makefile $(PAGES)

# targets:

install: $(RCSLIST)
	sudo cp -f -p $(PAGES) /var/www/html/
	@echo installed pages 

init:  $(RCSLIST)
	co RCS/*,v

coall: $(RCSLIST)
	co RCS/*,v

slib:
	@echo no static libs

dynlib:
	@echo no dynamic shared libs

#include ../../.maketargets
