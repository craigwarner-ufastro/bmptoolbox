package javaUFLib;

//Title:        UFMathLib.java
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2005-6
//Author:       Frank Varosi
//Company:      University of Florida
//Description:  Numerical methods, etc.

import java.math.*;
import java.util.*;

public class UFMathLib {

    public static final
	String rcsID = "$Name:  $ $Id: UFMathLib.java,v 1.2 2006/03/03 23:52:16 varosi Exp $";

    // PURPOSE:
    //	Linear Least-squares approximation in one-dimension (y = a + b*x).
    //
    //	coefs = LinLsqFit( x, y, sigmas );
    //
    // INPUTS:
    //	xdata = array of values for independent variable.
    //	ydata = array of data values assumed to be linearly dependent on x.
    //
    //	ysigma = scalar or array specifying the standard deviation of y data,
    //		if not given, assumed to be unity, and then
    //		sqrt( chi_sq/Ndata ) can be an estimate of Y_SIGMA.
    // OUTPUT:
    //	coefs[0] = A : intercept = constant parameter result of linear fit,
    //	coefs[1] = B : slope = slope parameter, so that:
    //			( A_intercept + B_slope * x ) approximates the y data.
    //
    //	coefs[2] = sigma(A) : standard deviation of A_intercept.
    //	coefs[3] = sigma(B) : standard deviation of B_slope parameter..
    //	coefs[4] = covariance(A,B) = co-variance of A_intercept and B_slope parameters.
    //
    //	Standard algorithm.
    //	Uses transform to reduce roundoff errors (cf. Numerical Recipes).
    //	Written, Frank Varosi, Univ. of Florida, 2005.


    public static double [] LinLsqFit(float[] xdata, float[] ydata) {
	return LinLsqFit(UFArrayOps.castAsDoubles(xdata), UFArrayOps.castAsDoubles(ydata), null);
    }

    public static double [] LinLsqFit(float[] xdata, float[] ydata, float[] ysigma) {
        return LinLsqFit(UFArrayOps.castAsDoubles(xdata), UFArrayOps.castAsDoubles(ydata), UFArrayOps.castAsDoubles(ysigma));
    }

    public static double[] LinLsqFit( double[] xdata, double[] ydata )
    {
	return LinLsqFit( xdata, ydata, null );
    }

    public static double[] LinLsqFit( double[] xdata, double[] ydata, double[] ysigma )
    {
	double[] coefs = new double[5];
	int nx = xdata.length;
	int ny = ydata.length;

	if( nx != ny ) {
	    System.err.println("UFMathLib::LinLsqFit> #x:" + nx +" != #y:" + ny);
	    return coefs;
	}

	double Sx, Sy;
	double[] Tx;
	double Sw = nx;

	if( ysigma == null ) {
	    Sx = UFArrayOps.totalValue( xdata );
	    Sy = UFArrayOps.totalValue( ydata );
	    Tx = UFArrayOps.subArrays( xdata, Sx/Sw );	   //Transform to reduce roundoff errors.
	    coefs[1] = UFArrayOps.totalValue( UFArrayOps.multArrays( ydata, Tx ) );
	}
	else {
	    double[] wy = new double[nx];
	    if( ysigma.length == nx ) {
		for( int i=0; i < nx; i++ ) wy[i] = 1/ysigma[i];
	    } else {
		for( int i=0; i < nx; i++ ) wy[i] = 1/ysigma[0];
	    }
	    double[] wy2 = UFArrayOps.multArrays( wy, wy );
	    Sw = UFArrayOps.totalValue( wy2 );
	    Sx = UFArrayOps.totalValue( UFArrayOps.multArrays( xdata, wy2 ) );
	    Sy = UFArrayOps.totalValue( UFArrayOps.multArrays( ydata, wy2 ) );
	    Tx = UFArrayOps.multArrays( UFArrayOps.subArrays( xdata, Sx/Sw ), wy2 );
	    coefs[1] = UFArrayOps.totalValue( UFArrayOps.multArrays( wy, UFArrayOps.multArrays( ydata, Tx )));
	}

	double Stt = UFArrayOps.totalValue( UFArrayOps.multArrays( Tx, Tx ) );
	coefs[1] /= Stt;
	coefs[0] = (Sy - coefs[1] * Sx) / Sw;

	coefs[2] = ( 1 + (Sx*Sx)/(Sw*Stt) )/Sw;
	coefs[3] = 1/Stt;
	coefs[4] = -Sx/(Sw*Stt);

	return coefs;
    }

    public static double[] LinLsqFit( double Aintercept, double Bslope, double[] xdata )
    {
	return UFArrayOps.addArrays( UFArrayOps.multArrays( xdata, Bslope ), Aintercept );
    }

    public static double chisq( double[] ydata, double[] yfit, double[] ysigma )
    {
	double[] ydif = UFArrayOps.subArrays( ydata, yfit );
	double[] yd2 = UFArrayOps.multArrays( ydif, ydif );

	if( ysigma != null ) {

	    int nx = ydata.length;

	    if( ysigma.length == nx ) {
		for( int i=0; i < nx; i++ ) yd2[i] = yd2[i]/ysigma[i];
	    } else {
		for( int i=0; i < nx; i++ ) yd2[i] = yd2[i]/ysigma[0];
	    }

	}

	return UFArrayOps.totalValue( yd2 );
    }
}
