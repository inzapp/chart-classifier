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
			if (line.indexOf("fs.append") > -1) {
				String[] sp = line.split("ocr, \\(");
				sb.append(sp[0]).append("ocr, ").append(sp[1].substring(0, sp[1].length() - 1)).append('\n');
			} else {
				sb.append(line).append('\n');
			}
		}
		br.close();
		System.out.println(sb.toString());
		FileOutputStream fos = new FileOutputStream(new File("auto2.py"));
		fos.write(sb.toString().getBytes());
		fos.close();
	}
	
	private static void processGVar() throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("auto.py")));
		StringBuilder sb = new StringBuilder();
		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}
			if (line.indexOf(" = \"\"") > -1 && line.indexOf("new_path") == -1) {
				String varName = line.split(" = \"\"")[0].trim();
				sb.append(String.format("            g_var['%s'] = \"\"\n", varName));
			} else {
				sb.append(line).append('\n');
			}
		}
		br.close();
		System.out.println(sb.toString());
		FileOutputStream fos = new FileOutputStream(new File("auto2.py"));
		fos.write(sb.toString().getBytes());
		fos.close();
	}
	
	private static void processAppend() throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("auto.py")));
		StringBuilder sb = new StringBuilder();
		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}
			if (line.indexOf(" = ") > -1 && line.indexOf("ocr(") > -1) {
				String[] sp = line.split(" = ");
				String varName = sp[0].trim();
				if (sp.length >= 2 && sp[1].indexOf("ocr(") > -1) {
					sp = sp[1].split("ocr\\(");
					String submat = sp[1].split("\\)")[0];
					sb.append(String.format("                processes.append(Process(target=ocr, args=(q, '%s', %s)))\n", varName, submat));
				}
			} else {
				sb.append(line).append('\n');
			}
		}
		br.close();
		System.out.println(sb.toString());
		FileOutputStream fos = new FileOutputStream(new File("auto2.py"));
		fos.write(sb.toString().getBytes());
		fos.close();
	}
}