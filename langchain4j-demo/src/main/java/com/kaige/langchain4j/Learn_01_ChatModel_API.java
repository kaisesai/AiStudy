package com.kaige.langchain4j;

import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * LangChain4j ChatModel API 学习示例
 * 
 * 这是一个基于LangChain4j框架和Ollama本地AI模型的交互式聊天程序。
 * 程序提供了一个控制台界面，允许用户与AI模型进行实时对话。
 * 
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>与Ollama本地部署的Qwen模型进行对话</li>
 *   <li>支持实时用户输入和AI回复</li>
 *   <li>提供友好的控制台交互界面</li>
 *   <li>包含完整的错误处理机制</li>
 *   <li>支持优雅退出程序</li>
 * </ul>
 * 
 * <p><strong>使用说明：</strong></p>
 * <ol>
 *   <li>确保Ollama服务在本地运行 (http://localhost:11434)</li>
 *   <li>确保已下载qwen:4b模型</li>
 *   <li>运行程序后，直接在控制台输入消息即可开始对话</li>
 *   <li>输入'exit'或'quit'可退出程序</li>
 * </ol>
 * 
 * <p><strong>技术要点：</strong></p>
 * <ul>
 *   <li>使用LangChain4j的OllamaChatModel进行AI模型调用</li>
 *   <li>采用Scanner实现控制台输入</li>
 *   <li>通过时间戳和格式化输出提升用户体验</li>
 *   <li>实现异常处理确保程序稳定性</li>
 * </ul>
 * 
 * @author kaige
 * @version 1.0
 * @since 2026-02-14
 * @see OllamaChatModel
 */
public class Learn_01_ChatModel_API {

    /**
     * 程序入口点
     * 
     * 启动交互式聊天程序，初始化并运行主对话循环。
     * 程序将持续运行直到用户输入退出命令。
     * 
     * @param args 命令行参数（本程序未使用）
     */
    public static void main(String[] args) {
        normalProgram();
    }


    /**
     * 主程序逻辑实现
     * 
     * 初始化Ollama聊天模型并启动主对话循环。
     * 该方法负责：
     * <ul>
     *   <li>配置和构建OllamaChatModel实例</li>
     *   <li>创建Scanner用于读取用户输入</li>
     *   <li>处理用户输入验证</li>
     *   <li>调用AI模型获取回复</li>
     *   <li>处理程序退出逻辑</li>
     *   <li>异常处理和资源清理</li>
     * </ul>
     * 
     * <p><strong>模型配置：</strong></p>
     * <ul>
     *   <li>模型名称：qwen:4b</li>
     *   <li>服务地址：http://localhost:11434</li>
     *   <li>最大重试次数：3次</li>
     * </ul>
     * 
     * @see #printUserInput(String)
     * @see #printModelResponse(String)
     */
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
     * 
     * 将用户输入的消息以标准化格式输出到控制台，
     * 包含时间戳和用户标识，提升对话可读性。
     * 
     * <p><strong>输出格式：</strong>[HH:mm:ss] 👤 用户: {input}</p>
     * 
     * @param input 用户输入的原始文本内容
     * @throws NullPointerException 当input为null时抛出
     */
    private static void printUserInput(String input) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("[" + timestamp + "] 👤 用户: " + input);
    }

    /**
     * 格式化打印模型响应
     * 
     * 将AI模型的回复以美观的格式输出到控制台，
     * 使用分隔线和时间戳来增强可读性和专业感。
     * 
     * <p><strong>输出格式：</strong></p>
     * <pre>
     * [HH:mm:ss] 🤖 AI助手:
     * ----------------------------------------
     * {response内容}
     * ----------------------------------------
     * </pre>
     * 
     * @param response AI模型返回的响应文本
     * @throws NullPointerException 当response为null时抛出
     */
    private static void printModelResponse(String response) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("[" + timestamp + "] 🤖 AI助手:");
        System.out.println("----------------------------------------");
        System.out.println(response);
        System.out.println("----------------------------------------");
    }

}
