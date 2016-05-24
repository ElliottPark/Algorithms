/*
 * Written by Elliott Park, 2016
 * Released to the public domain
 */

// Convert UTF-8 encoded multi-byte characters to chars
public class UTF_8Converter {
    public static final int NIL = -1;
    public static final int OFF1 = 0x0000080; // 2^7
    public static final int OFF2 = 0x0002080; // 2^13 + 2^7
    public static final int OFF3 = 0x0082080; // 2^19 + 2^13
    public static final int OFF4 = 0x2082080; // 2^25 + 2^19

    // Multibyte sequence to wide character
    static int mbtowc(byte[] s, int index) {

        int n = s.length - index;
        if ((s[0] == 0)){
            return 0;               /* no shift states */
        }

        if (n == 0) {
            return NIL;
        }
        int count = (255&s[0]);
        int decoded = count;
        if (count < 0x80){
            return (255&s[0]);
        } else if (count < 0xe0) {
            if (n < 2) {
                return NIL;
            }
            if ((255&s[1]) < 0x80) {
                return NIL;
            }
            decoded &= 0x3f;
            decoded <<= 6;
            decoded |= (255&s[1]);
            decoded &= ~(1 << 7);
            decoded += OFF1;
        } else if (count < 0xf0) {
            if (n < 3) {
                return NIL;
            }
            if ((255&s[1]) < 0x80 || (255&s[2]) < 0x80) {
                return NIL;
            }
            decoded &= 0x1f;
            decoded <<= 14;
            decoded |= (255&s[1] & 0x7f) << 7;
            decoded |= 255&s[2] & 0x7f;
            decoded += OFF2;
        } else if (count < 0xf8) {
            if (n < 4) {
                return NIL;
            }
            if ((255&s[1]) < 0x80 || (255&s[2]) < 0x80 || (255&s[3]) < 0x80) {
                return NIL;
            }
            decoded &= 0x0f;
            decoded <<= 21;
            decoded |= (255&s[1] & 0x7f) << 14;
            decoded |= (255&s[2] & 0x7f) << 7;
            decoded |= 255&s[3] & 0x7f;
            decoded += OFF3;
        } else if (count < 0xfc) {
            if (n < 5) {
                return NIL;
            }
            if ((255&s[1]) < 0x80 || (255&s[2]) < 0x80 || (255&s[3]) < 0x80 || (255&s[4]) < 0x80) {
                return NIL;
            }
            decoded &= 0x07;
            decoded <<= 28;
            decoded |= (255&s[1] & 0x7f) << 21;
            decoded |= (255&s[2] & 0x7f) << 14;
            decoded |= (255&s[3] & 0x7f) << 7;
            decoded |= 255&s[4] & 0x7f;
            decoded += OFF4;
        }
        return decoded;
    }

    public static String printBinary(int num, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = length-1; 0 <= i; i--) {
            int mask = 1<<i;
            stringBuilder.append((num&mask) == 0 ? 0 : 1);
        }
        return stringBuilder.toString();
    }

    public static String printBytes(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        byte[] bytes = s.getBytes();
        System.out.print(s + ": " + s.getBytes().length + ": ");
        for (byte b : bytes) {
            stringBuilder.append(printBinary(b, 7)).append(" ");
            System.out.print(" ");
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {

        String str = "å"; //"abcåäö";
        byte[] multibyte = str.getBytes();
        int value = mbtowc(multibyte, 0);
        System.out.println("UTF-8 char: " + str + " -- Num bytes: " + multibyte.length + " -- UTF-8 Binary: " + printBytes(str)
                + "\nUnicode char: " + (char)value + " -- Unicode Bytes: " + printBinary(value, 16));

        System.out.println("Program Finished");
    }
}
