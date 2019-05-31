package info.chrismcgee.sky.artdept;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

public class ReadStreamWithCall implements Callable<String> {

    String name;
    InputStream is;
    Thread thread;
    
    public ReadStreamWithCall(String name, InputStream is) {
        this.name = name;
        this.is = is;
    }

	@Override
	public String call() throws Exception {
        InputStreamReader isr = new InputStreamReader (is);
        BufferedReader br = new BufferedReader (isr);
        String json = "";
        while (true) {
            String s = br.readLine ();
            if (s == null) break;
            	json += s;
        }
        is.close ();
        return json;
	}

}
