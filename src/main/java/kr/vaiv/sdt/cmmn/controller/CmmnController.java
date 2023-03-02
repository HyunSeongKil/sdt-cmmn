/**
 * 
 */
package kr.vaiv.sdt.cmmn.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import kr.vaiv.sdt.cmmn.domain.*;
import kr.vaiv.sdt.cmmn.misc.CmmnConst;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 모든 controller 의 부모
 * 
 * @author gravity
 *
 */
@Slf4j
public class CmmnController {

	/**
	 * ResponseEntity 생성&리턴 2020/08/12 Modified - <T extend Domain> -> <T>
	 *
	 * @param t Domain의 자식
	 * @return
	 */
	protected <T> ResponseEntity<Map<String, Object>> res(T t) {

		//
		Map<String, Object> map = new HashMap<>();
		map.put(CmmnConst.DATA, t);

		//
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	/**
	 * ResponseEntity 생성&리턴
	 * 
	 * @param t Map의 자식
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected <T extends Map> ResponseEntity<Map<String, Object>> res(T t) {

		//
		Map<String, Object> map = new HashMap<>();
		map.put(CmmnConst.DATA, t);

		//
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	/**
	 * ResponseEntity 생성&리턴
	 * 
	 * @param vo
	 * @return
	 */
	protected <T extends CmmnEntity> ResponseEntity<Map<String, Object>> res(List<T> list) {

		Map<String, Object> map = new HashMap<>();
		map.put(CmmnConst.DATA, list);

		//
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	/**
	 * ResponseEntity 생성&리턴
	 * 
	 * @param vo
	 * @return
	 */
	protected <T extends CmmnEntity> ResponseEntity<Map<String, Object>> res(List<T> list, HttpStatus hs) {

		Map<String, Object> map = new HashMap<>();
		map.put(CmmnConst.DATA, list);

		//
		return new ResponseEntity<>(map, hs);
	}

	/**
	 * 파일을 Object 형태로 리턴
	 * 
	 * @param
	 * @return
	 */
	protected Object file2Object(String filePath) {
		return file2Object(new File(filePath));
	}

	/**
	 * @param path 파일의 fullpath
	 * @return
	 */
	protected Object file2Object(Path path) {
		return file2Object(path.toFile());
	}

	/**
	 * object로 변환
	 * 
	 * @param path     파일 경로
	 * @param filename 파일명
	 * @return
	 */
	protected Object file2Object(Path path, String filename) {
		return file2Object(path.resolve(filename));
	}

	/**
	 * file을 Object로 변환
	 * 
	 * @param file 파일
	 * @return
	 */
	protected Object file2Object(File file) {
		try {
			try (InputStream targetStream = new FileInputStream(file)) {
				return new ObjectMapper().readValue(targetStream, Object.class);
			}
		} catch (IOException e) {
			log.error("{}", e);
		}

		//
		return null;
	}

	/**
	 * image file을 base64 문자열로 변환
	 * 
	 * @param fullname
	 * @return
	 */
	protected String fileToBase64String(String fullname) {
		if (StringUtils.isEmpty(fullname)) {
			log.warn("<< empty fullname");
			return "";
		}

		//
		return fileToBase64String(Paths.get(fullname));
	}

	/**
	 * image file을 base64 문자열로 변환
	 * 
	 * @param filepath
	 * @return
	 */
	protected String fileToBase64String(Path filepath) {
		if (!Files.exists(filepath)) {
			log.warn("<< not exists file {}", filepath);
			return "";
		}

		//
		return fileToBase64String(filepath.toFile());
	}

	/**
	 * image file을 base64 문자열로 변환
	 * 
	 * @param file
	 * @return
	 */
	protected String fileToBase64String(File file) {
		if (null == file) {
			log.warn("<< null file");
			return "";
		}

		if (!file.exists()) {
			log.warn("<< not exists file {}", file);
			return "";
		}

		//
		try {
			byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(file);
			return Base64.getEncoder().encodeToString(bytes);
		} catch (IOException e) {
			log.error("{}", e);
		}

		//
		return "";
	}
}
