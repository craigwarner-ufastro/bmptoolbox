package javaUFLib;

//Title:        UFImageOps.java
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2004-5
//Author:       Craig Warner and Frank Varosi
//Company:      University of Florida
//Description:  For analysis of image data: Centroids and Full-Width Half-Max stats.

import java.math.*;
import java.io.*;
import java.util.*;

public class UFImageOps {

   public static final
	String rcsID = "$Name:  $ $Id: UFImageOps.java,v 1.45 2016/06/07 23:19:47 varosi Exp $";

    protected static boolean _verbose = false;

    public static void verbose( boolean v ) { _verbose = v; }

   public static float[] getCentroid(int[][] img, int mx, int my, float fwhm) {
      double[][] dimg = new double[img.length][img[0].length];
      for (int j = 0; j < img.length; j++) {
	for (int l = 0; l < img[0].length; l++) {
	   dimg[j][l] = (double)img[j][l];
	} 
      }
      return getCentroid( dimg, mx, my, fwhm );
   }

    // Input (mx,my) is assummed to be location of maximum pixel in image,
    //  and fwhm is either guess or actual FWHM.

   public static float[] getCentroid(double[][] img, int mx, int my, float fwhm) {
      int ysize = img.length;
      int xsize = img[0].length;
      int nhalf = (int)Math.floor(0.637*fwhm);
      if (nhalf < 2) nhalf = 2;
      int nbox = 2*nhalf+1;
      float[] cen = new float[2];
      cen[0] = -1;
      cen[1] = -1;
      
      String spos = " ( " + mx + ", " + my + " ) ";

      if( (mx < nhalf) || ((mx + nhalf) >= xsize) ||
	  (my < nhalf) || ((my + nhalf) >= ysize) ) {
	  if(_verbose) System.out.println("Position" + spos + "too near edge of image");
	  return cen;
      }

      double[][] starbox = UFArrayOps.extractValues( img, my-nhalf, my+nhalf, mx-nhalf, mx+nhalf );
      int ir = Math.max(nhalf-1, 1);
      double[] dd = new double[nbox-1];
      double[] ddsq = new double[nbox-1];
      for (int l=0; l < dd.length; l++) {
	  dd[l] = (double)(l+0.5-nhalf);
	  ddsq[l] = dd[l]*dd[l];
      }
      double[] w = new double[nbox-1];
      for (int l=0; l < w.length; l++)
      w[l] = (double)(1.0 - 0.5 * (Math.abs(dd[l])-0.5) / (nhalf-0.5));
      double sumc = UFArrayOps.totalValue(w);

      // Y partial derivative:
      double[][] deriv = UFArrayOps.shift(starbox,-1,0);
      deriv = UFArrayOps.subArrays(deriv, starbox);
      deriv = UFArrayOps.extractValues(deriv, 0, nbox-2, nhalf-ir, nhalf+ir);
      double[] derivtot = UFArrayOps.totalValue(deriv, 1);
      double sumd = UFArrayOps.totalValue(UFArrayOps.multArrays(w, derivtot));
      double sumxd = UFArrayOps.totalValue(UFArrayOps.multArrays(UFArrayOps.multArrays(w, dd), derivtot)); 
      double sumxsq = UFArrayOps.totalValue(UFArrayOps.multArrays(w, ddsq));

      if( sumxd < 0 ) {

	  double dy = sumxsq*sumd/(sumc*sumxd);

	  if( Math.abs(dy) <= nhalf )
	      cen[1] = (float)( my - dy + 0.5 );
	  else if(_verbose)
	      System.out.println("Computed Y centroid for position" + spos + "out of range");
      }
      else if(_verbose) System.out.println("Unable to compute Y centroid around position" + spos);

      // X partial derivative:
      deriv = UFArrayOps.shift(starbox,0,-1);
      deriv = UFArrayOps.subArrays(deriv, starbox);
      deriv = UFArrayOps.extractValues(deriv, nhalf-ir, nhalf+ir, 0, nbox-2);
      derivtot = UFArrayOps.totalValue(deriv, 2);
      sumd = UFArrayOps.totalValue(UFArrayOps.multArrays(w, derivtot));
      sumxd = UFArrayOps.totalValue(UFArrayOps.multArrays(UFArrayOps.multArrays(w, dd), derivtot));
      sumxsq = UFArrayOps.totalValue(UFArrayOps.multArrays(w, ddsq));

      if( sumxd < 0 ) {

	  double dx = sumxsq*sumd/(sumc*sumxd);

	  if( Math.abs(dx) <= nhalf )
	      cen[0] = (float)( mx - dx + 0.5 );
	  else if(_verbose)
	      System.out.println("Computed X centroid for position" + spos + "out of range");
      }
      else if(_verbose) System.out.println("Unable to compute X centroid around position" + spos);

      return cen;
   }
//-------------------------------------------------------------------------------------------------------

   public static double[][] smooth( int[][] data, int width ) {
      int m = data.length;
      int n = data[0].length;
      double[][] sdata = new double[m][n];

      if( width < 2 ) {
	  for (int i = 0; i < m; i++)
	      for (int j = 0; j < n; j++) sdata[i][j] = (double)data[i][j];
	  return sdata;
      }

      int[][] tot = new int[m][n];
      if (width % 2 == 0) width++;
      int w = width/2;
      int a = 0, b = 0, c = 0, d = 0; 
      int n_1 = n-1;

      for (int j = 0; j < m; j++) {
	  for (int k = 0; k < n; k++) {
	      if (k > 0)
		  tot[j][k] = tot[j][k-1] + data[j][k];
	      else
		  tot[j][k] = data[j][k];
	  }
      }

      for (int j = m-2; j >=0 ; j--) {
	  int j1 = j+1;
	  for (int k = 0; k < n; k++) tot[j][k] += tot[j1][k];
      }

      for (int j = 0; j < m; j++) {
	  int jw1 = j+w+1;
	  int j_w = j-w;
	  for (int k = 0; k < n; k++) {
	      int kw = k+w;
	      int k_w1 = k-(w+1);
	      if (j_w >=0 && kw < n) a = tot[j_w][kw];
	      if (jw1 < m && kw < n) b = tot[jw1][kw];
	      if (j_w >= 0 && k_w1 >= 0) c = tot[j_w][k_w1];
	      if (jw1 < m && k_w1 >= 0) d = tot[jw1][k_w1];
	      if (k_w1 < 0) {
		c = 0;
		d = 0;
	      }
	      if (jw1 >= m) {
		b = 0;
		d = 0;
	      }
	      int kwn = Math.min(kw,n_1);
	      int jw0 = Math.max(j_w,0);
	      if (kw >= n || j_w < 0) {
		a = tot[jw0][kwn];
		if (jw1 < m) b = tot[jw1][kwn];
		if (k_w1 >= 0) c = tot[jw0][k_w1];
	      }
	      double npts = (Math.min(jw1,m) - jw0) * (kwn - Math.max(k_w1,-1));
	      sdata[j][k] = (a-b-c+d)/npts;
	  }
      }

      return sdata;
   }

   public static void smooth( double[][] data, int width ) {
      int m = data.length;
      int n = data[0].length;

      if( width < 2 ) return;
      if (width % 2 == 0) width++;
      int w = width/2;
      //replace data array with moving boxcar average values:

      double[][] tot = new double[m][n];
      double a = 0, b = 0, c = 0, d = 0; 
      int n_1 = n-1;

      for (int j = 0; j < m; j++) {
	  for (int k = 0; k < n; k++) {
	      if (k > 0)
		  tot[j][k] = tot[j][k-1] + data[j][k];
	      else
		  tot[j][k] = data[j][k];
	  }
      }

      for (int j = m-2; j >=0 ; j--) {
	  for (int k = 0; k < n; k++) tot[j][k] += tot[j+1][k];
      }

      for (int j = 0; j < m; j++) {
	  int jw1 = j+w+1;
	  int j_w = j-w;
	  for (int k = 0; k < n; k++) {
	      int kw = k+w;
	      int k_w1 = k-(w+1);
	      if (j_w >=0 && kw < n) a = tot[j_w][kw];
	      if (jw1 < m && kw < n) b = tot[jw1][kw];
	      if (j_w >= 0 && k_w1 >= 0) c = tot[j_w][k_w1];
	      if (jw1 < m && k_w1 >= 0) d = tot[jw1][k_w1];
	      if (k_w1 < 0) {
		c = 0;
		d = 0;
	      }
	      if (jw1 >= m) {
		b = 0;
		d = 0;
	      }
	      int kwn = Math.min(kw,n_1);
	      int jw0 = Math.max(j_w,0);
	      if (kw >= n || j_w < 0) {
		a = tot[jw0][kwn];
		if (jw1 < m) b = tot[jw1][kwn];
		if (k_w1 >= 0) c = tot[jw0][k_w1];
	      }
	      double npts = (Math.min(jw1,m) - jw0) * (kwn - Math.max(k_w1,-1));
	      data[j][k] = (a-b-c+d)/npts;
	  }
      }
   }

   public static double[][] smooth( int[][] data, int width, int niter ) {

      if( niter <= 0 ) return smooth( data, 0 );

      double[][] sdata = smooth( data, width );

      for( int i=1; i < niter; i++ ) smooth( sdata, width );

      return sdata;
   }
//-------------------------------------------------------------------------------------------------------

    public static float[][] rebin( int[][] data, int binfactor ) {
	int ny = data.length;
	int nx = data[0].length;
	if( binfactor < 2 ) binfactor = 2;
	int nby = ny/binfactor;
	int nbx = nx/binfactor;
	float[][] bindata = new float[nby][nbx];
	int npbin = binfactor*binfactor;
	int my = 0;

	for( int iy = 0; iy < nby; iy++ ) {
	    int mx = 0;
	    for( int ix = 0; ix < nbx; ix++ ) {
		int ky = my;
		double sum = 0;
		for( int by = 0; by < binfactor; by++ ) {
		    int kx = mx;
		    for( int bx = 0; bx < binfactor; bx++ ) sum += (double)data[ky][kx++];
		    ky++;
		}
		bindata[iy][ix] = (float)(sum/npbin);
		mx += binfactor;
	    }
	    my += binfactor;
	}

	return bindata;
    }

    public static float[][] rebin( char[] data, int nx, int ny, int binfactor ) {
	int ndat = data.length;
	if( nx*ny != ndat ) System.out.println("UFImageOps.rebin> WARN: # pixels != nx * ny");
	if( binfactor < 2 ) binfactor = 2;
	int nby = ny/binfactor;
	int nbx = nx/binfactor;
	float[][] bindata = new float[nby][nbx];
	int npbin = binfactor*binfactor;
	int my = 0;

	for( int iy = 0; iy < nby; iy++ ) {
	    int mx = 0;
	    int ky = my*nx;
	    for( int ix = 0; ix < nbx; ix++ ) {
		int kxy = mx + ky;
		double sum = 0;
		for( int by = 0; by < binfactor; by++ ) {
		    int kd = kxy;
		    for( int bx = 0; bx < binfactor; bx++ ) sum += (double)data[kd++];
		    kxy += nx;
		}
		bindata[iy][ix] = (float)(sum/npbin);
		mx += binfactor;
	    }
	    my += binfactor;
	}

	return bindata;
    }
//-------------------------------------------------------------------------------------------------------

   //fwhm2D methods assume data[ypos][xpos] is maximum value (at previously determined centroid)

   public static float[] fwhm2D(double[][] data, int xpos, int ypos, int xmin, int ymin, int xmax, int ymax)
   {
       return fwhm2D( UFArrayOps.extractValues( data, ymin, ymax, xmin, xmax ), xpos-xmin, ypos-ymin );
   }

   public static float[] fwhm2D(double[][] data, int xpos, int ypos)
   {
       return fwhm2D( data, xpos, ypos, false );
   }

   public static float[] fwhm2D(double[][] data, int xpos, int ypos, boolean estimateBkgrnd)
   {
       //The maximum is assumed to occur at: data[ypos][xpos]
       //returned vector fwhm contains:
       // fwhm[0] = average of fwhm[2:5]
       // fwhm[1] = standard deviation of fwhm[2:5]
       // fwhm[2] = FWHM of X cut.
       // fwhm[3] = FWHM of Y cut.
       // fwhm[4] = FWHM of Y = X cut.
       // fwhm[5] = FWHM of Y = -X cut.
       // fwhm[6] = average background value used as zero level.

      float[] fwhm = new float[7];  
      float[] fwhm1ds = new float[4];

      int nx = data[0].length;
      int ny = data.length;
      int npix = nx * ny;
      double dmax = data[ypos][xpos];
      double dtot = 0;
      double bkgrnd = 0;
      double halfmax = (dmax + bkgrnd)/2;

      if( estimateBkgrnd ) {
	  dtot = UFArrayOps.totalValue( data );
	  double dtcen = UFArrayOps.totalValue( UFArrayOps.extractValues( data,
									  ypos-1, ypos+1,
									  xpos-1, xpos+1 ) );
	  bkgrnd = (dtot - dtcen)/(npix - 9);
      }

      fwhm[6] = (float)bkgrnd;

      double[] dcutx = new double[nx];
      for (int j = 0; j < nx; j++) dcutx[j] = data[ypos][j];
      fwhm1ds[0] = fwhm1D( dcutx, xpos, halfmax );

      double[] dcuty = new double[ny];
      for (int j = 0; j < ny; j++) dcuty[j] = data[j][xpos];
      fwhm1ds[1] = fwhm1D( dcuty, ypos, halfmax );

      //diagonals are sqrt(2) times larger:
      float sq2 = (float)Math.sqrt( 2.0 );
      int xystart = Math.min( xpos, ypos );
      int xyend = Math.min( nx-1-xpos, ny-1-ypos );
      int ncut = xystart+xyend+1;
      double[] dcutxy = new double[ncut];
      for (int j = 0; j < ncut; j++) dcutxy[j] = data[ypos-xystart+j][xpos-xystart+j]; // y = x
      fwhm1ds[2] = sq2 * fwhm1D( dcutxy, xystart, halfmax );

      int negxystart = Math.min( xpos, ny-1-ypos );
      int negxyend = Math.min( nx-1-xpos, ypos );
      ncut = negxystart+negxyend+1;
      double[] dcutnegxy = new double[ncut];
      for (int j = 0; j < ncut; j++) dcutnegxy[j] = data[ypos+negxystart-j][xpos-negxystart+j]; // y = -x
      fwhm1ds[3] = sq2 * fwhm1D( dcutnegxy, negxystart, halfmax );

      if( estimateBkgrnd ) {
	  //recompute average background rejecting larger region using new estimate of FWHM:
	  int rejSize = (int)Math.ceil( fwhm1ds[0] + fwhm1ds[1] );

	  if( rejSize > 1 ) {
	      int xmin = xpos - rejSize;
	      int xmax = xpos + rejSize;
	      int ymin = ypos - rejSize;
	      int ymax = ypos + rejSize;
	      if( xmin < 1 ) xmin = 1;
	      if( ymin < 1 ) ymin = 1;
	      if( xmax > (nx-2) ) xmax = nx - 2;
	      if( ymax > (ny-2) ) ymax = ny - 2;
	      double[][] dcen = UFArrayOps.extractValues( data, ymin, ymax, xmin, xmax );
	      double dtcen = UFArrayOps.totalValue( dcen );
	      int npcen = dcen.length * dcen[0].length;
	      bkgrnd = (dtot - dtcen)/(npix - npcen);
	      halfmax = (dmax + bkgrnd)/2;
	      fwhm[6] = (float)bkgrnd;
	      fwhm1ds[0] = fwhm1D( dcutx, xpos, halfmax );
	      fwhm1ds[1] = fwhm1D( dcuty, ypos, halfmax );
	      fwhm1ds[2] = sq2*fwhm1D(dcutxy, xystart, halfmax);
	      fwhm1ds[3] = sq2*fwhm1D(dcutnegxy, negxystart, halfmax);
	  }
      }

      fwhm[0] = (float)UFArrayOps.avgValue(fwhm1ds);
      fwhm[1] = (float)UFArrayOps.stddev(fwhm1ds);
      for (int j = 0; j < 4; j++) fwhm[j+2] = fwhm1ds[j];
      return fwhm;
   }

   public static float fwhm1D(float[] data, int mpos, float hmax) {
      double dhmax = (double)hmax;
      double[] ddata = UFArrayOps.castAsDoubles(data);
      return fwhm1D(ddata,mpos,dhmax);
   }

   public static float fwhm1D(double[] data, int mpos, double hmax)
   {
      int jstart = 0;
      int jend = data.length-1;

      for( int j = mpos; j > 0 ; j-- )
	  {
	      if( data[j] < hmax ) {
		  jstart = j;
		  break;
	      }
	  }

      for( int j = mpos; j < data.length; j++ )
	  {
	      if( data[j] < hmax ) {
		  jend = j;
		  break;
	      }
	  }

      if( jstart >= (data.length-1) ) jstart = data.length - 2;
      if( jend <= 0 ) jend = 1;

      double xs = jstart + ( hmax - data[jstart] )/( data[jstart+1] - data[jstart] );
      double xe = jend - ( hmax - data[jend] )/( data[jend-1] - data[jend] );

      return (float)( xe - xs );
   }
//-------------------------------------------------------------------------------------------------------

   public static float[] midpt1D(float[] data, int mpos, float hmax) {
      double dhmax = (double)hmax;
      double[] ddata = UFArrayOps.castAsDoubles(data);
      return midpt1D(ddata,mpos,dhmax);
   }

   public static float[] midpt1D(double[] data, int mpos, double hmax)
   {
      int jstart = 0;
      int jend = data.length-1;

      for( int j = mpos; j > 0 ; j-- )
          {
              if( data[j] < hmax ) { jstart = j;  break; }
          }

      for( int j = mpos; j < data.length; j++ )
          {
              if( data[j] < hmax ) { jend = j;  break; }
          }

      double xs = jstart + ( hmax - data[jstart] )/( data[jstart+1] - data[jstart] );
      double xe = jend - ( hmax - data[jend] )/( data[jend-1] - data[jend] );

      float[] midpt = new float[2];
      midpt[0] = (float)((xe+xs)/2.);
      midpt[1] = (float)(xe-xs);
      return midpt; 
   }
//-------------------------------------------------------------------------------------------------------
// Following methods handle the application of the Z-scale algorithm:
// to automatically find the min & max of range for scaling of data for display,
// that zooms in on the most frequently occuring values of the data.
 
    public static double[] Zscale( double[] data ) {
	return Zscale( data, 1, 1.0, 100, 2 );
    }

    public static double[] Zscale( double[] data, int npoints ) {
	return Zscale( data, 1, 1.0, npoints, 2 );
    }

    public static double[] Zscale( double[] data, int niter, int npoints ) {
	return Zscale( data, niter, 1.0, npoints, 2 );
    }

    public static double[] Zscale( double[] data, int niter, double contrast ) {
	return Zscale( data, niter, contrast, 100, 2 );
    }

    public static double[] Zscale( double[] data, int niter, double contrast, int npoints ) {
	return Zscale( data, niter, contrast, npoints, 2 );
    }

    public static double[] Zscale( double[] data, int niter, double contrast, int npoints, int Ndim )
    {
	int Nsamp = npoints;
	for( int i=1; i < Ndim; i++ ) Nsamp *= npoints;

	int nd = data.length;
	// if # samples is more than half of data points then apply to all the data.

	if( Nsamp > nd/2 ) return _Zscale( data, niter, contrast );
	else {
	    int nskip = nd/Nsamp;
	    double[] dsamp = new double[nd/nskip];
	    int j=0;

	    for( int i=0; i < dsamp.length; i++ ) {
		dsamp[i] = data[j];
		j += nskip;
	    }

	    return _Zscale( dsamp, niter, contrast );
	}
    }
//-------------------------------------------------------------------------------------------------------

    public static double[] Zscale( int[] data ) {
	return Zscale( data, 1, 1.0, 100, 2 );
    }

    public static double[] Zscale( int[] data, int npoints ) {
	return Zscale( data, 1, 1.0, npoints, 2 );
    }

    public static double[] Zscale( int[] data, int niter, int npoints ) {
	return Zscale( data, niter, 1.0, npoints, 2 );
    }

    public static double[] Zscale( int[] data, int niter, double contrast ) {
	return Zscale( data, niter, contrast, 100, 2 );
    }

    public static double[] Zscale( int[] data, int niter, double contrast, int npoints ) {
	return Zscale( data, niter, contrast, npoints, 2 );
    }

    public static double[] Zscale( int[] data, int niter, double contrast, int npoints, int Ndim )
    {
	int Nsamp = npoints;
	for( int i=1; i < Ndim; i++ ) Nsamp *= npoints;

	int nd = data.length;
	// if # samples is more than half of data points then apply to all the data.

	if( Nsamp > nd/2 ) {
	    double[] dsamp = new double[nd];
	    for( int i=0; i < nd; i++ ) dsamp[i] = (double)data[i];
	    return _Zscale( dsamp, niter, contrast );
	}
	else {
	    int nskip = nd/Nsamp;
	    double[] dsamp = new double[nd/nskip];
	    int j=0;

	    for( int i=0; i < dsamp.length; i++ ) {
		dsamp[i] = (double)data[j];
		j += nskip;
	    }

	    return _Zscale( dsamp, niter, contrast );
	}
    }
//-------------------------------------------------------------------------------------------------------

    public static double[] Zscale( float[] data ) {
	return Zscale( data, 1, 1.0, 100, 2 );
    }

    public static double[] Zscale( float[] data, int npoints ) {
	return Zscale( data, 1, 1.0, npoints, 2 );
    }

    public static double[] Zscale( float[] data, int niter, int npoints ) {
	return Zscale( data, niter, 1.0, npoints, 2 );
    }

    public static double[] Zscale( float[] data, int niter, double contrast ) {
	return Zscale( data, niter, contrast, 100, 2 );
    }

    public static double[] Zscale( float[] data, int niter, double contrast, int npoints ) {
	return Zscale( data, niter, contrast, npoints, 2 );
    }

    public static double[] Zscale( float[] data, int niter, double contrast, int npoints, int Ndim )
    {
	int Nsamp = npoints;
	for( int i=1; i < Ndim; i++ ) Nsamp *= npoints;

	int nd = data.length;
	// if # samples moret than half of data points then apply to all the data.

	if( Nsamp > nd/2 ) {
	    double[] dsamp = new double[nd];
	    for( int i=0; i < nd; i++ ) dsamp[i] = (double)data[i];
	    return _Zscale( dsamp, niter, contrast );
	}
	else {
	    int nskip = nd/Nsamp;
	    double[] dsamp = new double[nd/nskip];
	    int j=0;

	    for( int i=0; i < dsamp.length; i++ ) {
		dsamp[i] = (double)data[j];
		j += nskip;
	    }

	    return _Zscale( dsamp, niter, contrast );
	}
    }
//-------------------------------------------------------------------------------------------------------

    public static double[] Zscale( double[][] data ) {
	return Zscale( data, 1, 1.0, 100 );
    }

    public static double[] Zscale( double[][] data, int npoints ) {
	return Zscale( data, 1, 1.0, npoints );
    }

    public static double[] Zscale( double[][] data, int niter, int npoints ) {
	return Zscale( data, niter, 1.0, npoints );
    }

    public static double[] Zscale( double[][] data, int niter, double contrast ) {
	return Zscale( data, niter, contrast, 100 );
    }

    public static double[] Zscale( double[][] data, int niter, double contrast, int npoints )
    {
	int nx = data[0].length;
	int ny = data.length;
	int[] isx = sampleIndices( npoints, nx );
	int[] isy = sampleIndices( npoints, ny );
	int npx = isx.length;
	int npy = isy.length;
	double[] dsamp = new double[npx*npy];
	int ks=0;

	for( int j=0; j < npy; j++ ) {
	    int js = isy[j];
	    for( int i=0; i < npx; i++ ) dsamp[ks++] = data[js][isx[i]];
	}

	return _Zscale( dsamp, niter, contrast );
    }
//-------------------------------------------------------------------------------------------------------

    public static double[] Zscale( int[][] data ) {
	return Zscale( data, 1, 1.0, 100 );
    }

    public static double[] Zscale( int[][] data, int npoints ) {
	return Zscale( data, 1, 1.0, npoints );
    }

    public static double[] Zscale( int[][] data, int niter, int npoints ) {
	return Zscale( data, niter, 1.0, npoints );
    }

    public static double[] Zscale( int[][] data, int niter, double contrast ) {
	return Zscale( data, niter, contrast, 100 );
    }

    public static double[] Zscale( int[][] data, int niter, double contrast, int npoints )
    {
	int nx = data[0].length;
	int ny = data.length;
	int[] isx = sampleIndices( npoints, nx );
	int[] isy = sampleIndices( npoints, ny );
	int npx = isx.length;
	int npy = isy.length;
	double[] dsamp = new double[npx*npy];
	int ks=0;

	for( int j=0; j < npy; j++ ) {
	    int js = isy[j];
	    for( int i=0; i < npx; i++ ) dsamp[ks++] = (double)data[js][isx[i]];
	}

	return _Zscale( dsamp, niter, contrast );
    }
//-------------------------------------------------------------------------------------------------------

    public static double[] Zscale( float[][] data ) {
	return Zscale( data, 1, 1.0, 100 );
    }

    public static double[] Zscale( float[][] data, int npoints ) {
	return Zscale( data, 1, 1.0, npoints );
    }

    public static double[] Zscale( float[][] data, int niter, int npoints ) {
	return Zscale( data, niter, 1.0, npoints );
    }

    public static double[] Zscale( float[][] data, int niter, double contrast ) {
	return Zscale( data, niter, contrast, 100 );
    }

    public static double[] Zscale( float[][] data, int niter, double contrast, int npoints )
    {
	int nx = data[0].length;
	int ny = data.length;
	int[] isx = sampleIndices( npoints, nx );
	int[] isy = sampleIndices( npoints, ny );
	int npx = isx.length;
	int npy = isy.length;
	double[] dsamp = new double[npx*npy];
	int ks=0;

	for( int j=0; j < npy; j++ ) {
	    int js = isy[j];
	    for( int i=0; i < npx; i++ ) dsamp[ks++] = (double)data[js][isx[i]];
	}

	return _Zscale( dsamp, niter, contrast );
    }
//-------------------------------------------------------------------------------------------------------
// Method actually implementing the Z-scale algorithm (note "_" in name).
// This version does the only the basic case of niter = 1 and contrast = 1.

    public final static double[] _Zscale( double[] dsamp, int niter, double contrast )
    {
	java.util.Arrays.sort( dsamp );

	double[] x = new double[dsamp.length];
	for( int i=0; i < x.length; i++ ) x[i] = i;

	double[] coefs = UFMathLib.LinLsqFit( x, dsamp );

	double[] zrange = new double[2];
	zrange[0] = coefs[0];
	zrange[1] = coefs[0] + coefs[1]*x.length;
	return zrange;
    }
//-------------------------------------------------------------------------------------------------------

    public final static double[] randomVector( int nvals ) {

	if( nvals < 1 ) nvals = 2;
	double[] randVec = new double[nvals];
	for( int i=0; i < nvals; i++ ) randVec[i] = Math.random();
	return randVec;
    }
//-------------------------------------------------------------------------------------------------------

    public final static int[] randomInts( int nvals, int maxIndex ) {

	if( nvals < 1 ) nvals = 2;
	int[] randVec = new int[nvals];
	for( int i=0; i < nvals; i++ ) randVec[i] = (int)Math.round( maxIndex * Math.random() );
	return randVec;
    }
//-------------------------------------------------------------------------------------------------------

    public final static int[] sampleIndices( int nsamp, int ndata ) {

	// if # samples is more than half of data points then apply to all the data.
	// if # samples is more than quarter of data points then apply to half the data.
	// otherwise choose random sample indices.

	if( nsamp > ndata/2 ) {
	    nsamp = ndata;
	    int[] indices = new int[nsamp];
	    for( int i=0; i < nsamp; i++ ) indices[i] = i;
	    return indices;
	}
	else if( nsamp > ndata/3 ) {
	    nsamp = ndata/2;
	    int[] indices = new int[nsamp];
	    for( int i=0; i < nsamp; i++ ) indices[i] = 2*i;
	    return indices;
	}
	else if( nsamp > ndata/6 ) {
	    nsamp = ndata/4;
	    int[] indices = new int[nsamp];
	    for( int i=0; i < nsamp; i++ ) indices[i] = 4*i;
	    return indices;
	}
	else return randomInts( nsamp, ndata-1 );
    }
//-------------------------------------------------------------------------------------------------------

    public final static int[][] reform2D( int[] pixels, int npx, int npy ) {

	int k=0, npix = pixels.length;
	int[][]image = new int[npy][npx];

	if( npx * npy > npix ) npy = npix/npx;

	for( int i=0; i < npy; i++ ) {
	    for( int j=0; j < npx; j++ ) image[i][j] = pixels[k++];
	}

	return image;
    }
//-------------------------------------------------------------------------------------------------------

    public final static double[][] dreform2D( int[] pixels, int npx, int npy ) {

	int k=0, npix = pixels.length;
	double[][]image = new double[npy][npx];

	if( npx * npy > npix ) npy = npix/npx;

	for( int i=0; i < npy; i++ ) {
	    for( int j=0; j < npx; j++ ) image[i][j] = pixels[k++];
	}

	return image;
    }
//-------------------------------------------------------------------------------------------------------

    public final static double[][] dreform2D( double[] pixels, int npx, int npy ) {

	int k=0, npix = pixels.length;
	double[][]image = new double[npy][npx];

	for( int i=0; i < npy; i++ ) {
	    for( int j=0; j < npx; j++ ) {
		if( k < npix ) image[i][j] = pixels[k++];
	    }
	}

	return image;
    }
//-------------------------------------------------------------------------------------------------------

   public static void main(String[] args) {
      double[][] a = new double[530][530];
      String s;
      try {
        BufferedReader r = new BufferedReader(new FileReader("NGC3031.txt"));
        for (int j = 0; j < 530; j++) {
           for (int l = 0; l < 530; l++) {
              s = r.readLine();
              a[j][l] = Double.parseDouble(s);
           }
        }
      } catch(IOException e) {}

      float[] c = getCentroid(a, 39, 440, 4);
      System.out.println(UFArrayOps.arrayToString(c));
      int[][] data = {{0,1,2,3,4},{5,6,7,8,9},{10,17,12,13,14},{15,16,17,25,19},{20,21,22,23,24,25}};
      System.out.println(UFArrayOps.arrayToString(smooth(data, 3, 1)));
   }

}
