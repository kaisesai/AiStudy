package com.liukai.langchain4j.spring;

import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * @author liukai
 */
public class Learn_01_ChatModel_API {

    public static void main(String[] args) {

        normalProgram();
    }


    private static void normalProgram() {
        // 创建一个 ollama 模型
        OllamaChatModel model = OllamaChatModel.builder()
                .modelName("qwen:4b")
                .baseUrl("http://localhost:11434")
                .maxRetries(3)
                .build();


        // 编写一个用户控制台输入输出的程序

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\n👤 用户输入: ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String userInput = scanner.nextLine().trim();

            // 检查退出命令
            if (userInput.equalsIgnoreCase("exit") || userInput.equalsIgnoreCase("quit")) {
                System.out.println("👋 程序即将退出，再见！");
                break;
            }

            if (userInput.isEmpty()) {
                System.out.println("⚠️  输入不能为空，请重新输入");
                continue;
            }

            // 显示用户输入
            printUserInput(userInput);

            try {
                System.out.println("🤖 正在思考...");
                String modelResponse = model.chat(userInput);
                printModelResponse(modelResponse);

            } catch (Exception e) {
                System.err.println("❌ 模型调用出错: " + e.getMessage());
                System.out.println("💡 请检查Ollama服务是否正常运行");
            }
        }

        scanner.close();
    }

    /**
     * 格式化打印用户输入
     */
    private static void printUserInput(String input) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("[" + timestamp + "] 👤 用户: " + input);
    }

    /**
     * 格式化打印模型响应
     */
    private static void printModelResponse(String response) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("[" + timestamp + "] 🤖 AI助手:");
        System.out.println("----------------------------------------");
        System.out.println(response);
        System.out.println("----------------------------------------");
    }

}
