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
    // Crear nuevo entorno que hereda todo del padre
    Environment localEnv = new Environment();

    // 1. Copiar TODAS las funciones (incluyendo esta para recursividad)
    localEnv.getFuctions().putAll(env.getFuctions());

    // 2. Copiar variables del entorno padre
    localEnv.getVariables().putAll(env.getVariables());

    // 3. Asignar nuevos valores a los parámetros
    for (String param : parameters) {
      Object value = env.getVariable(param);
      localEnv.defineVariable(param, value);
    }

    // 4. IMPORTANTE: Registrar esta función en el entorno local
    localEnv.defineFunction(((LispSymbol<?>) body).getName(), this);

    // 5. Evaluar el cuerpo
    return body.evaluate(localEnv);
  }
}
