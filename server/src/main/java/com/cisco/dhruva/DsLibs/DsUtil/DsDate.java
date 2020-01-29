// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsSipParser.DsSipParserException;
import java.text.*;
import java.util.*;

/** DsDate provides SIP specific date formatting capabilities. */
public class DsDate {
  private static final DateFormatter formatter = new DateFormatter();

  /** The date stored internally as java.util.Date. */
  private Date parsed_date; // = null;

  private String date_String; // = null;
  private boolean m_bChanged;

  /**
   * Returns the string representation of the specified <code>date</code> in the standard SIP date
   * format "Day, DD Mon YYYY HH:MM:SS GMT".
   *
   * @param date Date to be formatted to SIP standard.
   * @return SIP date in standard "Day, DD Mon YYYY HH:MM:SS GMT" format.
   */
  public static String formatDsDate(Date date) {
    SimpleDateFormat format = (SimpleDateFormat) formatter.get();
    return format.format(date);
  }

  /**
   * Returns the string representation of the current date (today) in the standard SIP date format
   * "Day, DD Mon YYYY HH:MM:SS GMT".
   *
   * @return SIP date in standard "Day, DD Mon YYYY HH:MM:SS GMT" format.
   */
  public static String getCurrentDsDate() {
    SimpleDateFormat format = (SimpleDateFormat) formatter.get();
    return format.format(new Date());
  } // End getCurrentDsDate()

  /**
   * Method used to create DsDate from a string.
   *
   * @param inDate the string to construct date from
   * @throws DsSipParserException exception thrown if the date is not of appropriate format
   */
  public void constructDsDate(String inDate) throws DsSipParserException {
    try {
      SimpleDateFormat format = (SimpleDateFormat) formatter.get();
      parsed_date = format.parse(inDate);
      date_String = inDate;
      setChanged();
    } catch (java.text.ParseException parseexcep) {
      date_String = inDate;

      throw new DsSipParserException("Wrong DsDate format");
    }
  }

  /**
   * Date returned as a string.
   *
   * @return String the date as a string
   */
  public String getDateAsString() {
    return date_String;
  }

  /**
   * Date returned.
   *
   * @return Date the date
   */
  public Date getDate() {
    return parsed_date;
  }

  /**
   * Date returned.
   *
   * @param aDate the date
   */
  public void setDate(Date aDate) {
    parsed_date = aDate;
    date_String = formatDsDate(aDate);
    setChanged();
  }

  /**
   * Method used to make a copy of the current object.
   *
   * @return Object the cloned object
   */
  public Object clone() {
    DsDate date = new DsDate();

    if (parsed_date != null) {
      date.parsed_date = (Date) parsed_date.clone();
    }

    if (date_String != null) {
      date.date_String = date_String;
    }

    return date;
  }

  /**
   * Checks for equality of dates.
   *
   * @param obj the object to check
   * @return <code>true</code> if the dates are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    DsDate dsDate = null;
    try {
      dsDate = (DsDate) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (parsed_date != null) {
      if (dsDate.parsed_date == null) {
        return false;
      }
      if (!parsed_date.equals(dsDate.parsed_date)) {
        return false;
      }
    } else {
      if (dsDate.parsed_date != null) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns true if a value in this class has changed.
   *
   * @return true if a value in this class has changed.
   */
  public boolean isChanged() {
    return m_bChanged;
  }

  /** Sets this changed flag to false. */
  public void resetChanged() {
    m_bChanged = false;
  }

  /** Sets this changed flag to true. */
  public void setChanged() {
    m_bChanged = true;
  }
}

class DateFormatter extends ThreadLocal {
  public Object initialValue() {
    SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    return formatter;
  }
}
