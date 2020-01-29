/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
/*
 * Created by IntelliJ IDEA.
 * User: reddy
 * Date: Feb 18, 2002
 * Time: 12:18:02 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.cisco.dhruva.DsLibs.DsUtil;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/** This class shows the version info of this jar */
public class DsVersionInfo {
  private static String jarName = null;
  private static String jarVersion = null;
  private static String jarBuildDate = null;
  private static String jarBuildNum = null;
  private static StringBuffer tmpBuffer = null;
  private static String nedPath = null;
  private static String nedFile = null;
  private static String jarPath = null;
  private static String jarFile = null;
  private static String dsneJarName = null;
  private static String productName = null;
  private static String nedVersion = null;
  private static String nedBuildDate = null;
  private static String nedBuildNum = null;
  private static String VERSION_INFO = "version_info";

  public static void getVersion() {
    String classpath = System.getProperty("java.class.path");
    String pathSeparator = System.getProperty("path.separator");
    String fileSeparator = System.getProperty("file.separator");
    Properties version_info = new Properties();
    JarFile jf = null;
    Manifest m = null;
    Attributes values = null;
    String path = null;

    Process sum = null;
    InputStream sumin = null;
    byte[] sumbytes = null;
    StringTokenizer sumtk = null;
    String checksum = null;
    // System.out.println("full classpath : " + classpath);
    StringTokenizer st = new StringTokenizer(classpath, pathSeparator);
    int tokens = st.countTokens();

    for (int i = 0; i < tokens; i++) {
      path = st.nextToken();
      // System.out.println("ned file entry : " + path);
      if (path.endsWith(".ned")) {
        File f = new File(path);
        if ((f.exists()) && (f.isFile())) {
          if ((nedPath == null) && (nedFile == null)) {
            nedPath = path;
            nedFile = nedPath.substring(nedPath.lastIndexOf(fileSeparator) + 1);
            jf = null;
            m = null;
            values = null;
            try {
              jf = new JarFile(nedPath, true);
              m = jf.getManifest();
              values = m.getAttributes("com.dynamicsoft.core.ned");
              if (values == null) {
                values = m.getAttributes("dynamicsoft");
              }
              // comment the above line and uncomment the below line if you have moved to the new
              // agreed manifest format.
              // values = m.getAttributes("dynamicsoft");

              if (values != null) {
                productName = values.getValue("DisplayName");
                if (productName == null) {
                  productName = values.getValue("Product_Name");
                }
                // comment the above line and uncomment the below line if you have moved to the new
                // agreed manifest format.
                // productName = values.getValue("Product_Name");
                nedVersion = values.getValue("Version");
                nedBuildDate = values.getValue("Date"); // change this to Build_Date
              } else {
                System.out.println(nedFile + " version info is empty");
                throw new Exception();
              }
              tmpBuffer = new StringBuffer();
              if (productName != null) {
                tmpBuffer.append("\n");
                tmpBuffer.append("\n" + productName.toUpperCase() + "\n");
                tmpBuffer.append("\n");
              }
              if (nedFile != null) {
                tmpBuffer.append("Name : " + nedFile);
                tmpBuffer.append("\n");
              }
              if (nedVersion != null) {
                tmpBuffer.append("Version : " + nedVersion);
                tmpBuffer.append("\n");
              }
              if (nedBuildDate != null) {
                tmpBuffer.append("Build Date : " + nedBuildDate);
                tmpBuffer.append("\n");
              }

              if (nedFile != null) {
                tmpBuffer.append("Location : " + path);
                tmpBuffer.append("\n");
                if (!(System.getProperty("os.name").startsWith("Windows"))) {
                  sum = Runtime.getRuntime().exec("sum " + nedPath);
                  sum.waitFor();
                  sumin = sum.getInputStream();
                  sumbytes = new byte[sumin.available()];
                  sumin.read(sumbytes);
                  sumtk = new StringTokenizer(new String(sumbytes));
                  checksum = sumtk.nextToken();
                  if (checksum != null) {
                    tmpBuffer.append("Checksum Value : " + checksum);
                    tmpBuffer.append("\n");
                  }
                }
              }
              System.out.print(tmpBuffer.toString());
            } catch (Throwable t) {
              t.printStackTrace();
              System.out.println("ERROR: Network Element Definition is invalid, exiting...");
              System.exit(1);
            }
          } else {
            System.out.println(
                "ERROR: There is more than one Network Element Definitions, don't know what product this is!");
            System.exit(1);
          }
        }
      }
    }

    st = new StringTokenizer(classpath, pathSeparator);
    tokens = st.countTokens();
    for (int i = 0; i < tokens; i++) {
      path = st.nextToken();
      // System.out.println("jar file entry : " + path);
      if (path.endsWith(".jar")) {
        File f = new File(path);
        if ((f.exists()) && (f.isFile())) {
          jarPath = path;
          jarFile = jarPath.substring(jarPath.lastIndexOf(fileSeparator) + 1);

          version_info = new Properties();
          jf = null;
          m = null;
          values = null;
          jarVersion = null;
          jarBuildDate = null;
          jarBuildNum = null;
          try {
            jf = new JarFile(jarPath, true);
            m = jf.getManifest();

            // **************************************************
            values = m.getAttributes("dynamicsoft");
            if (values == null) {
              if (jarFile.equals("dsrep.jar")) values = m.getAttributes("com/dynamicsoft/re");
              else if (jarFile.equals("dscc.jar")) values = m.getAttributes("com/dynamicsoft/cc");
              else if (jarFile.equals("dsua.jar")) values = m.getAttributes("com/dynamicsoft/ua");
              else if (jarFile.equals("doapi.jar"))
                values = m.getAttributes("com/dynamicsoft/doapi");
              else if (jarFile.equals("dswbxml.jar"))
                values = m.getAttributes("com/dynamicsoft/wbxml");
              else if (jarFile.equals("dsservergroups.jar"))
                values = m.getAttributes("com/dynamicsoft/servergroups");
            }
            // ***************************************************

            // comment the above block and uncomment the below line if you have moved to the new
            // agreed manifest format.
            // values = m.getAttributes("dynamicsoft");

            if (values != null) {
              jarVersion = values.getValue("Version");
              jarBuildDate = values.getValue("Build_date");
              if (jarBuildDate == null) {
                jarBuildDate = values.getValue("Build_Date");
              } // change this to Build_Date for the new format
              jarBuildNum = values.getValue("Build_Number");
            } else {
              // ignore/comment this block if this thing doesn't apply to you
              String dsneJarName =
                  "ds"
                      + nedFile.substring(nedFile.indexOf("dsrep_") + 6, nedFile.lastIndexOf("_"))
                      + ".jar";
              if (jarFile.equals(dsneJarName)) {
                version_info = new Properties();
                jf = new JarFile(jarPath, true);
                version_info.load(jf.getInputStream(jf.getJarEntry(VERSION_INFO)));
                jarVersion = version_info.getProperty("Version");
                if (jarVersion == null) {
                  System.out.println(jarFile + " version info is empty");
                  throw new Exception();
                }
              }
              if (jarVersion == null) {
                jarFile = null;
              }
            }
            tmpBuffer = new StringBuffer();
            if (jarFile != null) {
              tmpBuffer.append("\n");
              tmpBuffer.append("Name : " + jarFile);
              tmpBuffer.append("\n");
            }
            if (jarVersion != null) {
              tmpBuffer.append("Version : " + jarVersion);
              tmpBuffer.append("\n");
            }
            if (jarBuildDate != null) {
              tmpBuffer.append("Build Date : " + jarBuildDate);
              tmpBuffer.append("\n");
            }
            if (jarBuildNum != null) {
              tmpBuffer.append("Build Number : " + jarBuildNum);
              tmpBuffer.append("\n");
            }
            if (jarFile != null) {
              tmpBuffer.append("Location : " + path);
              tmpBuffer.append("\n");
              if (!(System.getProperty("os.name").startsWith("Windows"))) {

                sum = Runtime.getRuntime().exec("sum " + jarPath);
                sum.waitFor();
                sumin = sum.getInputStream();
                sumbytes = new byte[sumin.available()];
                sumin.read(sumbytes);
                sumtk = new StringTokenizer(new String(sumbytes));
                checksum = sumtk.nextToken();
                if (checksum != null) {
                  tmpBuffer.append("Checksum Value : " + checksum);
                  tmpBuffer.append("\n");
                }
              }
            }
            System.out.print(tmpBuffer.toString());
          } catch (Throwable t) {
            // t.printStackTrace();
            System.out.println("The " + jarFile + " file has been illegally modified.");
          }
        }
      }
    }
    System.out.println();
  }

  /**
   * Displays the version information of the Route Engine Platform on to the standard output.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    getVersion();
  }
}
