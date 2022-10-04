!=======================================================================
!     Plant Subroutine for Container Production Model 
!	J. Ritchie, G. Schoene, T. Yeager...et al
!	
!	4/27/05 coding started M. Bostick
!	5/9/05 delivered M. Bostick
!	4/6/06 new Ritchie/Million plant subroutine debugged by MB
!    5/12/06 new root code  JR,MB
!    8/23/06 new plant N uptake code MB,JM,JR
!    11/3/06 modified N uptake code and NSUF   JR,JM
!    11/27/06 pruning code JM
!    7/26/07 new degree day growth curve JR
!    8-23-07 new sink relationship JR
!    12-12-07 new NSUPPLY code JR
!    2-2-09 new logistics sink function JR
!    12-21-09 dwsi mods JR
!    12-21-09 pruning factor mods JM
!    4-26-10 phosphorus added JM
!    3-14-11 size adjustment mods JM
!    3-28-11 DT mods JR

!=======================================================================
      SUBROUTINE Plant(DYNAMIC,DAY,DOY,SOLAR,TMAX,TMIN,DT,RDT,ILA,
     &LA,d_LA,LAI,RW,d_RW,TW,d_TW,SIZE,PTA,RTPF,
     &LGC1,LGC2,LGC3,LGC4,LGC5,RUE,CINT,DTC1,
     &SUB_N,NUPTAKE,NDEMAND,NSUPPLY,TRELN,N_TOP,N_ROOT,
     &N_PLT,TW_Nact,RW_Nact,TW_Nopt,RW_Nopt,TW_Nmin,SWDUL_cm3,
     &PRUNE,WSUF,NSUF,NSUP_MAX1,NSUP_MAX2,NSUP_C1,NSUP_C2,HT,WIDTH,HTC1,
     &HTC2,SZC1,SZC2,WDC1,WDC2,TBIAS,PHOTOTEMP1,
     &PHOTOTEMP2,PHOTOTEMP3,TEMFACSO,PARB,TW_Noptmax,TW_Noptmin,
     &DT_Nmax,DT_Nmin,NSUF_C1,NSUF_C2,NSUP_RF,NSUF_TF,S_VOL,
     &CHK_DAY1,CHK_DAY2,CHK_DAY3,CHK_HT1,CHK_W1,CHK_HT2,CHK_W2,CHK_HT3,
     &CHK_W3,POTDIAM,PR1,PR2,PR3,PR_H1,PR_W1,PR_H2,PR_W2,PR_H3,PR_W3,
     &PRTWF1,PRLAF1,IHT,IWD,TPR1,TPR2,TPR3,CUT,NOPMAX,PSUP_MAX1,
     &NUPTAKE_CUM,IN_STATUS,ITW_Nact,IRW_Nact,PRTWF2,PRLAF2,ITW_Pact,
     &IRW_Pact,ITW_Kact,IRW_Kact,PSUP_MAX2,PSUP_RF,PSUP_C1,PSUP_C2,
     &PSUF_C1,PSUF_C2,PSUF_TF,TW_Poptmax,TW_Poptmin,DT_Pmax,DT_Pmin,
     &TW_Pmin,RW_Popt,SUB_P,P_TOP,P_ROOT,TW_Pact,RW_Pact,TW_Popt,
     &PDEMAND,PUPTAKE,PSUPPLY,PSUF,PUPTAKE_CUM,RW_Noptdif,RW_Poptdif,
     &NOP)
      
!-----------------------------------------------------------------------
      IMPLICIT NONE			!every variable must be defined
      SAVE
	CHARACTER PRUNE*8,IN_STATUS*8
	INTEGER DYNAMIC,INIT,RATE,INTEG,DAY,DOY,CHK_DAY1,CHK_DAY2,
     &CHK_DAY3,PR1,PR2,PR3,DT_PR,NOPMAX
	PARAMETER (INIT=1,RATE=2,INTEG=3)
	REAL SOLAR,TMAX,TMIN,DT,RDT,ILA,LA,d_LA,LAI,PTA,
     &RW,d_RW,TW,d_TW,SIZE,RTPF,LGC1,LGC2,LGC3,LGC4,LGC5,
     &RUE,CINT,SUB_N,NUPTAKE,NDEMAND,NSUPPLY,TRELN,N_TOP,N_ROOT,N_PLT,
     &TW_Nact,RW_Nact,TW_Nopt,RW_Nopt,TW_Nmin,SWDUL_cm3,
     &WSUF,NSUF,TEMFACSO,d_PWSI,d_PWSO,SUB_NCONC,PRUNE_TW,PRUNE_N,
     &NSUP_MAX1,NSUP_MAX2,NSUP_C1,NSUP_C2,
     &HT,WIDTH,HTC1,HTC2,SZC1,SZC2,WDC1,WDC2,
     &TBIAS,PHOTOTEMP1,PHOTOTEMP2,PHOTOTEMP3,PARB,
     &PRHT_RED,PRTWF1,PRLAF1,TW_Noptmax,TW_Noptmin,DT_Nmax,DT_Nmin,
     &NSUF_C1,NSUF_C2,NSUP_RF,SLOPEN1,SLOPEN2,NSUF_TF,
     &S_VOL,CHK_HT1,CHK_W1,CHK_HT2,CHK_W2,CHK_HT3,
     &CHK_W3,POTDIAM,PR_H1,PR_W1,PR_H2,PR_W2,PR_H3,PR_W3,TPR1,TPR2,
     &TPR3,PR_DELAY,IHT,IWD,CUT,NOP,PR,PR_H,PR_W,TPR,NUPTAKE_CUM,
     &ITW_Nact,IRW_Nact,PRTWF2,PRLAF2,ITW_Pact,IRW_Pact,ITW_Kact,
     &IRW_Kact,PSUP_MAX1,PSUP_MAX2,PSUP_RF,PSUP_C1,PSUP_C2,
     &PSUF_C1,PSUF_C2,PSUF_TF,TW_Poptmax,TW_Poptmin,DT_Pmax,DT_Pmin,
     &TW_Pmin,RW_Popt,SLOPEP1,SLOPEP2,TW_Popt,TW_Pact,
     &RW_Pact,P_TOP,P_ROOT,P_PLT,PUPTAKE_CUM,PRUNE_P,TRELP,PSUF,
     &SUB_P,PUPTAKE,PDEMAND,PSUPPLY,SUB_PCONC,DT1,y_TW,
     &RW_Noptdif,RW_Poptdif,DTC1
      
      
C*****INITIALIZATION*******************************
	IF(DYNAMIC.EQ.INIT) THEN
	LA=ILA
      LAI=LA/PTA
      TW=LGC3*LA**2+LGC4*LA-0.28175   !jm 3-14-11
      RW=TW/1.5
      DT1=DT  !jr 3-29-11
      
	HT=IHT
      WIDTH=IWD
      SIZE=(HT+WIDTH)/2
      NSUF=1.
      PSUF=1.
      
      SLOPEN1=(TW_Noptmax-TW_Noptmin)/DT_Nmin
      SLOPEN2=(TW_Noptmax-TW_Noptmin)/(DT_Nmax-DT_Nmin)**2
      
      SLOPEP1=(TW_Poptmax-TW_Poptmin)/DT_Pmin
      SLOPEP2=(TW_Poptmax-TW_Poptmin)/(DT_Pmax-DT_Pmin)**2
      
C-------assign initial N concentration to transplant
      IF(DT.le.DT_Nmin) THEN
        TW_Nopt=TW_Noptmin+slopeN1*DT
      ELSE
        TW_Nopt=TW_Noptmin+slopeN2*(DT-DT_Nmax)**2
      ENDIF
      
      IF(DT.le.DT_Pmin) THEN
        TW_Popt=TW_Poptmin+slopeP1*DT
      ELSE
        TW_Popt=TW_Poptmin+slopeP2*(DT-DT_Pmax)**2
      ENDIF

      RW_Nopt=TW_Nopt-RW_Noptdif
      RW_Popt=TW_Popt-RW_Poptdif
      
      IF(IN_STATUS.eq.'LOW')THEN
        TW_Nact=0.5*TW_Nopt
        RW_Nact=0.5*RW_Nopt
        TW_Pact=0.5*TW_Popt
        RW_Pact=0.5*RW_Popt
      ELSEIF(IN_STATUS.eq.'INPUT')THEN
        TW_Nact=min(ITW_Nact,TW_Nopt)
        RW_Nact=min(IRW_Nact,RW_Nopt)
        TW_Pact=min(ITW_Pact,TW_Popt)
        RW_Pact=min(IRW_Pact,RW_Popt)
      ELSEIF(IN_STATUS.eq.'OPT')THEN
        TW_Nact=TW_Nopt
        RW_Nact=RW_Nopt
        TW_Pact=TW_Popt
        RW_Pact=RW_Popt
      ENDIF
       
      N_TOP=TW*TW_Nact             !N content of tops (g/plant)
      N_ROOT=RW*RW_Nact            !N content of roots  (g/plant)
      N_PLT=TW*TW_Nact+RW*RW_Nact  !N content of whole plant (g/plant)
      NUPTAKE_CUM=0
      
      P_TOP=TW*TW_Pact             !N content of tops (g/plant)
      P_ROOT=RW*RW_Pact            !N content of roots  (g/plant)
      P_PLT=TW*TW_Pact+RW*RW_Pact  !N content of whole plant (g/plant)
      PUPTAKE_CUM=0
      
C-------prune initialization
      PR_DELAY=2.0      !initialize high so that no delay until prune
      NOP=0
            
C*****RATE CALCULATIONS*****************************
	ELSEIF (DYNAMIC.EQ.RATE) THEN
      
      IF(NOP.eq.0)THEN
        PR=PR1
        PR_H=PR_H1
        PR_W=PR_W1
        TPR=TPR1
      ELSEIF(NOP.eq.1)THEN
        PR=PR2
        PR_H=PR_H2
        PR_W=PR_W2
        TPR=TPR2
      ELSEIF(NOP.eq.2)THEN
        PR=PR3
        PR_H=PR_H3
        PR_W=PR_W3
        TPR=TPR3
      ENDIF
      
C-----Height adjustment based upon check for any pruning--------     	
        IF(DAY.eq.CHK_DAY1)THEN
          HT=CHK_HT1
          WIDTH=CHK_W1
        ENDIF
        
        IF(DAY.eq.CHK_DAY2) THEN
          HT=CHK_HT2
          WIDTH=CHK_W2
        ENDIF
        
        IF(DAY.eq.CHK_DAY3) THEN
          HT=CHK_HT3
          WIDTH=CHK_W3
        ENDIF
        
        IF(DAY.eq.CHK_DAY1.or.DAY.eq.CHK_DAY2.or.DAY.eq.CHK_DAY3)THEN
          SIZE=(HT+WIDTH)/2
          LA=SZC1*SIZE**SZC2
          y_TW=TW
          TW=LGC3*LA**2+LGC4*LA-0.2817
          N_TOP=TW/y_TW*N_TOP
          N_PLT=TW/y_TW*N_PLT  
          N_TOP=TW/y_TW*N_TOP
          N_PLT=TW/y_TW*N_PLT 
        ENDIF
        
C-----Readjust for pruning--------     	
        IF(PRUNE.eq.'FIXED'.and.DAY.eq.PR)THEN
          DT_PR=DT
          
          IF(DT.lt.80)THEN
          PR_DELAY=0.
          ELSE
          PR_DELAY=1.
          ENDIF
          
          PRHT_RED=(HT-PR_H)/HT
          HT=PR_H       !new HT
          WIDTH=PR_W     !new WIDTH
          PRUNE_TW=TW*PRHT_RED*(PRTWF1*DT**(-PRTWF2))      !dwt of prunings
          PRUNE_N=PRUNE_TW*1.2*TW_Nact      !N content of prunings
          PRUNE_P=PRUNE_TW*1.2*TW_Pact 
          
          TW=TW*(1-PRHT_RED*(PRTWF1*DT**(-PRTWF2)))       !new TW
          LA=LA*(1-PRHT_RED*(PRLAF1*DT**(-PRLAF2)))       !new LA
          N_TOP=N_TOP-PRUNE_N               !N in tops after pruning
          N_PLT=N_PLT-PRUNE_N               !N in whole plant after pruning
          
          P_TOP=P_TOP-PRUNE_P               !N in tops after pruning
          P_PLT=P_PLT-PRUNE_P               !N in whole plant after pruning
          
          NOP=NOP+1          
        ENDIF
         
        IF(PRUNE.eq.'TRIG'.and.HT.ge.TPR.and.NOP.lt.NOPMAX)THEN
          DT_PR=DT
          
          IF(DT.lt.80)THEN
          PR_DELAY=0.
          ELSE
          PR_DELAY=1.
          ENDIF
          
          HT=HT-CUT                 !new HT
          WIDTH=HT*0.95             !new WIDTH slightly smaller than ht
          PRHT_RED=CUT/TPR
          PRUNE_TW=TW*PRHT_RED*(PRTWF1*DT**(-PRTWF2))      !dwt of prunings
          PRUNE_N=PRUNE_TW*1.2*TW_Nact      !N content of prunings
          
          PRUNE_P=PRUNE_TW*1.2*TW_Pact 
          
          TW=TW*(1-PRHT_RED*(PRTWF1*DT**(-PRTWF2)))       !new TW
          LA=LA*(1-PRHT_RED*(PRLAF1*DT**(-PRLAF2)))       !new LA
          N_TOP=N_TOP-PRUNE_N               !N in tops after pruning
          N_PLT=N_PLT-PRUNE_N               !N in whole plant after pruning
          
          P_TOP=P_TOP-PRUNE_P               !N in tops after pruning
          P_PLT=P_PLT-PRUNE_P               !N in whole plant after pruning
          
          NOP=NOP+1
         ENDIF
          
C *********************INTEGRATION CALCULATIONS********************************
	ELSEIF (DYNAMIC.EQ.INTEG) THEN
      
C---------Calculate optimum N conc in tops and roots----------    
      IF(DT.LE.DT_Nmin) THEN
        TW_Nopt=TW_Noptmin+slopeN1*DT
      ELSEIF(DT.GT.DT_Nmin.and.DT.LE.DT_Nmax)THEN
        TW_Nopt=TW_Noptmin+slopeN2*(DT-DT_Nmax)**2
      ELSE
        TW_Nopt=TW_Noptmin
      ENDIF
      
      IF(DT.LE.DT_Pmin) THEN
        TW_Popt=TW_Poptmin+slopeP1*DT
      ELSEIF(DT.GT.DT_Pmin.and.DT.LE.DT_Pmax)THEN
        TW_Popt=TW_Poptmin+slopeP2*(DT-DT_Pmax)**2
      ELSE
        TW_Popt=TW_Poptmin
      ENDIF
      
      RW_Nopt=TW_Nopt-RW_Noptdif    !changed back - used to be like this jm 4-21-11
      RW_Popt=TW_Popt-RW_Poptdif
      
     
      TRELN=max((TW_Nact-TW_Nmin)/(TW_Nopt-TW_Nmin),0.) !relative N deficiency factor
      TRELP=max((TW_Pact-TW_Pmin)/(TW_Popt-TW_Pmin),0.) !relative P deficiency factor
         
      IF(TRELN.LT.NSUF_TF) THEN         !JR 8-21-08
        NSUF=max(NSUF_C1*TRELN**NSUF_C2,0.05)
      ELSE 
        NSUF=1.
      ENDIF
      
      IF(TRELP.LT.PSUF_TF) THEN         
        PSUF=max(PSUF_C1*TRELP**PSUF_C2,0.05)
      ELSE 
        PSUF=1.
      ENDIF
       
C---------Calculate sink-limited growth potential--------- 
       IF(DT.lt.7)THEN
       d_LA=0.1
       ELSE
       d_LA=LGC1*LGC2*DT1**(LGC2-1)*RDT*min(WSUF,NSUF,PSUF)  !JR 3-29-11
       ENDIF

      d_PWSI=(2*LGC3*LA+LGC4)*d_LA    !JM 3-14-11 LGC3=.00000021 LGC4=0.0124 deriv of quadratic
      
C---------Calculate source-limited growth potential--------- 	
	TEMFACSO=PHOTOTEMP1-PHOTOTEMP2*(TBIAS-PHOTOTEMP3)**2  !temp factor for modifying source calc
	TEMFACSO=MIN(TEMFACSO,1.0)
	TEMFACSO=MAX(TEMFACSO,0.0)
      d_PWSO=RUE*PARB*TEMFACSO*(1-exp(-CINT*LAI))*min(WSUF,NSUF,PSUF)*
     &   0.0001*PTA   !source-limited growth (g/plant)
 
C---------Determine whether growth is source or sink-limited---------  
      IF(d_PWSI.ge.d_PWSO) THEN 
	  d_TW=d_PWSO*(1-RTPF)    !source-limited top growth
	  d_RW=d_PWSO*RTPF        !source-limited root growth
          IF(d_PWSI.eq.0) THEN !CW 2-25-09 floating point precision error can cause small
        ! values to be rounded to 0 causing NaNs
            d_LA=0
          ELSE
            d_LA=d_LA*(d_PWSO/d_PWSI)  !JR 10-23-07
          ENDIF
	ELSE
        d_TW=d_PWSI*(1-RTPF)    !sink-limited top growth
        d_RW=d_PWSI*RTPF+0.4*(d_PWSO-d_TW) !portion of extra source to roots JR 2-5-09
	ENDIF
     
C---------Calculate N supply, N demand and N uptake---------	
      SUB_NCONC=SUB_N/SWDUL_cm3*10**6  !N conc in substrate soluntion (ug/cm3)
     
      NSUPPLY=NSUP_MAX1*S_VOL**NSUP_MAX2*(1-exp(-NSUP_RF*RW))
     &*(1-1/(1.+(SUB_NCONC/NSUP_C1)**NSUP_C2)) 
     
      NDEMAND=(TW*(TW_Nopt-TW_Nact)+RW*(RW_Nopt-RW_Nact))+  !yesterday's N deficit plus today's
     &TW_Nopt*(d_TW)+RW_Nopt*(d_RW)     !MB 8-23-06       
       
      NUPTAKE=min(NDEMAND,NSUPPLY)  !N uptake is limited by demand or supply
      NUPTAKE_CUM=NUPTAKE_CUM+NUPTAKE
      
C---------Calculate P supply, P demand and P uptake---------	
      SUB_PCONC=SUB_P/SWDUL_cm3*10**6  !P conc in substrate soluntion (ug/cm3)
      
      PSUPPLY=PSUP_MAX1*S_VOL**PSUP_MAX2*(1-exp(-PSUP_RF*RW))
     &*(1-1/(1.+(SUB_PCONC/PSUP_C1)**PSUP_C2)) 

      PDEMAND=(TW*(TW_Popt-TW_Pact)+RW*(RW_Popt-RW_Pact))+  !yesterday's P deficit plus today's
     &TW_Popt*(d_TW)+RW_Popt*(d_RW)     !MB 8-23-06       

      PUPTAKE=min(PDEMAND,PSUPPLY)  !P uptake is limited by demand or supply
      PUPTAKE_CUM=PUPTAKE_CUM+PUPTAKE

C--------update top and root dry weights      
      TW=TW+d_TW
	RW=RW+d_RW
             
C---------N content and N conc based on new growth JM 11/06/06---------  
      N_PLT=N_PLT+NUPTAKE                       !new N content of whole plant 
      P_PLT=P_PLT+PUPTAKE
      RW_Nact=N_PLT/(TW*(TW_Nopt/RW_Nopt)+RW)   !new N conc of roots
      RW_Pact=P_PLT/(TW*(TW_Popt/RW_Popt)+RW)
      N_ROOT=RW*RW_Nact                         !new N content of roots
      P_ROOT=RW*RW_Pact
      N_TOP=N_PLT-N_ROOT                        !new N content of tops
      P_TOP=P_PLT-P_ROOT
      TW_Nact=N_TOP/TW                          !new N conc of tops
      TW_Pact=P_TOP/TW
      
C---------calculate prune delay----------
      IF(PR_DELAY.lt.1.2)THEN
        PR_DELAY=0.0328*exp(-22.77*PRHT_RED)*(DT-DT_PR)**3  !based upon 14 day delay for 20% prht_red
        d_LA=min(d_LA*PR_DELAY,d_LA)
      ENDIF
      
C---------leaf area calculation based on new growth---------
      LA=LA+d_LA     !JR 10-22-07
	DT1=((LA-min(ILA,DTC1))/LGC1)**LGC5  !DT1 based upon liner so min function added for large transplant
	LAI=LA/PTA
      
C---------size based on LA-----------
      HT=HT+HTC1*HTC2*LA**(HTC2-1)*d_LA
      WIDTH=WIDTH+WDC1*WDC2*LA**(WDC2-1)*d_LA
      SIZE=(HT+WIDTH)/2
      
      ENDIF
      
C ***************** END INTEGRATION CALCULATIONS ********************************

900   CONTINUE
      RETURN
      END	SUBROUTINE Plant
