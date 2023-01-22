/*
 * File:     Server
 * Package:  org.dromakin
 * Project:  netology_client_server
 *
 * Created by dromakin as 21.01.2023
 *
 * author - dromakin
 * maintainer - dromakin
 * version - 2023.01.21
 */

package org.dromakin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.BinaryOperator;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private static final Logger logger = LogManager.getLogger(Server.class);

    public void start(int port) throws ServerException {
        try {
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            logger.info("Server started!");

            logger.info("New connection accepted on port: {}", clientSocket.getPort());

            out.println("Choose option: calculateTwoNumbers (1) (default) / solveQuadraticEquation (2)");
            int option = Integer.parseInt(in.readLine());

            out.println("Write the first number");
            int numberA = Integer.parseInt(in.readLine());

            out.println("Write the second number");
            int numberB = Integer.parseInt(in.readLine());

            if (option == 1) {
                out.println("Write operation");
                String operation = in.readLine();
                out.println(SuperCalculator.calculateTwoNumbers(numberA, numberB, operation));

            } else if (option == 2) {
                out.println("Write the third number");
                int numberC = Integer.parseInt(in.readLine());
                out.println(SuperCalculator.solveQuadraticEquation(numberA, numberB, numberC));
            }


        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }

    }


    public void stop() throws ServerException {
        try {
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
            logger.info("Server stopped!");
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }

    }


    private static class SuperCalculator {

        private static final Logger logger = LogManager.getLogger(SuperCalculator.class);

        public static String solveQuadraticEquation(int a, int b, int c) {
            logger.info("running solveQuadraticEquation");
            StringBuilder result = new StringBuilder();

            double discriminant = b * b * 1.0 - 4 * a * c;

            if (a == 0) {
                double x = (-1.0 * b) / c;
                result.append("Обычное уравнение! Результат: ").append(x);
                return result.toString();
            }

            if (discriminant < 0) {
                result.append("Корней нет!");
            } else if (discriminant == 0) {
                double x = (-1.0 * b) / (2 * a);
                result.append("Один корень: ").append(x);
            } else {
                double x1 = (-1 * b + Math.sqrt(discriminant)) / (2 * a);
                double x2 = (-1 * b - Math.sqrt(discriminant)) / (2 * a);

                result.append("Два корня. ").append("Первый: ").append(x1).append(" Второй: ").append(x2);
            }

            return result.toString();
        }

        private static BinaryOperator<Integer> checkOperator(String operationString) {
            BinaryOperator<Integer> operation;

            switch (operationString) {
                case "+":
                    operation = Integer::sum;
                    break;

                case "-":
                    operation = (x, y) -> x - y;
                    break;

                case "*":
                    operation = (x, y) -> x * y;
                    break;

                case "/":
                    operation = (x, y) -> {
                        try {
                            return x / y;
                        } catch (ArithmeticException e) {
                            logger.error(e.getMessage(), e);
                            return x;
                        }
                    };
                    break;

                default:
                    logger.error("Can't detect operation. Use sum numbers by default&");
                    operation = Integer::sum;
                    break;

            }

            return operation;
        }

        public static String calculateTwoNumbers(int a, int b, String operationString) {
            logger.info("running calculateTwoNumbers");
            BinaryOperator<Integer> operation = checkOperator(operationString);
            int result = operation.apply(a, b);
            return String.format("Result of operation %d %s %d = %d", a, operationString, b, result);
        }

    }


    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start(8888);
        } catch (ServerException e) {
            logger.error(e.getMessage(), e);
            try {
                server.stop();
            } catch (ServerException ex) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
