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

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ChartController {
	private final String[] AVAILABLE_OPTIONS = { "grayscale", "threshold", "blur", "invert" };

	@SuppressWarnings("unchecked")
	@PostMapping("ocr")
	public Map<String, Object> ocr(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<>();
		List<String> paths = (List<String>) params.get("path");
		if (paths == null || paths.size() == 0) {
			map.put("result", "failure: need path parameter");
			return map;
		}
		List<String> options = (List<String>) params.get("option");

		// check path is valid
		String pathValidRes = this.checkPathIsValid(paths);
		if (!pathValidRes.equals("success")) {
			map.put("result", pathValidRes);
			return map;
		}

		// replace regex
		for (int i = 0; i < paths.size(); ++i) {
			paths.set(i, paths.get(i).replaceAll("\\\\", "/"));
		}

		// check option is valid
		String optionValidRes = this.checkOptionIsValid(options);
		if (!optionValidRes.equals("success")) {
			map.put("result", optionValidRes);
			return map;
		}

		// run python process and wait until end
		String runCommand = String.format("python ocr.py path=%s option=%s", this.pathsToString(paths), this.optionsToString(options));
		System.out.println(runCommand);
		Process process = Runtime.getRuntime().exec(runCommand);
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		while (true) {
			if (br.readLine() == null) {
				break;
			}
		}
		br.close();
		process.waitFor();
		process.destroy();

		// return result
		String result = this.getFileContent("result.txt");
		if (result.startsWith("0")) {
			map.put("result", "failure");
		} else {
			map.put("result", "success");
			for (String path : paths) {
				Map<String, Object> curResultMap = new HashMap<>();
				curResultMap.put("progress", this.getProgressFilePathList(path, options));
				curResultMap.put("ocrResult", this.getOcrResult(path));
				map.put(path, curResultMap);
			}
		}
		return map;
	}

	private String checkPathIsValid(List<String> paths) {
		for (String path : paths) {
			File pathFile = new File(path);
			if (!pathFile.exists()) {
				return "file not found : " + path;
			} else if (!pathFile.isFile()) {
				return "is not file : " + path;
			}
		}
		return "success";
	}

	private String checkOptionIsValid(List<String> options) {
		if (options == null) {
			return "success";
		}
		for (String option : options) {
			boolean isAvailable = false;
			for (String availableOption : AVAILABLE_OPTIONS) {
				if (option.equals(availableOption)) {
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
				return String.format("invalid option : %s. you can only use %s", option, availableOptions.toString());
			}
		}
		return "success";
	}

	private String pathsToString(List<String> paths) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < paths.size(); ++i) {
			sb.append(paths.get(i));
			if (i == paths.size() - 1) {
				break;
			}
			sb.append("*");
		}
		return sb.toString();
	}

	private String optionsToString(List<String> options) {
		if (options == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < options.size(); ++i) {
			sb.append(options.get(i));
			if (i == options.size() - 1) {
				break;
			}
			sb.append("*");
		}
		return sb.toString();
	}

	private List<String> getProgressFilePathList(String path, List<String> options) throws Exception {
		if (options == null) {
			return new ArrayList<>();
		}
		String[] sp = path.split("\\\\");
		String fileName = sp[sp.length - 1];
		String rawFileName = fileName.split("\\.")[0];
		List<File> progressFiles = new LinkedList<>();
		for (int i = 0; i < options.size(); ++i) {
			progressFiles.add(
					new File(String.format("progress\\%s_%s_%s.jpg", rawFileName, String.valueOf(i), options.get(i))));
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
			return "error to get file content. file not found: " + ocrResultFileName;
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
