package com.kaige.langchain4j;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 纯粹的智能体
 */
@Slf4j
public class learn_04_Agent_07_pure {

    public static void main(String[] args) {
        ChatModel chatModel = loadBaseModel();

        BankTool bankTool = new BankTool();
        bankTool.createAccount("小黑", 1000.0);
        bankTool.createAccount("小红", 1000.0);

        WithdrawAgent withdrawAgent = AgenticServices.agentBuilder(WithdrawAgent.class)
                .chatModel(chatModel)
                .tools(bankTool)
                .build();
        CreditAgent creditAgent = AgenticServices.agentBuilder(CreditAgent.class)
                .chatModel(chatModel)
                .tools(bankTool)
                .build();

        ExchangeAgent exchangeAgent = AgenticServices.agentBuilder(ExchangeAgent.class)
                .chatModel(chatModel)
                .tools(new ExchangeTool())
                .build();

        // 主管代理
        SupervisorAgent bankSupervisor = AgenticServices.supervisorBuilder()
                .chatModel(chatModel)
                .subAgents(withdrawAgent, creditAgent, exchangeAgent)
                // 生产环境: 用 SUMMARY 提供友好的用户界面
                .responseStrategy(SupervisorResponseStrategy.SUMMARY)
                // 开发调试: 用 LAST 快速查看每个 Agent 的原始输出
                // .responseStrategy(SupervisorResponseStrategy.LAST)
                // 追求质量: 用 SCORED 获得最佳结果（但会增加一次 LLM 调用成本和延迟）
                // .responseStrategy(SupervisorResponseStrategy.SCORED)
                .build();

        // SCORED 策略会：
        // 生成 LAST 结果：最后一个 Agent (CreditAgent) 的输出 "新余额: 1150 USD"
        // 生成 SUMMARY 结果：综合整个过程 "已将 100 欧元(折合 150 USD)从小黑账户转移到小红账户，小黑余额 850 USD，小红余额 1150 USD"
        // 用 LLM 对两个结果评分，选择更完整、更符合用户需求的那个返回

        String invoked = bankSupervisor.invoke("将 100 欧元从小黑的账户转到小红的账户上");
        log.info("最终结果: {}", invoked);
    }

    private static ChatModel loadBaseModel() {
        String apiKey = Constants.MINI_MAX_API_KEY;
        String model = Constants.MINI_MAX_API_MODEL;
        String baseUrl = Constants.MINI_MAX_API_BASE_URL;

        // 方案 1: 使用 OpenAI 兼容接口 (推荐 ✅)
        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl) // MiniMax OpenAI 兼容接口
                .modelName(model)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                // 注意: customParameters 会直接将参数添加到请求体顶层
                // reasoning_split: false - 思考内容包含在 content 中(默认行为)
                // reasoning_split: true - 思考内容分离到 reasoning_details 字段
                .customParameters(Map.of("reasoning_split", true))
                // .returnThinking(false)
                .build();

        return chatModel;
    }

    public interface WithdrawAgent {

        @SystemMessage("""
            你是一名银行职员，只能从用户账户中提取美元（USD）。
            """)
        @UserMessage("""
            从 {{user}} 的账户中扣除 {{amount}} 美元，并更新账户余额。
            """)
        @Agent("一位从账户中取出美元的银行家")
        String withdraw(@V("user") String user, @V("amount") Double amount);
    }

    public interface CreditAgent {
        @SystemMessage("""
            您是一名银行职员，只能将美元（USD）存入用户账户。
            """)
        @UserMessage("""
            将 {{amount}} 美元存入 {{user}} 的账户，并返回新的账户余额。
            """)
        @Agent("一位银行职员将美元存入客户的账户中")
        String credit(@V("user") String user, @V("amount") Double amount);
    }

    public interface ExchangeAgent {
        @UserMessage("""
            您是一名从事不同货币兑换工作的操作员。
            请使用此工具将 {{amount}} 的 {{originalCurrency}} 换算成 {{targetCurrency}}。
            仅返回工具提供的最终兑换金额，其他信息一并忽略。
            """)
        @Agent("一种货币兑换器，能够将一定金额的货币从原币转换为目标货币。")
        Double exchange(
                @V("originalCurrency") String originalCurrency,
                @V("amount") Double amount,
                @V("targetCurrency") String targetCurrency);
    }

    public static class ExchangeTool {

        @Tool("将给定金额从原币种兑换为目标币种")
        Double exchange(
                @P("originalCurrency") String originalCurrency,
                @P("amount") Double amount,
                @P("targetCurrency") String targetCurrency) {
            // Invoke a REST service to get the exchange rate
            return amount * 1.5;
        }
    }

    public static class BankTool {

        private final Map<String, Double> accounts = new HashMap<>();

        void createAccount(String user, Double initialBalance) {
            if (accounts.containsKey(user)) {
                throw new RuntimeException("Account for user " + user + " already exists");
            }
            accounts.put(user, initialBalance);
        }

        double getBalance(String user) {
            Double balance = accounts.get(user);
            if (balance == null) {
                throw new RuntimeException("No balance found for user " + user);
            }
            return balance;
        }

        @Tool("将给定的金额记入该用户账户，并返回新的余额")
        Double credit(@P("userName") String user, @P("amount") Double amount) {
            Double balance = accounts.get(user);
            if (balance == null) {
                throw new RuntimeException("No balance found for user " + user);
            }
            Double newBalance = balance + amount;
            accounts.put(user, newBalance);
            return newBalance;
        }

        @Tool("按照指定的用户信息提取指定金额，并返回新的余额。")
        Double withdraw(@P("userName") String user, @P("amount") Double amount) {
            Double balance = accounts.get(user);
            if (balance == null) {
                throw new RuntimeException("No balance found for user " + user);
            }
            Double newBalance = balance - amount;
            accounts.put(user, newBalance);
            return newBalance;
        }
    }
}
