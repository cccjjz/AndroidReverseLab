/**
 * @Author：chenjianzhou
 * @Package：PACKAGE_NAME
 * @Project：lab7
 * @name：Checker
 * @Date：2023/5/6 23:40
 * @Filename：Checker
 */
public class Checker {
    byte[] secret;

    public Checker() {
        this.secret = new byte[]{0x70, 100, 100, 68, 0x1F, 5, 0x72, 120};
    }

    private static byte charToByteAscii(char arg1) {
        return (byte)arg1;
    }

    public boolean check(String arg5) {
        if(arg5.length() != 12) {
            return false;
        }

        String v1 = arg5.substring(0, 8);
        String v2 = arg5.substring(8, 12);
        return (this.checkStr1(v1)) && (this.checkStr2(v2));
    }

    private boolean checkStr1(String arg5) {
        int v0;
        for(v0 = 0; v0 < arg5.length(); ++v0) {
            if((Checker.charToByteAscii(arg5.charAt(v0)) ^ v0 * 11) != this.secret[v0]) {
                return false;
            }
        }

        return true;
    }

    private boolean checkStr2(String arg5) {
        try {
            if(((int)(((int)Integer.parseInt(arg5)))) >= 1000) {
                if(((int)(((int)Integer.parseInt(arg5)))) % 16 == 0 || ((int)(((int)Integer.parseInt(arg5)))) % 27 == 0) {
                    return true;
                }

                int v1_1 = ((int)(((int)Integer.parseInt(arg5)))) % 10;
                if(v1_1 == 8) {
                    return true;
                }
            }
        }
        catch(NumberFormatException v1) {
        }

        return false;
    }
}
