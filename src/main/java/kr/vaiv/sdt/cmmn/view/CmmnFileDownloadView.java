package kr.vaiv.sdt.cmmn.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.view.AbstractView;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일 다운로드
 */
@Component
@Slf4j
public class CmmnFileDownloadView extends AbstractView {
  /**
   * 파일 인스턴스. 필수
   */
  public static final String FILE = "file";

  /**
   * 파일 명. 필수
   */
  public static final String FILE_NAME = "fileName";

  public void Download() {
    setContentType("application/octet-stream; utf-8");
  }

  @Override
  protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    /**
     * 
     */
    Runnable logginModelEntries = () -> {
      for (Map.Entry<String, Object> entry : model.entrySet()) {
        log.debug("{}\t{}", entry.getKey(), entry.getValue());
      }
    };

    /**
     * 
     */
    Supplier<Boolean> validateModel = () -> {
      if (model.get(FILE) == null) {
        log.error("FILE IS NULL");
        return false;
      }

      File file = (File) model.get(FILE);
      if (!file.exists()) {
        log.error("{} NOT FOUND", file.toPath());
        return false;
      }

      return true;
    };

    /**
     * 
     */
    Function<String, String> processFilename = (fileName) -> {

      boolean b = request.getHeader("User-Agent").indexOf("MSIE") > -1;

      if (b) {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8);
      }

      try {
        return new String(fileName.getBytes(StandardCharsets.UTF_8), "iso-8859-1");
      } catch (UnsupportedEncodingException e) {
        log.error("{}", e);
      }

      return "";

    };

    /**
     * 
     */
    Consumer<String> setResponseHeaderValue = (fileName) -> {
      response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\";");
      response.setHeader("Content-Transfer-Encoding", "binary");
    };

    /**
     * 
     */
    Consumer<File> writeFile = (file) -> {
      // option 1
      try (OutputStream out = response.getOutputStream()) {
        try (FileInputStream fis = new FileInputStream(file)) {
          FileCopyUtils.copy(fis, out);
        }

        out.flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      // option 2
      // FileInputStream fis = null;
      // try {
      // fis = new FileInputStream(file);
      // FileCopyUtils.copy(fis, out);
      // } catch (Exception e) {
      // e.printStackTrace();
      // } finally {
      // if (fis != null) {
      // try {
      // fis.close();
      // } catch (IOException ioe) {
      // ioe.printStackTrace();
      // }
      // }
      // out.flush();
      // }
    };
    ////

    setContentType("application/octet-stream; utf-8");

    logginModelEntries.run();

    if (!validateModel.get()) {
      return;
    }

    File file = (File) model.get(FILE);
    String fileName = processFilename.apply("" + model.get(FILE_NAME));

    response.setContentType(getContentType());
    response.setContentLength((int) file.length());

    setResponseHeaderValue.accept(fileName);

    writeFile.accept(file);

  }

}
