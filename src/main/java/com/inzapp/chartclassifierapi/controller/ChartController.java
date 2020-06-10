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
	private final String[] AVAILABLE_OPTIONS = { "grayscale", "threshold", "blur", "invert" };

	@GetMapping("ocr")
	public Map<String, Object> ocr(@RequestParam("path") String path, HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<>();

		// check path is valid
		String pathValidRes = this.checkPathIsValid(path);
		if (!pathValidRes.equals("success")) {
			map.put("result", pathValidRes);
			return map;
		}

		// replace regex
		path.replaceAll("/", "\\");

		// check option is valid
		String option = request.getParameter("option") != null ? request.getParameter("option") : "";
		String optionValidRes = this.checkOptionIsValid(option);
		if (!optionValidRes.equals("success")) {
			map.put("result", optionValidRes);
			return map;
		}

		// run python process and wait until end
		Process p = Runtime.getRuntime().exec(String.format("python ocr.py %s option=%s", path, option));
		p.waitFor();
		p.destroy();

		// return result
		String result = this.getFileContent("result.txt");
		if (result.startsWith("0")) {
			map.put("result", "failure");
		} else {
			map.put("result", "success");
			String[] paths = path.split(",");
			for (String curPath : paths) {
				Map<String, Object> curResultMap = new HashMap<>();
				curResultMap.put("progress", this.getProgressFilePathList(curPath, option));
				curResultMap.put("ocrResult", this.getOcrResult(curPath));
				map.put(curPath, curResultMap);
			}
		}
		return map;
	}

	private String checkPathIsValid(String path) {
		String[] paths = path.split(",");
		for (int i = 0; i < paths.length; ++i) {
			File pathFile = new File(paths[i]);
			if (!pathFile.exists()) {
				return "file not found : " + paths[i];
			} else if (!pathFile.isFile()) {
				return "is not file : " + paths[i];
			}
		}
		return "success";
	}

	private String checkOptionIsValid(String option) {
		if (option.equals("")) {
			return "success";
		}
		String[] options = option.split(",");
		for (int i = 0; i < options.length; ++i) {
			boolean isAvailable = false;
			for (int j = 0; j < AVAILABLE_OPTIONS.length; ++j) {
				if (options[i].equals(AVAILABLE_OPTIONS[j])) {
					isAvailable = true;
					break;
				}
			}
			if (!isAvailable) {
				StringBuilder availableOptions = new StringBuilder();
				for (int j = 0; j < AVAILABLE_OPTIONS.length; ++j) {
					availableOptions.append(AVAILABLE_OPTIONS[j]);
					if (j == AVAILABLE_OPTIONS.length - 1) {
						break;
					}
					availableOptions.append(", ");
				}
				return String.format("invalid option : %s. you can only use %s", options[i],
						availableOptions.toString());
			}
		}
		return "success";
	}

	private List<String> getProgressFilePathList(String path, String option) throws Exception {
		String[] sp = path.split("\\\\");
		String fileName = sp[sp.length - 1];
		String rawFileName = fileName.split("\\.")[0];
		List<File> progressFiles = new LinkedList<>();
		String[] options = option.split(",");
		for (int i = 0; i < options.length; ++i) {
			progressFiles
					.add(new File(String.format("progress\\%s_%s_%s.jpg", rawFileName, String.valueOf(i), options[i])));
		}
		List<String> progressFilePaths = new ArrayList<>();
		for (File progressFile : progressFiles) {
			if (progressFile.exists() && progressFile.isFile()) {
				progressFilePaths.add(progressFile.getAbsolutePath());
			}
		}
		return progressFilePaths;
	}

	private String getOcrResult(String path) throws Exception {
		String[] sp = path.split("\\\\");
		String fileNameWithExtension = sp[sp.length - 1];
		String rawFileName = fileNameWithExtension.split("\\.")[0];
		String ocrResultFileName = String.format("result\\%s.txt", rawFileName);
		File ocrResultFile = new File(ocrResultFileName);
		if (ocrResultFile.exists() && ocrResultFile.isFile()) {
			return this.getFileContent(ocrResultFile.getAbsolutePath());
		} else {
			return "error to get file content. file not found - " + ocrResultFileName;
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
