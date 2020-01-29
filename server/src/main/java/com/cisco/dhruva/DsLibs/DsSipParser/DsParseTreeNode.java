// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

final class DsParseTreeNode {
  // positions match between next and nextChar
  protected DsParseTreeNode next[] = null;
  protected byte[] nextChars = null;

  protected int match = -1;

  protected DsParseTreeNode() {}

  protected DsParseTreeNode next(byte ch) {
    int index = indexOf(ch);
    if (index == -1) {
      return null;
    }

    return next[index];
  }

  protected void addChar(byte ch) {
    // character known to be ASCII - optimize - jsm
    if (ch <= 'Z' && ch >= 'A') {
      ch += 32; // 'a' - 'A' (97 - 65)
    }

    if (nextChars == null) {
      // new array with this byte
      nextChars = new byte[1];
      nextChars[0] = ch;

      // new next node that matches this byte
      next = new DsParseTreeNode[1];
      next[0] = new DsParseTreeNode();
    } else {
      if (!exists(ch)) {
        // new array, 1 longer, add this byte
        int len = nextChars.length;
        byte newNextChars[] = new byte[len + 1];
        System.arraycopy(nextChars, 0, newNextChars, 0, len);
        newNextChars[len] = ch;
        nextChars = newNextChars;

        // new array, 1 longer, add a new node
        DsParseTreeNode newNext[] = new DsParseTreeNode[len + 1];
        System.arraycopy(next, 0, newNext, 0, len);
        newNext[len] = new DsParseTreeNode();
        next = newNext;
      }
    }

    return;
  }

  // must pass in lower case
  protected boolean exists(byte ch) {
    // ch = Character.toUpperCase(ch);

    if (nextChars == null) {
      return false;
    }

    for (int i = 0; i < nextChars.length; i++) {
      if (ch == nextChars[i]) {
        return true;
      }
    }

    return false;
  }

  protected int indexOf(byte ch) {
    if (nextChars == null) {
      return -1;
    }

    // character known to be ASCII - optimize - jsm
    if (ch <= 'Z' && ch >= 'A') {
      ch += 32; // 'a' - 'A' (97 - 65)
    }

    for (int i = 0; i < nextChars.length; i++) {
      if (ch == nextChars[i]) {
        return i;
      }
    }

    return -1;
  }
  //
  //    public void print(byte ch, String indent)
  //    {
  //        if (indent == null)
  //        {
  //            indent = "";
  //        }
  //
  //        System.out.println(indent + (char)ch);
  //        print(indent + " ");
  //    }
  //
  //    public void print(String indent)
  //    {
  //        if (next != null)
  //        {
  //            for (int i = 0; i < next.length; i++)
  //            {
  //                next[i].print(nextChars[i], indent);
  //
  //                if (match >= 0)
  //                {
  //                    System.out.println("match: " + match);
  //                }
  //            }
  //        }
  //        else
  //        {
  //            if (match >= 0)
  //            {
  //                System.out.println("match: " + match);
  //            }
  //        }
  //    }
}
