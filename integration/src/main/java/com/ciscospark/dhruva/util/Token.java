package com.ciscospark.dhruva.util;

public class Token {

  public static final String At_Sign = "@";
  public static final String Colon = ":";
  public static final String Dash = "-";
  public static final String Dot = ".";
  public static final String EmptyString = "";
  public static final String Equals = "=";
  public static final String LeftAngleBracket = "<";
  public static final String LeftSquareBracket = "[";
  public static final String Quote = "\"";
  public static final String RightAngleBracket = ">";
  public static final String RightSquareBracket = "]";
  public static final String Semicolon = ";";
  public static final String Slash = "/";
  public static final String DoubleSlash = Slash + Slash;
  public static final String Space = " ";
  public static final String Underscore = "_";

  // sip tokens
  public static final String Sip = "sip";
  public static final String SipColon = Sip + Colon;
  public static final String SipColonDoubleSlash = SipColon + DoubleSlash;

  // sips tokens
  public static final String Sips = Sip + "s";
  public static final String SipsColon = Sips + Colon;
  public static final String SipsColonDoubleSlash = SipsColon + DoubleSlash;

  public static final String Tcp = "tcp";
  public static final String Tls = "tls";
  public static final String Udp = "udp";

  //   _sip._tcp.
  public static final String SipTcpSrvPrefix =
      Token.Underscore + Token.Sip + Token.Dot + Token.Underscore + Token.Tcp + Token.Dot;
  //   _sips._tcp.
  public static final String SipTlsSrvPrefix =
      Token.Underscore + Token.Sips + Token.Dot + Token.Underscore + Token.Tcp + Token.Dot;

  // tel tokens
  public static final String Tel = "tel";
  public static final String TelColon = Tel + Colon;

  public static class Chars {
    public static final char Dot = '.';
    public static final char Slash = '/';
    public static final char Space = ' ';
  }
}
