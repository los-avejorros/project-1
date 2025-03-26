package com.avejorros;

import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Test;

import com.avejorros.bean.Environment;
import com.avejorros.bean.LispFunction;
import com.avejorros.bean.LispNumber;

public class EnvironmentTest {
    
    @Test(expected = RuntimeException.class)
    public void testUndefinedVariable() {
        Environment env = new Environment();
        env.getVariable("y");
    }
    
    @Test
    public void testFunctionManagement() {
        Environment env = new Environment();
        LispFunction<Double> func = new LispFunction<>(
            List.of("x"), 
            new LispNumber(10.0)
        );
        env.defineFunction("test", func);
        assertSame(func, env.getFunction("test"));
    }
}
