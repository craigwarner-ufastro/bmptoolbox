public class Test2 {
  public static void main(String[] args) {
    int h = Integer.parseInt(args[0]);
    System.out.println(hexToDec(h));
  }

    public static int decToHex(int dec) {
      int hex = 0;
      int ndec = 1;
      int nhex = 1;
      int count = 0;
      while (ndec*10 < dec) {
        ndec*=10;
        nhex*=16;
        count++;
        if (count > 10) {
          //Some weird error state, break out of loop
          return -1;
        }
      }
      while (dec > 0) {
        int x = dec/ndec;
        hex += nhex*x;
        dec -= ndec*x;
        ndec/=10;
        nhex/=16;
        count++;
        if (count > 10) {
          //Some weird error state, break out of loop
          return -1;
        }
      }
      return hex;
    }

    public static int hexToDec(int hex) {
      int dec = 0;
      int ndec = 1;
      int nhex = 1;
      int count = 0;
      while (nhex*16 <= hex) {
        //if (_verbose) System.out.println("nhex = "+nhex+"; hex = "+hex);
        ndec*=10;
        nhex*=16;
        count++;
        if (count > 10) {
          //Some weird error state, break out of loop
          return -1;
        }
      }
System.out.println("HEX "+hex+" nhex "+nhex+" dec "+dec+" ndec "+ndec);
      count = 0;
      while (hex > 0) {
        int x = hex/nhex;
System.out.println("x "+x+" HEX "+hex+" nhex "+nhex+" dec "+dec+" ndec "+ndec);
        dec += ndec*x;
        hex -= nhex*x;
        nhex/=16;
        ndec/=10;
        count++;
        if (count > 10) {
          //Some weird error state, break out of loop
          return -1;
        }
      }
      return dec;
    }

}
