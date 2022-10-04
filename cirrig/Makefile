# RCS: 
# $Name:  $ $Id: Makefile,v 1.7 2010/09/01 21:16:35 warner Exp $
#
# Makefile for MMT
# Macros:

SHELL := /bin/tcsh -f

DOMAIN := $(shell domainname)

ifndef RCSMASTER
  RCSMASTER := $(shell \ls -l | \grep RCS | \cut -d'>' -f2 | \cut -c2- | sed 's^/RCS*^^')
  #RCSMASTER := $(shell /bin/ls -l | grep RCS | awk '{print $$11}' | sed 's^/RCS*^^')
endif

ifndef RCSDEV
  RCSDEV := $(shell /bin/pwd)
endif

ifndef BMPINSTALL
  BMPINSTALL := /home/$(USER)/bmpinstall
endif

BMPINSTALL := $(BMPINSTALL)

include .makerules

STDLIBA  := $(shell find $(GCCLIB) -follow -type f -name "*std*.a" |tail -1)
STDLIBSO := $(shell find $(GCCLIB) -follow -type f -name "*std*.so" |tail -1)

RCSLIST := ReadMe Makefile .makerules .maketargets .gccv .ufcshrc

AGENTS  := bmpAgent  ccropAgent  weatherAgent  winAgent  cirrigAgent  weewxAgent  boot  scripts

UFLIB_JAVA := javaUFProtocol  lib  javaUFLib  javaMMTLib  bmp.jec weather.jec  cirrig.jec

UFDIRS := $(UFLIB_JAVA) $(AGENTS) 

UFRELEASE := BMP

# Targets:
all: show init install
	@echo made all

show:
	-@echo :
	-@echo JDATE= $(JDATE)
	-@echo :
	-@echo RCSMASTER=$(RCSMASTER)
	-@echo RCSDEV=$(RCSDEV)
	-@echo JAR= $(JAR)
	-@echo LIB= $(LIB)
	-@echo EXE= $(EXE)
	-@echo STARGET= $(STARGET)
	-@echo TARGET= $(TARGET)
	-@echo COMPILER_ID= $(COMPILER_ID)
	-@echo OS= $(OS)
	-@echo OS_VERSION= $(OS_VERSION)
	-@echo RCSLIST = $(RCSLIST)
	-@echo PUBHDR = $(PUBHDR)
	-@echo PUBLIB = $(PUBLIB)
	-@echo PUBSLIB = $(PUBSLIB)
	-@echo PUBEXE = $(PUBEXE) 
	-@echo PUBBIN = $(PUBBIN)
	-@echo PUBSBIN = $(PUBSBIN)
	-@echo BMPINSTALL = $(BMPINSTALL)
	-@echo BMPINSTALL = $(BMPINSTALL)
	-@echo INSTALL_HDR = $(INSTALL_HDR)
	-@echo INSTALL_LIB = $(INSTALL_LIB)
	-@echo INSTALL_EXE = $(INSTALL_EXE)
	-@echo INSTALL_SLIB = $(INSTALL_SLIB)
	-@echo INSTALL_SBIN = $(INSTALL_SBIN)
	-@echo UFRELEASE = $(UFRELEASE)
	-@echo EXTINC = $(EXTINC)
	-@echo GCCIDM = $(GCCIDM)
	-@echo GCCLIB = $(GCCLIB)
	-@echo STDLIBA = $(STDLIBA)
	-@echo STDLIBSO = $(STDLIBSO)
	-@echo JAVA_GUI = $(JAVA_GUI)
	-@echo UFDIRS = $(UFDIRS)

# RCSMASTER must be either "." OR a full path, not relative path! i.e. no "../" or "../../" or etc.

init: initco pubclean
	source .ufcshrc;
	$(foreach i, $(UFDIRS), pushd $i; $(MAKE) -i -k init; popd;)

initco: 
	$(foreach i, $(UFDIRS), if( ! -e $i ) mkdir $i;)

initpub: .ufcshrc .makerules .maketargets
	if ( ! -e $(INSTALL_PERL)/ ) mkdir -p $(INSTALL_PERL)
	if ( ! -e $(INSTALL_LIB)/ )  mkdir -p $(INSTALL_LIB)
	if ( ! -e $(INSTALL_SLIB)/ ) mkdir -p $(INSTALL_SLIB)
	if ( ! -e $(INSTALL_BIN)/ )  mkdir -p $(INSTALL_BIN)
	if ( ! -e $(INSTALL_SBIN)/ ) mkdir -p $(INSTALL_SBIN)
	if ( ! -e $(INSTALL_HDR)/ )  mkdir -p $(INSTALL_HDR)
	if ( ! -e $(INSTALL_ETC)/ )  mkdir -p $(INSTALL_ETC)
	cd $(INSTALL_LIB); ln -s $(STDLIBSO) libstdc++.so
	cd $(INSTALL_SLIB); ln -s $(STDLIBA) libstdc++.a
	cp -f -p .ufcshrc $(BMPINSTALL)/

cleanpub:
	if( -e $(BMPINSTALL)/ ) $(RM) -r $(BMPINSTALL)/
	mkdir -p $(BMPINSTALL)

ifeq ($(STL),sgi)
pubclean: cleanpub initpub
	@echo cleaned and re-initialized $(BMPINSTALL) using $(STL)
else
pubclean: cleanpub initpub 
	@echo cleaned and re-initialized $(BMPINSTALL) using $(STL)
endif

# only public headers, libraries, and executables, meant for public consumption
# should be "installed", nominally into /usr/local/...
# force source of .ufcshrc

install:  pubheader publib pubexe pubjar
	source .ufcshrc;
	$(foreach i, $(UFDIRS), pushd $i; $(MAKE) -i -k install; popd;)

initjava:
	cp -f -p .ufcshrc $(BMPINSTALL)/
	source .ufcshrc;
	$(foreach i, $(JAVA_GUI), if( ! -e $i ) mkdir $i;) \
	if( ! -e $i/RCS && -e $(RCSMASTER) ) ln -s $(RCSMASTER)/$i/RCS $i/RCS;)
	$(foreach i, $(JAVA_GUI), pushd $i; $(MAKE) -i -k clean; popd;)
	$(foreach i, $(JAVA_GUI), pushd $i; $(MAKE) -i -k init; popd;)
	$(foreach i, $(JAVA_GUI), pushd $i; $(MAKE) -i -k install; popd;)

newjava:
	cp -f -p .ufcshrc $(BMPINSTALL)/
	source .ufcshrc;
	$(foreach i, $(JAVA_GUI), pushd $i; $(MAKE) -i -k clean; popd;)
	$(foreach i, $(JAVA_GUI), pushd $i; $(MAKE) -i -k coall; popd;)
	$(foreach i, $(JAVA_GUI), pushd $i; $(MAKE) -i -k install; popd;)

java:
	source .ufcshrc;
	$(foreach i, $(JAVA_GUI), pushd $i; $(MAKE) -i -k clean; popd;)
	$(foreach i, $(JAVA_GUI), pushd $i; $(MAKE) -i -k install; popd;)

clean:
	source .ufcshrc;
	$(foreach i, $(UFDIRS), pushd $i; $(MAKE) -i -k clean; popd;)

pubheader:
	$(foreach i, $(UFDIRS), pushd $i; $(MAKE) -i -k $@; popd;)

publib: 
	@if( ! -e $(INSTALL_LIB)/ ) mkdir -p $(INSTALL_LIB)
	$(foreach i, $(UFDIRS), pushd $i; $(MAKE) -i -k $@; popd;)

pubslib: 
	@if( ! -e $(INSTALL_SLIB)/ ) mkdir -p $(INSTALL_SLIB)
	$(foreach i, $(UFDIRS), pushd $i; $(MAKE) -i -k $@; popd;)

pubexe: 
	@if( ! -e $(INSTALL_EXE)/ ) mkdir -p $(INSTALL_EXE)
	$(foreach i, $(UFDIRS), pushd $i; $(MAKE) -i -k $@; popd;)

pubsbin: 
	@if( ! -e $(INSTALL_SBIN)/ ) mkdir -p $(INSTALL_SBIN)
	$(foreach i, $(UFDIRS), pushd $i; $(MAKE) -i -k $@; popd;)

shlib: pubheader
	$(foreach i, $(UFDIRS), pushd $i; $(MAKE) $@; popd;)

exe: shlib publib
	$(foreach i, $(UFDIRS), pushd $i; $(MAKE) $@; popd;)

depend: pubheader
	$(foreach i, $(UFDIRS), pushd $i; $(MAKE) $(MAKEFLAGS) $@; popd;)
