// package com.kaige.langchain4j.spring.controller;
//
// import dev.langchain4j.data.message.UserMessage;
// import dev.langchain4j.model.StreamingResponseHandler;
// import dev.langchain4j.model.chat.StreamingChatModel;
// import dev.langchain4j.model.output.Response;
// import org.springframework.http.MediaType;
// import org.springframework.web.bind.annotation.*;
// import reactor.core.publisher.Flux;
//
// import java.io.IOException;
// import java.io.PipedInputStream;
// import java.io.PipedOutputStream;
// import java.nio.charset.StandardCharsets;
//
// /**
//  * LangChain4j Stream模式Spring Boot控制器示例
//  *
//  * 演示如何在Spring Boot应用中实现流式AI响应。
//  * 提供两种流式处理方式的REST API端点。
//  *
//  * <p><strong>API端点：</strong></p>
//  * <ul>
//  *   <li>GET /stream/callback - Callback方式流式响应</li>
//  *   <li>GET /stream/reactive - Reactive方式流式响应</li>
//  *   <li>POST /stream/chat - POST方式的流式聊天</li>
//  * </ul>
//  *
//  * <p><strong>使用场景：</strong></p>
//  * <ul>
//  *   <li>实时聊天应用</li>
//  *   <li>长文本生成展示</li>
//  *   <li>需要即时反馈的AI应用</li>
//  * </ul>
//  *
//  * @author kaige
//  * @version 1.0
//  * @since 2026-02-14
//  */
// @RestController
// @RequestMapping("/stream")
// public class StreamModeController {
//
//     private final StreamingChatModel streamingChatModel;
//
//     public StreamModeController(StreamingChatModel streamingChatModel) {
//         this.streamingChatModel = streamingChatModel;
//     }
//
//     /**
//      * Callback方式的流式响应
//      *
//      * 使用Server-Sent Events (SSE) 格式返回流式数据
//      *
//      * @param message 用户输入消息
//      * @return Flux<String> 流式响应
//      */
//     @GetMapping(value = "/callback", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//     public Flux<String> streamWithCallback(@RequestParam(defaultValue = "你好") String message) {
//         return Flux.create(sink -> {
//             streamingChatModel.generate(
//                 UserMessage.from(message),
//                 new StreamingResponseHandler<dev.langchain4j.data.message.AiMessage>() {
//                     @Override
//                     public void onNext(String token) {
//                         // 发送每个token作为SSE事件
//                         sink.next("data: " + token.replace("\n", "\\n") + "\n\n");
//                     }
//
//                     @Override
//                     public void onComplete(Response<dev.langchain4j.data.message.AiMessage> response) {
//                         // 发送完成信号
//                         sink.next("data: [DONE]\n\n");
//                         sink.complete();
//                     }
//
//                     @Override
//                     public void onError(Throwable error) {
//                         sink.error(error);
//                     }
//                 }
//             );
//         });
//     }
//
//     /**
//      * Reactive方式的流式响应
//      *
//      * 直接使用Flux处理流式响应
//      *
//      * @param message 用户输入消息
//      * @return Flux<String> 流式响应
//      */
//     @GetMapping(value = "/reactive", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//     public Flux<String> streamWithReactive(@RequestParam(defaultValue = "你好") String message) {
//         return streamingChatModel
//             .generate(UserMessage.from(message))
//             .map(token -> "data: " + token.replace("\n", "\\n") + "\n\n")
//             .concatWith(Flux.just("data: [DONE]\n\n"));
//     }
//
//     /**
//      * POST方式的流式聊天
//      *
//      * 支持更复杂的消息格式和上下文传递
//      *
//      * @param chatRequest 聊天请求对象
//      * @return Flux<String> 流式响应
//      */
//     @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//     public Flux<String> streamChat(@RequestBody ChatRequest chatRequest) {
//         return streamingChatModel
//             .generate(UserMessage.from(chatRequest.getMessage()))
//             .map(token -> "data: " + token.replace("\n", "\\n") + "\n\n")
//             .concatWith(Flux.just("data: [DONE]\n\n"));
//     }
//
//     /**
//      * 使用Piped Stream的传统方式（适用于不支持Reactive的场景）
//      *
//      * @param message 用户输入消息
//      * @return Flux<String> 流式响应
//      */
//     @GetMapping(value = "/traditional", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//     public Flux<String> streamTraditional(@RequestParam(defaultValue = "你好") String message) {
//         try {
//             PipedOutputStream outputStream = new PipedOutputStream();
//             PipedInputStream inputStream = new PipedInputStream(outputStream);
//
//             // 在后台线程中处理流式响应
//             Thread streamingThread = new Thread(() -> {
//                 try {
//                     streamingChatModel.generate(
//                         UserMessage.from(message),
//                         new StreamingResponseHandler<dev.langchain4j.data.message.AiMessage>() {
//                             @Override
//                             public void onNext(String token) {
//                                 outputStream.write(("data: " + token.replace("\n", "\\n") + "\n\n")
//                                     .getBytes(StandardCharsets.UTF_8));
//                                 outputStream.flush();
//                             }
//
//                             @Override
//                             public void onComplete(Response<dev.langchain4j.data.message.AiMessage> response) {
//                                 try {
//                                     outputStream.write("data: [DONE]\n\n".getBytes(StandardCharsets.UTF_8));
//                                     outputStream.close();
//                                 } catch (IOException e) {
//                                     e.printStackTrace();
//                                 }
//                             }
//
//                             @Override
//                             public void onError(Throwable error) {
//                                 try {
//                                     outputStream.close();
//                                 } catch (IOException e) {
//                                     e.printStackTrace();
//                                 }
//                             }
//                         }
//                     );
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
//             });
//
//             streamingThread.start();
//
//             // 返回从PipedInputStream读取的数据流
//             return Flux.generate(sink -> {
//                 try {
//                     byte[] buffer = new byte[1024];
//                     int bytesRead = inputStream.read(buffer);
//                     if (bytesRead != -1) {
//                         String data = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
//                         sink.next(data);
//                     } else {
//                         sink.complete();
//                     }
//                 } catch (IOException e) {
//                     sink.error(e);
//                 }
//             });
//
//         } catch (IOException e) {
//             return Flux.error(e);
//         }
//     }
//
//     /**
//      * 聊天请求数据传输对象
//      */
//     public static class ChatRequest {
//         private String message;
//         private String context;
//
//         // 构造函数
//         public ChatRequest() {}
//
//         public ChatRequest(String message) {
//             this.message = message;
//         }
//
//         // Getters and Setters
//         public String getMessage() {
//             return message;
//         }
//
//         public void setMessage(String message) {
//             this.message = message;
//         }
//
//         public String getContext() {
//             return context;
//         }
//
//         public void setContext(String context) {
//             this.context = context;
//         }
//     }
// }