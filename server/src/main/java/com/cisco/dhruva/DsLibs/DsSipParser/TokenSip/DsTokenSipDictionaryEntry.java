// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

/*
 * Created by IntelliJ IDEA.
 * User: trang
 * Date: Jan 7, 2003
 * Time: 11:18:19 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.cisco.dhruva.DsLibs.DsSipParser.TokenSip;

import java.io.*;
import java.io.IOException;
import java.io.OutputStream;

public class DsTokenSipDictionaryEntry implements Serializable {
  private final int m_dictionaryID;
  private final int m_dictionaryEntry;
  private final int m_shortcut;

  private final boolean m_EmptyToken;

  public static final int NO_SHORTCUT = -1;

  public static final DsTokenSipDictionaryEntry sm_EmptyTokenEntry =
      new DsTokenSipDictionaryEntry(true);

  public int getDictionaryID() {
    return m_dictionaryID;
  }

  public int getDictionaryEntry() {
    return m_dictionaryEntry;
  }

  public int getShortcut() {
    return m_shortcut;
  }

  public DsTokenSipDictionaryEntry(boolean emptyToken) {
    m_EmptyToken = emptyToken;
    m_dictionaryID = -1;
    m_dictionaryEntry = -1;
    m_shortcut = NO_SHORTCUT;
  }

  public DsTokenSipDictionaryEntry(int dictionaryID, int dictionaryEntry, int shortcut) {
    m_dictionaryID = dictionaryID;
    m_dictionaryEntry = dictionaryEntry;
    m_shortcut = shortcut;
    m_EmptyToken = false;
  }

  public void write(OutputStream out) throws IOException {
    if (m_shortcut != NO_SHORTCUT) {
      out.write(m_shortcut);
    } else if (m_EmptyToken == false) {
      out.write(m_dictionaryID);
      out.write(m_dictionaryEntry);
    }
  }
}
