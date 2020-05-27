package com.inzapp.chartclassifierapi.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ChartController {
	@GetMapping("ocr")
	public Map<String, Object> ocr(@RequestParam("path") String path) throws Exception {
		File progressDir = new File("progress");
		if (progressDir.exists() && progressDir.isDirectory()) {
			for (File f : progressDir.listFiles()) {
				f.delete();
			}
		}
		File resultDir = new File("result");
		if (resultDir.exists() && resultDir.isDirectory()) {
			for (File f : resultDir.listFiles()) {
				f.delete();
			}
		}
		Process p = Runtime.getRuntime().exec("python ocr.py " + path);
		p.waitFor();
		p.destroy();

		Map<String, Object> map = new HashMap<>();
		String result = this.getFileContent("result.txt");
		if (result.startsWith("0")) {
			map.put("result", "failure");
		} else {
			map.put("result", "success");
			String[] paths = path.split("\\s+");
			for (String s : paths) {
				Map<String, Object> m = new HashMap<>();
				m.put("progress", this.getProgressFilePathList(s));
				m.put("result", this.getOcrResult(s));
				map.put(s, m);
			}
		}
		return map;
	}

	private List<String> getProgressFilePathList(String path) throws Exception {
		String[] sp = path.split("/");
		String fileName = sp[sp.length - 1];
		String rawFileName = fileName.split("\\.")[0];
		List<File> progressFiles = new ArrayList<>();
		progressFiles.add(new File("progress/" + rawFileName + "_grayscale.jpg"));
		progressFiles.add(new File("progress/" + rawFileName + "_threshold.jpg"));
		progressFiles.add(new File("progress/" + rawFileName + "_blur.jpg"));
		List<String> progressFilePaths = new ArrayList<>();
		for (File f : progressFiles) {
			if (f.exists() && f.isFile()) {
				progressFilePaths.add(f.getAbsolutePath());
			}
		}
		return progressFilePaths;
	}

	private String getOcrResult(String path) throws Exception {
		String[] sp = path.split("/");
		String fileName = sp[sp.length - 1];
		String rawFileName = fileName.split("\\.")[0];
		File ocrResultFile = new File("result/" + rawFileName + ".txt");
		if (ocrResultFile.exists() && ocrResultFile.isFile()) {
			return this.getFileContent(ocrResultFile.getAbsolutePath());
		} else {
			return "error to get file content";
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
