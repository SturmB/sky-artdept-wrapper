package info.chrismcgee.sky.artdept;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@SuppressWarnings("SpellCheckingInspection")
public class FsTest {

	public static void main(String[] args) {
		Process returned;
		StringBuilder processed = new StringBuilder();
		String netPath = File.separator + File.separator
				+ "SKYFS" + File.separator
				+ "ArtDept" + File.separator
				+ "ArtDept" + File.separator
				+ "Scripts" + File.separator
				+ "sky-artdept" + File.separator
				+ "Test" + File.separator;
//		String data1 = "\"Here is a String making a round-trip.\"";
//		String data2 = "\"" + netPath + "\"";
		String data3 = "\"C:\\blank.pdf\"";
		String data4 = "\"{" + 
				"\"\"~pdfFilePath~\"\": \"\"~//SKYFS/ArtDept/ArtDept/JOBS/999000-999999/999999/999999_D-N10-WHITE_PROOF.pdf~\"\"," + 
				"\"\"~duplex~\"\": false," + 
				"\"\"~printerName~\"\": \"\"~\\\\\\\\SKYFS\\\\art 1~\"\"," + 
				"\"\"~printEnabled~\"\": false" + 
				"}\"";
//		File proofScriptFile = new File(netPath + "Proof.vbs");
		File tapScriptFile = new File(netPath + "TestAndPrint2.vbs");
		try {
//			returned = Runtime.getRuntime().exec("cscript //NoLogo C:/test2.vbs " + data1);
//			returned = Runtime.getRuntime().exec("cscript //NoLogo " + proofScriptFile + " " + data1 + " " + data1 + " " + data2);
			returned = Runtime.getRuntime().exec("cscript //NoLogo " + tapScriptFile + " " + data3 + " " + data4);
			returned.waitFor();
			BufferedReader input =
					new BufferedReader(
							new InputStreamReader(returned.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				processed.append(line);
			}
			input.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}
		
	    System.out.println(processed.toString().trim());
	}

}
