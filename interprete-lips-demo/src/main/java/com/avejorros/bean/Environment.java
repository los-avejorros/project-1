package com.avejorros.bean;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private Map<String, Object> variables;
    private Map<String, LispFunction<?>> functions;

    public Environment() {
        variables = new HashMap<>();
        functions = new HashMap<>();
    }

    public <T> void defineVariable(String name, T value) {
        variables.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getVariable(String name) {
        Object value = variables.get(name);
        if (value == null) {
            throw new RuntimeException("Variable no definida: " + name);
        }
        return (T) value;
    }

    public Map<String, Object> getVariables(){
        return  variables;
    }

    public void defineFunction(String name, LispFunction<?> function) {
        functions.put(name, function);
    }

    @SuppressWarnings("unchecked")
    public <T> LispFunction<T> getFunction(String name) {
        LispFunction<?> function = functions.get(name);
        if (function == null) {
            throw new RuntimeException("Función no definida: " + name);
        }
        return (LispFunction<T>) function;
    }

    public void copyFunctionsFrom(Environment source) {
        this.functions.putAll(source.functions);
    }

    public Map<String, LispFunction<?>> getFuctions(){
        return functions;
    }
}
