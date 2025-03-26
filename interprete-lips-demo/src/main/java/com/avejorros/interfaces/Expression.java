package com.avejorros.interfaces;

import com.avejorros.bean.Environment;

public interface Expression<T> {
  T evaluate(Environment env);
}
