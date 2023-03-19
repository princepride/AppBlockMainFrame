import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AppUnblock {
    public static void main(String[] args) {
        try {
            // 构造shell指令
            String[] cmd = { "cmd", "/c", "icacls \"C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe\" /remove:d Everyone"};

            // 执行指令
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();

            // 输出指令执行结果
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
