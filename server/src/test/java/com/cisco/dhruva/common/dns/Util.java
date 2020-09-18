package com.cisco.dhruva.common.dns;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.xbill.DNS.*;

public class Util {

  static Name getName(String name) {
    try {
      Name retVal = Name.fromString(name);
      return retVal;
    } catch (TextParseException e) {
      throw new RuntimeException(e);
    }
  }

  static List<Record> srvNodes(String... nodeNames) {
    return Stream.of(nodeNames)
        .map(input -> Record.newRecord(getName(input), Type.SRV, DClass.IN))
        .collect(Collectors.toList());
  }

  static List<Record> aNodes(String... nodeNames) {
    return Stream.of(nodeNames)
        .map(input -> Record.newRecord(getName(input), Type.A, DClass.IN))
        .collect(Collectors.toList());
  }
}
