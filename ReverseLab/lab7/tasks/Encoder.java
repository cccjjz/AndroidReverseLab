import java.util.Random;

/**
 * @Author：chenjianzhou
 * @Package：PACKAGE_NAME
 * @Project：lab7
 * @name：Encoder
 * @Date：2023/5/6 23:40
 * @Filename：Encoder
 */
public class Encoder {
    private String convertHexToString(String arg5) {
        StringBuilder v1 = new StringBuilder();
        int v0;
        for(v0 = 0; v0 < arg5.length() - 1; v0 += 2) {
            v1.append(((char)(Integer.parseInt(arg5.substring(v0, v0 + 2), 16) ^ 0xFF)));
        }
        return v1.toString();
    }

    private String convertStringToHex(String arg5) {
        char[] v1 = arg5.toCharArray();
        StringBuffer v2 = new StringBuffer();
        int v0;
        for(v0 = 0; v0 < v1.length; ++v0) {
            v2.append(Integer.toHexString(v1[v0] ^ 0xFF));
        }

        return v2.toString();
    }

    public String decode(String arg8) {
        if(arg8.length() == 0) {
            return "";
        }

        StringBuffer v2 = new StringBuffer();
        int v0;
        for(v0 = 0; v0 < arg8.length(); v0 += 5) {
            int v3 = 4 - Integer.parseInt(arg8.substring(v0, v0 + 1), 16) % 4;
            v2.append(arg8.substring(v0 + 1 + v3, v0 + 5) + arg8.substring(v0 + 1, v3 + (v0 + 1)));
        }

        return this.convertHexToString(v2.toString()).substring(0, 11);
    }

    public String encode(String arg9) {
        if(arg9.length() != 11) {
            System.out.println("input error!");
            return "";
        }

        byte[] v1 = getSalt();
        String v2 = this.convertStringToHex(arg9 + "a");
        StringBuffer v3 = new StringBuffer();
        int v0;
        for(v0 = 0; v0 < v2.length(); v0 += 4) {
            int v5 = v1[v0 / 4] % 4;
            v3.append(Integer.toHexString(v1[v0 / 4]));
            v3.append(v2.substring(v0 + v5, v0 + 4) + v2.substring(v0, v5 + v0));
        }

        return v3.toString();
    }

    private byte[] getSalt() {
        byte[] v1 = new byte[]{0, 0, 0, 0, 0, 0};
        Random v2 = new Random();
        int v0;
        for(v0 = 0; v0 < v1.length; ++v0) {
            v1[v0] = (byte)v2.nextInt(15);
        }

        return v1;
    }
}
