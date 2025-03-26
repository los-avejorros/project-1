package com.avejorros.bean;

import java.util.List;

import com.avejorros.interfaces.Expression;

public class LispFunction<T> implements Expression<T> {
  private List<String> parameters;
  private Expression<T> body;

  public LispFunction(List<String> parameters, Expression<T> body) {
    this.parameters = parameters;
    this.body = body;
  }

  public List<String> getParameters() {
    return parameters;
  }

  public Expression<T> getBody() {
    return body;
  }

  @Override
  public T evaluate(Environment env) {
    // Crear un nuevo entorno local para la función
    Environment localEnv = new Environment();

    // Asignar valores a los parámetros
    for (String param : parameters) {
      Object value = env.getVariable(param);
      localEnv.defineVariable(param, value);
    }

    // Evaluar el cuerpo de la función en el entorno local
    return body.evaluate(localEnv);
  }
}
