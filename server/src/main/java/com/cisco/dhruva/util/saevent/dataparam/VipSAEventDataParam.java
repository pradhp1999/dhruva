package com.cisco.dhruva.util.saevent.dataparam;

// POJO class builder for vip state events

public class VipSAEventDataParam extends DataParam {

  public static class Builder {
    private String optionsPing;
    private String vipState;

    public VipSAEventDataParam build() {
      return new VipSAEventDataParam(this);
    }

    public Builder optionsPing(String optionsPing) {
      this.optionsPing = optionsPing;
      return this;
    }

    public Builder vipState(String vipState) {
      this.vipState = vipState;
      return this;
    }
  }

  private String optionsPing;

  private String vipState;

  private VipSAEventDataParam(Builder builder) {
    this.vipState = builder.vipState;
    this.optionsPing = builder.optionsPing;
  }

  public String getOptionsPing() {
    return optionsPing;
  }

  public String getVipState() {
    return vipState;
  }

  public void setOptionsPing(String optionsPing) {
    this.optionsPing = optionsPing;
  }

  public void setVipState(String vipState) {
    this.vipState = vipState;
  }
}
