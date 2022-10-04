!=======================================================================
!    Output Subroutine for Container Production Model 
!    J. Ritchie, G. Schoene, T. Yeager...et al
!	
!	4/27/05 coding started M. Bostick
!	5/9/05 delivered M. Bostick
!    7/13/07 added Summaryarea.txt output and changed summaryoutput.txt jm
!    7/17/08 added tempoutput.txt  jm
!    4-26-10 phosphorus output files (plant and leaching) added JM
!=======================================================================
	SUBROUTINE Output(DYNAMIC,LA,LAI,SW_cm,POT_ET_cm,POT_RAIN_cm,
     &POT_IRRIG_cm,DRAIN_cm,RUNOFF_cm,CF_IRR,TW,RW,DT,SOLAR,TMIN,TMAX,
     &RAIN_cm,IRRIG_cm, N_TOP,N_ROOT,TW_Nact,RW_Nact,NDEMAND,NUPTAKE,
     &NSUPPLY,NRELEASE,d_TW,d_RW,TW_Nopt,RW_Nopt,NSUF,SUB_N,
     &DRAIN_NCONC,DRAIN_N,CUMDRAIN_N,HARVDAY,RAIN_CUM_cm3,IRRIG_CUM_cm3,
     &ET_CUM_cm3,DRAIN_CUM_cm3,RUNOFF_CUM_cm3,RUN,
     &R_OFF_NCONC,RUNOFF_cm3,CRF_DAYS,CRF_N,PEAKDAYS,
     &AVGTEMP2,PEAKRATE,FINALDAYS,RELEASERATE,CUMNRELEASE,CRF_N_BAL,
     &VP,AVGFD2,CRF_TFAC,FERTDAYS,TMEAN,AVGTEMP,AVGFD,SIZE,NRELEASE2,
     &CRF_N2_BAL,REL_DAYS2,HARV_HT,POT_IRR_N,POT_IRR_N_CUM,
     &YR,PLT_YR,DOY,WSUF,FINISH,IS_FIRST,POT_DRAIN_cm,PTA,AVG_PTA,
     &POT_DRAIN_cm3,RAIN_CUM_cm,IRRIG_CUM_cm,ET_CUM_cm,DRAIN_CUM_cm,
     &RUNOFF_CUM_cm,NLOAD,NLOAD_CUM,ET_cm,DAY,THRU_N,
     &RUNOFF_N,CUMRUNOFF_N,HT,WIDTH,TMAXB,FCDR,DRF,RDT,TEMFACSO,IRSTAT,
     &NUPTAKE_CUM,TBIAS,P_TOP,P_ROOT,TW_Pact,RW_Pact,
     &TW_Popt,RW_Popt,PDEMAND,PUPTAKE,PSUPPLY,PSUF,PUPTAKE_CUM,
     &SUB_P,DRAIN_PCONC,DRAIN_P,PRELEASE,CUMDRAIN_P,R_OFF_PCONC,
     &RUNOFF_P,CUMRUNOFF_P,THRU_P,POT_IRR_P,POT_IRR_P_CUM,PLOAD_CUM,
     &CUMPRELEASE,CRF_P_BAL,CRF_P_BAL2,PRELEASE2,TOT_NOM,NOP)
!=======================================================================
      IMPLICIT NONE
      SAVE
      CHARACTER RUN*32,FNAME*24,OUT*48,OUTSUM*48,OUTNPLT*48,
     &OUTNLEACH*48,OUTNREL*48,FINISH*8,OUTSUMAREA*48,OUTTEMP*48,
     &OUTPLEACH*48,OUTPPLT*48,OUTPREL*48
      INTEGER DYNAMIC,INIT,RATE,INTEG,DAY,HARVDAY,CRF_DAYS,
     &REL_DAYS2,YR,PLT_YR,DOY,IS_FIRST,IRSTAT
   	PARAMETER (INIT=1,RATE=2,INTEG=3)
      REAL LAI,SW_cm,RAIN_cm,RUNOFF_cm3,RAIN_CUM_cm3,IRRIG_CUM_cm3,
     &RUNOFF_CUM_cm3,ET_CUM_cm3,POT_ET_cm,DRAIN_CUM_cm3,SWDUL_cm,
     &SWLL_cm,DRAIN_cm,POT_DRAIN_cm,NLOAD,NLOAD_CUM,
     &RUNOFF_cm,POT_IRRIG_cm,POT_RAIN_cm,LA,CF_IRR,TW,
     &DT,SOLAR,TMIN,TMAX,RTPF,RW,NRELEASE,N_TOP,
     &N_ROOT,TW_Nact,RW_Nact,NDEMAND,NUPTAKE,SUB_N,d_TW,d_RW,DRAIN_N,
     &DRAIN_NCONC,CUMNRELEASE,CUMDRAIN_N,POT_DRAIN_cm3,IRRIG_cm,NSUPPLY,
     &TW_Nopt,RW_Nopt,NSUF,R_OFF_NCONC,CRF_N,PEAKDAYS,PEAKRATE,
     &FINALDAYS,RELEASERATE,CRF_N_BAL,VP,CRF_TFAC,FERTDAYS,TMEAN,
     &AVGTEMP,AVGFD,SIZE,NRELEASE2,CRF_N2_BAL,AVGTEMP2,AVGFD2,
     &HARV_HT,POT_IRR_N,POT_IRR_N_CUM,ET_cm,WSUF,PTA,AVG_PTA,
     &RAIN_CUM_cm,IRRIG_CUM_cm,ET_CUM_cm,DRAIN_CUM_cm,RUNOFF_CUM_cm,
     &RUNOFF_N,THRU_N,CUMRUNOFF_N,HT,WIDTH,TMAXB,FCDR,DRF,RDT,TEMFACSO,
     &NUPTAKE_CUM,TBIAS,P_TOP,P_ROOT,TW_Pact,RW_Pact,TW_Popt,RW_Popt,
     &PDEMAND,PUPTAKE,PSUPPLY,PSUF,PUPTAKE_CUM,SUB_P,
     &DRAIN_PCONC,DRAIN_P,PRELEASE,CUMDRAIN_P,R_OFF_PCONC,
     &RUNOFF_P,CUMRUNOFF_P,THRU_P,POT_IRR_P,POT_IRR_P_CUM,PLOAD_CUM,
     &CUMPRELEASE,CRF_P_BAL,CRF_P_BAL2,PRELEASE2,TOT_NOM,NOP
      
*************************INITIALIZATION*******************************
	IF (DYNAMIC.EQ.INIT) THEN
	  OUT=trim(run)//'_dailyoutput.txt'
        OUTSUM=trim(run)//'_summaryoutput.txt'
        OUTNPLT=trim(run)//'_Nplantoutput.txt'
        OUTNLEACH=trim(run)//'_Nleachoutput.txt'
        OUTNREL=trim(run)//'_NReloutput.txt'
        OUTSUMAREA=trim(run)//'_summaryarea.txt'
        OUTTEMP=trim(run)//'_tempoutput.txt'
        OUTPPLT=trim(run)//'_Pplantoutput.txt'
        OUTPLEACH=trim(run)//'_Pleachoutput.txt'
        OUTPREL=trim(run)//'_PReloutput.txt'
        
	  OPEN(UNIT=7,FILE=OUT,STATUS='UNKNOWN') 
        OPEN(UNIT=8,FILE=OUTSUM,STATUS='UNKNOWN')
        OPEN(UNIT=9,FILE=OUTNPLT,STATUS='UNKNOWN')
        OPEN(UNIT=10,FILE=OUTNLEACH,STATUS='UNKNOWN')
        OPEN(UNIT=11,FILE=OUTNREL,STATUS='UNKNOWN') 
        OPEN(UNIT=12,FILE=OUTSUMAREA,STATUS='UNKNOWN')
        OPEN(UNIT=13,FILE=OUTTEMP,STATUS='UNKNOWN')
        OPEN(UNIT=14,FILE=OUTPPLT,STATUS='UNKNOWN')
        OPEN(UNIT=15,FILE=OUTPLEACH,STATUS='UNKNOWN')
        OPEN(UNIT=16,FILE=OUTPREL,STATUS='UNKNOWN')
        
      IF (IS_FIRST.EQ.1) THEN
	  WRITE(7,*)'Run: ',RUN
	  WRITE(7,7)
	  WRITE(8,*)'Run: ',RUN
	  WRITE(8,7)
	  WRITE(9,*)'Run: ',RUN
	  WRITE(9,7)
	  WRITE(10,*)'Run: ',RUN
	  WRITE(10,7)
	  WRITE(11,*)'Run: ',RUN
	  WRITE(11,7)
	  WRITE(11,*)'CRF_N  CRF_DAYS'
	  WRITE(11,1)CRF_N,CRF_DAYS
	  WRITE(11,7)
	  WRITE(12,*)'RUN:  ',RUN
	  WRITE(12,7)
	  WRITE(13,*)'RUN:  ',RUN
	  WRITE(13,7)
	  WRITE(14,*)'RUN:  ',RUN
	  WRITE(14,7)
	  WRITE(15,*)'RUN:  ',RUN
	  WRITE(15,7)
	  WRITE(16,*)'RUN:  ',RUN
	  WRITE(16,7)
	  WRITE(7,10)
	  WRITE(8,20)
	  WRITE(9,30)
	  WRITE(10,40)
	  WRITE(11,50)
	  WRITE(12,60)
	  WRITE(13,70)
	  WRITE(14,80)
	  WRITE(15,90)
	  WRITE(16,100)
	  
      ENDIF
	     
C  ****************************RATE CALCULATIONS*****************************
		ELSEIF (DYNAMIC.EQ.RATE) THEN

C *********************INTEGRATION CALCULATIONS********************************
		ELSEIF (DYNAMIC.EQ.INTEG) THEN
      
      WRITE(7,11)YR,DOY,DAY,LA,LAI,TW,RW,DT,SOLAR,TMIN,TMAX,RAIN_cm,    !dailyoutput
     &IRRIG_cm,CF_IRR,POT_RAIN_cm,POT_IRRIG_cm,SW_cm,POT_DRAIN_cm,
     &RUNOFF_cm,POT_ET_cm,HT,ET_cm,PTA
    
      WRITE(9,31)YR,DOY,DAY,d_TW,d_RW,TW,RW,N_TOP,N_ROOT,TW_Nact*100,   !Nplantoutput
     &RW_Nact*100,TW_Nopt*100,RW_Nopt*100,NDEMAND,NUPTAKE,NSUPPLY,NSUF,
     &WSUF,NUPTAKE_CUM
      
      WRITE(10,41)YR,DOY,DAY,SUB_N,POT_DRAIN_cm3,DRAIN_NCONC,DRAIN_N,   !Nleachout
     &NRELEASE,NUPTAKE,DRAIN_CUM_cm3,CUMDRAIN_N,RUNOFF_cm3,R_OFF_NCONC,
     &RUNOFF_N,CUMRUNOFF_N,THRU_N,POT_IRR_N, POT_IRR_N_CUM

      WRITE(11,51)YR,DOY,DAY,TMEAN,VP,CRF_TFAC,RELEASERATE,NRELEASE,    !NReloutput
     &CUMNRELEASE,CRF_N_BAL,NDEMAND,FERTDAYS,AVGTEMP,AVGFD,NRELEASE2,
     &CRF_N2_BAL,AVGTEMP2,AVGFD2,SUB_N,REL_DAYS2
     
      WRITE(13,71)YR,DOY,DAY,TMIN,TMAX,TMAXB,TBIAS,SOLAR,FCDR,DRF,RDT,  !tempoutput
     &DT,TEMFACSO,RW,TW,LA,LAI
     
      WRITE(14,81)YR,DOY,DAY,d_TW,d_RW,TW,RW,P_TOP,P_ROOT,              !Pplantoutput
     &TW_Pact*100,RW_Pact*100,TW_Popt*100,RW_Popt*100,PDEMAND,PUPTAKE,
     &PSUPPLY,PSUF,WSUF,PUPTAKE_CUM
      
      WRITE(15,91)YR,DOY,DAY,SUB_P,POT_DRAIN_cm3,DRAIN_PCONC,DRAIN_P,   !Pleachout
     &PRELEASE,PDEMAND,DRAIN_CUM_cm3,CUMDRAIN_P,RUNOFF_cm3,R_OFF_PCONC,
     &RUNOFF_P,CUMRUNOFF_P,THRU_P,POT_IRR_P, POT_IRR_P_CUM
      
      WRITE(16,101)YR,DOY,DAY,TMEAN,VP,CRF_TFAC,RELEASERATE,PRELEASE,    !PReloutput
     &CUMPRELEASE,CRF_P_BAL,PDEMAND,FERTDAYS,AVGTEMP,AVGFD,PRELEASE2,
     &CRF_P_BAL2,AVGTEMP2,AVGFD2,SUB_P,REL_DAYS2
           
      IF((FINISH.eq.'SIZE'.and.(HT.ge.HARV_HT.OR.NSUF.eq.0.05)).or.
     &(FINISH.eq.'FIXED'.and.DAY.eq.HARVDAY).or.
     &IRSTAT.eq.-1) THEN
     
        WRITE(8,21)PLT_YR,DOY,DAY,RAIN_CUM_cm3/1000,IRRIG_CUM_cm3/1000, !summaryoutput
     &ET_CUM_cm3/1000,DRAIN_CUM_cm3/1000,RUNOFF_CUM_cm3/1000,           !all converted to liters
     &CUMRUNOFF_N,CUMRUNOFF_N*1000000/RUNOFF_CUM_cm3,HT,TW,AVG_PTA,
     &NUPTAKE_CUM,CUMRUNOFF_P,CUMRUNOFF_P*1000000/RUNOFF_CUM_cm3,
     &PUPTAKE_CUM,NSUF,REL_DAYS2,TOT_NOM,NOP
     
        WRITE(12,61)PLT_YR,DOY,DAY,RAIN_CUM_cm,IRRIG_CUM_cm,ET_CUM_cm, !summaryarea
     &DRAIN_CUM_cm,RUNOFF_CUM_cm,NLOAD_CUM,PLOAD_CUM
        
      ENDIF
      
      ENDIF
	
1     FORMAT(F5.2,3x,I3)
7     FORMAT(' ')	

10	FORMAT('  YR DOY DAY      LA   LAI    TW    RW     DT SRad
     & TMIN TMAX    Ra   Ir   CF  P_Ra  P_Ir    Sw  P_Dr    Ro P_Et
     &    HT   Et   P_Ar')
11	FORMAT(I4,x,I3,x,I3,x,F7.1,x,F5.2,x,F5.1,x,F5.1,x,F6.1,x,F4.1,
     &x,F4.1,x,F4.1,F6.2,x,F4.2,x,F4.2,x,F5.2,x,F5.2,x,F5.3,x,F5.2,
     &x,F5.2,x,F4.2,x,F5.1,x,F4.2,x,F6.1)
  
20	FORMAT('  YR DOY DAY   Rain  Irrig     ET  Drain 
     &Runoff Nloss  NConc    HT    TW  P_Area NupCum  Ploss  PConc 
     &PupCum NSUF RD2 NOM NOP')
     
21	FORMAT (I4,x,I3,x,I3,x,F6.2,x,F6.2,x,F6.2,x,F6.2,x,F6.2,F6.2,x,
     &F6.2,x,F5.1,x,F5.1,x,F7.1,x,F6.2,x,F6.2,x,F6.2,x,F6.3,x,F5.3,x,
     &I3,x,F5.1,x,F5.1)

30    FORMAT ('  YR DOY Day     d_TW     d_RW    TW    RW    N_TOP 
     &  N_ROOT Tact Ract Topt Ropt  NDemand  NUptake  NSupply  NSUF
     &  WSUF NupCum')
     
31    FORMAT (I4,x,I3,x,I3,x,F8.6,x,F8.6,x,F5.1,x,F5.1,x,F8.6,x,F8.6,x,
     &F4.2,x,F4.2,x,F4.2,x,F4.2,x,F8.6,x,F8.6,x,F8.6,x,F5.3,x,
     &F5.3,x,F6.3)
     
40    FORMAT ('  YR DOY DAY     SubN  DrCm3  DrNconc   DrainN NRelease
     &  NUptake    CumDr   CumDrN  ROcm3 RO_Nconc     Ro_N   CumRoN
     &   THRU_N      IrN   CumIrN')
     
41    FORMAT (I4,x,I3,x,I3,x,F8.6,x,F6.1,x,F8.6,x,F8.6,x,F8.6,x,F8.6,x,
     &F8.1,x,F8.6,x,F6.1,x,F8.6,x,F8.6,x,F8.6,x,F8.6,x,F8.6,x,F8.6)
     
50    FORMAT ('  YR DOY DAY Tmean    VP  TFac     Rate     NRel  
     & CumNRel     CRF_N  NDemand    FD  AvT  AvFD    NRel2   CRF_N2
     & AvT2   FD2    SUB_N  RD2')
     
51    FORMAT (I4,x,I3,x,I3,x,F5.2,x,F5.3,x,F5.3,x,F8.6,x,F8.6,x,F9.6,x,
     &F9.6,x,F8.6,x,F5.1,x,F4.1,x,F5.1,x,F8.6,x,F8.6,x,F4.1,x,F5.1,x,
     &F8.6,x,I4)
     
60    FORMAT ('  YR DOY DAY   Rain   Irr    ET Drain  Roff NLoad PLoad')

61    FORMAT (I4,x,I3,x,I3,x,5F6.1,x,F5.1,x,F5.1)
      
70    FORMAT ('  YR DOY DAY   TMIN  TMAX TMAXB TBIAS  SRAD  FCDR   DRF 
     &  RDT    DT TEMF    RW    TW      LA  LAI')

71    FORMAT (I4,x,I3,x,I3,x,9F6.1,F5.2,x,F5.1,x,F5.1,x,F7.1,x,F4.1)

80    FORMAT ('  YR DOY Day     d_TW     d_RW    TW    RW    P_TOP 
     &  P_ROOT Tact Ract Topt Ropt  PDemand  PUptake  PSupply  PSUF
     &  WSUF PupCum')
     
81    FORMAT (I4,x,I3,x,I3,x,F8.6,x,F8.6,x,F5.1,x,F5.1,x,F8.6,x,F8.6,x,
     &F4.2,x,F4.2,x,F4.2,x,F4.2,x,F8.6,x,F8.6,x,F8.6,x,F5.3,x,
     &F5.3,x,F6.3)

90    FORMAT ('  YR DOY DAY     SubP  DrCm3  DrPconc   DrainP PRelease
     &  PUptake     CumDr  CumDrP  ROcm3 RO_Pconc     Ro_P   CumRoP
     &   THRU_P      IrP   CumIrP')
     
91    FORMAT (I4,x,I3,x,I3,x,F8.6,x,F6.1,x,F8.6,x,F8.6,x,F8.6,x,F8.6,x,
     &F8.1,x,F8.6,x,F6.1,x,F8.6,x,F8.6,x,F8.6,x,F8.6,x,F8.6,x,F8.6)
     
100   FORMAT ('  YR DOY DAY Tmean    VP  TFac     Rate     PRel  
     & CumPRel     CRF_P  PDemand    FD  AvT  AvFD    PRel2   CRF_P2
     & AvT2   FD2    SUB_P  RD2')
     
101   FORMAT (I4,x,I3,x,I3,x,F5.2,x,F5.3,x,F5.3,x,F8.6,x,F8.6,x,F9.6,x,
     &F9.6,x,F8.6,x,F5.1,x,F4.1,x,F5.1,x,F8.6,x,F8.6,x,F4.1,x,F5.1,x,
     &F8.6,x,I4)
     
C *****************END INTEGRATION CALCULATIONS********************************

900   CONTINUE
      
	RETURN

      END	SUBROUTINE Output
