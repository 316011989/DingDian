package cn.yumi.daka.zuida;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PingUtil {

    @Test
    public  void ping1() throws Exception {
        String line = null;
        try {
            Process pro = Runtime.getRuntime().exec("ping v.163yuncdn.com" );
            BufferedReader buf = new BufferedReader(new InputStreamReader(
                    pro.getInputStream()));
            while ((line = buf.readLine()) != null)
                System.out.println(line);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }
}
