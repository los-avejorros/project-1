package com.avejorros.bean;

import com.avejorros.interfaces.Expression;

public class LispNumber implements Expression<Double> {
  private double value;

  public LispNumber(double value) {
      this.value = value;
  }

  @Override
  public Double evaluate(Environment env) {
      return value;
  }
}
