package com.cisco.dhruva.sip.servergroups.util;

public class ServerGroupInput implements Cloneable {
  public ServerGroupInput() {};

  String description;
  String id;
  InputServerGroup serverGroup;

  enum failoverType {
    NONE,
    PROXY_FAILURE,
    FAILURE_RESPONSE,
    REQUEST_TIMEOUT,
    ICMP_ERROR
  };

  public static class InputServerGroup implements Cloneable {
    public InputServerGroup() {}

    String name;
    Integer[] failover;
    Elements[] elements;
    TestConfig testConfig;

    public static class TestConfig implements Cloneable {
      public TestConfig() {}

      int totalCalls;
      TestCombination testCombination;

      public static class TestCombination implements Cloneable {
        public TestCombination() {}

        String[] incomingTransport;
        String[] outgoingTransport;
        String[] loadBalancerType;

        public String[] getIncomingTransport() {
          return incomingTransport;
        }

        public void setIncomingTransport(String[] incomingTransport) {
          this.incomingTransport = incomingTransport;
        }

        public String[] getOutgoingTransport() {
          return outgoingTransport;
        }

        public void setOutgoingTransport(String[] outgoingTransport) {
          this.outgoingTransport = outgoingTransport;
        }

        public String[] getLoadBalancerType() {
          return loadBalancerType;
        }

        public void setLoadBalancerType(String[] loadBalancerType) {
          this.loadBalancerType = loadBalancerType;
        }

        public Object clone() throws CloneNotSupportedException {
          TestCombination testCombination = (TestCombination) super.clone();
          testCombination.setIncomingTransport(this.getIncomingTransport().clone());
          testCombination.setOutgoingTransport(this.getIncomingTransport().clone());
          testCombination.setLoadBalancerType(this.getLoadBalancerType().clone());
          return testCombination;
        }
      }

      public int getTotalCalls() {
        return totalCalls;
      }

      public void setTotalCalls(int totalCalls) {
        this.totalCalls = totalCalls;
      }

      public TestCombination getTestCombination() {
        return testCombination;
      }

      public void setTestCombination(TestCombination testCombination) {
        this.testCombination = testCombination;
      }

      public Object clone() throws CloneNotSupportedException {
        TestConfig testConfig = (TestConfig) super.clone();
        testConfig.setTestCombination((TestCombination) this.getTestCombination().clone());
        return testConfig;
      }
    }

    public static class Elements {
      public Elements() {}

      String ip;
      int port;
      int weight;
      float qvalue;

      public String getIp() {
        return ip;
      }

      public void setIp(String ip) {
        this.ip = ip;
      }

      public int getPort() {
        return port;
      }

      public void setPort(int port) {
        this.port = port;
      }

      public int getWeight() {
        return weight;
      }

      public void setWeight(int weight) {
        this.weight = weight;
      }

      public float getQvalue() {
        return qvalue;
      }

      public void setQvalue(float qvalue) {
        this.qvalue = qvalue;
      }

      public TestConfig getTestConfig() {
        return testConfig;
      }

      public void setTestConfig(TestConfig testConfig) {
        this.testConfig = testConfig;
      }

      TestConfig testConfig;

      public static class TestConfig {
        public TestConfig() {}

        String status = "up";
        ServerGroupInput.failoverType failoverType = ServerGroupInput.failoverType.NONE;
        int failoverCode;

        public String getStatus() {
          return status;
        }

        public void setStatus(String status) {
          this.status = status;
        }

        public ServerGroupInput.failoverType getFailoverType() {
          return failoverType;
        }

        public void setFailoverType(ServerGroupInput.failoverType failoverType) {
          this.failoverType = failoverType;
        }

        public int getFailoverCode() {
          return failoverCode;
        }

        public void setFailoverCode(int failoverCode) {
          this.failoverCode = failoverCode;
        }
      }
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Elements[] getElements() {
      return elements;
    }

    public void setElements(Elements[] elements) {
      this.elements = elements;
    }

    public TestConfig getTestConfig() {
      return testConfig;
    }

    public void setTestConfig(TestConfig testConfig) {
      this.testConfig = testConfig;
    }

    public Integer[] getFailover() {
      return failover;
    }

    public void setFailover(Integer[] failover) {
      this.failover = failover;
    }

    public Object clone() throws CloneNotSupportedException {
      InputServerGroup serverGroup = (InputServerGroup) super.clone();
      serverGroup.setTestConfig((TestConfig) this.getTestConfig().clone());
      return serverGroup;
    }
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public InputServerGroup getInputServerGroup() {
    return serverGroup;
  }

  public void setInputServerGroup(InputServerGroup serverGroup) {
    this.serverGroup = serverGroup;
  }

  public String getInfo() {
    String info = new String();
    info =
        "\nId: "
            + getId()
            + "\nDescription: "
            + getDescription()
            + "\nIncoming Transport: "
            + getInputServerGroup().getTestConfig().getTestCombination().getIncomingTransport()[0]
            + "\nOutgoing Transport: "
            + getInputServerGroup().getTestConfig().getTestCombination().getOutgoingTransport()[0]
            + "\nLoadBalancer: "
            + getInputServerGroup().getTestConfig().getTestCombination().getLoadBalancerType()[0];
    return info;
  }

  public Object clone() throws CloneNotSupportedException {
    ServerGroupInput serverGroupInput = (ServerGroupInput) super.clone();
    serverGroupInput.setInputServerGroup((InputServerGroup) this.getInputServerGroup().clone());
    return serverGroupInput;
  }
}
