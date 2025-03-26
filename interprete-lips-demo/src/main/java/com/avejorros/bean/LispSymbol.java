package com.avejorros.bean;

import com.avejorros.interfaces.Expression;

public class LispSymbol<T> implements Expression<T> {
  private String name;

  public LispSymbol(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T evaluate(Environment env) {
    Object value = env.getVariable(name);
    if (value == null) {
      throw new RuntimeException("Variable no definida: " + name);
    }
    return (T) value;
  }
}
