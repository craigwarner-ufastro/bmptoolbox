!=======================================================================
!	Nutrient Subroutine for Container Production Model 
!	J. Ritchie, G. Schoene, T. Yeager...et al
!	
!	4/27/05 coding Started M. Bostick
!	5/9/05 delivered M. Bostick
!    8/23/06 CRF release and N leaching code JR,MB,JM
!    11/03/06 new NSUF, NSUPPLY, N conc code JR,JM
!    11/27/06 temp modification of N release JR,JM
!    11/30/06 topdress code  JM
!    7-3-09 N leaching mods JR,JM
!    4-26-10 phosphorus added JM
!=======================================================================
	SUBROUTINE Nutrient(DYNAMIC,CRF_N,CRF_DAYS,NRELEASE,CUMNRELEASE,
     &RELEASERATE,FERT_N,SUB_N,FERT,PCT_N,PCT_CRN,SWDUL_cm3,NUPTAKE,
     &DRAIN_N,DRAIN_NCONC,CUMDRAIN_N,POT_DRAIN_cm3,REL_DAYS,PEAKDAYS,
     &FINALDAYS,PEAKRATE,R_OFF_NCONC,RUNOFF_cm3,
     &CRF_N_BAL,TMEAN,VP,CRF_TFAC,FERTDAYS,AVGFD,FERT2,CRF_N2,
     &CRF_DAYS2,NRELEASE2,CUMNRELEASE2,RELEASERATE2,FERT_N2,PCT_N2,
     &PCT_CRN2,PEAKDAYS2,FINALDAYS2,PEAKRATE2,
     &CRF_N2_BAL,FERTDAYS2,AVGFD2,TD_DAY,REL_DAYS2,TD_TF,NDEMAND,
     &DAY,YR,DOY,NLOAD,NLOAD_CUM,PTA,THRU_IRR_cm3,IRR_NCONC,
     &RUNOFF_N,THRU_N,CUMRUNOFF_N,H2O_NCONC,SF,SF_START,SF_END,SF_INT,
     &SF_NCONC,TDF,SF_TF,DRPV,PVLF,TBIAS,SWLL_cm3,TP_cm3,SFYR,SFDOY,
     &SFSTAT,NEXTSF,PCT_P,PCT_K,PCT_P2,PCT_K2,H2O_PCONC,SF_PCONC,SUB_P,
     &DRAIN_P,PRELEASE,CUMPRELEASE,PRELEASE2,CUMPRELEASE2,
     &DRAIN_PCONC,CUMDRAIN_P,R_OFF_PCONC,RUNOFF_P,CRF_P_BAL,CRF_P_BAL2,
     &CUMRUNOFF_P,THRU_P,POT_IRR_P,POT_IRR_P_CUM,PLOAD_CUM)
      
!=======================================================================
      IMPLICIT NONE
      SAVE
      CHARACTER TDF*8,SF*8
      INTEGER DYNAMIC,INIT,RATE,INTEG,CRF_DAYS,CRF_DAYS2,TD_DAY,
     &DAY,TD_TF,REL_DAYS,REL_DAYS2,YR,DOY,SFYR,SFDOY,SFSTAT,NEXTSF
	PARAMETER (INIT=1,RATE=2,INTEG=3)
      REAL CRF_N,NRELEASE,CUMNRELEASE,PEAKDAYS,PEAKRATE,UPCOEFF,
     &DNCOEFF,RELEASERATE,FINALDAYS,FERT_N,SUB_N,FERT,PCT_N,PCT_CRN,
     &SWDUL_cm3,NUPTAKE,DRAIN_N,DRAIN_NCONC,CUMDRAIN_N,POT_DRAIN_cm3,
     &R_OFF_NCONC,RUNOFF_cm3,CRF_N_BAL,TMEAN,VP,CRF_TFAC,FERTDAYS,
     &AVGFD,FERT2,CRF_N2,CUMNRELEASE2,RELEASERATE2,NRELEASE2,FERT_N2,
     &PCT_N2,PCT_CRN2,PEAKDAYS2,UPCOEFF2,DNCOEFF2,FINALDAYS2,
     &PEAKRATE2,CRF_N2_BAL,FERTDAYS2,AVGFD2,TD_UPFAC,NDEMAND,NLOAD,
     &NLOAD_CUM,PTA,THRU_N,THRU_IRR_cm3,IRR_NCONC,RUNOFF_N,CUMTHRU_N,
     &CUMRUNOFF_N,POT_IRR_N,POT_IRR_N_CUM,SF_UPFAC,H2O_NCONC,
     &SF_START,SF_END,SF_INT,SF_NCONC,SF_TF,SF_DAY,DRPV,PVLF,
     &NLF,NCONC_MAX,TBIAS,SWLL_cm3,TP_cm3,
     &PCT_P,PCT_K,FERT_P,FERT_K,CRF_P,CRF_K,SUB_P,SUB_K,
     &PCT_P2,PCT_K2,FERT_P2,FERT_K2,CRF_P2,CRF_K2,PRELEASE,PRELEASE2,
     &CRF_P_BAL,PCONC_MAX,DRAIN_P,PLF,DRAIN_PCONC,THRU_P,RUNOFF_P,
     &IRR_PCONC,H2O_PCONC,SF_PCONC,CUMDRAIN_P,CUMTHRU_P,CUMRUNOFF_P,
     &PLOAD_CUM,CRF_P_BAL2,CUMPRELEASE,CUMPRELEASE2,PLOAD,POT_IRR_P_CUM,
     &R_OFF_PCONC,PSUP_MAX1,SUB_PCONC,POT_IRR_P
     
C*************************INITIALIZATION*******************************
	IF (DYNAMIC.EQ.INIT) THEN
	FERT_N=FERT*PCT_N*0.01      !grams total nutrient per container
	FERT_P=FERT*PCT_P*0.01
	FERT_K=FERT*PCT_K*0.01
	
      CRF_N=FERT*PCT_CRN*0.01    !grams of coated nutrient per container
      CRF_P=FERT_P*(PCT_CRN/PCT_N)
      CRF_K=FERT_K*(PCT_CRN/PCT_N)
      
      SUB_N=FERT_N-CRF_N          !grams of uncoated nutrient per container
      SUB_P=FERT_P-CRF_P
      SUB_K=FERT_K-CRF_K
      
      FERT_N2=FERT2*PCT_N2*0.01     !grams total nutrient per container in topdress fert
      FERT_P2=FERT2*PCT_P2*0.01
	FERT_K2=FERT2*PCT_K2*0.01
      
      CRF_N2=FERT_N2*PCT_CRN2*0.01     !grams of coated N per container in topdress fert
      CRF_P2=FERT_P2*(PCT_CRN2/PCT_N2)
      CRF_K2=FERT_K2*(PCT_CRN2/PCT_N2)
     
            POT_IRR_N_CUM=0   !running tally of N applied to container through irrigation water
      POT_IRR_P_CUM=0
      DRAIN_N=0         !grams of N in pot drainage
      DRAIN_P=0
      REL_DAYS=0        !no. of days since CRF addition
      AVGFD=0          !running avg. of FERTDAYS of initial CRF appl.
      AVGFD2=0         !running avg. of FERTDAYS of topdress CRF appl.
      CUMNRELEASE=0     !running tally of N release (g/container)
      CUMPRELEASE=0
      CUMNRELEASE2=0    !cum. N release of top dress application (g/container0
      CUMPRELEASE2=0
      CUMDRAIN_N=0      !running tally of N loss in drainage (g/container)
      CUMDRAIN_P=0
      CRF_N2_BAL=0
      CRF_P_BAL2=0
      REL_DAYS2=-99
      CUMTHRU_N=0
      CUMTHRU_P=0
      CUMRUNOFF_N=0
      CUMRUNOFF_P=0
	NLOAD_CUM=0
	PLOAD_CUM=0
	
	IF(SF.eq.'TRIG')THEN
	  SF_START=0
	  SF_UPFAC=NDEMAND*SF_TF*.01
	ENDIF
	
C****************************RATE CALCULATIONS*****************************
	ELSEIF (DYNAMIC.EQ.RATE) THEN
		
C----------Determine N release from CRF----------
      VP=0.611*exp(17.27*TBIAS/(TBIAS+237.3))   !vapor pressure in kPa  JR 11/27/06
      CRF_TFAC=0.5225+0.2109*VP         !temperature factor for CRF release JR 11/27/06
      FERTDAYS=CRF_DAYS*1/CRF_TFAC     !today's temperature-modified CRF rating
      REL_DAYS=REL_DAYS+1       !days from CRF application
      AVGFD=(AVGFD*(REL_DAYS-1)+FERTDAYS)/REL_DAYS     !running avg
      PEAKDAYS=0.2947*FERTDAYS    !days to peak release of N from CRF related to CRF rating 
      PEAKRATE=0.35/PEAKDAYS      !fractional release of CRF_N at PEAKDAYS 
      UPCOEFF=0.175/PEAKDAYS**2   !used to describe CRF release during period before PEAKDAYS
      DNCOEFF=(0.625-PEAKRATE*(FERTDAYS-PEAKDAYS))/
     &  (FERTDAYS-PEAKDAYS)**2      !used to describe CRF release during period after PEAKDAYS
      FINALDAYS=PEAKDAYS-PEAKRATE/(2*DNCOEFF) !days to 100% release of CRF_N
      
C------Start topdress application if FIXED date specified in p_spec file------     
      IF(TDF.eq.'FIXED'.and.DAY.eq.TD_DAY)THEN
        REL_DAYS2=0          !initiate days from topdress application
        SUB_N=SUB_N+FERT_N2-CRF_N2
        SUB_P=SUB_P+FERT_P2-CRF_P2
      ENDIF
      
C-------Start supplemental solution feed--------- 
      IF(SF.eq.'FILE')THEN
        IF (YR.eq.SFYR.and.DOY.eq.SFDOY.and.SFSTAT.eq.0) THEN
          !Year and doy match next line in file
          IRR_NCONC=SF_NCONC
          IRR_PCONC=SF_PCONC
          NEXTSF=1 
        ELSE
          IRR_NCONC=H2O_NCONC
          IRR_PCONC=H2O_PCONC
        ENDIF
      ELSEIF(SF.ne.'NONE'.and.DAY.eq.SF_START.and.DAY.lt.SF_END)THEN
        IRR_NCONC=SF_NCONC
        IRR_PCONC=SF_PCONC
        SF_START=SF_START+SF_INT
      ELSE
        IRR_NCONC=H2O_NCONC
        IRR_PCONC=H2O_PCONC
      ENDIF
          
C-------Start topdress nutrient release functions------------       
      IF(REL_DAYS2.ne.-99) THEN
        REL_DAYS2=REL_DAYS2+1     !days from topdress CRF application
        CRF_N2_BAL=CRF_N2
        CRF_P_BAL2=CRF_P2
      ENDIF
      
      IF(REL_DAYS2.ge.1) THEN
        FERTDAYS2=CRF_DAYS2*1/CRF_TFAC     !today's temperature-modified CRF rating
        AVGFD2=(AVGFD2*(REL_DAYS2-1)+FERTDAYS2)/REL_DAYS2     !running avg
        PEAKDAYS2=0.2947*FERTDAYS2    !days to peak release of N from CRF related to CRF rating 
        PEAKRATE2=0.35/PEAKDAYS2      !fractional release of CRF_N at PEAKDAYS 
        UPCOEFF2=0.175/PEAKDAYS2**2   !used to describe CRF release during period before PEAKDAYS
        DNCOEFF2=(0.625-PEAKRATE2*(FERTDAYS2-PEAKDAYS2))/
     &  (FERTDAYS-PEAKDAYS)**2      !used to describe CRF release during period after PEAKDAYS
        FINALDAYS2=PEAKDAYS2-PEAKRATE2/(2*DNCOEFF2) !days to 100% release of CRF_N
      ENDIF
       
C-----Determine N release rate for start-up CRF       
      IF(CRF_N_BAL.GT.0.and.REL_DAYS.lt.FINALDAYS) THEN       
	  IF(REL_DAYS.LT.PEAKDAYS) THEN
	    RELEASERATE=2*UPCOEFF*REL_DAYS   !fractional release before PEAKDAYS
	  ELSE
          RELEASERATE=PEAKRATE+2*DNCOEFF*(REL_DAYS-PEAKDAYS) !fractional release after PEAKDAYS
        ENDIF
	ELSE
	  RELEASERATE=0    !fraction release is 0 after FINALDAYS reached
	ENDIF
	
C--------Determine N release rate for topdress CRF	
	IF(CRF_N2_BAL.GT.0.and.REL_DAYS2.gt.0.and.REL_DAYS2.lt.
     & FINALDAYS) THEN
	  IF(REL_DAYS2.LT.PEAKDAYS2) THEN
	    RELEASERATE2=2*UPCOEFF2*REL_DAYS2 !fractional release before PEAKDAYS
	  ELSE
          RELEASERATE2=PEAKRATE2+2*DNCOEFF2*(REL_DAYS2-PEAKDAYS2) !fractional release after PEAKDAYS
        ENDIF
	ELSE
	  RELEASERATE2=0    !fraction release is 0 after FINALDAYS reached
	ENDIF
	
C-------Determine N release from start-up and/or topdress CRF      
	NRELEASE=CRF_N*RELEASERATE       !g N per container per day
	NRELEASE2=CRF_N2*RELEASERATE2    !g N per container per day
	
	PRELEASE=CRF_P*RELEASERATE       !g P per container per day
	PRELEASE2=CRF_P2*RELEASERATE2    !g P per container per day
	
C *********************INTEGRATION CALCULATIONS********************************
	ELSEIF (DYNAMIC.EQ.INTEG) THEN
			
C----------Determine N Leaching--------- JR,JM 7-3-09
      NCONC_MAX=SUB_N/(TP_cm3-SWLL_cm3)
      NLF=0.5*exp(-PVLF*DRPV)
      DRAIN_N=NLF*NCONC_MAX*POT_DRAIN_cm3
      DRAIN_NCONC=max(DRAIN_N/POT_DRAIN_cm3,0.)
      THRU_N=THRU_IRR_cm3*IRR_NCONC*0.000001
      RUNOFF_N=DRAIN_N+THRU_N         !accounts for both drainage and N in unintercepted irrigation
      CUMDRAIN_N=CUMDRAIN_N+DRAIN_N       !running tally of CRF N Release
      CUMTHRU_N=CUMTHRU_N+THRU_N
      CUMRUNOFF_N=CUMRUNOFF_N+RUNOFF_N
      NLOAD=RUNOFF_N*10000/PTA             !convert to g/m2		    
      NLOAD_CUM=NLOAD_CUM+NLOAD
      
C----------Determine P Leaching--------- 
      PCONC_MAX=SUB_P/(TP_cm3-SWLL_cm3)
      PLF=0.25*exp(-3.5*DRPV)
      DRAIN_P=PLF*PCONC_MAX*POT_DRAIN_cm3
      DRAIN_PCONC=max(DRAIN_P/POT_DRAIN_cm3,0.)
      THRU_P=THRU_IRR_cm3*IRR_PCONC*0.000001
      RUNOFF_P=DRAIN_P+THRU_P 
      CUMDRAIN_P=CUMDRAIN_P+DRAIN_P       !running tally of CRF N Release
      CUMTHRU_P=CUMTHRU_P+THRU_P
      CUMRUNOFF_P=CUMRUNOFF_P+RUNOFF_P
      PLOAD=RUNOFF_P*10000/PTA             !convert to g/m2		    
      PLOAD_CUM=PLOAD_CUM+PLOAD
    
      IF(RUNOFF_cm3.gt.0) THEN
	  R_OFF_NCONC=DRAIN_N/RUNOFF_cm3
	  R_OFF_PCONC=DRAIN_P/RUNOFF_cm3
	ELSE 
	  R_OFF_NCONC=0
	  R_OFF_PCONC=0
	ENDIF
	
	CUMNRELEASE=CUMNRELEASE+NRELEASE    !running tally of CRF N Release
	CUMNRELEASE2=CUMNRELEASE2+NRELEASE2    !running tally of CRF N Release
	CRF_N_BAL=CRF_N-CUMNRELEASE
	
	CUMPRELEASE=CUMPRELEASE+PRELEASE    !running tally of CRF P Release
	CUMNRELEASE2=CUMPRELEASE2+PRELEASE2   !running tally of sidedress CRF P Release
	CRF_P_BAL=CRF_P-CUMPRELEASE
	
	IF(CUMNRELEASE2.gt.0)THEN
	  CRF_N2_BAL=CRF_N2-CUMNRELEASE2
	  CRF_P_BAL2=CRF_P2-CUMPRELEASE2
	ENDIF
      
C------Determine need for topdress------
      IF(TDF.eq.'TRIG') THEN
        TD_UPFAC=NDEMAND*TD_TF*.01
        IF(NRELEASE.lt.TD_UPFAC.and.DAY.gt.30.and.SUB_N.lt.NDEMAND.and.
     &    REL_DAYS2.eq.-99) THEN
          REL_DAYS2=0
          SUB_N=SUB_N+FERT_N2-CRF_N2
          SUB_P=SUB_P+FERT_P2-CRF_P2
        ENDIF
      ENDIF
      
      IF(SF.eq.'TRIG'.and.SF_START.eq.0) THEN
        IF(NRELEASE.lt.SF_UPFAC.and.DAY.gt.30.and.SUB_N.lt.NDEMAND) THEN
          SF_START=DAY+1
        ENDIF
      ENDIF
     
	ENDIF
	
C *****************END INTEGRATION CALCULATIONS********************************

900   CONTINUE
      RETURN
      END	SUBROUTINE Nutrient
