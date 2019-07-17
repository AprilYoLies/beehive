## 简介
beehive 是一款轻量级的 RPC 框架，通过 spring 容器来管理 bean，做到了对用户代码的零入侵，同时通过 spi 拓展机制，实现了自己的 ioc 容器，使得 beehive 能够很方便的对组件进行拓展。

## 功能特性

* 实现了 SPI 拓展机制，能够方便的进行组件的自定义和替换

* 提供了两种代理方式的支持（JDK 原生、Javassist）

* 底层通信采用 Netty 框架，保证稳定性和高效性

* 对 Zookeeper 注册中心的支持，能够自动的侦测服务的状态，同步进行更新

* 完成了对与 fastjson 和 hessian 两种序列化器的支持

* 整合 spring 容器，对用户代码零入侵，使用方便

* 在客户端实现了两种负载均衡策略的支持（随机选取，轮询选取）

## 使用方式

beehive 加入了对于 spring 容器的支持，使得它在使用过程中可以做到对用户代码的零入侵，使用方式和 dubbo 很类似，在服务端，只需要定义要发布的服务的接口类型，服务的实现类，以及注册中心的地址即可完成启动，其他的一些相关参数可以作为备选项，这里给出一个使用样例。

> provider.xml

``` java
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:beehive="https://www.aprilyolies.top/schema/beehive"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        https://www.aprilyolies.top/schema/beehive https://www.aprilyolies.top/schema/beehive.xsd">

    <bean id="demoServiceImpl" class="top.aprilyolies.service.BeehiveServiceImpl"/>

    <bean id="userServiceImpl" class="top.aprilyolies.service.UserServiceImpl"/>

    <beehive:service id="demoService" service="top.aprilyolies.service.BeehiveService"
                     ref="demoServiceImpl" proxy-factory="jdk" serializer="hessian" server-port="7442"/>

    <beehive:service id="userService" service="top.aprilyolies.service.UserService"
                     ref="userServiceImpl" proxy-factory="javassist" serializer="hessian" server-port="7442"/>

    <beehive:registry id="registry" address="zookeeper://119.23.247.86:2181"/>
</beans>
```

这是服务端的 spring 配置文件，其中 id="demoServiceImpl" 和 id="userServiceImpl" 的 bean 就是服务的实现类，而 <beehive:service/> 标签中的 service="top.aprilyolies.service.BeehiveService" 就是待发布服务的接口类型，另外一个 <beehive:service/> 类似，最后 <beehive:registry/> 标签中定义的就是注册中心地址，目前仅支持 zookeeer。

说完了配置文件，再看看启动程序，很简单，就是一个典型的 spring 容器启动程序如下，不做过多说明。

``` java
public static void main(String[] args) throws IOException {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("provider.xml");
    context.start();
    System.out.println("Provider started on thread " + Thread.currentThread().getName() + "..");
    System.in.read();
}
```

启动服务端程序后就是客户端程序了，要做的配置也非常简单，仅仅是通过 beehive 引入服务即可，具体的 rpc 过程对于用户来说是绝对透明的，内容如下：

``` java
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:beehive="https://www.aprilyolies.top/schema/beehive"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        https://www.aprilyolies.top/schema/beehive https://www.aprilyolies.top/schema/beehive.xsd">

    <beehive:registry id="registry" address="zookeeper://119.23.247.86:2181"/>

    <beehive:reference id="demoService" service="top.aprilyolies.service.BeehiveService" load-balance="poll"
                       serializer="hessian" read-timeout="1000" retry-times="2"/>

    <beehive:reference id="userService" service="top.aprilyolies.service.UserService" load-balance="poll"
                       serializer="hessian" read-timeout="1000" retry-times="2"/>
</beans>
```

基本跟服务端的配置相似，注册中心的配置是一样的，只有引用服务的标签变成为 <beehive:reference/>，该标签的属性设置也跟服务端的服务发布标签有所不同，关于详细的属性说明请看下文。

客户端启动程序服务端一样简单，直接启动程序，查看输出结果即可，不做过多说明。

``` java
public class Consumer {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("consumer.xml");
        context.start();
        BeehiveService demoService = context.getBean("demoService", BeehiveService.class);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 50000; i++) {
            String hello = demoService.say("world - " + i);
            System.out.println("result: " + hello);
            Thread.sleep(500);
        }
        System.out.println(System.currentTimeMillis() - start);
    }
}
```

## 参数设置说明

### \<beehive:service/\> 标签

* id：为 bean 在 spring 容器中的唯一标识符

* service：将要发布的服务，值为你想要发布的服务的全限定名

* ref：你所发布服务的实现类，它的值为 spring <bean/> 标签的 id 值

* proxy-factory：服务提供端代理类创建的方式，目前支持 javassist 和 jdk 可选

* serializer：序列化器，默认为 fastjson，可选 hessian

* sever-port：服务启动的端口号，默认使用 7440，你也可以再启动程序时通过 -Dport=端口号 来指定，优先级最高

### \<beehive:reference/\> 标签

* id：为 bean 在 spring 容器中的唯一标识符

* service：将要发布的服务，值为你想要发布的服务的全限定名

* load-balance：负载均衡设置，由客户端实现，目前只支持 random 和 poll 两种方式

* proxy-factory：服务提供端代理类创建的方式，目前支持 javassist 和 jdk 可选

* serializer：序列化器，默认为 fastjson，可选 hessian

* read-timeout：指定 rpc 结果读取超时时间，如果本次结果获取失败，将会重试

* retry-times：指定重试次数，即 rpc 结果获取超时重试次数

### \<beehive:registry/\> 标签

* id：为 bean 在 spring 容器中的唯一标识符

* address：注册中心的地址，目前只支持 zookeeper，格式如 “zookeeper://host:port”

## 样例测试

### 基本测试

项目中提供了实例程序（位于 beehive-demo）模块下，通过 git clone 将工程拉取下来后，在根目录下输入如下指令进行安装。

> mvn clean install -Dmaven.test.skip=true

因为需要用到注册中心，所以实例程序中注册中心的地址是我的阿里云服务器地址，正常情况下我会启动 zookeeper 服务，那么示例程序就会将服务注册到我的阿里云服务器的 zookeeper 上，当然你也可以在本机启动一个 zookeeper，然后修改 spring 配置文件中的注册中心地址。

启动服务器，如果你没有修改实例程序的配置文件，默认使用我阿里云的 zookeeper，输入如下指令：

> java -jar beehive-demo/provider/target/provider-1.0-SNAPSHOT-jar-with-dependencies.jar

启动客户端，没有修改代码的情况下，会从注册中心获取服务信息，输入如下指令：

> java -jar beehive-demo/consumer/target/consumer-1.0-SNAPSHOT-jar-with-dependencies.jar

或者启动多线程的 consumer，输入如下指令：

> java -cp beehive-demo/consumer/target/consumer-1.0-SNAPSHOT-jar-with-dependencies.jar top.aprilyolies.consumer.MultiThreadConsumer

### 服务切换的测试
示例程序中提供了两个服务端程序，表示两个服务提供者，测试服务切换需要同时启动这两个程序，指令如下：

> java -cp beehive-demo/provider/target/provider-1.0-SNAPSHOT-jar-with-dependencies.jar top.aprilyolies.provider.Provider

> java -cp beehive-demo/provider/target/provider-1.0-SNAPSHOT-jar-with-dependencies.jar top.aprilyolies.provider.AnotherProvider

启动客户端，没有修改代码的情况下，会从注册中心获取服务信息，输入如下指令：

> java -jar beehive-demo/consumer/target/consumer-1.0-SNAPSHOT-jar-with-dependencies.jar

或者启动多线程的 consumer，输入如下指令：

> java -cp beehive-demo/consumer/target/consumer-1.0-SNAPSHOT-jar-with-dependencies.jar top.aprilyolies.consumer.MultiThreadConsumer

实例程序默认是使用的轮询负载均衡机制，如果过程没错的话，那么你将会看到客户端会交替的从两个 provider 进行 rpc 调用。

尝试关掉其中一个 provider，客户端会侦测到这个变化，随即将这个下线的 provider 剔除，仅仅从剩下的 provider 中进行 rpc 调用。

再尝试重启这个 provider，客户端也会侦测到这个变化，随即将这个 provider 加入到可调用的 providers 列表中，进而进行 rpc 调用。

## TODO-LIST
* 底层通信框架的支持有待完善，比如 Mina（我没接触过）

* 缺少一个服务的管理模块，框架当前提供了相应的 filter 接口，通过实现该接口即可对 rpc 请求进行拦截，由此来获取部分监控信息

* SPI 拓展机制没有完成相应的组件筛选功能，比如 filter 接口实现类，框架没有提供基础的选择性获取方式

* 异步消息处理的线程池的构建应该更加灵活，相应的拒绝执行策略有待完善

* 负载均衡策略缺陷严重，应该需要根据实际的 provider 负载情况来动态的调整