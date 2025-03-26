package com.avejorros.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.avejorros.bean.Environment;
import com.avejorros.controller.ParserLisp;
import com.avejorros.interfaces.Expression;

public class REPL {
  public static void main(String[] args) {
    Environment env = new Environment();
    Scanner sc = new Scanner(System.in);
    boolean flag = true;

    // Ruta estática del archivo de prueba
    String filePath = "test.txt"; // Cambiamos la extensión a .txt

    // Cargar el archivo automáticamente
    try {
      System.out.println("Cargando archivo de prueba: " + filePath);
      executeFile(filePath, env);
    } catch (FileNotFoundException e) {
      System.out.println("Archivo no encontrado: " + filePath);
    }

    // Entrar en el REPL interactivo
    while (flag) {
      System.out.print("LISP> ");
      String input = sc.nextLine();

      if (input.equals("exit")) {
        break;
      }

      try {
        Expression<?> expr = ParserLisp.parse(input);
        Object result = expr.evaluate(env);
        System.out.println(result);
      } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
      }
    }

    sc.close();
  }

  private static void executeFile(String filePath, Environment env) throws FileNotFoundException {
    File file = new File(filePath);
    Scanner fileScanner = new Scanner(file);

    while (fileScanner.hasNextLine()) {
      String input = fileScanner.nextLine();
      System.out.println("---> linea de archivo: " + input);
      try {
        Expression<?> expr = ParserLisp.parse(input);
        expr.evaluate(env);
        //Object result = expr.evaluate(env);
        //System.out.println(result);
      } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
      }
    }

    fileScanner.close();
  }
}
