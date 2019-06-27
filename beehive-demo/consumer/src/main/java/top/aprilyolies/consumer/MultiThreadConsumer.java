package top.aprilyolies.consumer;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import top.aprilyolies.service.BeehiveService;

/**
 * @Author EvaJohnson
 * @Date 2019-06-23
 * @Email g863821569@gmail.com
 */
public class MultiThreadConsumer {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("consumer.xml");
        context.start();
        BeehiveService demoService = context.getBean("demoService", BeehiveService.class);
//        ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
//            int id = 0;
//
//            @Override
//            public Thread newThread(Runnable r) {
//                Thread thread = new Thread(r);
//                thread.setName("MultiThreadConsumer-pool-thread-" + id++);
//                return thread;
//            }
//        });
//        for (int i = 0; i < 10; i++) {
//            executor.submit(() -> {
//                try {
//                    for (int i1 = 0; i1 < 1000; i1++) {
//                        BeehiveService service = context.getBean("demoService", BeehiveService.class);
//                        String hello = service.say(Thread.currentThread().getName() + " - " + i1);
//                        System.out.println("result: " + hello);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        }
        for (int i = 0; i < 1000; i++) {
            String hello = demoService.say("world - " + i);
            System.out.println("result: " + hello);
        }
    }
}
