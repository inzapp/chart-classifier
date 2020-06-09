package com.inzapp.chartclassifierapi.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ChartController {
	private final String[] OPTIONS = { "grayscale", "threshold", "blur", "invert" };

	@GetMapping("ocr")
	public Map<String, Object> ocr(@RequestParam("path") String path, HttpServletRequest request) throws Exception {
		String option = request.getParameter("option") != null ? request.getParameter("option") : "";
		String[] receivedOptions = option.equals("") ? new String[] {} : option.split(",");
		int c = 0;
		for (String receivedOption : receivedOptions) {
			for (String curOption : OPTIONS) {
				if (receivedOption.equals(curOption)) {
					++c;
					continue;
				}
			}
		}
		Map<String, Object> map = new HashMap<>();
		if (c != receivedOptions.length) {
			StringBuilder sb = new StringBuilder();
			sb.append("failure: invalid option. you can only use ");
			for (String curOption : OPTIONS) {
				sb.append(curOption).append(" ");
			}
			map.put("result", sb.toString());
			return map;
		}

		Process p = Runtime.getRuntime().exec(String.format("python ocr.py %s option=%s", path, option));
		p.waitFor();
		p.destroy();

		String result = this.getFileContent("result.txt");
		if (result.startsWith("0")) {
			map.put("result", "failure");
		} else {
			map.put("result", "success");
			String[] paths = path.split("\\s+");
			for (String s : paths) {
				Map<String, Object> m = new HashMap<>();
				m.put("progress", this.getProgressFilePathList(s, receivedOptions));
				m.put("result", this.getOcrResult(s));
				map.put(s, m);
			}
		}
		return map;
	}

	private List<String> getProgressFilePathList(String path, String[] receivedOptions) throws Exception {
		String[] sp = path.split("\\\\");
		String fileName = sp[sp.length - 1];
		String rawFileName = fileName.split("\\.")[0];
		List<File> progressFiles = new LinkedList<>();
		for (int i = 0; i < receivedOptions.length; ++i) {
			progressFiles.add(new File(String.format("progress\\%s_%s_%s.jpg", rawFileName, String.valueOf(i), receivedOptions[i])));
		}
		List<String> progressFilePaths = new ArrayList<>();
		for (File f : progressFiles) {
			if (f.exists() && f.isFile()) {
				progressFilePaths.add(f.getAbsolutePath());
			}
		}
		return progressFilePaths;
	}

	private String getOcrResult(String path) throws Exception {
		String[] sp = path.split("\\\\");
		String fileName = sp[sp.length - 1];
		String rawFileName = fileName.split("\\.")[0];
		String ocrFileName = String.format("result\\%s.txt", rawFileName);
		File ocrResultFile = new File(ocrFileName);
		if (ocrResultFile.exists() && ocrResultFile.isFile()) {
			return this.getFileContent(ocrResultFile.getAbsolutePath());
		} else {
			return "error to get file content. file not found - " + ocrFileName;
		}
	}

	private String getFileContent(String filePath) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
		StringBuilder sb = new StringBuilder();
		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}
			sb.append(line).append('\n');
		}
		br.close();
		return sb.toString();
	}
}
