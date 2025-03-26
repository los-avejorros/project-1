package com.avejorros;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.avejorros.bean.Environment;
import com.avejorros.bean.LispList;
import com.avejorros.bean.LispNumber;
import com.avejorros.bean.LispSymbol;

public class LispListTest {
  @Test
    public void testArithmeticOperations() {
        Environment env = new Environment();
        
        // (+ 2 3)
        LispList<Double> list = new LispList<>();
        list.addExpression(new LispSymbol<>("+"));
        list.addExpression(new LispNumber(2));
        list.addExpression(new LispNumber(3));
        
        assertEquals(5.0, list.evaluate(env), 0.001);
    }
    
    @Test
    public void testNestedOperations() {
        Environment env = new Environment();
        
        // (* (+ 2 3) (- 5 1))
        LispList<Double> addList = new LispList<>();
        addList.addExpression(new LispSymbol<>("+"));
        addList.addExpression(new LispNumber(2));
        addList.addExpression(new LispNumber(3));
        
        LispList<Double> subList = new LispList<>();
        subList.addExpression(new LispSymbol<>("-"));
        subList.addExpression(new LispNumber(5));
        subList.addExpression(new LispNumber(1));
        
        LispList<Double> mainList = new LispList<>();
        mainList.addExpression(new LispSymbol<>("*"));
        mainList.addExpression(addList);
        mainList.addExpression(subList);
        
        assertEquals(20.0, mainList.evaluate(env), 0.001);
    }

}
