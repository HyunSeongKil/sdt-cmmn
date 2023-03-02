package kr.vaiv.sdt.cmmn.misc;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;

import org.junit.jupiter.api.Test;

public class CmmnUtilsTest {

  @Test
  void getMemoryInfoMapTest() {
    System.out.println(CmmnUtils.getMemoryInfoMap());
  }

  @Test
  void getCpuUsageTest() {
    System.out.println(CmmnUtils.getAllDiskInfos());
    System.out.println(CmmnUtils.getMemoryInfoMap());
    System.out.println(CmmnUtils.getOsInfoMap());
  }

  public static void showDisk(File drive) {

    // 현재 가지고 있는 디스크의 크기 확인하는 코드

    try {
      System.out.println("Total  Space: " + (int) (drive.getTotalSpace() / 1024) + "kbytes");
      System.out.println("Usable Space: " + (int) (drive.getUsableSpace() / 1024) + "kbytes");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void showMemory() {

    // 자바 힙메모리 크기 확인하는 코드
    MemoryMXBean membean = (MemoryMXBean) ManagementFactory.getMemoryMXBean();
    System.out.println(membean.getHeapMemoryUsage());
    System.out.println(membean.getNonHeapMemoryUsage());
    System.out.println(CmmnUtils.getMemoryInfoMap());
    // MemoryUsage heap = membean.getHeapMemoryUsage();
    // System.out.println("Heap Memory: " + heap.getUsed() / 1024 / 1024 + "MB");
    // MemoryUsage nonheap = membean.getNonHeapMemoryUsage();
    // System.out.println("NonHeap Memory: " + nonheap.getUsed() / 1024 / 1024 +
    // "MB");
  }
}
