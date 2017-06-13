package com.oneops.api.resource.model;

public class RedundancyConfig {

  private int max = 10;
  private int current = 2;
  private int min = 2;
  private int stepUp = 1;
  private int stepDown = 1;
  private int percentDeploy = 100;

  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public int getCurrent() {
    return current;
  }

  public void setCurrent(int current) {
    this.current = current;
  }

  public int getMin() {
    return min;
  }

  public void setMin(int min) {
    this.min = min;
  }

  public int getStepUp() {
    return stepUp;
  }

  public void setStepUp(int stepUp) {
    this.stepUp = stepUp;
  }

  public int getStepDown() {
    return stepDown;
  }

  public void setStepDown(int stepDown) {
    this.stepDown = stepDown;
  }

  public int getPercentDeploy() {
    return percentDeploy;
  }

  public void setPercentDeploy(int percentDeploy) {
    this.percentDeploy = percentDeploy;
  }
}
