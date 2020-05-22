package scoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class CodeAssister {
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("auto.py")));
		StringBuilder sb = new StringBuilder();
		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}
			sb.append(line).append('\n');
			if (line.indexOf(" = ") > -1) {
				String[] sp = line.split(" = ");
				if (sp.length >= 2 && sp[1].indexOf("(") > -1) {
					String varName = sp[0].trim();
					sp = sp[1].split("\\(");
					if (sp.length >= 2 && sp[0].equals("pytesseract.image_to_string")) {
						sb.append(String.format("                print(%s)", varName)).append('\n');
					}
				}
			}
		}
		br.close();
		System.out.println(sb.toString());
		FileOutputStream fos = new FileOutputStream(new File("auto2.py"));
		fos.write(sb.toString().getBytes());
		fos.close();
	}
}