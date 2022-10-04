/* UFArrayOps.java
Author: Craig Warner
Purpose: Includes static methods that perform IDL style array operations.

Partial method list:
1-D ARRAY METHODS
static int maxValue(int[] a, int m, int n)
        Returns the maximum value in the array a between indices m and
        n, inclusive.
static int maxValue(int[] a)
        Returns the maximum value in the array a.
static int minValue(int[] a, int m, int n)
        Returns the minimum value in the array a between indices m and
        n, inclusive.
static int minValue(int[] a)
        Returns the minimum value in the array a.
static int countN(int[] a, int m, int n, int x)
        Returns the number of occurances of x in the array a between
        indices m and n, inclusive.
static int countN(int[] a, int x)
        Returns the number of occurances of x in the array a.
static int[] reverse(int[] a)
        Returns the array in reverse order.
static int totalValue(int[] a, int m, int n)
        Returns the sum of all elements in the array a between indices m
        and n, inclusive.
static int totalValue(int[] a)
        Returns the sum of all elements in the array a.
static double avgValue(int[] a, int m, int n)
        Returns the average of all elements in the array a between indices
        m and n, inclusive.
static double avgValue(int[] a)
        Returns the average of all elements in the array a.
static int[] shift(int[] a, int x)
        Shifts all the elements of the array a by a specified number, and
        wraps around at the end.
static int[] where(int[] a, String op, float x)
        Returns an array containing the indices of all elements of the
        array a that match the specified condition or an array of length
        1 containing the value -1 if no elements match.  The condition is
        specified in two parts: op can be one of "==", "!=", ">" ,"<",
        ">=", and "<=".  x is any expression that evaluates to a number.
        Example: int[] w = where(a, "==", maxValue(a));
static int[] andUnion(int[] a, int[] b)
        Can be used to combine the results of two where operations with
        a logical AND.
        Example: int[] w = andUnion(where(a,">",5), where(a,"<",9));
static int[] orUnion(int[] a, int[] b)
        Can be used to combine the results of two where operations with
        a logical OR.
        Example: int[] w = orUnion(where(a,"<",5), where(a,">",9));
static int[] extractValues(int[] a, int[] n)
        Returns an array of length n.length containing the elements of
        array a at the subscripts listed in array n (i.e. uses the array
        n to subscript the array a).
static int[] extractValues(int[] a, int m, int n)
        Returns an array of length n-m+1 with all the values of a between
        indices m and n, inclusive.
static String arrayToString(int[] a, int m, int n)
        Returns a String containing a tab-separated list of all the elements
        of the array a between elements m and n, inclusive.
static String arrayToString(int[] a)
        Returns a String containing a tab-separated list of all the elements
        of the array a.
static String arrayWithIndexToString(int[] a, int m, int n)
        Returns a String containing a newline-separated list of all the
        elements of a, each tagged by its index, between elements m and
        n, inclusive.
static String arrayWithIndexToString(int[] a)
        Returns a String containing a newline-separated list of all the
        elements of a, each tagged by its index.

All of the above methods except andUnion and orUnion also have versions for 
float, double, long, short, and byte.

All of the above methods except andUnion, orUnion, totalValue, and avgValue
also have versions for char, String, and Object.

The following methods are only available for String arrays:

static int[] strlenArray(String[] a)
        Returns an int[] with the length of every String in the array a.
static int[] strposArray(String[] a, String s, int m)
        Returns an int[] containing the index of the first occurance of
        the substring s within each String a, starting at the character
        position specified by m or -1 if not found.
static int[] strposArray(String[] a, String s)
        Same as above, with m defaulting to 0, so it searches the entire
        string.
static String[] strmidArray(String[] a, int m, int n)
        Returns a new String[] containing the substring of each String in
        the array a, beginning at character position m and ending at
        position n-1.

2-D ARRAY METHODS
static int maxValue(int[][] a, int mr, int nr, int mc, int nc)
	Returns the maximum value in the array a between indices [mr][mc] 
	and [nr][nc], inclusive.
static int maxValue(int[][] a)
	Returns the maximum value in the array a.
static int minValue(int[][] a, int mr, int nr, int mc, int nc)
	Returns the minimum value in the array a between indices [mr][mc]
        and [nr][nc], inclusive. 
static int minValue(int[][] a)
	Returns the minimum value in the array a.
static int countN(int[][] a, int mr, int nr, int mc, int nc, int x)
	Returns the number of occurances of x in the array a between
	indices [mr][mc] and [nr][nc], inclusive. 
static int countN(int[][] a, int x)
	Returns the number of occurances of x in the array a.
static int[][] reverse(int[][] a, int dim)
	Returns the array with the specified dimension (1 or 2) in reverse
	order.  If dim is 1, the columns will be reversed and if dim is 2,
	the rows will be reversed.
static int[][] reverse(int[][] a)
	Same as reverse(a, 1) above.
static int totalValue(int[][] a, int mr, int nr, int mc, int nc) 
	Returns the sum of all elements in the array a between indices
	[mr][mc] and [nr][nc], inclusive. 
static int totalValue(int[][] a)
	Returns the sum of all elements in the array a.
static double avgValue(int[][] a, int mr, int nr, int mc, int nc) 
	Returns the average of all elements in the array a between indices
	[mr][mc] and [nr][nc], inclusive.
static double avgValue(int[][] a)
        Returns the average of all elements in the array a.
static int[][] shift(int[][] a, int x, int y)
	Shifts all the rows of the array a by x and the columns by y, and
	wraps around at the ends.
static int[][] where(int[][] a, String op, float x)
	Returns an array containing the indices of all elements of the
	array a that match the specified condition or an array of length
	[1][2] containing the value {-1,-1} if no elements match.
	The returned array will have dimensions [n][2] for n matches, where
	[n][0] will contain the row and [n][1] the column of the matching
	element.  The condition is specified in two parts: op can be one of
	"==", "!=", ">" ,"<", ">=", and "<=".  x is any expression that
	evaluates to a number.
	Example: int[][] w = where(a, "==", maxValue(a));
static int[][] andUnion(int[][] a, int[][] b)
	Can be used to combine the results of two where operations with
	a logical AND.
	Example: int[][] w = andUnion(where(a,">",5), where(a,"<",9));
static int[][] orUnion(int[][] a, int[][] b)
        Can be used to combine the results of two where operations with
        a logical OR.
	Example: int[][] w = orUnion(where(a,"<",5), where(a,">",9));
static int[] extractValues(int[][] a, int[][] n)
	Returns an array of length n.length containing the elements of
	array a at the subscripts listed in array n (i.e. uses the array
	n to subscript the array a).  n should have dimensions
	[n.length][2] and n[j][0] should correspond to a row in a and
	n[j][1] should correspond to a column in a (i.e. n is of the format
	returned by the where, andUnion, and orUnion functions for 2d arrays).
	A one-dimensional array containing the specified datapoints is
	returned.
static int[][] extractValues(int[][] a, int mr, int nr, int mc, int nc)
	Returns a 2-d array of length [nr-mr+1][nc-mc+1] with all the values
	of a between [mr][mc] and [nr][nc], inclusive.
static String arrayToString(int[][] a, int mr, int nr, int mc, int nc)
	Returns a String containing a table listing all the values of a
	between [mr][mc] and [nr][nc], inclusive.
static String arrayToString(int[][] a)
        Returns a String containing a table listing all the values of a.

All of the above methods except andUnion and orUnion also have versions for 
2-D arrays of the types float, double, long, short, and byte.

All of the above methods except andUnion, orUnion, totalValue, and avgValue
also have versions for 2-D arrays of the types char, String, and Object.

The following methods are only available for 2-D String arrays:

static int[][] strlenArray(String[][] a)
        Returns an int[][] with the length of every String in the array a.
static int[][] strposArray(String[][] a, String s, int m)
        Returns an int[][] containing the index of the first occurance of
        the substring s within each String a, starting at the character
        position specified by m or -1 if not found.
static int[][] strposArray(String[][] a, String s)
        Same as above, with m defaulting to 0, so it searches the entire
        string.
static String[][] strmidArray(String[][] a, int m, int n)
        Returns a new String[][] containing the substring of each String in
        the array a, beginning at character position m and ending at
        position n-1.
*/

package javaUFLib;

import java.util.Arrays;

public class UFArrayOps {

   public static int maxValue(int[] a, int m, int n) {
      if (a.length == 0) return 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
	x = Math.max(x, a[j]);
      }
      return x;
   }

   public static int maxValue(int[] a) {
      return maxValue(a, 0, a.length-1);
   }

   public static int minValue(int[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        x = Math.min(x, a[j]);
      }
      return x;
   }

   public static int minValue(int[] a) {
      return minValue(a, 0, a.length-1);
   }

   public static int countN(int[] a, int m, int n, int x) {
      if (a.length == 0) return 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int b = 0;
      for (int j = m; j <= n; j++) {
	if (a[j] == x) b++;
      }
      return b;
   }

   public static int countN(int[] a, int x) {
      return countN(a, 0, a.length-1, x);
   }

   public static int[] reverse(int[] a) {
      int temp;
      int[] b = new int[a.length];
      int n = a.length-1;
      for (int j=0; j < a.length/2; j++) {
	b[n-j] = a[j];
	b[j] = a[n-j];
      }
      return b;
   }

   public static int totalValue(int[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int x = 0;
      for (int j = m; j <= n; j++) {
	x+= a[j];
      }
      return x;
   }

   public static int totalValue(int[] a) {
      return totalValue(a, 0, a.length-1);
   }

   public static double avgValue(int[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double x = 0;
      for (int j = m; j <= n; j++) {
        x+= a[j];
      }
      return x/(n-m+1);
   }

   public static double avgValue(int[] a) {
      return avgValue(a, 0, a.length-1);
   }

   public static int[] shift(int[] a, int x) {
      int n;
      int[] b = new int[a.length];
      for (int j = 0; j < a.length; j++) {
	n = (x+j) % a.length;
	if (n < 0) n+=a.length;
	b[n] = a[j];
      }
      return b;
   }

   public static int[] where(int[] a, String op, float x) {
      int[] b = new int[a.length];
      int[] w;
      int n = 0;
      for (int j = 0; j < a.length; j++) {
	if (op.equals("==")) {
	   if (a[j] == x) {
	      b[n] = j;
	      n++;
	   } 
	} else if (op.equals("!=")) {
           if (a[j] != x) {
              b[n] = j;
              n++;
           }
        } else if (op.equals(">")) {
           if (a[j] > x) {
              b[n] = j;
              n++;
           }
        } else if (op.equals(">=")) {
           if (a[j] >= x) {
              b[n] = j;
              n++;
           }
        } else if (op.equals("<")) {
           if (a[j] < x) {
              b[n] = j;
              n++;
           }
        } else if (op.equals("<=")) {
           if (a[j] <= x) {
              b[n] = j;
              n++;
           }
        } else {
	   System.out.println("UFArrayOps::where> invalid operation");
	   b = new int[1];
	   b[0] = -1;
	   return b;
        }
      }
      if (n == 0) {
	w = new int[1];
	w[0] = -1;
      } else {
	w = new int[n];
	for (int j=0; j<n; j++) w[j]=b[j];
      }
      return w;
   }

   public static int[] andUnion(int[] a, int[] b) {
      int[] x = new int[a.length];
      int n = 0;
      int[] w;
      for (int j = 0; j < a.length; j++) {
	if (Arrays.binarySearch(b, a[j]) >= 0) {
	   x[n] = a[j];
	   n++;
	}
      }
      if (n == 0) {
        w = new int[1];
        w[0] = -1;
      } else {
        w = new int[n];
        for (int j=0; j<n; j++) w[j]=x[j];
      }
      return w;
   }

   public static int[] orUnion(int[] a, int[] b) {
      int[] x = new int[a.length + b.length];
      int n = 0;
      int[] w;
      for (int j = 0; j < b.length; j++) {
	if (b[j] != -1 && Arrays.binarySearch(a, b[j]) < 0) {
	   x[n] = b[j];
	   n++;
        }
      } 
      if (n == 0) w = a;
      else if (a[0] == -1) w = b;
      else {
	w = new int[n+a.length];
	for (int j = 0; j < a.length; j++) w[j] = a[j];
	for (int j = 0; j < n; j++) w[a.length+j] = x[j];
      }
      Arrays.sort(w);
      return w;
   }

   public static int[] extractValues(int[] a, int[] n) {
      int[] b = new int[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j] >= 0 && n[j] < a.length) b[j] = a[n[j]];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j]);
      }
      return b;
   }

   public static int[] extractValues(int[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int[] b = new int[n-m+1];
      for (int j = m; j <= n; j++) {
	b[j-m] = a[j];
      }
      return b;
   }

   public static int[][] extractValues(int[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int[][] b = new int[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   b[j-mr][l-mc] = a[j][l];
	}
      }
      return b;
   }

   public static String arrayToString(int[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
	if (j == n) s += a[j];
	else s += a[j] + "\t";
      }
      return s;
   }

   public static String arrayToString(int[] a) {
      return arrayToString(a, 0, a.length-1);
   }

   public static String arrayToString(int[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String s = "";
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (l == nc) s += a[j][l];
	   else s += a[j][l] + "\t";
	}
        if (j != nr) s += "\n";
      }
      return s;
   }

   public static String arrayToString(int[][] a) {
      return arrayToString(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static String arrayWithIndexToString(int[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
        if (j == n) s += j + ": " + a[j];
        else s += j + ": " + a[j] + "\n";
      }
      return s;
   }

   public static String arrayWithIndexToString(int[] a) {
      return arrayWithIndexToString(a, 0, a.length-1);
   }

   public static float maxValue(float[] a, int m, int n) {
      if (a.length == 0) return 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      float x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
	x = Math.max(x, a[j]);
      }
      return x;
   }

   public static float maxValue(float[] a) {
      return maxValue(a, 0, a.length-1);
   }

   public static float minValue(float[] a, int m, int n) {
      if (a.length == 0) return 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      float x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        x = Math.min(x, a[j]);
      }
      return x;
   }

   public static float minValue(float[] a) {
      return minValue(a, 0, a.length-1);
   }

   public static int countN(float[] a, int m, int n, float x) {
      if (a.length == 0) return 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int b = 0;
      for (int j = m; j <= n; j++) {
	if (a[j] == x) b++;
      }
      return b;
   }

   public static int countN(float[] a, float x) {
      return countN(a, 0, a.length-1, x);
   }

   public static float[] reverse(float[] a) {
      int temp;
      float[] b = new float[a.length];
      int n = a.length-1;
      for (int j=0; j < a.length/2; j++) {
	b[n-j] = a[j];
	b[j] = a[n-j];
      }
      return b;
   }

   public static float totalValue(float[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      float x = 0;
      for (int j = m; j <= n; j++) {
	x+= a[j];
      }
      return x;
   }

   public static float totalValue(float[] a) {
      return totalValue(a, 0, a.length-1);
   }

   public static double avgValue(float[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double x = 0;
      for (int j = m; j <= n; j++) {
        x+= a[j];
      }
      return x/(n-m+1);
   }

   public static double avgValue(float[] a) {
      return avgValue(a, 0, a.length-1);
   }

   public static float[] shift(float[] a, int x) {
      int n;
      float[] b = new float[a.length];
      for (int j = 0; j < a.length; j++) {
	n = (x+j) % a.length;
	if (n < 0) n+=a.length;
	b[n] = a[j];
      }
      return b;
   }

   public static int[] where(float[] a, String op, float x) {
      int[] b = new int[a.length];
      int[] w;
      int n = 0;
      if (op.equals("==")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] == x) b[n++] = j;
	  } 
      } else if (op.equals("!=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] != x) b[n++] = j;
	  }
      } else if (op.equals(">")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] > x) b[n++] = j;
	  }
      } else if (op.equals(">=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] >= x) b[n++] = j;
	  }
      } else if (op.equals("<")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] < x) b[n++] = j;
	  }
      } else if (op.equals("<=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] <= x) b[n++] = j;
	  }
      } else {
	  System.out.println("UFArrayOps::where> invalid operation");
	  b = new int[1];
	  b[0] = -1;
	  return b;
      }
      if (n == 0) {
	w = new int[1];
	w[0] = -1;
      } else {
	w = new int[n];
	for (int j=0; j<n; j++) w[j]=b[j];
      }
      return w;
   }

   public static float[] extractValues(float[] a, int[] n) {
      float[] b = new float[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j] >= 0 && n[j] < a.length) b[j] = a[n[j]];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j]);
      }
      return b;
   }

   public static float[] extractValues(float[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      float[] b = new float[n-m+1];
      for (int j = m; j <= n; j++) {
	b[j-m] = a[j];
      }
      return b;
   }

   public static float[][] extractValues(float[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      float[][] b = new float[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   b[j-mr][l-mc] = a[j][l];
	}
      }
      return b;
   }

   public static String arrayToString(float[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
	if (j == n) s += a[j];
	else s += a[j] + "\t";
      }
      return s;
   }

   public static String arrayToString(float[] a) {
      return arrayToString(a, 0, a.length-1);
   }

   public static String arrayToString(float[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String s = "";
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (l == nc) s += a[j][l];
	   else s += a[j][l] + "\t";
	}
        if (j != nr) s += "\n";
      }
      return s;
   }

   public static String arrayToString(float[][] a) {
      return arrayToString(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static String arrayWithIndexToString(float[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
        if (j == n) s += j + ": " + a[j];
        else s += j + ": " + a[j] + "\n";
      }
      return s;
   }

   public static String arrayWithIndexToString(float[] a) {
      return arrayWithIndexToString(a, 0, a.length-1);
   }

   public static double maxValue(double[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
	x = Math.max(x, a[j]);
      }
      return x;
   }

   public static double maxValue(double[] a) {
      return maxValue(a, 0, a.length-1);
   }

   public static double minValue(double[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        x = Math.min(x, a[j]);
      }
      return x;
   }

   public static double minValue(double[] a) {
      return minValue(a, 0, a.length-1);
   }

   public static int countN(double[] a, int m, int n, double x) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int b = 0;
      for (int j = m; j <= n; j++) {
	if (a[j] == x) b++;
      }
      return b;
   }

   public static int countN(double[] a, double x) {
      return countN(a, 0, a.length-1, x);
   }

   public static double[] reverse(double[] a) {
      int temp;
      double[] b = new double[a.length];
      int n = a.length-1;
      for (int j=0; j < a.length/2; j++) {
	b[n-j] = a[j];
	b[j] = a[n-j];
      }
      return b;
   }

   public static double totalValue(double[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double x = 0;
      for (int j = m; j <= n; j++) {
	x+= a[j];
      }
      return x;
   }

   public static double totalValue(double[] a) {
      return totalValue(a, 0, a.length-1);
   }

   public static double avgValue(double[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double x = 0;
      for (int j = m; j <= n; j++) {
        x+= a[j];
      }
      return x/(n-m+1);
   }

   public static double avgValue(double[] a) {
      return avgValue(a, 0, a.length-1);
   }

   public static double[] shift(double[] a, int x) {
      int n;
      double[] b = new double[a.length];
      for (int j = 0; j < a.length; j++) {
	n = (x+j) % a.length;
	if (n < 0) n+=a.length;
	b[n] = a[j];
      }
      return b;
   }

   public static int[] where(double[] a, String op, double x) {
      int[] b = new int[a.length];
      int[] w;
      int n = 0;
      if (op.equals("==")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] == x) b[n++] = j;
	  } 
      } else if (op.equals("!=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] != x) b[n++] = j;
	  }
      } else if (op.equals(">")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] > x) b[n++] = j;
	  }
      } else if (op.equals(">=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] >= x) b[n++] = j;
	  }
      } else if (op.equals("<")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] < x) b[n++] = j;
	  }
      } else if (op.equals("<=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] <= x) b[n++] = j;
	  }
      } else {
	  System.out.println("UFArrayOps::where> invalid operation");
	  b = new int[1];
	  b[0] = -1;
	  return b;
      }
      if (n == 0) {
	w = new int[1];
	w[0] = -1;
      } else {
	w = new int[n];
	for (int j=0; j<n; j++) w[j]=b[j];
      }
      return w;
   }

   public static double[] extractValues(double[] a, int[] n) {
      double[] b = new double[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j] >= 0 && n[j] < a.length) b[j] = a[n[j]];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j]);
      }
      return b;
   }

   public static double[] extractValues(double[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double[] b = new double[n-m+1];
      for (int j = m; j <= n; j++) {
	b[j-m] = a[j];
      }
      return b;
   }

   public static double[][] extractValues(double[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double[][] b = new double[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   b[j-mr][l-mc] = a[j][l];
	}
      }
      return b;
   }

   public static String arrayToString(double[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
	if (j == n) s += a[j];
	else s += a[j] + "\t";
      }
      return s;
   }

   public static String arrayToString(double[] a) {
      return arrayToString(a, 0, a.length-1);
   }

   public static String arrayToString(double[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String s = "";
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (l == nc) s += a[j][l];
	   else s += a[j][l] + "\t";
	}
        if (j != nr) s += "\n";
      }
      return s;
   }

   public static String arrayToString(double[][] a) {
      return arrayToString(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static String arrayWithIndexToString(double[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
        if (j == n) s += j + ": " + a[j];
        else s += j + ": " + a[j] + "\n";
      }
      return s;
   }

   public static String arrayWithIndexToString(double[] a) {
      return arrayWithIndexToString(a, 0, a.length-1);
   }

   public static long maxValue(long[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      long x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
	x = Math.max(x, a[j]);
      }
      return x;
   }

   public static long maxValue(long[] a) {
      return maxValue(a, 0, a.length-1);
   }

   public static long minValue(long[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      long x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        x = Math.min(x, a[j]);
      }
      return x;
   }

   public static long minValue(long[] a) {
      return minValue(a, 0, a.length-1);
   }

   public static int countN(long[] a, int m, int n, long x) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int b = 0;
      for (int j = m; j <= n; j++) {
	if (a[j] == x) b++;
      }
      return b;
   }

   public static int countN(long[] a, long x) {
      return countN(a, 0, a.length-1, x);
   }

   public static long[] reverse(long[] a) {
      int temp;
      long[] b = new long[a.length];
      int n = a.length-1;
      for (int j=0; j < a.length/2; j++) {
	b[n-j] = a[j];
	b[j] = a[n-j];
      }
      return b;
   }

   public static long totalValue(long[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      long x = 0;
      for (int j = m; j <= n; j++) {
	x+= a[j];
      }
      return x;
   }

   public static long totalValue(long[] a) {
      return totalValue(a, 0, a.length-1);
   }

   public static double avgValue(long[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double x = 0;
      for (int j = m; j <= n; j++) {
        x+= a[j];
      }
      return x/(n-m+1);
   }

   public static double avgValue(long[] a) {
      return avgValue(a, 0, a.length-1);
   }

   public static long[] shift(long[] a, int x) {
      int n;
      long[] b = new long[a.length];
      for (int j = 0; j < a.length; j++) {
	n = (x+j) % a.length;
	if (n < 0) n+=a.length;
	b[n] = a[j];
      }
      return b;
   }

   public static int[] where(long[] a, String op, float x) {
      int[] b = new int[a.length];
      int[] w;
      int n = 0;
      if (op.equals("==")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] == x) b[n++] = j;
	  } 
      } else if (op.equals("!=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] != x) b[n++] = j;
	  }
      } else if (op.equals(">")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] > x) b[n++] = j;
	  }
      } else if (op.equals(">=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] >= x) b[n++] = j;
	  }
      } else if (op.equals("<")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] < x) b[n++] = j;
	  }
      } else if (op.equals("<=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] <= x) b[n++] = j;
	  }
      } else {
	  System.out.println("UFArrayOps::where> invalid operation");
	  b = new int[1];
	  b[0] = -1;
	  return b;
      }
      if (n == 0) {
	w = new int[1];
	w[0] = -1;
      } else {
	w = new int[n];
	for (int j=0; j<n; j++) w[j]=b[j];
      }
      return w;
   }

   public static long[] extractValues(long[] a, int[] n) {
      long[] b = new long[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j] >= 0 && n[j] < a.length) b[j] = a[n[j]];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j]);
      }
      return b;
   }

   public static long[] extractValues(long[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      long[] b = new long[n-m+1];
      for (int j = m; j <= n; j++) {
	b[j-m] = a[j];
      }
      return b;
   }

   public static long[][] extractValues(long[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      long[][] b = new long[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   b[j-mr][l-mc] = a[j][l];
	}
      }
      return b;
   }

   public static String arrayToString(long[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
	if (j == n) s += a[j];
	else s += a[j] + "\t";
      }
      return s;
   }

   public static String arrayToString(long[] a) {
      return arrayToString(a, 0, a.length-1);
   }

   public static String arrayToString(long[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String s = "";
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (l == nc) s += a[j][l];
	   else s += a[j][l] + "\t";
	}
        if (j != nr) s += "\n";
      }
      return s;
   }

   public static String arrayToString(long[][] a) {
      return arrayToString(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static String arrayWithIndexToString(long[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
        if (j == n) s += j + ": " + a[j];
        else s += j + ": " + a[j] + "\n";
      }
      return s;
   }

   public static String arrayWithIndexToString(long[] a) {
      return arrayWithIndexToString(a, 0, a.length-1);
   }

   public static short maxValue(short[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      short x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
	x = (short)Math.max(x, a[j]);
      }
      return x;
   }

   public static short maxValue(short[] a) {
      return maxValue(a, 0, a.length-1);
   }

   public static short minValue(short[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      short x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        x = (short)Math.min(x, a[j]);
      }
      return x;
   }

   public static short minValue(short[] a) {
      return minValue(a, 0, a.length-1);
   }

   public static int countN(short[] a, int m, int n, short x) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int b = 0;
      for (int j = m; j <= n; j++) {
	if (a[j] == x) b++;
      }
      return b;
   }

   public static int countN(short[] a, short x) {
      return countN(a, 0, a.length-1, x);
   }

   public static short[] reverse(short[] a) {
      int temp;
      short[] b = new short[a.length];
      int n = a.length-1;
      for (int j=0; j < a.length/2; j++) {
	b[n-j] = a[j];
	b[j] = a[n-j];
      }
      return b;
   }

   public static short totalValue(short[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      short x = 0;
      for (int j = m; j <= n; j++) {
	x+= a[j];
      }
      return x;
   }

   public static short totalValue(short[] a) {
      return totalValue(a, 0, a.length-1);
   }

   public static double avgValue(short[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double x = 0;
      for (int j = m; j <= n; j++) {
        x+= a[j];
      }
      return x/(n-m+1);
   }

   public static double avgValue(short[] a) {
      return avgValue(a, 0, a.length-1);
   }

   public static short[] shift(short[] a, int x) {
      int n;
      short[] b = new short[a.length];
      for (int j = 0; j < a.length; j++) {
	n = (x+j) % a.length;
	if (n < 0) n+=a.length;
	b[n] = a[j];
      }
      return b;
   }

   public static int[] where(short[] a, String op, float x) {
      int[] b = new int[a.length];
      int[] w;
      int n = 0;
      if (op.equals("==")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] == x) b[n++] = j;
	  } 
      } else if (op.equals("!=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] != x) b[n++] = j;
	  }
      } else if (op.equals(">")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] > x) b[n++] = j;
	  }
      } else if (op.equals(">=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] >= x) b[n++] = j;
	  }
      } else if (op.equals("<")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] < x) b[n++] = j;
	  }
      } else if (op.equals("<=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] <= x) b[n++] = j;
	  }
      } else {
	  System.out.println("UFArrayOps::where> invalid operation");
	  b = new int[1];
	  b[0] = -1;
	  return b;
      }
      if (n == 0) {
	w = new int[1];
	w[0] = -1;
      } else {
	w = new int[n];
	for (int j=0; j<n; j++) w[j]=b[j];
      }
      return w;
   }

   public static short[] extractValues(short[] a, int[] n) {
      short[] b = new short[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j] >= 0 && n[j] < a.length) b[j] = a[n[j]];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j]);
      }
      return b;
   }

   public static short[] extractValues(short[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      short[] b = new short[n-m+1];
      for (int j = m; j <= n; j++) {
	b[j-m] = a[j];
      }
      return b;
   }

   public static short[][] extractValues(short[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      short[][] b = new short[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   b[j-mr][l-mc] = a[j][l];
	}
      }
      return b;
   }

   public static String arrayToString(short[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
	if (j == n) s += a[j];
	else s += a[j] + "\t";
      }
      return s;
   }

   public static String arrayToString(short[] a) {
      return arrayToString(a, 0, a.length-1);
   }

   public static String arrayToString(short[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String s = "";
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (l == nc) s += a[j][l];
	   else s += a[j][l] + "\t";
	}
        if (j != nr) s += "\n";
      }
      return s;
   }

   public static String arrayToString(short[][] a) {
      return arrayToString(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static String arrayWithIndexToString(short[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
        if (j == n) s += j + ": " + a[j];
        else s += j + ": " + a[j] + "\n";
      }
      return s;
   }

   public static String arrayWithIndexToString(short[] a) {
      return arrayWithIndexToString(a, 0, a.length-1);
   }

   public static byte maxValue(byte[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      byte x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
	x = (byte)Math.max(x, a[j]);
      }
      return x;
   }

   public static byte maxValue(byte[] a) {
      return maxValue(a, 0, a.length-1);
   }

   public static byte minValue(byte[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      byte x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        x = (byte)Math.min(x, a[j]);
      }
      return x;
   }

   public static byte minValue(byte[] a) {
      return minValue(a, 0, a.length-1);
   }

   public static int countN(byte[] a, int m, int n, byte x) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int b = 0;
      for (int j = m; j <= n; j++) {
	if (a[j] == x) b++;
      }
      return b;
   }

   public static int countN(byte[] a, byte x) {
      return countN(a, 0, a.length-1, x);
   }

   public static byte[] reverse(byte[] a) {
      int temp;
      byte[] b = new byte[a.length];
      int n = a.length-1;
      for (int j=0; j < a.length/2; j++) {
	b[n-j] = a[j];
	b[j] = a[n-j];
      }
      return b;
   }

   public static byte totalValue(byte[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      byte x = 0;
      for (int j = m; j <= n; j++) {
	x+= a[j];
      }
      return x;
   }

   public static byte totalValue(byte[] a) {
      return totalValue(a, 0, a.length-1);
   }

   public static double avgValue(byte[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double x = 0;
      for (int j = m; j <= n; j++) {
        x+= a[j];
      }
      return x/(n-m+1);
   }

   public static double avgValue(byte[] a) {
      return avgValue(a, 0, a.length-1);
   }

   public static byte[] shift(byte[] a, int x) {
      int n;
      byte[] b = new byte[a.length];
      for (int j = 0; j < a.length; j++) {
	n = (x+j) % a.length;
	if (n < 0) n+=a.length;
	b[n] = a[j];
      }
      return b;
   }

   public static int[] where(byte[] a, String op, float x) {
      int[] b = new int[a.length];
      int[] w;
      int n = 0;
      if (op.equals("==")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] == x) b[n++] = j;
	  } 
      } else if (op.equals("!=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] != x) b[n++] = j;
	  }
      } else if (op.equals(">")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] > x) b[n++] = j;
	  }
      } else if (op.equals(">=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] >= x) b[n++] = j;
	  }
      } else if (op.equals("<")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] < x) b[n++] = j;
	  }
      } else if (op.equals("<=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] <= x) b[n++] = j;
	  }
      } else {
	  System.out.println("UFArrayOps::where> invalid operation");
	  b = new int[1];
	  b[0] = -1;
	  return b;
      }
      if (n == 0) {
	w = new int[1];
	w[0] = -1;
      } else {
	w = new int[n];
	for (int j=0; j<n; j++) w[j]=b[j];
      }
      return w;
   }

   public static byte[] extractValues(byte[] a, int[] n) {
      byte[] b = new byte[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j] >= 0 && n[j] < a.length) b[j] = a[n[j]];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j]);
      }
      return b;
   }

   public static byte[] extractValues(byte[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      byte[] b = new byte[n-m+1];
      for (int j = m; j <= n; j++) {
	b[j-m] = a[j];
      }
      return b;
   }

   public static byte[][] extractValues(byte[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      byte[][] b = new byte[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   b[j-mr][l-mc] = a[j][l];
	}
      }
      return b;
   }

   public static String arrayToString(byte[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
	if (j == n) s += a[j];
	else s += a[j] + "\t";
      }
      return s;
   }

   public static String arrayToString(byte[] a) {
      return arrayToString(a, 0, a.length-1);
   }

   public static String arrayToString(byte[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String s = "";
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (l == nc) s += a[j][l];
	   else s += a[j][l] + "\t";
	}
        if (j != nr) s += "\n";
      }
      return s;
   }

   public static String arrayToString(byte[][] a) {
      return arrayToString(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static String arrayWithIndexToString(byte[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
        if (j == n) s += j + ": " + a[j];
        else s += j + ": " + a[j] + "\n";
      }
      return s;
   }

   public static String arrayWithIndexToString(byte[] a) {
      return arrayWithIndexToString(a, 0, a.length-1);
   }

   public static char maxValue(char[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      char x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
	x = (char)Math.max(x, a[j]);
      }
      return x;
   }

   public static char maxValue(char[] a) {
      return maxValue(a, 0, a.length-1);
   }

   public static char minValue(char[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      char x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        x = (char)Math.min(x, a[j]);
      }
      return x;
   }

   public static char minValue(char[] a) {
      return minValue(a, 0, a.length-1);
   }

   public static int countN(char[] a, int m, int n, char x) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int b = 0;
      for (int j = m; j <= n; j++) {
	if (a[j] == x) b++;
      }
      return b;
   }

   public static int countN(char[] a, char x) {
      return countN(a, 0, a.length-1, x);
   }

   public static char[] reverse(char[] a) {
      int temp;
      char[] b = new char[a.length];
      int n = a.length-1;
      for (int j=0; j < a.length/2; j++) {
	b[n-j] = a[j];
	b[j] = a[n-j];
      }
      return b;
   }

   public static char[] shift(char[] a, int x) {
      int n;
      char[] b = new char[a.length];
      for (int j = 0; j < a.length; j++) {
	n = (x+j) % a.length;
	if (n < 0) n+=a.length;
	b[n] = a[j];
      }
      return b;
   }

   public static int[] where(char[] a, String op, char x) {
      int[] b = new int[a.length];
      int[] w;
      int n = 0;
      if (op.equals("==")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] == x) b[n++] = j;
	  } 
      } else if (op.equals("!=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] != x) b[n++] = j;
	  }
      } else if (op.equals(">")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] > x) b[n++] = j;
	  }
      } else if (op.equals(">=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] >= x) b[n++] = j;
	  }
      } else if (op.equals("<")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] < x) b[n++] = j;
	  }
      } else if (op.equals("<=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j] <= x) b[n++] = j;
	  }
      } else {
	  System.out.println("UFArrayOps::where> invalid operation");
	  b = new int[1];
	  b[0] = -1;
	  return b;
      }
      if (n == 0) {
	w = new int[1];
	w[0] = -1;
      } else {
	w = new int[n];
	for (int j=0; j<n; j++) w[j]=b[j];
      }
      return w;
   }

   public static char[] extractValues(char[] a, int[] n) {
      char[] b = new char[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j] >= 0 && n[j] < a.length) b[j] = a[n[j]];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j]);
      }
      return b;
   }

   public static char[] extractValues(char[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      char[] b = new char[n-m+1];
      for (int j = m; j <= n; j++) {
	b[j-m] = a[j];
      }
      return b;
   }

   public static char[][] extractValues(char[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      char[][] b = new char[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   b[j-mr][l-mc] = a[j][l];
	}
      }
      return b;
   }

   public static String arrayToString(char[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
	if (j == n) s += a[j];
	else s += a[j] + "\t";
      }
      return s;
   }

   public static String arrayToString(char[] a) {
      return arrayToString(a, 0, a.length-1);
   }

   public static String arrayToString(char[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String s = "";
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (l == nc) s += a[j][l];
	   else s += a[j][l] + "\t";
	}
        if (j != nr) s += "\n";
      }
      return s;
   }

   public static String arrayToString(char[][] a) {
      return arrayToString(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static String arrayWithIndexToString(char[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
        if (j == n) s += j + ": " + a[j];
        else s += j + ": " + a[j] + "\n";
      }
      return s;
   }

   public static String arrayWithIndexToString(char[] a) {
      return arrayWithIndexToString(a, 0, a.length-1);
   }

   public static String maxValue(String[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
	if (a[j].compareTo(x) > 0) x = a[j];
      }
      return x;
   }

   public static String maxValue(String[] a) {
      return maxValue(a, 0, a.length-1);
   }

   public static String minValue(String[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
	if (a[j].compareTo(x) < 0) x = a[j];
      }
      return x;
   }

   public static String minValue(String[] a) {
      return minValue(a, 0, a.length-1);
   }

   public static int countN(String[] a, int m, int n, String x) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int b = 0;
      for (int j = m; j <= n; j++) {
	if (a[j].equals(x)) b++;
      }
      return b;
   }

   public static int countN(String[] a, String x) {
      return countN(a, 0, a.length-1, x);
   }

   public static String[] reverse(String[] a) {
      int temp;
      String[] b = new String[a.length];
      int n = a.length-1;
      for (int j=0; j < a.length/2; j++) {
	b[n-j] = a[j];
	b[j] = a[n-j];
      }
      return b;
   }

   public static String[] shift(String[] a, int x) {
      int n;
      String[] b = new String[a.length];
      for (int j = 0; j < a.length; j++) {
	n = (x+j) % a.length;
	if (n < 0) n+=a.length;
	b[n] = a[j];
      }
      return b;
   }

   public static int[] where(String[] a, String op, String x) {
      int[] b = new int[a.length];
      int[] w;
      int n = 0;
      if (op.equals("==")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j].equals(x)) b[n++] = j;
	  } 
      } else if (op.equals("!=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (!a[j].equals(x)) b[n++] = j;
	  }
      } else if (op.equals(">")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j].compareTo(x) > 0) b[n++] = j;
	  }
      } else if (op.equals(">=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j].compareTo(x) >= 0) b[n++] = j;
	  }
      } else if (op.equals("<")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j].compareTo(x) < 0) b[n++] = j;
	  }
      } else if (op.equals("<=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j].compareTo(x) <= 0) b[n++] = j;
	  }
      } else {
	  System.out.println("UFArrayOps::where> invalid operation");
	  b = new int[1];
	  b[0] = -1;
	  return b;
      }
      if (n == 0) {
	w = new int[1];
	w[0] = -1;
      } else {
	w = new int[n];
	for (int j=0; j<n; j++) w[j]=b[j];
      }
      return w;
   }

   public static String[] extractValues(String[] a, int[] n) {
      String[] b = new String[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j] >= 0 && n[j] < a.length) b[j] = a[n[j]];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j]);
      }
      return b;
   }

   public static String[] extractValues(String[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String[] b = new String[n-m+1];
      for (int j = m; j <= n; j++) {
	b[j-m] = a[j];
      }
      return b;
   }

   public static String[][] extractValues(String[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String[][] b = new String[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   b[j-mr][l-mc] = a[j][l];
	}
      }
      return b;
   }

   public static String arrayToString(String[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
	if (j == n) s += a[j];
	else s += a[j] + "\t";
      }
      return s;
   }

   public static String arrayToString(String[] a) {
      return arrayToString(a, 0, a.length-1);
   }

   public static String arrayToString(String[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String s = "";
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (l == nc) s += a[j][l];
	   else s += a[j][l] + "\t";
	}
        if (j != nr) s += "\n";
      }
      return s;
   }

   public static String arrayToString(String[][] a) {
      return arrayToString(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static String arrayWithIndexToString(String[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
        if (j == n) s += j + ": " + a[j];
        else s += j + ": " + a[j] + "\n";
      }
      return s;
   }

   public static String arrayWithIndexToString(String[] a) {
      return arrayWithIndexToString(a, 0, a.length-1);
   }

   public static int[] strlenArray(String[] a) {
      int[] x = new int[a.length];
      for (int j = 0; j < a.length; j++) {
	x[j] = a[j].length();
      }
      return x;
   }

   public static int[] strposArray(String[] a, String s, int m) {
      if (m < 0) m = 0;
      int[] x = new int[a.length];
      for (int j = 0; j < a.length; j++) {
	x[j] = a[j].indexOf(s, m);
      }
      return x;
   }

   public static int[] strposArray(String[] a, String s) {
      return strposArray(a, s, 0);
   }

   public static String[] strmidArray(String[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String[] x = new String[a.length];
      for (int j = 0; j < a.length; j++) {
	x[j] = a[j].substring(m, n);
      }
      return x;
   }

   public static Object maxValue(Comparable[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      Object x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j].compareTo(x) > 0) x = a[j];
      }
      return x;
   }

   public static Object maxValue(Comparable[] a) {
      return maxValue(a, 0, a.length-1);
   }

   public static Object minValue(Comparable[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      Object x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j].compareTo(x) < 0) x = a[j];
      }
      return x;
   }

   public static Object minValue(Comparable[] a) {
      return minValue(a, 0, a.length-1);
   }

   public static int countN(Comparable[] a, int m, int n, Object x) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int b = 0;
      for (int j = m; j <= n; j++) {
	if (a[j].equals(x)) b++;
      }
      return b;
   }

   public static int countN(Comparable[] a, Object x) {
      return countN(a, 0, a.length-1, x);
   }

   public static Object[] reverse(Object[] a) {
      int temp;
      Object[] b = new Object[a.length];
      int n = a.length-1;
      for (int j=0; j < a.length/2; j++) {
	b[n-j] = a[j];
	b[j] = a[n-j];
      }
      return b;
   }

   public static Object[] shift(Object[] a, int x) {
      int n;
      Object[] b = new Object[a.length];
      for (int j = 0; j < a.length; j++) {
	n = (x+j) % a.length;
	if (n < 0) n+=a.length;
	b[n] = a[j];
      }
      return b;
   }

   public static int[] where(Comparable[] a, String op, Object x) {
      int[] b = new int[a.length];
      int[] w;
      int n = 0;
      if (op.equals("==")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j].equals(x)) b[n++] = j;
	  } 
      } else if (op.equals("!=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (!a[j].equals(x)) b[n++] = j;
	  }
      } else if (op.equals(">")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j].compareTo(x) > 0) b[n++] = j;
	  }
      } else if (op.equals(">=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j].compareTo(x) >= 0) b[n++] = j;
	  }
      } else if (op.equals("<")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j].compareTo(x) < 0) b[n++] = j;
	  }
      } else if (op.equals("<=")) {
	  for (int j = 0; j < a.length; j++) {
	      if (a[j].compareTo(x) <= 0) b[n++] = j;
	  }
      } else {
	  System.out.println("UFArrayOps::where> invalid operation");
	  b = new int[1];
	  b[0] = -1;
	  return b;
      }
      if (n == 0) {
	w = new int[1];
	w[0] = -1;
      } else {
	w = new int[n];
	for (int j=0; j<n; j++) w[j]=b[j];
      }
      return w;
   }

   public static Object[] extractValues(Object[] a, int[] n) {
      Object[] b = new Object[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j] >= 0 && n[j] < a.length) b[j] = a[n[j]];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j]);
      }
      return b;
   }

   public static Object[] extractValues(Object[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      Object[] b = new Object[n-m+1];
      for (int j = m; j <= n; j++) {
	b[j-m] = a[j];
      }
      return b;
   }

   public static Object[][] extractValues(Object[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      Object[][] b = new Object[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   b[j-mr][l-mc] = a[j][l];
	}
      }
      return b;
   }

   public static String arrayToString(Object[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
	if (j == n) s += a[j].toString();
	else s += a[j].toString() + "\t";
      }
      return s;
   }

   public static String arrayToString(Object[] a) {
      return arrayToString(a, 0, a.length-1);
   }

   public static String arrayToString(Object[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String s = "";
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (l == nc) s += a[j][l].toString();
	   else s += a[j][l].toString() + "\t";
	}
        if (j != nr) s += "\n";
      }
      return s;
   }

   public static String arrayToString(Object[][] a) {
      return arrayToString(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static String arrayWithIndexToString(Object[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String s = "";
      for (int j = m; j <= n; j++) {
        if (j == n) s += j + ": " + a[j].toString();
        else s += j + ": " + a[j].toString() + "\n";
      }
      return s;
   }

   public static String arrayWithIndexToString(Object[] a) {
      return arrayWithIndexToString(a, 0, a.length-1);
   }

   public static int maxValue(int[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x = Math.max(x, a[j][l]);
	}
      }
      return x;
   }

   public static int maxValue(int[][] a) {
      return maxValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static int minValue(int[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
           x = Math.min(x, a[j][l]);
        }
      }
      return x;
   }

   public static int minValue(int[][] a) {
      return minValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static int countN(int[][] a, int mr, int nr, int mc, int nc, int x) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int b = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (a[j][l] == x) b++;
	}
      }
      return b;
   }

   public static int countN(int[][] a, int x) {
      return countN(a, 0, a.length-1, 0, a[0].length-1, x);
   }

   public static int[][] reverse(int[][] a, int dim) {
      if (dim != 2) dim = 1;
      int temp;
      int[][] b = new int[a.length][a[0].length];
      if (dim == 1) {
        int n = a[0].length-1;
        for (int j=0; j < a.length; j++) {
	   for (int l = 0; l <= a[0].length/2; l++) {
	      b[j][n-l] = a[j][l];
	      b[j][l] = a[j][n-l];
	   }
        }
      } else {
        int n = a.length-1;
        for (int j=0; j <= a.length/2; j++) {
           for (int l = 0; l < a[0].length; l++) {
              b[n-j][l] = a[j][l];
              b[j][l] = a[n-j][l];
           }
        }
      }
      return b;
   }

   public static int[][] reverse(int[][] a) {
      return reverse(a, 1); 
   }

   public static int totalValue(int[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int x = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x+= a[j][l];
	}
      }
      return x;
   }

   public static int totalValue(int[][] a) {
      return totalValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static double avgValue(int[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double x = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
           x+= a[j][l];
	}
      }
      return x/((nr-mr+1)*(nc-mc+1));
   }

   public static double avgValue(int[][] a) {
      return avgValue(a, 0, a.length-1, 0, a[0].length);
   }

   public static int[][] shift(int[][] a, int x, int y) {
      int nr, nc;
      int[][] b = new int[a.length][a[0].length];
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   nr = (x+j) % a.length;
	   if (nr < 0) nr+=a.length;
	   nc = (y+l) % a[0].length;
	   if (nc < 0) nc+=a[0].length;
	   b[nr][nc] = a[j][l];
	}
      }
      return b;
   }

   public static int[][] where(int[][] a, String op, float x) {
      int[][] b = new int[a.length*a[0].length][2];
      int[][] w;
      int n = 0;
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   if (op.equals("==")) {
	      if (a[j][l] == x) {
	        b[n][0] = j;
		b[n][1] = l;
	        n++;
	      } 
	   } else if (op.equals("!=")) {
              if (a[j][l] != x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">")) {
              if (a[j][l] > x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">=")) {
              if (a[j][l] >= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<")) {
              if (a[j][l] < x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<=")) {
              if (a[j][l] <= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else {
	      System.out.println("UFArrayOps::where> invalid operation");
	      b = new int[1][2];
	      b[0][0] = -1;
	      b[0][1] = -1;
	      return b;
           }
	}
      }
      if (n == 0) {
	w = new int[1][2];
	w[0][0] = -1;
	w[0][1] = -1;
      } else {
	w = new int[n][2];
	for (int j=0; j<n; j++) {
	   w[j][0]=b[j][0];
	   w[j][1]=b[j][1];
	}
      }
      return w;
   }

   public static int[][] andUnion(int[][] a, int[][] b) {
      int[][] x = new int[a.length][2];
      int n = 0;
      int[][] w;
      int[] bx = new int[b.length];
      for (int j=0; j < b.length; j++)
	bx[j] = b[j][0]*(a.length+b.length)+b[j][1];
      for (int j = 0; j < a.length; j++) {
        if (Arrays.binarySearch(bx,a[j][0]*(a.length+b.length)+a[j][1]) >= 0) {
           x[n][0] = a[j][0];
	   x[n][1] = a[j][1];
           n++;
        }
      }
      if (n == 0) {
        w = new int[1][2];
        w[0][0] = -1;
	w[0][1] = -1;
      } else {
        w = new int[n][2];
        for (int j=0; j<n; j++) {
	   w[j][0]=x[j][0];
	   w[j][1]=x[j][1];
	}
      }
      return w;
   }

   public static int[][] orUnion(int[][] a, int[][] b) {
      int[][] x = new int[a.length + b.length][2];
      int n = 0;
      int[][] w;
      int[] ax = new int[b.length];
      for (int j=0; j < a.length; j++)
        ax[j] = a[j][0]*(a.length+b.length)+a[j][1];
      for (int j = 0; j < b.length; j++) {
        if (b[j][0] != -1 && Arrays.binarySearch(ax, b[j][0]*(a.length+b.length)+b[j][1]) < 0) {
           x[n][0] = b[j][0];
	   x[n][1] = b[j][1];
           n++;
        }
      }
      if (n == 0) w = a;
      else if (a[0][0] == -1) w = b;
      else {
        w = new int[n+a.length][2];
        for (int j = 0; j < a.length; j++) {
	   w[j][0] = a[j][0];
	   w[j][1] = a[j][1];
	}
        for (int j = 0; j < n; j++) {
	   w[a.length+j][0] = x[j][0];
	   w[a.length+j][1] = x[j][1];
	}
      }
      return w;
   }

   public static int[] extractValues(int[][] a, int[][] n) {
      int[] b = new int[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j][0] >= 0 && n[j][0] < a.length && n[j][1] >= 0 && n[j][1] < a[0].length)
	    b[j] = a[ n[j][0] ][ n[j][1] ];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j][0] + ", " + n[j][1]);
      }
      return b;
   }

   public static int[] map2DByRow(int[][] a) {
      int[] b = new int[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
	for (int l=0; l < a[0].length; l++) {
	   b[j*a[0].length+l]=a[j][l];
	}
      }
      return b;
   }

   public static int[] map2DByColumn(int[][] a) {
      int[] b = new int[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
        for (int l=0; l < a[0].length; l++) {
           b[j+a.length*l]=a[j][l];
        }
      }
      return b;
   }
   
   public static float maxValue(float[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      float x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x = Math.max(x, a[j][l]);
	}
      }
      return x;
   }

   public static float maxValue(float[][] a) {
      return maxValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static float minValue(float[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      float x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
           x = Math.min(x, a[j][l]);
        }
      }
      return x;
   }

   public static float minValue(float[][] a) {
      return minValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static int countN(float[][] a, int mr, int nr, int mc, int nc, float x) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int b = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (a[j][l] == x) b++;
	}
      }
      return b;
   }

   public static int countN(float[][] a, float x) {
      return countN(a, 0, a.length-1, 0, a[0].length-1, x);
   }

   public static float[][] reverse(float[][] a, int dim) {
      if (dim != 2) dim = 1;
      int temp;
      float[][] b = new float[a.length][a[0].length];
      if (dim == 1) {
        int n = a[0].length-1;
        for (int j=0; j < a.length; j++) {
	   for (int l = 0; l <= a[0].length/2; l++) {
	      b[j][n-l] = a[j][l];
	      b[j][l] = a[j][n-l];
	   }
        }
      } else {
        int n = a.length-1;
        for (int j=0; j <= a.length/2; j++) {
           for (int l = 0; l < a[0].length; l++) {
              b[n-j][l] = a[j][l];
              b[j][l] = a[n-j][l];
           }
        }
      }
      return b;
   }

   public static float[][] reverse(float[][] a) {
      return reverse(a, 1); 
   }

   public static float totalValue(float[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      float x = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x+= a[j][l];
	}
      }
      return x;
   }

   public static float totalValue(float[][] a) {
      return totalValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static double avgValue(float[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double x = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
           x+= a[j][l];
	}
      }
      return x/((nr-mr+1)*(nc-mc+1));
   }

   public static double avgValue(float[][] a) {
      return avgValue(a, 0, a.length-1, 0, a[0].length);
   }

   public static float[][] shift(float[][] a, int x, int y) {
      int nr, nc;
      float[][] b = new float[a.length][a[0].length];
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   nr = (x+j) % a.length;
	   if (nr < 0) nr+=a.length;
	   nc = (y+l) % a[0].length;
	   if (nc < 0) nc+=a[0].length;
	   b[nr][nc] = a[j][l];
	}
      }
      return b;
   }

   public static int[][] where(float[][] a, String op, float x) {
      int[][] b = new int[a.length*a[0].length][2];
      int[][] w;
      int n = 0;
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   if (op.equals("==")) {
	      if (a[j][l] == x) {
	        b[n][0] = j;
		b[n][1] = l;
	        n++;
	      } 
	   } else if (op.equals("!=")) {
              if (a[j][l] != x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">")) {
              if (a[j][l] > x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">=")) {
              if (a[j][l] >= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<")) {
              if (a[j][l] < x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<=")) {
              if (a[j][l] <= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else {
	      System.out.println("UFArrayOps::where> invalid operation");
	      b = new int[1][2];
	      b[0][0] = -1;
	      b[0][1] = -1;
	      return b;
           }
	}
      }
      if (n == 0) {
	w = new int[1][2];
	w[0][0] = -1;
	w[0][1] = -1;
      } else {
	w = new int[n][2];
	for (int j=0; j<n; j++) {
	   w[j][0]=b[j][0];
	   w[j][1]=b[j][1];
	}
      }
      return w;
   }

   public static float[] extractValues(float[][] a, int[][] n) {
      float[] b = new float[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j][0] >= 0 && n[j][0] < a.length && n[j][1] >= 0 && n[j][1] < a[0].length)
	    b[j] = a[ n[j][0] ][ n[j][1] ];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j][0] + ", " + n[j][1]);
      }
      return b;
   }

   public static float[] map2DByRow(float[][] a) {
      float[] b = new float[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
	for (int l=0; l < a[0].length; l++) {
	   b[j*a[0].length+l]=a[j][l];
	}
      }
      return b;
   }

   public static float[] map2DByColumn(float[][] a) {
      float[] b = new float[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
        for (int l=0; l < a[0].length; l++) {
           b[j+a.length*l]=a[j][l];
        }
      }
      return b;
   }
 
   public static double maxValue(double[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x = Math.max(x, a[j][l]);
	}
      }
      return x;
   }

   public static double maxValue(double[][] a) {
      return maxValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static double minValue(double[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
           x = Math.min(x, a[j][l]);
        }
      }
      return x;
   }

   public static double minValue(double[][] a) {
      return minValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static int countN(double[][] a, int mr, int nr, int mc, int nc, double x) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int b = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (a[j][l] == x) b++;
	}
      }
      return b;
   }

   public static int countN(double[][] a, double x) {
      return countN(a, 0, a.length-1, 0, a[0].length-1, x);
   }

   public static double[][] reverse(double[][] a, int dim) {
      if (dim != 2) dim = 1;
      int temp;
      double[][] b = new double[a.length][a[0].length];
      if (dim == 1) {
        int n = a[0].length-1;
        for (int j=0; j < a.length; j++) {
	   for (int l = 0; l <= a[0].length/2; l++) {
	      b[j][n-l] = a[j][l];
	      b[j][l] = a[j][n-l];
	   }
        }
      } else {
        int n = a.length-1;
        for (int j=0; j <= a.length/2; j++) {
           for (int l = 0; l < a[0].length; l++) {
              b[n-j][l] = a[j][l];
              b[j][l] = a[n-j][l];
           }
        }
      }
      return b;
   }

   public static double[][] reverse(double[][] a) {
      return reverse(a, 1); 
   }

   public static double totalValue(double[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double x = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x+= a[j][l];
	}
      }
      return x;
   }

   public static double totalValue(double[][] a) {
      return totalValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static double avgValue(double[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double x = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
           x+= a[j][l];
	}
      }
      return x/((nr-mr+1)*(nc-mc+1));
   }

   public static double avgValue(double[][] a) {
      return avgValue(a, 0, a.length-1, 0, a[0].length);
   }

   public static double[][] shift(double[][] a, int x, int y) {
      int nr, nc;
      double[][] b = new double[a.length][a[0].length];
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   nr = (x+j) % a.length;
	   if (nr < 0) nr+=a.length;
	   nc = (y+l) % a[0].length;
	   if (nc < 0) nc+=a[0].length;
	   b[nr][nc] = a[j][l];
	}
      }
      return b;
   }

   public static int[][] where(double[][] a, String op, double x) {
      int[][] b = new int[a.length*a[0].length][2];
      int[][] w;
      int n = 0;
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   if (op.equals("==")) {
	      if (a[j][l] == x) {
	        b[n][0] = j;
		b[n][1] = l;
	        n++;
	      } 
	   } else if (op.equals("!=")) {
              if (a[j][l] != x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">")) {
              if (a[j][l] > x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">=")) {
              if (a[j][l] >= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<")) {
              if (a[j][l] < x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<=")) {
              if (a[j][l] <= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else {
	      System.out.println("UFArrayOps::where> invalid operation");
	      b = new int[1][2];
	      b[0][0] = -1;
	      b[0][1] = -1;
	      return b;
           }
	}
      }
      if (n == 0) {
	w = new int[1][2];
	w[0][0] = -1;
	w[0][1] = -1;
      } else {
	w = new int[n][2];
	for (int j=0; j<n; j++) {
	   w[j][0]=b[j][0];
	   w[j][1]=b[j][1];
	}
      }
      return w;
   }

   public static double[] extractValues(double[][] a, int[][] n) {
      double[] b = new double[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j][0] >= 0 && n[j][0] < a.length && n[j][1] >= 0 && n[j][1] < a[0].length)
	    b[j] = a[ n[j][0] ][ n[j][1] ];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j][0] + ", " + n[j][1]);
      }
      return b;
   }

   public static double[] map2DByRow(double[][] a) {
      double[] b = new double[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
	for (int l=0; l < a[0].length; l++) {
	   b[j*a[0].length+l]=a[j][l];
	}
      }
      return b;
   }

   public static double[] map2DByColumn(double[][] a) {
      double[] b = new double[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
        for (int l=0; l < a[0].length; l++) {
           b[j+a.length*l]=a[j][l];
        }
      }
      return b;
   }
   
   public static long maxValue(long[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      long x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x = Math.max(x, a[j][l]);
	}
      }
      return x;
   }

   public static long maxValue(long[][] a) {
      return maxValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static long minValue(long[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      long x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
           x = Math.min(x, a[j][l]);
        }
      }
      return x;
   }

   public static long minValue(long[][] a) {
      return minValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static int countN(long[][] a, int mr, int nr, int mc, int nc, long x) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int b = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (a[j][l] == x) b++;
	}
      }
      return b;
   }

   public static int countN(long[][] a, long x) {
      return countN(a, 0, a.length-1, 0, a[0].length-1, x);
   }

   public static long[][] reverse(long[][] a, int dim) {
      if (dim != 2) dim = 1;
      int temp;
      long[][] b = new long[a.length][a[0].length];
      if (dim == 1) {
        int n = a[0].length-1;
        for (int j=0; j < a.length; j++) {
	   for (int l = 0; l <= a[0].length/2; l++) {
	      b[j][n-l] = a[j][l];
	      b[j][l] = a[j][n-l];
	   }
        }
      } else {
        int n = a.length-1;
        for (int j=0; j <= a.length/2; j++) {
           for (int l = 0; l < a[0].length; l++) {
              b[n-j][l] = a[j][l];
              b[j][l] = a[n-j][l];
           }
        }
      }
      return b;
   }

   public static long[][] reverse(long[][] a) {
      return reverse(a, 1); 
   }

   public static long totalValue(long[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      long x = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x+= a[j][l];
	}
      }
      return x;
   }

   public static long totalValue(long[][] a) {
      return totalValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static double avgValue(long[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double x = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
           x+= a[j][l];
	}
      }
      return x/((nr-mr+1)*(nc-mc+1));
   }

   public static double avgValue(long[][] a) {
      return avgValue(a, 0, a.length-1, 0, a[0].length);
   }

   public static long[][] shift(long[][] a, int x, int y) {
      int nr, nc;
      long[][] b = new long[a.length][a[0].length];
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   nr = (x+j) % a.length;
	   if (nr < 0) nr+=a.length;
	   nc = (y+l) % a[0].length;
	   if (nc < 0) nc+=a[0].length;
	   b[nr][nc] = a[j][l];
	}
      }
      return b;
   }

   public static int[][] where(long[][] a, String op, float x) {
      int[][] b = new int[a.length*a[0].length][2];
      int[][] w;
      int n = 0;
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   if (op.equals("==")) {
	      if (a[j][l] == x) {
	        b[n][0] = j;
		b[n][1] = l;
	        n++;
	      } 
	   } else if (op.equals("!=")) {
              if (a[j][l] != x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">")) {
              if (a[j][l] > x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">=")) {
              if (a[j][l] >= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<")) {
              if (a[j][l] < x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<=")) {
              if (a[j][l] <= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else {
	      System.out.println("UFArrayOps::where> invalid operation");
	      b = new int[1][2];
	      b[0][0] = -1;
	      b[0][1] = -1;
	      return b;
           }
	}
      }
      if (n == 0) {
	w = new int[1][2];
	w[0][0] = -1;
	w[0][1] = -1;
      } else {
	w = new int[n][2];
	for (int j=0; j<n; j++) {
	   w[j][0]=b[j][0];
	   w[j][1]=b[j][1];
	}
      }
      return w;
   }

   public static long[] extractValues(long[][] a, int[][] n) {
      long[] b = new long[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j][0] >= 0 && n[j][0] < a.length && n[j][1] >= 0 && n[j][1] < a[0].length)
	    b[j] = a[ n[j][0] ][ n[j][1] ];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j][0] + ", " + n[j][1]);
      }
      return b;
   }

   public static long[] map2DByRow(long[][] a) {
      long[] b = new long[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
	for (int l=0; l < a[0].length; l++) {
	   b[j*a[0].length+l]=a[j][l];
	}
      }
      return b;
   }

   public static long[] map2DByColumn(long[][] a) {
      long[] b = new long[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
        for (int l=0; l < a[0].length; l++) {
           b[j+a.length*l]=a[j][l];
        }
      }
      return b;
   }
   
   public static short maxValue(short[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      short x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x = (short)Math.max(x, a[j][l]);
	}
      }
      return x;
   }

   public static short maxValue(short[][] a) {
      return maxValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static short minValue(short[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      short x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
           x = (short)Math.min(x, a[j][l]);
        }
      }
      return x;
   }

   public static short minValue(short[][] a) {
      return minValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static int countN(short[][] a, int mr, int nr, int mc, int nc, short x) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int b = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (a[j][l] == x) b++;
	}
      }
      return b;
   }

   public static int countN(short[][] a, short x) {
      return countN(a, 0, a.length-1, 0, a[0].length-1, x);
   }

   public static short[][] reverse(short[][] a, int dim) {
      if (dim != 2) dim = 1;
      int temp;
      short[][] b = new short[a.length][a[0].length];
      if (dim == 1) {
        int n = a[0].length-1;
        for (int j=0; j < a.length; j++) {
	   for (int l = 0; l <= a[0].length/2; l++) {
	      b[j][n-l] = a[j][l];
	      b[j][l] = a[j][n-l];
	   }
        }
      } else {
        int n = a.length-1;
        for (int j=0; j <= a.length/2; j++) {
           for (int l = 0; l < a[0].length; l++) {
              b[n-j][l] = a[j][l];
              b[j][l] = a[n-j][l];
           }
        }
      }
      return b;
   }

   public static short[][] reverse(short[][] a) {
      return reverse(a, 1); 
   }

   public static short totalValue(short[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      short x = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x+= a[j][l];
	}
      }
      return x;
   }

   public static short totalValue(short[][] a) {
      return totalValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static double avgValue(short[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double x = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
           x+= a[j][l];
	}
      }
      return x/((nr-mr+1)*(nc-mc+1));
   }

   public static double avgValue(short[][] a) {
      return avgValue(a, 0, a.length-1, 0, a[0].length);
   }

   public static short[][] shift(short[][] a, int x, int y) {
      int nr, nc;
      short[][] b = new short[a.length][a[0].length];
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   nr = (x+j) % a.length;
	   if (nr < 0) nr+=a.length;
	   nc = (y+l) % a[0].length;
	   if (nc < 0) nc+=a[0].length;
	   b[nr][nc] = a[j][l];
	}
      }
      return b;
   }

   public static int[][] where(short[][] a, String op, float x) {
      int[][] b = new int[a.length*a[0].length][2];
      int[][] w;
      int n = 0;
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   if (op.equals("==")) {
	      if (a[j][l] == x) {
	        b[n][0] = j;
		b[n][1] = l;
	        n++;
	      } 
	   } else if (op.equals("!=")) {
              if (a[j][l] != x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">")) {
              if (a[j][l] > x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">=")) {
              if (a[j][l] >= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<")) {
              if (a[j][l] < x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<=")) {
              if (a[j][l] <= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else {
	      System.out.println("UFArrayOps::where> invalid operation");
	      b = new int[1][2];
	      b[0][0] = -1;
	      b[0][1] = -1;
	      return b;
           }
	}
      }
      if (n == 0) {
	w = new int[1][2];
	w[0][0] = -1;
	w[0][1] = -1;
      } else {
	w = new int[n][2];
	for (int j=0; j<n; j++) {
	   w[j][0]=b[j][0];
	   w[j][1]=b[j][1];
	}
      }
      return w;
   }

   public static short[] extractValues(short[][] a, int[][] n) {
      short[] b = new short[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j][0] >= 0 && n[j][0] < a.length && n[j][1] >= 0 && n[j][1] < a[0].length)
	    b[j] = a[ n[j][0] ][ n[j][1] ];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j][0] + ", " + n[j][1]);
      }
      return b;
   }

   public static short[] map2DByRow(short[][] a) {
      short[] b = new short[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
	for (int l=0; l < a[0].length; l++) {
	   b[j*a[0].length+l]=a[j][l];
	}
      }
      return b;
   }

   public static short[] map2DByColumn(short[][] a) {
      short[] b = new short[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
        for (int l=0; l < a[0].length; l++) {
           b[j+a.length*l]=a[j][l];
        }
      }
      return b;
   }
   
   public static byte maxValue(byte[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      byte x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x = (byte)Math.max(x, a[j][l]);
	}
      }
      return x;
   }

   public static byte maxValue(byte[][] a) {
      return maxValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static byte minValue(byte[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      byte x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
           x = (byte)Math.min(x, a[j][l]);
        }
      }
      return x;
   }

   public static byte minValue(byte[][] a) {
      return minValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static int countN(byte[][] a, int mr, int nr, int mc, int nc, byte x) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int b = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (a[j][l] == x) b++;
	}
      }
      return b;
   }

   public static int countN(byte[][] a, byte x) {
      return countN(a, 0, a.length-1, 0, a[0].length-1, x);
   }

   public static byte[][] reverse(byte[][] a, int dim) {
      if (dim != 2) dim = 1;
      int temp;
      byte[][] b = new byte[a.length][a[0].length];
      if (dim == 1) {
        int n = a[0].length-1;
        for (int j=0; j < a.length; j++) {
	   for (int l = 0; l <= a[0].length/2; l++) {
	      b[j][n-l] = a[j][l];
	      b[j][l] = a[j][n-l];
	   }
        }
      } else {
        int n = a.length-1;
        for (int j=0; j <= a.length/2; j++) {
           for (int l = 0; l < a[0].length; l++) {
              b[n-j][l] = a[j][l];
              b[j][l] = a[n-j][l];
           }
        }
      }
      return b;
   }

   public static byte[][] reverse(byte[][] a) {
      return reverse(a, 1); 
   }

   public static byte totalValue(byte[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      byte x = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x+= a[j][l];
	}
      }
      return x;
   }

   public static byte totalValue(byte[][] a) {
      return totalValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static double avgValue(byte[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double x = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
           x+= a[j][l];
	}
      }
      return x/((nr-mr+1)*(nc-mc+1));
   }

   public static double avgValue(byte[][] a) {
      return avgValue(a, 0, a.length-1, 0, a[0].length);
   }

   public static byte[][] shift(byte[][] a, int x, int y) {
      int nr, nc;
      byte[][] b = new byte[a.length][a[0].length];
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   nr = (x+j) % a.length;
	   if (nr < 0) nr+=a.length;
	   nc = (y+l) % a[0].length;
	   if (nc < 0) nc+=a[0].length;
	   b[nr][nc] = a[j][l];
	}
      }
      return b;
   }

   public static int[][] where(byte[][] a, String op, float x) {
      int[][] b = new int[a.length*a[0].length][2];
      int[][] w;
      int n = 0;
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   if (op.equals("==")) {
	      if (a[j][l] == x) {
	        b[n][0] = j;
		b[n][1] = l;
	        n++;
	      } 
	   } else if (op.equals("!=")) {
              if (a[j][l] != x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">")) {
              if (a[j][l] > x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">=")) {
              if (a[j][l] >= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<")) {
              if (a[j][l] < x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<=")) {
              if (a[j][l] <= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else {
	      System.out.println("UFArrayOps::where> invalid operation");
	      b = new int[1][2];
	      b[0][0] = -1;
	      b[0][1] = -1;
	      return b;
           }
	}
      }
      if (n == 0) {
	w = new int[1][2];
	w[0][0] = -1;
	w[0][1] = -1;
      } else {
	w = new int[n][2];
	for (int j=0; j<n; j++) {
	   w[j][0]=b[j][0];
	   w[j][1]=b[j][1];
	}
      }
      return w;
   }

   public static byte[] extractValues(byte[][] a, int[][] n) {
      byte[] b = new byte[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j][0] >= 0 && n[j][0] < a.length && n[j][1] >= 0 && n[j][1] < a[0].length)
	    b[j] = a[ n[j][0] ][ n[j][1] ];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j][0] + ", " + n[j][1]);
      }
      return b;
   }

   public static byte[] map2DByRow(byte[][] a) {
      byte[] b = new byte[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
	for (int l=0; l < a[0].length; l++) {
	   b[j*a[0].length+l]=a[j][l];
	}
      }
      return b;
   }

   public static byte[] map2DByColumn(byte[][] a) {
      byte[] b = new byte[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
        for (int l=0; l < a[0].length; l++) {
           b[j+a.length*l]=a[j][l];
        }
      }
      return b;
   }   

   public static char maxValue(char[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      char x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   x = (char)Math.max(x, a[j][l]);
	}
      }
      return x;
   }

   public static char maxValue(char[][] a) {
      return maxValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static char minValue(char[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      char x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
           x = (char)Math.min(x, a[j][l]);
        }
      }
      return x;
   }

   public static char minValue(char[][] a) {
      return minValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static int countN(char[][] a, int mr, int nr, int mc, int nc, char x) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int b = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (a[j][l] == x) b++;
	}
      }
      return b;
   }

   public static int countN(char[][] a, char x) {
      return countN(a, 0, a.length-1, 0, a[0].length-1, x);
   }

   public static char[][] reverse(char[][] a, int dim) {
      if (dim != 2) dim = 1;
      int temp;
      char[][] b = new char[a.length][a[0].length];
      if (dim == 1) {
        int n = a[0].length-1;
        for (int j=0; j < a.length; j++) {
	   for (int l = 0; l <= a[0].length/2; l++) {
	      b[j][n-l] = a[j][l];
	      b[j][l] = a[j][n-l];
	   }
        }
      } else {
        int n = a.length-1;
        for (int j=0; j <= a.length/2; j++) {
           for (int l = 0; l < a[0].length; l++) {
              b[n-j][l] = a[j][l];
              b[j][l] = a[n-j][l];
           }
        }
      }
      return b;
   }

   public static char[][] reverse(char[][] a) {
      return reverse(a, 1); 
   }

   public static char[][] shift(char[][] a, int x, int y) {
      int nr, nc;
      char[][] b = new char[a.length][a[0].length];
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   nr = (x+j) % a.length;
	   if (nr < 0) nr+=a.length;
	   nc = (y+l) % a[0].length;
	   if (nc < 0) nc+=a[0].length;
	   b[nr][nc] = a[j][l];
	}
      }
      return b;
   }

   public static int[][] where(char[][] a, String op, char x) {
      int[][] b = new int[a.length*a[0].length][2];
      int[][] w;
      int n = 0;
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   if (op.equals("==")) {
	      if (a[j][l] == x) {
	        b[n][0] = j;
		b[n][1] = l;
	        n++;
	      } 
	   } else if (op.equals("!=")) {
              if (a[j][l] != x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">")) {
              if (a[j][l] > x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">=")) {
              if (a[j][l] >= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<")) {
              if (a[j][l] < x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<=")) {
              if (a[j][l] <= x) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else {
	      System.out.println("UFArrayOps::where> invalid operation");
	      b = new int[1][2];
	      b[0][0] = -1;
	      b[0][1] = -1;
	      return b;
           }
	}
      }
      if (n == 0) {
	w = new int[1][2];
	w[0][0] = -1;
	w[0][1] = -1;
      } else {
	w = new int[n][2];
	for (int j=0; j<n; j++) {
	   w[j][0]=b[j][0];
	   w[j][1]=b[j][1];
	}
      }
      return w;
   }

   public static char[] extractValues(char[][] a, int[][] n) {
      char[] b = new char[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j][0] >= 0 && n[j][0] < a.length && n[j][1] >= 0 && n[j][1] < a[0].length)
	    b[j] = a[ n[j][0] ][ n[j][1] ];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j][0] + ", " + n[j][1]);
      }
      return b;
   }

   public static char[] map2DByRow(char[][] a) {
      char[] b = new char[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
	for (int l=0; l < a[0].length; l++) {
	   b[j*a[0].length+l]=a[j][l];
	}
      }
      return b;
   }

   public static char[] map2DByColumn(char[][] a) {
      char[] b = new char[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
        for (int l=0; l < a[0].length; l++) {
           b[j+a.length*l]=a[j][l];
        }
      }
      return b;
   }
   
   public static String maxValue(String[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
           if (a[j][l].compareTo(x) > 0) x = a[j][l];
	}
      }
      return x;
   }

   public static String maxValue(String[][] a) {
      return maxValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static String minValue(String[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
           if (a[j][l].compareTo(x) < 0) x = a[j][l];
        }
      }
      return x;
   }

   public static String minValue(String[][] a) {
      return minValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static int countN(String[][] a, int mr, int nr, int mc, int nc, String x) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int b = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (a[j][l] == x) b++;
	}
      }
      return b;
   }

   public static int countN(String[][] a, String x) {
      return countN(a, 0, a.length-1, 0, a[0].length-1, x);
   }

   public static String[][] reverse(String[][] a, int dim) {
      if (dim != 2) dim = 1;
      int temp;
      String[][] b = new String[a.length][a[0].length];
      if (dim == 1) {
        int n = a[0].length-1;
        for (int j=0; j < a.length; j++) {
	   for (int l = 0; l <= a[0].length/2; l++) {
	      b[j][n-l] = a[j][l];
	      b[j][l] = a[j][n-l];
	   }
        }
      } else {
        int n = a.length-1;
        for (int j=0; j <= a.length/2; j++) {
           for (int l = 0; l < a[0].length; l++) {
              b[n-j][l] = a[j][l];
              b[j][l] = a[n-j][l];
           }
        }
      }
      return b;
   }

   public static String[][] reverse(String[][] a) {
      return reverse(a, 1); 
   }

   public static String[][] shift(String[][] a, int x, int y) {
      int nr, nc;
      String[][] b = new String[a.length][a[0].length];
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   nr = (x+j) % a.length;
	   if (nr < 0) nr+=a.length;
	   nc = (y+l) % a[0].length;
	   if (nc < 0) nc+=a[0].length;
	   b[nr][nc] = a[j][l];
	}
      }
      return b;
   }

   public static int[][] where(String[][] a, String op, String x) {
      int[][] b = new int[a.length*a[0].length][2];
      int[][] w;
      int n = 0;
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   if (op.equals("==")) {
	      if (a[j][l].equals(x)) {
	        b[n][0] = j;
		b[n][1] = l;
	        n++;
	      } 
	   } else if (op.equals("!=")) {
              if (!a[j][l].equals(x)) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">")) {
              if (a[j][l].compareTo(x) > 0) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">=")) {
              if (a[j][l].compareTo(x) >= 0) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<")) {
              if (a[j][l].compareTo(x) < 0) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<=")) {
              if (a[j][l].compareTo(x) <= 0) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else {
	      System.out.println("UFArrayOps::where> invalid operation");
	      b = new int[1][2];
	      b[0][0] = -1;
	      b[0][1] = -1;
	      return b;
           }
	}
      }
      if (n == 0) {
	w = new int[1][2];
	w[0][0] = -1;
	w[0][1] = -1;
      } else {
	w = new int[n][2];
	for (int j=0; j<n; j++) {
	   w[j][0]=b[j][0];
	   w[j][1]=b[j][1];
	}
      }
      return w;
   }

   public static String[] extractValues(String[][] a, int[][] n) {
      String[] b = new String[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j][0] >= 0 && n[j][0] < a.length && n[j][1] >= 0 && n[j][1] < a[0].length)
	    b[j] = a[n[j][0]][n[j][1]];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j][0] + ", " + n[j][1]);
      }
      return b;
   }

   public static String[] map2DByRow(String[][] a) {
      String[] b = new String[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
	for (int l=0; l < a[0].length; l++) {
	   b[j*a[0].length+l]=a[j][l];
	}
      }
      return b;
   }

   public static String[] map2DByColumn(String[][] a) {
      String[] b = new String[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
        for (int l=0; l < a[0].length; l++) {
           b[j+a.length*l]=a[j][l];
        }
      }
      return b;
   }

   public static int[][] strlenArray(String[][] a) {
      int[][] x = new int[a.length][a[0].length];
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   x[j][l] = a[j][l].length();
	}
      }
      return x;
   }

   public static int[][] strposArray(String[][] a, String s, int m) {
      if (m < 0) m = 0;
      int[][] x = new int[a.length][a[0].length];
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   x[j][l] = a[j][l].indexOf(s, m);
	}
      }
      return x;
   }

   public static int[][] strposArray(String[][] a, String s) {
      return strposArray(a, s, 0);
   }

   public static String[][] strmidArray(String[][] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String[][] x = new String[a.length][a[0].length];
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   x[j][l] = a[j][l].substring(m, n);
	}
      }
      return x;
   }
   
   public static Object maxValue(Comparable[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      Object x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
           if (a[j][l].compareTo(x) > 0) x = a[j][l];
	}
      }
      return x;
   }

   public static Object maxValue(Comparable[][] a) {
      return maxValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static Object minValue(Comparable[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      Object x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
           if (a[j][l].compareTo(x) < 0) x = a[j][l];
        }
      }
      return x;
   }

   public static Object minValue(Comparable[][] a) {
      return minValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static int countN(Comparable[][] a, int mr, int nr, int mc, int nc, Object x) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int b = 0;
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) {
	   if (a[j][l].equals(x)) b++;
	}
      }
      return b;
   }

   public static int countN(Comparable[][] a, Object x) {
      return countN(a, 0, a.length-1, 0, a[0].length-1, x);
   }

   public static Object[][] reverse(Object[][] a, int dim) {
      if (dim != 2) dim = 1;
      int temp;
      Object[][] b = new Object[a.length][a[0].length];
      if (dim == 1) {
        int n = a[0].length-1;
        for (int j=0; j < a.length; j++) {
	   for (int l = 0; l <= a[0].length/2; l++) {
	      b[j][n-l] = a[j][l];
	      b[j][l] = a[j][n-l];
	   }
        }
      } else {
        int n = a.length-1;
        for (int j=0; j <= a.length/2; j++) {
           for (int l = 0; l < a[0].length; l++) {
              b[n-j][l] = a[j][l];
              b[j][l] = a[n-j][l];
           }
        }
      }
      return b;
   }

   public static Object[][] reverse(Object[][] a) {
      return reverse(a, 1); 
   }

   public static Object[][] shift(Object[][] a, int x, int y) {
      int nr, nc;
      Object[][] b = new Object[a.length][a[0].length];
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   nr = (x+j) % a.length;
	   if (nr < 0) nr+=a.length;
	   nc = (y+l) % a[0].length;
	   if (nc < 0) nc+=a[0].length;
	   b[nr][nc] = a[j][l];
	}
      }
      return b;
   }

   public static int[][] where(Comparable[][] a, String op, Object x) {
      int[][] b = new int[a.length*a[0].length][2];
      int[][] w;
      int n = 0;
      for (int j = 0; j < a.length; j++) {
	for (int l = 0; l < a[0].length; l++) {
	   if (op.equals("==")) {
	      if (a[j][l].equals(x)) {
	        b[n][0] = j;
		b[n][1] = l;
	        n++;
	      } 
	   } else if (op.equals("!=")) {
              if (!a[j][l].equals(x)) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">")) {
              if (a[j][l].compareTo(x) > 0) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals(">=")) {
              if (a[j][l].compareTo(x) >= 0) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<")) {
              if (a[j][l].compareTo(x) < 0) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else if (op.equals("<=")) {
              if (a[j][l].compareTo(x) <= 0) {
                b[n][0] = j;
                b[n][1] = l;
                n++;
              }
           } else {
	      System.out.println("UFArrayOps::where> invalid operation");
	      b = new int[1][2];
	      b[0][0] = -1;
	      b[0][1] = -1;
	      return b;
           }
	}
      }
      if (n == 0) {
	w = new int[1][2];
	w[0][0] = -1;
	w[0][1] = -1;
      } else {
	w = new int[n][2];
	for (int j=0; j<n; j++) {
	   w[j][0]=b[j][0];
	   w[j][1]=b[j][1];
	}
      }
      return w;
   }

   public static Object[] extractValues(Object[][] a, int[][] n) {
      Object[] b = new Object[n.length];
      for (int j=0; j<n.length; j++) {
	if (n[j][0] >= 0 && n[j][0] < a.length && n[j][1] >= 0 && n[j][1] < a[0].length)
	    b[j] = a[n[j][0]][n[j][1]];
	else System.out.println("UFArrayOps::extractValues> invalid array index: " + n[j][0] + ", " + n[j][1]);
      }
      return b;
   }

   public static Object[] map2DByRow(Object[][] a) {
      Object[] b = new Object[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
	for (int l=0; l < a[0].length; l++) {
	   b[j*a[0].length+l]=a[j][l];
	}
      }
      return b;
   }

   public static Object[] map2DByColumn(Object[][] a) {
      Object[] b = new Object[a.length*a[0].length];
      for (int j=0; j < a.length; j++) {
        for (int l=0; l < a[0].length; l++) {
           b[j+a.length*l]=a[j][l];
        }
      }
      return b;
   }

   public static int[] extractRow(int[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a[0].length) x = a[0].length-1;
      int[] b = new int[a[0].length];
      for (int j = 0; j < a[0].length; j++) {
	b[j] = a[x][j];
      }
      return b;
   }

   public static int[] extractColumn(int[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a.length) x = a.length-1;
      int[] b = new int[a.length];
      for (int j = 0; j < a.length; j++) {
        b[j] = a[j][x];
      }
      return b;
   }

   public static int[] totalValue(int[][] a, int dim) {
      if (dim != 2) dim=1;
      int[] x;
      if (dim == 1) {
	x = new int[a.length];
	for (int j = 0; j < a.length; j++) {
	   for (int l = 0; l < a[j].length; l++) {
	      x[j]+=a[j][l];
	   }
	}
      } else {
	x = new int[a[0].length];
	for (int j = 0; j < a[0].length; j++) {
	   for (int l = 0; l < a.length; l++) {
	      x[j]+=a[l][j];
	   }
	}
      }
      return x;
   }

   public static double[] avgValue(int[][] a, int dim) {
      if (dim != 2) dim=1;
      double[] x;
      if (dim == 1) {
        x = new double[a.length];
        for (int j = 0; j < a.length; j++) {
           for (int l = 0; l < a[j].length; l++) {
              x[j]+=a[j][l];
           }
	   x[j]=x[j]/a[j].length;
        }
      } else {
        x = new double[a[0].length];
        for (int j = 0; j < a[0].length; j++) {
           for (int l = 0; l < a.length; l++) {
              x[j]+=a[l][j];
           }
	   x[j]=x[j]/a.length;
        }
      }
      return x;
   }

   public static int[] addArrays(int[] a, int[] b) {
      int n = Math.min(a.length, b.length);
      int[] x = new int[n];
      for (int j = 0; j < n; j++) x[j] = a[j] + b[j];
      return x;
   }

   public static int[][] addArrays(int[][] a, int[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      int[][] x = new int[nr][nc];
      for (int j = 0; j < nr; j++) {
	for (int l = 0; l < nc; l++) x[j][l] = a[j][l] + b[j][l];
      }
      return x;
   }

   public static int[] subArrays(int[] a, int[] b) {
      int n = Math.min(a.length, b.length);
      int[] x = new int[n];
      for (int j = 0; j < n; j++) x[j] = a[j] - b[j];
      return x;
   }

   public static int[][] subArrays(int[][] a, int[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      int[][] x = new int[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] - b[j][l];
      }
      return x;
   }

   public static int[] multArrays(int[] a, int[] b) {
      int n = Math.min(a.length, b.length);
      int[] x = new int[n];
      for (int j = 0; j < n; j++) x[j] = a[j] * b[j];
      return x;
   }

   public static int[][] multArrays(int[][] a, int[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      int[][] x = new int[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] * b[j][l];
      }
      return x;
   }

   public static int[] divArrays(int[] a, int[] b) {
      int n = Math.min(a.length, b.length);
      int[] x = new int[n];
      for (int j = 0; j < n; j++) x[j] = a[j] / b[j];
      return x;
   }

   public static int[][] divArrays(int[][] a, int[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      int[][] x = new int[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] / b[j][l];
      }
      return x;
   }

   public static int[] modArrays(int[] a, int[] b) {
      int n = Math.min(a.length, b.length);
      int[] x = new int[n];
      for (int j = 0; j < n; j++) x[j] = a[j] % b[j];
      return x;
   }

   public static int[][] modArrays(int[][] a, int[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      int[][] x = new int[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] % b[j][l];
      }
      return x;
   }

   public static int[] powArray(int[] a, int b) {
      int n = a.length;
      int[] x = new int[n];
      for (int j = 0; j < n; j++) x[j] = (int)Math.pow((double)a[j], (double)b);
      return x;
   }

   public static int[][] powArray(int[][] a, int b) {
      int nr = a.length;
      int nc = a[0].length;
      int[][] x = new int[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (int)Math.pow((double)a[j][l], (double)b); 
      }
      return x;
   }

   public static double[] powArray(int[] a, double b) {
      int n = a.length;
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = Math.pow((double)a[j], b);
      return x;
   }

   public static double[][] powArray(int[][] a, double b) {
      int nr = a.length;
      int nc = a[0].length;
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = Math.pow((double)a[j][l], b);
      }
      return x;
   }
   
   public static float[] extractRow(float[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a[0].length) x = a[0].length-1;
      float[] b = new float[a[0].length];
      for (int j = 0; j < a[0].length; j++) {
	b[j] = a[x][j];
      }
      return b;
   }

   public static float[] extractColumn(float[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a.length) x = a.length-1;
      float[] b = new float[a.length];
      for (int j = 0; j < a.length; j++) {
        b[j] = a[j][x];
      }
      return b;
   }

   public static float[] totalValue(float[][] a, int dim) {
      if (dim != 2) dim=1;
      float[] x;
      if (dim == 1) {
	x = new float[a.length];
	for (int j = 0; j < a.length; j++) {
	   for (int l = 0; l < a[j].length; l++) {
	      x[j]+=a[j][l];
	   }
	}
      } else {
	x = new float[a[0].length];
	for (int j = 0; j < a[0].length; j++) {
	   for (int l = 0; l < a.length; l++) {
	      x[j]+=a[l][j];
	   }
	}
      }
      return x;
   }

   public static double[] avgValue(float[][] a, int dim) {
      if (dim != 2) dim=1;
      double[] x;
      if (dim == 1) {
        x = new double[a.length];
        for (int j = 0; j < a.length; j++) {
           for (int l = 0; l < a[j].length; l++) {
              x[j]+=a[j][l];
           }
	   x[j]=x[j]/a[j].length;
        }
      } else {
        x = new double[a[0].length];
        for (int j = 0; j < a[0].length; j++) {
           for (int l = 0; l < a.length; l++) {
              x[j]+=a[l][j];
           }
	   x[j]=x[j]/a.length;
        }
      }
      return x;
   }

   public static float[] addArrays(float[] a, float[] b) {
      int n = Math.min(a.length, b.length);
      float[] x = new float[n];
      for (int j = 0; j < n; j++) x[j] = a[j] + b[j];
      return x;
   }

   public static float[][] addArrays(float[][] a, float[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      float[][] x = new float[nr][nc];
      for (int j = 0; j < nr; j++) {
	for (int l = 0; l < nc; l++) x[j][l] = a[j][l] + b[j][l];
      }
      return x;
   }

   public static float[] subArrays(float[] a, float[] b) {
      int n = Math.min(a.length, b.length);
      float[] x = new float[n];
      for (int j = 0; j < n; j++) x[j] = a[j] - b[j];
      return x;
   }

   public static float[][] subArrays(float[][] a, float[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      float[][] x = new float[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] - b[j][l];
      }
      return x;
   }

   public static float[] multArrays(float[] a, float[] b) {
      int n = Math.min(a.length, b.length);
      float[] x = new float[n];
      for (int j = 0; j < n; j++) x[j] = a[j] * b[j];
      return x;
   }

   public static float[][] multArrays(float[][] a, float[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      float[][] x = new float[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] * b[j][l];
      }
      return x;
   }

   public static float[] divArrays(float[] a, float[] b) {
      int n = Math.min(a.length, b.length);
      float[] x = new float[n];
      for (int j = 0; j < n; j++) x[j] = a[j] / b[j];
      return x;
   }

   public static float[][] divArrays(float[][] a, float[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      float[][] x = new float[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] / b[j][l];
      }
      return x;
   }

   public static float[] modArrays(float[] a, float[] b) {
      int n = Math.min(a.length, b.length);
      float[] x = new float[n];
      for (int j = 0; j < n; j++) x[j] = a[j] % b[j];
      return x;
   }

   public static float[][] modArrays(float[][] a, float[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      float[][] x = new float[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] % b[j][l];
      }
      return x;
   }

   public static float[] powArray(float[] a, float b) {
      int n = a.length;
      float[] x = new float[n];
      for (int j = 0; j < n; j++) x[j] = (float)Math.pow((double)a[j], (double)b);
      return x;
   }

   public static float[][] powArray(float[][] a, float b) {
      int nr = a.length;
      int nc = a[0].length;
      float[][] x = new float[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (float)Math.pow((double)a[j][l], (double)b);
      }
      return x;
   }

   public static double[] powArray(float[] a, double b) {
      int n = a.length;
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = Math.pow((double)a[j], b);
      return x;
   }

   public static double[][] powArray(float[][] a, double b) {
      int nr = a.length;
      int nc = a[0].length;
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = Math.pow((double)a[j][l], b);
      }
      return x;
   }

   public static double[] extractRow(double[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a[0].length) x = a[0].length-1;
      double[] b = new double[a[0].length];
      for (int j = 0; j < a[0].length; j++) {
	b[j] = a[x][j];
      }
      return b;
   }

   public static double[] extractColumn(double[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a.length) x = a.length-1;
      double[] b = new double[a.length];
      for (int j = 0; j < a.length; j++) {
        b[j] = a[j][x];
      }
      return b;
   }

   public static double[] totalValue(double[][] a, int dim) {
      if (dim != 2) dim=1;
      double[] x;
      if (dim == 1) {
	x = new double[a.length];
	for (int j = 0; j < a.length; j++) {
	   for (int l = 0; l < a[j].length; l++) {
	      x[j]+=a[j][l];
	   }
	}
      } else {
	x = new double[a[0].length];
	for (int j = 0; j < a[0].length; j++) {
	   for (int l = 0; l < a.length; l++) {
	      x[j]+=a[l][j];
	   }
	}
      }
      return x;
   }

   public static double[] avgValue(double[][] a, int dim) {
      if (dim != 2) dim=1;
      double[] x;
      if (dim == 1) {
        x = new double[a.length];
        for (int j = 0; j < a.length; j++) {
           for (int l = 0; l < a[j].length; l++) {
              x[j]+=a[j][l];
           }
	   x[j]=x[j]/a[j].length;
        }
      } else {
        x = new double[a[0].length];
        for (int j = 0; j < a[0].length; j++) {
           for (int l = 0; l < a.length; l++) {
              x[j]+=a[l][j];
           }
	   x[j]=x[j]/a.length;
        }
      }
      return x;
   }

   public static double[] addArrays(double[] a, double[] b) {
      int n = Math.min(a.length, b.length);
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = a[j] + b[j];
      return x;
   }

   public static double[][] addArrays(double[][] a, double[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
	for (int l = 0; l < nc; l++) x[j][l] = a[j][l] + b[j][l];
      }
      return x;
   }

   public static double[] subArrays(double[] a, double[] b) {
      int n = Math.min(a.length, b.length);
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = a[j] - b[j];
      return x;
   }

   public static double[][] subArrays(double[][] a, double[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] - b[j][l];
      }
      return x;
   }

   public static double[] multArrays(double[] a, double[] b) {
      int n = Math.min(a.length, b.length);
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = a[j] * b[j];
      return x;
   }

   public static double[][] multArrays(double[][] a, double[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] * b[j][l];
      }
      return x;
   }

   public static double[] divArrays(double[] a, double[] b) {
      int n = Math.min(a.length, b.length);
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = a[j] / b[j];
      return x;
   }

   public static double[][] divArrays(double[][] a, double[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] / b[j][l];
      }
      return x;
   }

   public static double[] modArrays(double[] a, double[] b) {
      int n = Math.min(a.length, b.length);
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = a[j] % b[j];
      return x;
   }

   public static double[][] modArrays(double[][] a, double[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] % b[j][l];
      }
      return x;
   }

   public static double[] powArray(double[] a, double b) {
      int n = a.length;
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = Math.pow(a[j], b);
      return x;
   }

   public static double[][] powArray(double[][] a, double b) {
      int nr = a.length;
      int nc = a[0].length;
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = Math.pow(a[j][l], b);
      }
      return x;
   }

   public static long[] extractRow(long[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a[0].length) x = a[0].length-1;
      long[] b = new long[a[0].length];
      for (int j = 0; j < a[0].length; j++) {
	b[j] = a[x][j];
      }
      return b;
   }

   public static long[] extractColumn(long[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a.length) x = a.length-1;
      long[] b = new long[a.length];
      for (int j = 0; j < a.length; j++) {
        b[j] = a[j][x];
      }
      return b;
   }

   public static long[] totalValue(long[][] a, int dim) {
      if (dim != 2) dim=1;
      long[] x;
      if (dim == 1) {
	x = new long[a.length];
	for (int j = 0; j < a.length; j++) {
	   for (int l = 0; l < a[j].length; l++) {
	      x[j]+=a[j][l];
	   }
	}
      } else {
	x = new long[a[0].length];
	for (int j = 0; j < a[0].length; j++) {
	   for (int l = 0; l < a.length; l++) {
	      x[j]+=a[l][j];
	   }
	}
      }
      return x;
   }

   public static double[] avgValue(long[][] a, int dim) {
      if (dim != 2) dim=1;
      double[] x;
      if (dim == 1) {
        x = new double[a.length];
        for (int j = 0; j < a.length; j++) {
           for (int l = 0; l < a[j].length; l++) {
              x[j]+=a[j][l];
           }
	   x[j]=x[j]/a[j].length;
        }
      } else {
        x = new double[a[0].length];
        for (int j = 0; j < a[0].length; j++) {
           for (int l = 0; l < a.length; l++) {
              x[j]+=a[l][j];
           }
	   x[j]=x[j]/a.length;
        }
      }
      return x;
   }

   public static long[] addArrays(long[] a, long[] b) {
      int n = Math.min(a.length, b.length);
      long[] x = new long[n];
      for (int j = 0; j < n; j++) x[j] = a[j] + b[j];
      return x;
   }

   public static long[][] addArrays(long[][] a, long[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      long[][] x = new long[nr][nc];
      for (int j = 0; j < nr; j++) {
	for (int l = 0; l < nc; l++) x[j][l] = a[j][l] + b[j][l];
      }
      return x;
   }

   public static long[] subArrays(long[] a, long[] b) {
      int n = Math.min(a.length, b.length);
      long[] x = new long[n];
      for (int j = 0; j < n; j++) x[j] = a[j] - b[j];
      return x;
   }

   public static long[][] subArrays(long[][] a, long[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      long[][] x = new long[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] - b[j][l];
      }
      return x;
   }

   public static long[] multArrays(long[] a, long[] b) {
      int n = Math.min(a.length, b.length);
      long[] x = new long[n];
      for (int j = 0; j < n; j++) x[j] = a[j] * b[j];
      return x;
   }

   public static long[][] multArrays(long[][] a, long[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      long[][] x = new long[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] * b[j][l];
      }
      return x;
   }

   public static long[] divArrays(long[] a, long[] b) {
      int n = Math.min(a.length, b.length);
      long[] x = new long[n];
      for (int j = 0; j < n; j++) x[j] = a[j] / b[j];
      return x;
   }

   public static long[][] divArrays(long[][] a, long[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      long[][] x = new long[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] / b[j][l];
      }
      return x;
   }

   public static long[] modArrays(long[] a, long[] b) {
      int n = Math.min(a.length, b.length);
      long[] x = new long[n];
      for (int j = 0; j < n; j++) x[j] = a[j] % b[j];
      return x;
   }

   public static long[][] modArrays(long[][] a, long[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      long[][] x = new long[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] % b[j][l];
      }
      return x;
   }

   public static long[] powArray(long[] a, long b) {
      int n = a.length;
      long[] x = new long[n];
      for (int j = 0; j < n; j++) x[j] = (long)Math.pow((double)a[j], (double)b);
      return x;
   }  
      
   public static long[][] powArray(long[][] a, long b) {
      int nr = a.length;
      int nc = a[0].length;
      long[][] x = new long[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (long)Math.pow((double)a[j][l], (double)b);
      }
      return x;
   }

   public static double[] powArray(long[] a, double b) {
      int n = a.length;
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = Math.pow((double)a[j], b);
      return x;
   }

   public static double[][] powArray(long[][] a, double b) {
      int nr = a.length;
      int nc = a[0].length;
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = Math.pow((double)a[j][l], b);
      }
      return x;
   }

   public static short[] extractRow(short[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a[0].length) x = a[0].length-1;
      short[] b = new short[a[0].length];
      for (int j = 0; j < a[0].length; j++) {
	b[j] = a[x][j];
      }
      return b;
   }

   public static short[] extractColumn(short[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a.length) x = a.length-1;
      short[] b = new short[a.length];
      for (int j = 0; j < a.length; j++) {
        b[j] = a[j][x];
      }
      return b;
   }

   public static short[] totalValue(short[][] a, int dim) {
      if (dim != 2) dim=1;
      short[] x;
      if (dim == 1) {
	x = new short[a.length];
	for (int j = 0; j < a.length; j++) {
	   for (int l = 0; l < a[j].length; l++) {
	      x[j]+=a[j][l];
	   }
	}
      } else {
	x = new short[a[0].length];
	for (int j = 0; j < a[0].length; j++) {
	   for (int l = 0; l < a.length; l++) {
	      x[j]+=a[l][j];
	   }
	}
      }
      return x;
   }

   public static double[] avgValue(short[][] a, int dim) {
      if (dim != 2) dim=1;
      double[] x;
      if (dim == 1) {
        x = new double[a.length];
        for (int j = 0; j < a.length; j++) {
           for (int l = 0; l < a[j].length; l++) {
              x[j]+=a[j][l];
           }
	   x[j]=x[j]/a[j].length;
        }
      } else {
        x = new double[a[0].length];
        for (int j = 0; j < a[0].length; j++) {
           for (int l = 0; l < a.length; l++) {
              x[j]+=a[l][j];
           }
	   x[j]=x[j]/a.length;
        }
      }
      return x;
   }

   public static short[] addArrays(short[] a, short[] b) {
      int n = Math.min(a.length, b.length);
      short[] x = new short[n];
      for (int j = 0; j < n; j++) x[j] = (short)(a[j] + b[j]);
      return x;
   }

   public static short[][] addArrays(short[][] a, short[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      short[][] x = new short[nr][nc];
      for (int j = 0; j < nr; j++) {
	for (int l = 0; l < nc; l++) x[j][l] = (short)(a[j][l] + b[j][l]);
      }
      return x;
   }

   public static short[] subArrays(short[] a, short[] b) {
      int n = Math.min(a.length, b.length);
      short[] x = new short[n];
      for (int j = 0; j < n; j++) x[j] = (short)(a[j] - b[j]);
      return x;
   }

   public static short[][] subArrays(short[][] a, short[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      short[][] x = new short[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (short)(a[j][l] - b[j][l]);
      }
      return x;
   }

   public static short[] multArrays(short[] a, short[] b) {
      int n = Math.min(a.length, b.length);
      short[] x = new short[n];
      for (int j = 0; j < n; j++) x[j] = (short)(a[j] * b[j]);
      return x;
   }

   public static short[][] multArrays(short[][] a, short[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      short[][] x = new short[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (short)(a[j][l] * b[j][l]);
      }
      return x;
   }

   public static short[] divArrays(short[] a, short[] b) {
      int n = Math.min(a.length, b.length);
      short[] x = new short[n];
      for (int j = 0; j < n; j++) x[j] = (short)(a[j] / b[j]);
      return x;
   }

   public static short[][] divArrays(short[][] a, short[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      short[][] x = new short[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (short)(a[j][l] / b[j][l]);
      }
      return x;
   }

   public static short[] modArrays(short[] a, short[] b) {
      int n = Math.min(a.length, b.length);
      short[] x = new short[n];
      for (int j = 0; j < n; j++) x[j] = (short)(a[j] % b[j]);
      return x;
   }

   public static short[][] modArrays(short[][] a, short[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      short[][] x = new short[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (short)(a[j][l] % b[j][l]);
      }
      return x;
   }

   public static short[] powArray(short[] a, short b) {
      int n = a.length;
      short[] x = new short[n];
      for (int j = 0; j < n; j++) x[j] = (short)Math.pow((double)a[j], (double)b);
      return x;
   }  
      
   public static short[][] powArray(short[][] a, short b) {
      int nr = a.length;
      int nc = a[0].length;
      short[][] x = new short[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (short)Math.pow((double)a[j][l], (double)b);
      }
      return x;
   }

   public static double[] powArray(short[] a, double b) {
      int n = a.length;
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = Math.pow((double)a[j], b);
      return x;
   }

   public static double[][] powArray(short[][] a, double b) {
      int nr = a.length;
      int nc = a[0].length;
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = Math.pow((double)a[j][l], b);
      }
      return x;
   }

   public static byte[] extractRow(byte[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a[0].length) x = a[0].length-1;
      byte[] b = new byte[a[0].length];
      for (int j = 0; j < a[0].length; j++) {
	b[j] = a[x][j];
      }
      return b;
   }

   public static byte[] extractColumn(byte[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a.length) x = a.length-1;
      byte[] b = new byte[a.length];
      for (int j = 0; j < a.length; j++) {
        b[j] = a[j][x];
      }
      return b;
   }

   public static byte[] totalValue(byte[][] a, int dim) {
      if (dim != 2) dim=1;
      byte[] x;
      if (dim == 1) {
	x = new byte[a.length];
	for (int j = 0; j < a.length; j++) {
	   for (int l = 0; l < a[j].length; l++) {
	      x[j]+=a[j][l];
	   }
	}
      } else {
	x = new byte[a[0].length];
	for (int j = 0; j < a[0].length; j++) {
	   for (int l = 0; l < a.length; l++) {
	      x[j]+=a[l][j];
	   }
	}
      }
      return x;
   }

   public static double[] avgValue(byte[][] a, int dim) {
      if (dim != 2) dim=1;
      double[] x;
      if (dim == 1) {
        x = new double[a.length];
        for (int j = 0; j < a.length; j++) {
           for (int l = 0; l < a[j].length; l++) {
              x[j]+=a[j][l];
           }
	   x[j]=x[j]/a[j].length;
        }
      } else {
        x = new double[a[0].length];
        for (int j = 0; j < a[0].length; j++) {
           for (int l = 0; l < a.length; l++) {
              x[j]+=a[l][j];
           }
	   x[j]=x[j]/a.length;
        }
      }
      return x;
   }

   public static byte[] addArrays(byte[] a, byte[] b) {
      int n = Math.min(a.length, b.length);
      byte[] x = new byte[n];
      for (int j = 0; j < n; j++) x[j] = (byte)(a[j] + b[j]);
      return x;
   }

   public static byte[][] addArrays(byte[][] a, byte[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      byte[][] x = new byte[nr][nc];
      for (int j = 0; j < nr; j++) {
	for (int l = 0; l < nc; l++) x[j][l] = (byte)(a[j][l] + b[j][l]);
      }
      return x;
   }

   public static byte[] subArrays(byte[] a, byte[] b) {
      int n = Math.min(a.length, b.length);
      byte[] x = new byte[n];
      for (int j = 0; j < n; j++) x[j] = (byte)(a[j] - b[j]);
      return x;
   }

   public static byte[][] subArrays(byte[][] a, byte[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      byte[][] x = new byte[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (byte)(a[j][l] - b[j][l]);
      }
      return x;
   }

   public static byte[] multArrays(byte[] a, byte[] b) {
      int n = Math.min(a.length, b.length);
      byte[] x = new byte[n];
      for (int j = 0; j < n; j++) x[j] = (byte)(a[j] * b[j]);
      return x;
   }

   public static byte[][] multArrays(byte[][] a, byte[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      byte[][] x = new byte[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (byte)(a[j][l] * b[j][l]);
      }
      return x;
   }

   public static byte[] divArrays(byte[] a, byte[] b) {
      int n = Math.min(a.length, b.length);
      byte[] x = new byte[n];
      for (int j = 0; j < n; j++) x[j] = (byte)(a[j] / b[j]);
      return x;
   }

   public static byte[][] divArrays(byte[][] a, byte[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      byte[][] x = new byte[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (byte)(a[j][l] / b[j][l]);
      }
      return x;
   }

   public static byte[] modArrays(byte[] a, byte[] b) {
      int n = Math.min(a.length, b.length);
      byte[] x = new byte[n];
      for (int j = 0; j < n; j++) x[j] = (byte)(a[j] % b[j]);
      return x;
   }

   public static byte[][] modArrays(byte[][] a, byte[][] b) {
      int nr = Math.min(a.length, b.length);
      int nc = Math.min(a[0].length, b[0].length);
      byte[][] x = new byte[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (byte)(a[j][l] % b[j][l]);
      }
      return x;
   }

   public static char[] extractRow(char[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a[0].length) x = a[0].length-1;
      char[] b = new char[a[0].length];
      for (int j = 0; j < a[0].length; j++) {
	b[j] = a[x][j];
      }
      return b;
   }

   public static char[] extractColumn(char[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a.length) x = a.length-1;
      char[] b = new char[a.length];
      for (int j = 0; j < a.length; j++) {
        b[j] = a[j][x];
      }
      return b;
   }

   public static String[] extractRow(String[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a[0].length) x = a[0].length-1;
      String[] b = new String[a[0].length];
      for (int j = 0; j < a[0].length; j++) {
	b[j] = a[x][j];
      }
      return b;
   }

   public static String[] extractColumn(String[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a.length) x = a.length-1;
      String[] b = new String[a.length];
      for (int j = 0; j < a.length; j++) {
        b[j] = a[j][x];
      }
      return b;
   }
   public static Object[] extractRow(Object[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a[0].length) x = a[0].length-1;
      Object[] b = new Object[a[0].length];
      for (int j = 0; j < a[0].length; j++) {
	b[j] = a[x][j];
      }
      return b;
   }

   public static Object[] extractColumn(Object[][] a, int x) {
      if (x < 0) x = 0;
      if (x >= a.length) x = a.length-1;
      Object[] b = new Object[a.length];
      for (int j = 0; j < a.length; j++) {
        b[j] = a[j][x];
      }
      return b;
   }
   
   public static int[] addArrays(int[] a, int b) {
      int n = a.length;
      int[] x = new int[n];
      for (int j = 0; j < n; j++) x[j] = a[j] + b;
      return x;
   }

   public static int[][] addArrays(int[][] a, int b) {
      int nr = a.length;
      int nc = a[0].length;
      int[][] x = new int[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] + b;
      }
      return x;
   }

   public static int[] subArrays(int[] a, int b) {
      int n = a.length;
      int[] x = new int[n];
      for (int j = 0; j < n; j++) x[j] = a[j] - b;
      return x;
   }

   public static int[][] subArrays(int[][] a, int b) {
      int nr = a.length;
      int nc = a[0].length;
      int[][] x = new int[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] - b;
      }
      return x;
   }

   public static int[] multArrays(int[] a, int b) {
      int n = a.length;
      int[] x = new int[n];
      for (int j = 0; j < n; j++) x[j] = a[j] * b;
      return x;
   }

   public static int[][] multArrays(int[][] a, int b) {
      int nr = a.length;
      int nc = a[0].length;
      int[][] x = new int[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] * b;
      }
      return x;
   }

  public static int[] divArrays(int[] a, int b) {
      int n = a.length;
      int[] x = new int[n];
      for (int j = 0; j < n; j++) x[j] = a[j] / b;
      return x;
   }

   public static int[][] divArrays(int[][] a, int b) {
      int nr = a.length;
      int nc = a[0].length;
      int[][] x = new int[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] / b;
      }
      return x;
   }

   public static int[] modArrays(int[] a, int b) {
      int n = a.length;
      int[] x = new int[n];
      for (int j = 0; j < n; j++) x[j] = a[j] % b;
      return x;
   }

   public static int[][] modArrays(int[][] a, int b) {
      int nr = a.length;
      int nc = a[0].length;
      int[][] x = new int[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] % b;
      }
      return x;
   }

   public static float[] addArrays(float[] a, float b) {
      int n = a.length;
      float[] x = new float[n];
      for (int j = 0; j < n; j++) x[j] = a[j] + b;
      return x;
   }

   public static float[][] addArrays(float[][] a, float b) {
      int nr = a.length;
      int nc = a[0].length;
      float[][] x = new float[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] + b;
      }
      return x;
   }

   public static float[] subArrays(float[] a, float b) {
      int n = a.length;
      float[] x = new float[n];
      for (int j = 0; j < n; j++) x[j] = a[j] - b;
      return x;
   }

   public static float[][] subArrays(float[][] a, float b) {
      int nr = a.length;
      int nc = a[0].length;
      float[][] x = new float[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] - b;
      }
      return x;
   }

   public static float[] multArrays(float[] a, float b) {
      int n = a.length;
      float[] x = new float[n];
      for (int j = 0; j < n; j++) x[j] = a[j] * b;
      return x;
   }

   public static float[][] multArrays(float[][] a, float b) {
      int nr = a.length;
      int nc = a[0].length;
      float[][] x = new float[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] * b;
      }
      return x;
   }

  public static float[] divArrays(float[] a, float b) {
      int n = a.length;
      float[] x = new float[n];
      for (int j = 0; j < n; j++) x[j] = a[j] / b;
      return x;
   }

   public static float[][] divArrays(float[][] a, float b) {
      int nr = a.length;
      int nc = a[0].length;
      float[][] x = new float[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] / b;
      }
      return x;
   }

   public static float[] modArrays(float[] a, float b) {
      int n = a.length;
      float[] x = new float[n];
      for (int j = 0; j < n; j++) x[j] = a[j] % b;
      return x;
   }

   public static float[][] modArrays(float[][] a, float b) {
      int nr = a.length;
      int nc = a[0].length;
      float[][] x = new float[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] % b;
      }
      return x;
   }

   public static double[] addArrays(double[] a, double b) {
      int n = a.length;
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = a[j] + b;
      return x;
   }

   public static double[][] addArrays(double[][] a, double b) {
      int nr = a.length;
      int nc = a[0].length;
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] + b;
      }
      return x;
   }

   public static double[] subArrays(double[] a, double b) {
      int n = a.length;
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = a[j] - b;
      return x;
   }

   public static double[][] subArrays(double[][] a, double b) {
      int nr = a.length;
      int nc = a[0].length;
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] - b;
      }
      return x;
   }

   public static double[] multArrays(double[] a, double b) {
      int n = a.length;
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = a[j] * b;
      return x;
   }

   public static double[][] multArrays(double[][] a, double b) {
      int nr = a.length;
      int nc = a[0].length;
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] * b;
      }
      return x;
   }

  public static double[] divArrays(double[] a, double b) {
      int n = a.length;
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = a[j] / b;
      return x;
   }

   public static double[][] divArrays(double[][] a, double b) {
      int nr = a.length;
      int nc = a[0].length;
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] / b;
      }
      return x;
   }

   public static double[] modArrays(double[] a, double b) {
      int n = a.length;
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = a[j] % b;
      return x;
   }

   public static double[][] modArrays(double[][] a, double b) {
      int nr = a.length;
      int nc = a[0].length;
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] % b;
      }
      return x;
   }

   public static long[] addArrays(long[] a, long b) {
      int n = a.length;
      long[] x = new long[n];
      for (int j = 0; j < n; j++) x[j] = a[j] + b;
      return x;
   }

   public static long[][] addArrays(long[][] a, long b) {
      int nr = a.length;
      int nc = a[0].length;
      long[][] x = new long[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] + b;
      }
      return x;
   }

   public static long[] subArrays(long[] a, long b) {
      int n = a.length;
      long[] x = new long[n];
      for (int j = 0; j < n; j++) x[j] = a[j] - b;
      return x;
   }

   public static long[][] subArrays(long[][] a, long b) {
      int nr = a.length;
      int nc = a[0].length;
      long[][] x = new long[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] - b;
      }
      return x;
   }

   public static long[] multArrays(long[] a, long b) {
      int n = a.length;
      long[] x = new long[n];
      for (int j = 0; j < n; j++) x[j] = a[j] * b;
      return x;
   }

   public static long[][] multArrays(long[][] a, long b) {
      int nr = a.length;
      int nc = a[0].length;
      long[][] x = new long[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] * b;
      }
      return x;
   }

  public static long[] divArrays(long[] a, long b) {
      int n = a.length;
      long[] x = new long[n];
      for (int j = 0; j < n; j++) x[j] = a[j] / b;
      return x;
   }

   public static long[][] divArrays(long[][] a, long b) {
      int nr = a.length;
      int nc = a[0].length;
      long[][] x = new long[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] / b;
      }
      return x;
   }

   public static long[] modArrays(long[] a, long b) {
      int n = a.length;
      long[] x = new long[n];
      for (int j = 0; j < n; j++) x[j] = a[j] % b;
      return x;
   }

   public static long[][] modArrays(long[][] a, long b) {
      int nr = a.length;
      int nc = a[0].length;
      long[][] x = new long[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = a[j][l] % b;
      }
      return x;
   }

   public static short[] addArrays(short[] a, short b) {
      int n = a.length;
      short[] x = new short[n];
      for (int j = 0; j < n; j++) x[j] = (short)(a[j] + b);
      return x;
   }

   public static short[][] addArrays(short[][] a, short b) {
      int nr = a.length;
      int nc = a[0].length;
      short[][] x = new short[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (short)(a[j][l] + b);
      }
      return x;
   }

   public static short[] subArrays(short[] a, short b) {
      int n = a.length;
      short[] x = new short[n];
      for (int j = 0; j < n; j++) x[j] = (short)(a[j] - b);
      return x;
   }

   public static short[][] subArrays(short[][] a, short b) {
      int nr = a.length;
      int nc = a[0].length;
      short[][] x = new short[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (short)(a[j][l] - b);
      }
      return x;
   }

   public static short[] multArrays(short[] a, short b) {
      int n = a.length;
      short[] x = new short[n];
      for (int j = 0; j < n; j++) x[j] = (short)(a[j] * b);
      return x;
   }

   public static short[][] multArrays(short[][] a, short b) {
      int nr = a.length;
      int nc = a[0].length;
      short[][] x = new short[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (short)(a[j][l] * b);
      }
      return x;
   }

  public static short[] divArrays(short[] a, short b) {
      int n = a.length;
      short[] x = new short[n];
      for (int j = 0; j < n; j++) x[j] = (short)(a[j] / b);
      return x;
   }

   public static short[][] divArrays(short[][] a, short b) {
      int nr = a.length;
      int nc = a[0].length;
      short[][] x = new short[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (short)(a[j][l] / b);
      }
      return x;
   }

   public static short[] modArrays(short[] a, short b) {
      int n = a.length;
      short[] x = new short[n];
      for (int j = 0; j < n; j++) x[j] = (short)(a[j] % b);
      return x;
   }

   public static short[][] modArrays(short[][] a, short b) {
      int nr = a.length;
      int nc = a[0].length;
      short[][] x = new short[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (short)(a[j][l] % b);
      }
      return x;
   }

   public static byte[] addArrays(byte[] a, byte b) {
      int n = a.length;
      byte[] x = new byte[n];
      for (int j = 0; j < n; j++) x[j] = (byte)(a[j] + b);
      return x;
   }

   public static byte[][] addArrays(byte[][] a, byte b) {
      int nr = a.length;
      int nc = a[0].length;
      byte[][] x = new byte[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (byte)(a[j][l] + b);
      }
      return x;
   }

   public static byte[] subArrays(byte[] a, byte b) {
      int n = a.length;
      byte[] x = new byte[n];
      for (int j = 0; j < n; j++) x[j] = (byte)(a[j] - b);
      return x;
   }

   public static byte[][] subArrays(byte[][] a, byte b) {
      int nr = a.length;
      int nc = a[0].length;
      byte[][] x = new byte[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (byte)(a[j][l] - b);
      }
      return x;
   }

   public static byte[] multArrays(byte[] a, byte b) {
      int n = a.length;
      byte[] x = new byte[n];
      for (int j = 0; j < n; j++) x[j] = (byte)(a[j] * b);
      return x;
   }

   public static byte[][] multArrays(byte[][] a, byte b) {
      int nr = a.length;
      int nc = a[0].length;
      byte[][] x = new byte[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (byte)(a[j][l] * b);
      }
      return x;
   }

  public static byte[] divArrays(byte[] a, byte b) {
      int n = a.length;
      byte[] x = new byte[n];
      for (int j = 0; j < n; j++) x[j] = (byte)(a[j] / b);
      return x;
   }

   public static byte[][] divArrays(byte[][] a, byte b) {
      int nr = a.length;
      int nc = a[0].length;
      byte[][] x = new byte[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (byte)(a[j][l] / b);
      }
      return x;
   }

   public static byte[] modArrays(byte[] a, byte b) {
      int n = a.length;
      byte[] x = new byte[n];
      for (int j = 0; j < n; j++) x[j] = (byte)(a[j] % b);
      return x;
   }

   public static byte[][] modArrays(byte[][] a, byte b) {
      int nr = a.length;
      int nc = a[0].length;
      byte[][] x = new byte[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (byte)(a[j][l] % b);
      }
      return x;
   }

   public static byte[] powArray(byte[] a, byte b) {
      int n = a.length;
      byte[] x = new byte[n];
      for (int j = 0; j < n; j++) x[j] = (byte)Math.pow((double)a[j], (double)b);
      return x;
   }  
      
   public static byte[][] powArray(byte[][] a, byte b) {
      int nr = a.length;
      int nc = a[0].length;
      byte[][] x = new byte[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = (byte)Math.pow((double)a[j][l], (double)b);
      }
      return x;
   }

   public static double[] powArray(byte[] a, double b) {
      int n = a.length;
      double[] x = new double[n];
      for (int j = 0; j < n; j++) x[j] = Math.pow((double)a[j], b);
      return x;
   }

   public static double[][] powArray(byte[][] a, double b) {
      int nr = a.length;
      int nc = a[0].length;
      double[][] x = new double[nr][nc];
      for (int j = 0; j < nr; j++) {
        for (int l = 0; l < nc; l++) x[j][l] = Math.pow((double)a[j][l], b);
      }
      return x;
   }

   public static double stddev(int[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double[] resid = new double[n-m+1];
      for (int j = m; j <= n; j++) {
        resid[j-m] = (double)a[j];
      }
      resid = subArrays(resid, avgValue(a, m, n));
      return Math.sqrt( totalValue( multArrays(resid, resid) ) / (resid.length-1.0f) );
   }

   public static double stddev(int[] a) {
      return stddev(a, 0, a.length-1);
   }

   public static double stddev(float[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double[] resid = new double[n-m+1];
      for (int j = m; j <= n; j++) {
        resid[j-m] = (double)a[j];
      }
      resid = subArrays(resid, avgValue(a, m, n));
      return Math.sqrt( totalValue( multArrays(resid, resid) ) / (resid.length-1.0f) );
   }

   public static double stddev(float[] a) {
      return stddev(a, 0, a.length-1);
   }

   public static double stddev(double[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double[] resid = new double[n-m+1];
      for (int j = m; j <= n; j++) {
        resid[j-m] = a[j];
      }
      resid = subArrays(resid, avgValue(a, m, n));
      return Math.sqrt( totalValue( multArrays(resid, resid) ) / (resid.length-1.0f) );
   }

   public static double stddev(double[] a) {
      return stddev(a, 0, a.length-1);
   }

   public static double stddev(long[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double[] resid = new double[n-m+1];
      for (int j = m; j <= n; j++) {
        resid[j-m] = (double)a[j];
      }
      resid = subArrays(resid, avgValue(a, m, n));
      return Math.sqrt( totalValue( multArrays(resid, resid) ) / (resid.length-1.0f) );
   }

   public static double stddev(long[] a) {
      return stddev(a, 0, a.length-1);
   }

   public static double stddev(short[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double[] resid = new double[n-m+1];
      for (int j = m; j <= n; j++) {
        resid[j-m] = (double)a[j];
      }
      resid = subArrays(resid, avgValue(a, m, n));
      return Math.sqrt( totalValue( multArrays(resid, resid) ) / (resid.length-1.0f) );
   }

   public static double stddev(short[] a) {
      return stddev(a, 0, a.length-1);
   }

   public static double stddev(byte[] a, int m, int n) {
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double[] resid = new double[n-m+1];
      for (int j = m; j <= n; j++) {
        resid[j-m] = (double)a[j];
      }
      resid = subArrays(resid, avgValue(a, m, n));
      return Math.sqrt( totalValue( multArrays(resid, resid) ) / (resid.length-1.0f) );
   }

   public static double stddev(byte[] a) {
      return stddev(a, 0, a.length-1);
   }

   public static double stddev(int[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double[][] resid = new double[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) resid[j-mr][l-mc] = (double)a[j][l];
      }
      resid = subArrays(resid, avgValue(a, mr, nr, mc, nc));
      float ndegfree = resid.length * resid[0].length - 1.0f;
      return Math.sqrt( totalValue( multArrays(resid, resid) ) / ndegfree );
   }

   public static double stddev(int[][] a) {
      return stddev(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static double stddev(float[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double[][] resid = new double[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) resid[j-mr][l-mc] = (double)a[j][l];
      }
      resid = subArrays(resid, avgValue(a, mr, nr, mc, nc));
      float ndegfree = resid.length * resid[0].length - 1.0f;
      return Math.sqrt( totalValue( multArrays(resid, resid) ) / ndegfree );
   }

   public static double stddev(float[][] a) {
      return stddev(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static double stddev(double[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double[][] resid = new double[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) resid[j-mr][l-mc] = (double)a[j][l];
      }
      resid = subArrays(resid, avgValue(a, mr, nr, mc, nc));
      double ndegfree = resid.length * resid[0].length - 1.0;
      return Math.sqrt( totalValue( multArrays(resid, resid) ) / ndegfree );
   }

   public static double stddev(double[][] a) {
      return stddev(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static double stddev(long[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double[][] resid = new double[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) resid[j-mr][l-mc] = (double)a[j][l];
      }
      resid = subArrays(resid, avgValue(a, mr, nr, mc, nc));
      float ndegfree = resid.length * resid[0].length - 1.0f;
      return Math.sqrt( totalValue( multArrays(resid, resid) ) / ndegfree );
   }

   public static double stddev(long[][] a) {
      return stddev(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static double stddev(short[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double[][] resid = new double[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) resid[j-mr][l-mc] = (double)a[j][l];
      }
      resid = subArrays(resid, avgValue(a, mr, nr, mc, nc));
      float ndegfree = resid.length * resid[0].length - 1.0f;
      return Math.sqrt( totalValue( multArrays(resid, resid) ) / ndegfree );
   }

   public static double stddev(short[][] a) {
      return stddev(a, 0, a.length-1, 0, a[0].length-1);
   }
   
   public static double stddev(byte[][] a, int mr, int nr, int mc, int nc) {
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double[][] resid = new double[nr-mr+1][nc-mc+1];
      for (int j = mr; j <= nr; j++) {
	for (int l = mc; l <= nc; l++) resid[j-mr][l-mc] = (double)a[j][l];
      }
      resid = subArrays(resid, avgValue(a, mr, nr, mc, nc));
      float ndegfree = resid.length * resid[0].length - 1.0f;
      return Math.sqrt( totalValue( multArrays(resid, resid) ) / ndegfree );
   }

   public static double stddev(byte[][] a) {
      return stddev(a, 0, a.length-1, 0, a[0].length-1);
   }   
   
   public static int whereMaxValue(int[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
	if (a[j] > x) {
	   x = a[j];
	   b = j;
	}
      }
      return b; 
   }

   public static int whereMaxValue(int[] a) {
      return whereMaxValue(a, 0, a.length-1);
   }

   public static int whereMaxValue(float[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0; 
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      float x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j] > x) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMaxValue(float[] a) {
      return whereMaxValue(a, 0, a.length-1);
   }

   public static int whereMaxValue(double[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0; 
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j] > x) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMaxValue(double[] a) {
      return whereMaxValue(a, 0, a.length-1);
   }

   public static int whereMaxValue(long[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      long x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j] > x) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMaxValue(long[] a) {
      return whereMaxValue(a, 0, a.length-1);
   }

   public static int whereMaxValue(short[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      short x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j] > x) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMaxValue(short[] a) {
      return whereMaxValue(a, 0, a.length-1);
   }

   public static int whereMaxValue(byte[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      byte x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j] > x) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMaxValue(byte[] a) {
      return whereMaxValue(a, 0, a.length-1);
   }
   
   public static int whereMaxValue(char[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      char x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j] > x) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMaxValue(char[] a) {
      return whereMaxValue(a, 0, a.length-1);
   }

   public static int whereMaxValue(String[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j].compareTo(x) > 0) {
	   x = a[j];
           b = j;
	}
      }
      return b;
   }

   public static int whereMaxValue(String[] a) {
      return whereMaxValue(a, 0, a.length-1);
   }

   public static int whereMaxValue(Comparable[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0; 
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      Object x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j].compareTo(x) > 0) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMaxValue(Comparable[] a) {
      return whereMaxValue(a, 0, a.length-1);
   }

//------all function whereMaxValue( of 2D arrays ) return [xpos,ypos] :

   public static int[] whereMaxValue(int[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] > x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMaxValue(int[][] a) {
      return whereMaxValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMaxValue(float[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      float x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] > x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMaxValue(float[][] a) {
      return whereMaxValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMaxValue(double[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] > x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMaxValue(double[][] a) {
      return whereMaxValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMaxValue(long[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      long x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] > x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMaxValue(long[][] a) {
      return whereMaxValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static int[] whereMaxValue(short[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      short x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] > x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMaxValue(short[][] a) {
      return whereMaxValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMaxValue(byte[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      byte x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] > x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMaxValue(byte[][] a) {
      return whereMaxValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMaxValue(char[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      char x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] > x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMaxValue(char[][] a) {
      return whereMaxValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMaxValue(String[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l].compareTo(x) > 0) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMaxValue(String[][] a) {
      return whereMaxValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMaxValue(Comparable[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      Object x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
           if (a[j][l].compareTo(x) > 0) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMaxValue(Comparable[][] a) {
      return whereMaxValue(a, 0, a.length-1, 0, a[0].length-1);
   }



   public static int whereMinValue(int[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      int x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
	if (a[j] < x) {
	   x = a[j];
	   b = j;
	}
      }
      return b; 
   }

   public static int whereMinValue(int[] a) {
      return whereMinValue(a, 0, a.length-1);
   }

   public static int whereMinValue(float[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0; 
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      float x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j] < x) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMinValue(float[] a) {
      return whereMinValue(a, 0, a.length-1);
   }

   public static int whereMinValue(double[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0; 
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      double x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j] < x) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMinValue(double[] a) {
      return whereMinValue(a, 0, a.length-1);
   }

   public static int whereMinValue(long[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      long x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j] < x) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMinValue(long[] a) {
      return whereMinValue(a, 0, a.length-1);
   }

   public static int whereMinValue(short[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      short x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j] < x) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMinValue(short[] a) {
      return whereMinValue(a, 0, a.length-1);
   }

   public static int whereMinValue(byte[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      byte x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j] < x) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMinValue(byte[] a) {
      return whereMinValue(a, 0, a.length-1);
   }
   
   public static int whereMinValue(char[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      char x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j] < x) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMinValue(char[] a) {
      return whereMinValue(a, 0, a.length-1);
   }

   public static int whereMinValue(String[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0;
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      String x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j].compareTo(x) > 0) {
	   x = a[j];
           b = j;
	}
      }
      return b;
   }

   public static int whereMinValue(String[] a) {
      return whereMinValue(a, 0, a.length-1);
   }

   public static int whereMinValue(Comparable[] a, int m, int n) {
      if (a.length == 0) return 0;
      int b = 0; 
      if (m < 0) m = 0;
      if (n < 0) n = 0;
      if (m >= a.length) m = a.length-1;
      if (n >= a.length) n = a.length-1;
      if (n-m < 0) {
        int temp = n;
        n = m;
        m = temp;
      }
      Object x = a[m];
      if (a.length > 1) for (int j = m+1; j <= n; j++) {
        if (a[j].compareTo(x) > 0) {
           x = a[j];
           b = j;
        }
      }
      return b;
   }

   public static int whereMinValue(Comparable[] a) {
      return whereMinValue(a, 0, a.length-1);
   }

//------all function whereMinValue( of 2D arrays ) return [xpos,ypos] :

   public static int[] whereMinValue(int[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      int x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] < x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMinValue(int[][] a) {
      return whereMinValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMinValue(float[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      float x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] < x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMinValue(float[][] a) {
      return whereMinValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMinValue(double[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      double x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] < x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMinValue(double[][] a) {
      return whereMinValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMinValue(long[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      long x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] < x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMinValue(long[][] a) {
      return whereMinValue(a, 0, a.length-1, 0, a[0].length-1);
   }

   public static int[] whereMinValue(short[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      short x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] < x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMinValue(short[][] a) {
      return whereMinValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMinValue(byte[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      byte x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] < x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMinValue(byte[][] a) {
      return whereMinValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMinValue(char[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      char x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l] < x) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMinValue(char[][] a) {
      return whereMinValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMinValue(String[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      String x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
	   if (a[j][l].compareTo(x) > 0) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMinValue(String[][] a) {
      return whereMinValue(a, 0, a.length-1, 0, a[0].length-1);
   }


   public static int[] whereMinValue(Comparable[][] a, int mr, int nr, int mc, int nc) {
      int[] b = new int[2];
      if (mr < 0) mr = 0;
      if (nr < 0) nr = 0;
      if (mr >= a.length) mr = a.length-1;
      if (nr >= a.length) nr = a.length-1;
      if (nr-mr < 0) {
        int temp = nr;
        nr = mr;
        mr = temp;
      }
      if (mc < 0) mc = 0;
      if (nc < 0) nc = 0;
      if (mc >= a[0].length) mc = a[0].length-1;
      if (nc >= a[0].length) nc = a[0].length-1;
      if (nc-mc < 0) {
        int temp = nc;
        nc = mc;
        mc = temp;
      }
      Object x = a[mr][mc];
      for (int j = mr; j <= nr; j++) {
        for (int l = mc; l <= nc; l++) {
           if (a[j][l].compareTo(x) > 0) {
	      x = a[j][l];
	      b[1] = j;
	      b[0] = l;
	   }
        }
      }
      return b;
   }

   public static int[] whereMinValue(Comparable[][] a) {
      return whereMinValue(a, 0, a.length-1, 0, a[0].length-1);
   }



   public static int[] castAsInts(float[] a) {
     int[] x = new int[a.length];
     for (int j = 0; j < a.length; j++) {
	x[j] = (int)a[j];
     }
     return x;
   }

   public static int[] roundAsInts(float[] a) {
     int[] x = new int[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = Math.round(a[j]);
     }
     return x;
   }

   public static int[][] castAsInts(float[][] a) {
     int[][] x = new int[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
	   x[j][l] = (int)a[j][l];
	}
     }
     return x;
   }

   public static int[][] roundAsInts(float[][] a) {
     int[][] x = new int[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = Math.round(a[j][l]);
	}
     }
     return x;
   }

   public static int[] castAsInts(double[] a) {
     int[] x = new int[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = (int)a[j];
     }
     return x;
   }

   public static int[] roundAsInts(double[] a) {
     int[] x = new int[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = Math.round((float)a[j]);
     }
     return x;
   }

   public static int[][] castAsInts(double[][] a) {
     int[][] x = new int[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = (int)a[j][l];
        }
     }
     return x;
   }

   public static int[][] roundAsInts(double[][] a) {
     int[][] x = new int[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = Math.round((float)a[j][l]);
        }
     }
     return x;
   }

   public static int[] castAsInts(short[] a) {
     int[] x = new int[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = (int)a[j];
     }
     return x;
   }

   public static int[][] castAsInts(short[][] a) {
     int[][] x = new int[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = (int)a[j][l];
        }
     }
     return x;
   }

   public static int[] castAsInts(char[] a) {
     int[] x = new int[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = (int)a[j];
     }
     return x;
   }

   public static int[][] castAsInts(char[][] a) {
     int[][] x = new int[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = (int)a[j][l];
        }
     }
     return x;
   }

   public static int[] castAsInts(byte[] a) {
     int[] x = new int[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = (int)a[j];
     }
     return x;
   }

   public static int[][] castAsInts(byte[][] a) {
     int[][] x = new int[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = (int)a[j][l];
        }
     }
     return x;
   }

   public static float[] castAsFloats(int[] a) {
     float[] x = new float[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = (float)a[j];
     }
     return x;
   }

   public static float[][] castAsFloats(int[][] a) {
     float[][] x = new float[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = (float)a[j][l];
        }
     }
     return x;
   }

   public static float[] castAsFloats(double[] a) {
     float[] x = new float[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = (float)a[j];
     }
     return x;
   }

   public static float[][] castAsFloats(double[][] a) {
     float[][] x = new float[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = (float)a[j][l];
        }
     }
     return x;
   }

   public static float[] castAsFloats(short[] a) {
     float[] x = new float[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = (float)a[j];
     }
     return x;
   }

   public static float[][] castAsFloats(short[][] a) {
     float[][] x = new float[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = (float)a[j][l];
        }
     }
     return x;
   }

   public static float[] castAsFloats(long[] a) {
     float[] x = new float[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = (float)a[j];
     }
     return x;
   }

   public static float[][] castAsFloats(long[][] a) {
     float[][] x = new float[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = (float)a[j][l];
        }
     }
     return x;
   }

   public static double[] castAsDoubles(int[] a) {
     double[] x = new double[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = (double)a[j];
     }
     return x;
   }

   public static double[][] castAsDoubles(int[][] a) {
     double[][] x = new double[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = (double)a[j][l];
        }
     }
     return x;
   }

   public static double[] castAsDoubles(float[] a) {
     double[] x = new double[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = (double)a[j];
     }
     return x;
   }

   public static double[][] castAsDoubles(float[][] a) {
     double[][] x = new double[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = (double)a[j][l];
        }
     }
     return x;
   }

   public static double[] castAsDoubles(short[] a) {
     double[] x = new double[a.length];
     for (int j = 0; j < a.length; j++) {
        x[j] = (double)a[j];
     }
     return x;
   }

   public static double[][] castAsDoubles(short[][] a) {
     double[][] x = new double[a.length][a[0].length];
     for (int j = 0; j < a.length; j++) {
        for (int l = 0; l < a[0].length; l++) {
           x[j][l] = (double)a[j][l];
        }
     }
     return x;
   }

   public static int median(int[] a) {
      int n = a.length;
      int[] b = (int[])a.clone();
      Arrays.sort(b);
      if (n % 2 == 1) return b[n/2]; else return (b[n/2]+b[n/2-1])/2;
   }

   public static float median(float[] a) {
      int n = a.length;
      float[] b = (float[])a.clone();
      Arrays.sort(b);
      if (n % 2 == 1) return b[n/2]; else return (b[n/2]+b[n/2-1])/2;
   }

   public static double median(double[] a) {
      int n = a.length;
      double[] b = (double[])a.clone();
      Arrays.sort(b);
      if (n % 2 == 1) return b[n/2]; else return (b[n/2]+b[n/2-1])/2;
   }

   public static long median(long[] a) {
      int n = a.length;
      long[] b = (long[])a.clone();
      Arrays.sort(b);
      if (n % 2 == 1) return b[n/2]; else return (b[n/2]+b[n/2-1])/2;
   }

   public static short median(short[] a) {
      int n = a.length;
      short[] b = (short[])a.clone();
      Arrays.sort(b);
      if (n % 2 == 1) return b[n/2]; else return (short)((b[n/2]+b[n/2-1])/2);
   }

   public static byte median(byte[] a) {
      int n = a.length;
      byte[] b = (byte[])a.clone();
      Arrays.sort(b);
      if (n % 2 == 1) return b[n/2]; else return (byte)((b[n/2]+b[n/2-1])/2);
   }

   public static void main(String[] args) {
      float[] a = {3,7,5,4,7,2};
      System.out.println(maxValue(a,4,2));
      System.out.println(minValue(a));
      System.out.println(countN(a,2));
      float[] b = reverse(a);
      System.out.println(b[0] + " " + b[5]);
      System.out.println(totalValue(a));
      System.out.println(avgValue(a));
      b = shift(a,3);
      System.out.println(b[0] + " " + b[1]);
      int[] c = where(a, "==", 7);
      System.out.println(arrayToString(c));
      b = extractValues(a,c);
      System.out.println(arrayWithIndexToString(b));
      b = extractValues(a, 1, 4);
      System.out.println(arrayToString(b));
      int[][] a2 = {{1,2,3}, {4,5,9}, {7,8,9}, {10,11,12}};
      int[][] b2 = extractValues(a2,0,2,0,1);
      System.out.println(arrayToString(b2));
      c = andUnion(where(a,">",2), where(a,"<",7));
      System.out.println(arrayToString(b));
      c = orUnion(where(a,"<=",3), where(a,"==",5));
      System.out.println(arrayToString(b));
      char[] ca = {'e','b','f','a','g','b'};
      System.out.println(maxValue(ca));
      System.out.println(countN(ca,'b'));
      c = where(ca, ">", 'e');
      System.out.println(arrayToString(c));
      String[] cs = {"Craig","Kyle","Kelly","Dhaval","Gerald","Eric"};
      System.out.println(maxValue(cs));
      System.out.println(countN(cs,"Craig"));
      c = where(cs, "<=", "Kelly");
      System.out.println(arrayToString(c));
      System.out.println(arrayToString(extractValues(cs, c)));
      System.out.println(arrayToString(strlenArray(cs)));
      System.out.println(arrayToString(strposArray(cs, "e")));
      System.out.println(arrayToString(strmidArray(cs, 0, 3)));
      System.out.println(maxValue(a2,1,3,0,1));
      System.out.println(countN(a2,9));
      System.out.println(arrayToString(reverse(a2)));
      System.out.println(totalValue(a2));
      System.out.println(avgValue(a2));
      System.out.println(arrayToString(shift(a2,-1,0)));
      System.out.println(arrayToString(where(a2,">=",9)));
      System.out.println(arrayToString(extractValues(a2,where(a2,">=",9))));
      System.out.println(arrayToString(map2DByRow(a2)));
      System.out.println(arrayToString(map2DByColumn(a2)));
      System.out.println(arrayToString(andUnion(where(a2,">",3),where(a2,"<",8))));
      System.out.println(arrayToString(orUnion(where(a2,"<",3),where(a2,">",9))));
      System.out.println(arrayToString(extractRow(a2,1)));
      System.out.println(arrayToString(extractColumn(a2,1)));
      System.out.println(arrayToString(avgValue(a2,1)));
      System.out.println(arrayToString(avgValue(a2,2)));
      System.out.println(arrayToString(addArrays(a2,a2)));
      System.out.println(stddev(a));
      System.out.println(stddev(a2));
   }
}
