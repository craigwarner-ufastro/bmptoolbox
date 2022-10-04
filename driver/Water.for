!=======================================================================
!     Waterbalance Subroutine for Container Production Model 
!	J. Ritchie, G. Schoene, T. Yeager...et al
!	
!	4/27/05 coding started M. Bostick
!	5/9/05 delivered M. Bostick
!    5/9/06 new potential ET code JR
!    11/30/06 new irrig scheduling input JM
!    2/6/07 new water balance code for using rain better JM
!    3/8/07 rework evapotranspiration code JR
!    4/4/07 rework ESO code JM
!    4/10/07 more ET code changes JM/JR
!    5/22/07 more ET code changes JR,JM
!    7-3-09  ET mods  JR,JM
!    12-21-09 ET mods eliminating aerodynamic component JR
!    4-2-10 WSUF mod eliminating SW_DF JR,JM
!    7-3-10 RAIN_EF to distinguish rain effectiveness JM,JR 
!    2-8-11 ET mods JR
!    2-13-12 MAD schedule bug fix
!=======================================================================
	SUBROUTINE WaterBalance(DYNAMIC,SOLAR,TMEAN,BTMEAN,RAIN_cm,
     &WFNAME,LA,DAY,YR,DOY,PRESS,RAINCUT,CF_IRR,CF_RAIN,MAD_TF,
     &POT_TOPAREA,WSUF,SCHED,PTA,AREA_RATIO,SWLL,SWDUL,SW,SW_cm,S_VOL,
     &CROPEC,MAD,IRRIG,D_IRR,IRRIG_CUM_cm3,RUNOFF_CUM_cm3,ET_CUM_cm3,
     &DRAIN_CUM_cm3,RAIN_CUM_cm3,RUNOFF_cm3,IRRIG_cm3,SWLL_cm3,
     &SWDUL_cm3,THRU_cm3,A_SW_cm3,SWA_cm3,SW_cm3,POT_DRAIN_cm3,
     &POT_IRRIG_cm3,THRU_IRR_cm3,SWLL_cm,DRAIN_cm,RUNOFF_cm,
     &POT_IRRIG_cm,POT_ET_cm,SWDUL_cm,POT_RAIN_cm,IRRIG_cm,ET_cm,TMIN,
     &TMAX,Y_RAIN_cm,POT_DRAIN_cm,DRAIN_CUM_cm,ET_CUM_cm,IRRIG_CUM_cm,
     &RAIN_CUM_cm,RUNOFF_CUM_cm,IRYR,IRDOY,IRSTAT,NEXTIR,TP,DRPV,TBIAS,
     &TP_cm3,LAI,SW_TF,MAD_DIF,CDR,RAIN_EF)
!=======================================================================
      IMPLICIT NONE
      SAVE
      CHARACTER WFNAME*24,SCHED*8,RAINCUT*4
      INTEGER DYNAMIC,INIT,RATE,INTEG,YR,DOY,DAY,MAD,IRYR,IRDOY,
     &IRSTAT,NEXTIR
	PARAMETER (INIT=1,RATE=2,INTEG=3)
      REAL SWDUL,SW,SWLL,RAIN_cm,EPO,POT_TOPAREA,TOTAL_RAIN_cm3,ESO,LA,
     &SOLAR,POT_IRRIG_cm3,IRRIG,TMEAN,BTMEAN,ALT,PRESS,CF_RAIN,RADCOM,
     &LATHEAT,ESO_cm3,RUNOFF_cm3,CF_IRR,SWLL_cm,SWDUL_cm,
     &SW_cm,EPO_cm3,IRRIG_CUM_cm3,RUNOFF_CUM_cm3,IRRIG_cm3,
     &RAIN_CUM_cm3,CROPEC,S_VOL,POT_RAIN_cm3,
     &PTA,AREA_RATIO,A_SW_cm3,SW_cm3,SWLL_cm3,SWDUL_cm3,
     &POT_DRAIN_cm3,ET_cm3,MAD_TF,DRAIN_CUM_cm3,
     &ET_CUM_cm3,DRAIN_cm,RUNOFF_cm,POT_IRRIG_cm,D_IRR,POT_ET_cm,
     &POT_RAIN_cm,WSUF,IRRIG_cm,ET_cm,
     &TMIN,TMAX,GAMMA,DELTA,Y_RAIN_cm,SWA_cm3,
     &EP_cm3,ES_cm3,THRU_cm3,TOTAL_IRRIG_cm3,POT_DRAIN_cm,
     &THRU_CUM_cm3,THRU_cm,THRU_CUM_cm,DRAIN_CUM_cm,ET_CUM_cm,
     &IRRIG_CUM_cm,RAIN_CUM_cm,RUNOFF_CUM_cm,THRU_IRR_cm3,TP,DRPV,TP_cm3
     &,TBIAS,ETO,ETO_cm3,LAI,A_SWavg_cm3,SW_TF,MAD_DIF,
     &CDR,RADCOM_CDR,EPO_MAD,EPO_MAD_cm3,SW_DIF_cm3,MAD_SW_cm3,RAIN_EF,
     &POT_RAIN1_cm3,POT_RAIN2_cm3,RUNOFF1_cm3,RUNOFF2_cm3,
     &POT_DRAIN1_cm3,POT_DRAIN2_cm3,AEROCOMS,AEROCOMP,TDAY,VPD,EO,
     EO_cm3
     
*************************INITIALIZATION*******************************
	IF (DYNAMIC.EQ.INIT) THEN
	CF_IRR=1.0
	CF_RAIN=1.0
	Y_RAIN_cm=0
	IRRIG_cm3=0
	SWLL_cm3=SWLL*S_VOL             
	SWDUL_cm3=SWDUL*S_VOL
	
	TP_cm3=TP*S_VOL
	DRPV=0
	
	SWA_cm3=SWDUL_cm3-SWLL_cm3
	SWDUL_cm=SWDUL_cm3/POT_TOPAREA
	SWLL_cm=SWLL_cm3/POT_TOPAREA
	
	RAIN_CUM_cm3=0		
	IRRIG_CUM_cm3=0
	RUNOFF_CUM_cm3=0
	DRAIN_CUM_cm3=0
	ET_CUM_cm3=0
	WSUF=1
	
	MAD_TF=1-MAD*0.01
	
	SW_cm3=SW*S_VOL
	A_SW_cm3=SW_cm3-SWLL_cm3  !available substrate water content
      SW_cm=SW_cm3/POT_TOPAREA
      MAD_SW_cm3=MAD_TF*SWA_cm3
      SW_DIF_cm3=SWDUL_cm3-(MAD_DIF*0.01*SWA_cm3) !for MAD irrigation JM 4-15-10
            
      POT_DRAIN_cm3=0
	RUNOFF_cm3=0
	
	RAIN_CUM_cm=0		
	IRRIG_CUM_cm=0
	ET_CUM_cm=0
	RUNOFF_CUM_cm=0
	DRAIN_CUM_cm=0
	THRU_CUM_cm=0
			
C  ****************************RATE CALCULATIONS*****************************
	 ELSEIF (DYNAMIC.EQ.RATE) THEN
      
C  ----------Determine potential evaporation 3-2-07 -------------  
      LATHEAT=(25.01-TBIAS/42.3)  ! This is for cm/day units
      GAMMA=0.0674*PRESS          ! JR 4-23-10
      
      DELTA=2503*EXP(17.27*TBIAS/(TBIAS+237.3))/(TBIAS+237.3)**2
      RADCOM=DELTA/(DELTA+GAMMA)*SOLAR*0.6/LATHEAT
      RADCOM_CDR=DELTA/(DELTA+GAMMA)*CDR*0.6/LATHEAT    !for max EP
      TDAY=TMAX*0.75+TMIN*0.25
      VPD=0.6108*EXP(17.27*TDAY/(TDAY+237.3))-0.6108*EXP(17.27*TMIN/
     &(TMIN+237.3))
      AEROCOMP=(CROPEC*VPD**1.5)/LATHEAT
      AEROCOMS=(4.0*VPD**1.5)/LATHEAT
      
      EPO=(RADCOM+AEROCOMP)*(1.-EXP(-0.7*LAI))  !potential plant evaporation JR 2-7-11
      ESO=(RADCOM+AEROCOMS)*EXP(-0.6*LAI)      !potential soil evaporation if the whole surface were covered
      EPO_MAD=(RADCOM_CDR+AEROCOMP)*(1.-EXP(-0.7*LAI))  !note changes
      ETO=RADCOM+AEROCOMP
     
      ESO_cm3=ESO*POT_TOPAREA       
	EPO_cm3=EPO*PTA
	EPO_MAD_cm3=EPO_MAD*PTA
	ETO_cm3=ETO*PTA
	    
C *********************INTEGRATION CALCULATIONS********************************
	ELSEIF (DYNAMIC.EQ.INTEG) THEN
**********************************************************************
                 
      THRU_cm3=0
	RUNOFF_cm3=0
	RUNOFF1_cm3=0
	RUNOFF2_cm3=0
	POT_DRAIN_cm3=0
	POT_DRAIN1_cm3=0
	POT_DRAIN2_cm3=0
	TOTAL_RAIN_cm3=0
	TOTAL_IRRIG_cm3=0
	POT_RAIN_cm3=0
	POT_RAIN1_cm3=0
	POT_RAIN2_cm3=0
	POT_IRRIG_cm3=0
	
		A_SW_cm3=SW_cm3-SWLL_cm3

C--------Calculate irrigation to be applied following morning-------------
      IF(SCHED.eq.'FILE') THEN        !read irrigation from weather file
        IF (YR.eq.IRYR.and.DOY.eq.IRDOY.and.IRSTAT.eq.0) THEN
          !Year and doy match next line in file
          IRRIG_cm3=IRRIG*POT_TOPAREA
          NEXTIR=1
        ELSE
          IRRIG_cm3=0.0
        ENDIF
        
      ELSEIF(SCHED.eq.'FIXED') THEN         !use fixed daily rate
        IF(RAINCUT.eq.'YES'.and.Y_RAIN_cm.lt.D_IRR) THEN
	    IRRIG_cm3=D_IRR*POT_TOPAREA
	  ELSEIF(RAINCUT.eq.'YES'.and.Y_RAIN_cm.ge.D_IRR) THEN
	     IRRIG_cm3=0.0
	  ELSE
	     IRRIG_cm3=D_IRR*POT_TOPAREA
	  ENDIF
	  	  
      ELSEIF(SCHED.eq.'MAD') THEN           !MAD irrigation scheduling
        IF(A_SW_cm3.lt.MAD_SW_cm3) THEN
	      IRRIG_cm3=(SW_DIF_cm3-SW_cm3)/CF_IRR  !JR,JM allows for deficit irrigation 4-15-10     
        ELSEIF(A_SW_cm3-0.5*EPO_MAD_cm3.lt.SW_TF*SWA_cm3) THEN !above MAD but EP brings into critical (corrected 2-13-12)
          IRRIG_cm3=max((SW_DIF_cm3-SW_cm3)/CF_IRR,0.)
        ELSE 
	    IRRIG_cm3=0.0
	  ENDIF

      ELSEIF(SCHED.eq.'REALTIME') THEN
        IF (YR.eq.IRYR.and.DOY.eq.IRDOY.and.IRSTAT.eq.0) THEN
          !Update NEXTIR if date matches
          NEXTIR=1
        ENDIF
        IF (YR.eq.IRYR.and.DOY.eq.IRDOY.and.IRSTAT.eq.0.and.
     &IRRIG.GE.0) THEN
          !only use from irrigation file if >= 0 (-1 is a flag not to use)
          IRRIG_cm3=IRRIG*POT_TOPAREA 
        ELSE  !MAD irrigation scheduling
	  IF(A_SW_cm3.lt.MAD_SW_cm3) THEN
	    IRRIG_cm3=(SW_DIF_cm3-SW_cm3)/CF_IRR  !JR,JM allows for deficit irrigation 4-15-10     
	  ELSEIF(A_SW_cm3-0.5*EPO_MAD_cm3.lt.SW_TF*SWA_cm3) THEN !above MAD but EP brings into critical
          IRRIG_cm3=max((SW_DIF_cm3-SW_cm3)/CF_IRR,0.)
	  ELSE
	    IRRIG_cm3=0.0
	  ENDIF
        ENDIF
      ENDIF

C------set today's rain for tomorrow's cutoff--------
      Y_RAIN_cm=RAIN_cm
            
C-----Calculate irrigation water entering the container and area alloted to container------
	POT_IRRIG_cm3=IRRIG_cm3*CF_IRR
	TOTAL_IRRIG_cm3=IRRIG_cm3*AREA_RATIO
      THRU_IRR_cm3=TOTAL_IRRIG_cm3-POT_IRRIG_cm3	!irrig only for irrig N contribution calc
      
C ----Calculate rain entering the container and area alloted to container------
      POT_RAIN1_cm3=(1-RAIN_EF)*RAIN_cm*POT_TOPAREA*CF_RAIN  !accounts for rain distribution JM/JR 7-3-10

C ----Update substrate water and calculate drainage and runoff	
	SW_cm3=SW_cm3+POT_IRRIG_cm3+POT_RAIN1_cm3  
	
	IF(SW_cm3.gt.SWDUL_cm3)THEN           !case where drainage occurs
	   POT_DRAIN1_cm3=SW_cm3-SWDUL_cm3
	   SW_cm3=SWDUL_cm3
	ENDIF
    
C------calculate actual substrate evaporation JR,JM 7-3-09 substrate-limited ES vs ESO
	ES_cm3=min(1.5*POT_TOPAREA*(max((SW_cm3-0.5*EPO_cm3),0.)
     &/SWDUL_cm3)**2.5,ESO_cm3)      

C------calculate actual plant evaporation and water sufficiency JR,JM 4-1-10
      ET_cm3=ES_cm3+EPO_cm3
      
      IF(ET_cm3>ETO_cm3)THEN
         EP_cm3=ETO_cm3-ES_cm3
      ELSE
         EP_cm3=EPO_cm3
      ENDIF
  
      A_SWavg_cm3=max(SW_cm3-0.5*(ES_cm3+EP_cm3)-SWLL_cm3,0.)
	WSUF=min(1.,1/SW_TF*A_SWavg_cm3/SWA_cm3)
	EP_cm3=EP_cm3*WSUF
	
C------calculate actual ET JR 4-1-10
      ET_cm3=ES_cm3+EP_cm3
            
C------readjust substrate water content   JR 4-1-10    
      POT_RAIN2_cm3=(RAIN_EF)*RAIN_cm*POT_TOPAREA*CF_RAIN  !new rain distribution
      SW_cm3=SW_cm3-ES_cm3-EP_cm3+POT_RAIN2_cm3  !with rain distribution
     
      IF(SW_cm3.gt.SWDUL_cm3)THEN     !case where drainage occurs
	   POT_DRAIN2_cm3=SW_cm3-SWDUL_cm3
	   SW_cm3=SWDUL_cm3
	ENDIF

      TOTAL_RAIN_cm3=RAIN_cm*PTA
      POT_RAIN_cm3=POT_RAIN1_cm3+POT_RAIN2_cm3
      THRU_cm3=TOTAL_RAIN_cm3-POT_RAIN_cm3+Total_IRRIG_cm3-POT_IRRIG_cm3   !rain and irrig
     
      POT_DRAIN_cm3=POT_DRAIN1_cm3+POT_DRAIN2_cm3
      DRPV=POT_DRAIN_cm3/(TP_cm3-SWLL_cm3)  !JR,JM 7-3-09
      RUNOFF_cm3=POT_DRAIN_cm3+THRU_cm3
      
C------calculate parameters on per production area basis (cm)-------      
	IRRIG_cm=TOTAL_IRRIG_cm3/PTA
	ET_cm=ET_cm3/PTA  !differentiate between pot vs area ET
	RUNOFF_cm=RUNOFF_cm3/PTA
	DRAIN_cm=POT_DRAIN_cm3/PTA
	THRU_cm=THRU_cm3/PTA
	
C------calculate parameters on pot basis (cm/container)-------
      SW_cm=SW_cm3/POT_TOPAREA !SW_cm at end of day
	
	POT_IRRIG_cm=POT_IRRIG_cm3/POT_TOPAREA
	POT_RAIN_cm=POT_RAIN_cm3/POT_TOPAREA
	POT_DRAIN_cm=POT_DRAIN_cm3/POT_TOPAREA
	POT_ET_cm=ET_cm3/POT_TOPAREA   !differentiate between pot vs area ET
      
C--------running totals on a volume per pot basis------------
	RAIN_CUM_cm3=RAIN_CUM_cm3+TOTAL_RAIN_cm3		
	IRRIG_CUM_cm3=IRRIG_CUM_cm3+TOTAL_IRRIG_cm3
	ET_CUM_cm3=ET_CUM_cm3+ET_cm3
	RUNOFF_CUM_cm3=RUNOFF_CUM_cm3+RUNOFF_cm3
	DRAIN_CUM_cm3=DRAIN_CUM_cm3+POT_DRAIN_cm3
	THRU_CUM_cm3=THRU_CUM_cm3+THRU_cm3
	
C--------running totals on a per area (depth) basis------------	
	RAIN_CUM_cm=RAIN_CUM_cm+RAIN_cm		
	IRRIG_CUM_cm=IRRIG_CUM_cm+IRRIG_cm
	ET_CUM_cm=ET_CUM_cm+ET_cm
	RUNOFF_CUM_cm=RUNOFF_CUM_cm+RUNOFF_cm
	DRAIN_CUM_cm=DRAIN_CUM_cm+DRAIN_cm
	THRU_CUM_cm=THRU_CUM_cm+THRU_cm
      
	ENDIF 
	
C *****************END INTEGRATION CALCULATIONS********************************

900   CONTINUE
	RETURN
      END	SUBROUTINE WaterBalance
