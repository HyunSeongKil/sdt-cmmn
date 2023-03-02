package kr.vaiv.sdt.cmmn.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
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
   * 파일 인스턴스
   */
  public static final String FILE = "file";

  /**
   * 파일 명
   */
  public static final String FILE_NAME = "fileName";

  public void Download() {
    setContentType("application/octet-stream; utf-8");
  }

  @Override
  protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    setContentType("application/octet-stream; utf-8");

    for (Map.Entry<String, Object> entry : model.entrySet()) {
      log.debug("{}\t{}", entry.getKey(), entry.getValue());
    }

    File file = (File) model.get(FILE);
    if (null == file || !file.exists()) {
      log.error("FILE NOT FOUND");
      return;
      // throw new FileNotFoundException();
    }

    String fileName = "" + model.get(FILE_NAME);

    response.setContentType(getContentType());
    response.setContentLength((int) file.length());

    String header = request.getHeader("User-Agent");
    boolean b = header.indexOf("MSIE") > -1;

    if (b) {
      fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
    } else {
      fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), "iso-8859-1");
    }

    response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\";");
    response.setHeader("Content-Transfer-Encoding", "binary");

    OutputStream out = response.getOutputStream();

    // option 1
    try (FileInputStream fis = new FileInputStream(file)) {
      FileCopyUtils.copy(fis, out);
    }

    out.flush();


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

  }

}
