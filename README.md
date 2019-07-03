## 简介
beehive 是一款轻量级的 RPC 框架，通过 spring 容器来管理 bean，做到了对用户代码的零入侵，同时通过 spi 拓展机制，实现了自己的 ioc 容器，使得 beehive 能够很方便的对组件进行拓展。

## 功能特性

* 实现了 SPI 拓展机制，能够方便的进行组件的自定义和替换

* 提供了两种代理方式的支持（JDK 原生、Javassist）

* 底层通信采用 Netty 框架，保证稳定性和高效性

* 对 Zookeeper 注册中心的支持，能够自动的侦测服务的状态，同步进行更新

* 完成了对与 fastjson 和 hessian 两种序列化器的支持

* 整合 spring 容器，对用户代码零入侵，使用方便

* 在客户端实现了两种负载均衡策略的支持（随机选取，轮训选取）

## 使用方式

### 基本使用

项目中提供了实例程序（位于 beehive-demo）模块下，通过 git clone 将工程拉取下来后，在根目录下输入如下指令进行安装。

> mvn clean install -Dmaven.test.skip=true

因为需要用到注册中心，所以实例程序中注册中心的地址是我的阿里云服务器地址，正常情况下我会启动 zookeeper 服务，那么示例程序就会将服务注册到我的阿里云服务器的 zookeeper 上，当然你也可以在本机启动一个 zookeeper，然后修改 spring 配置文件中的注册中心地址。

启动服务器，如果你没有修改实例程序的配置文件，默认使用我阿里云的 zookeeper，输入如下指令：

> java -jar beehive-demo/provider/target/provider-1.0-SNAPSHOT-jar-with-dependencies.jar

启动客户端，没有修改代码的情况下，会从注册中心获取服务信息，输入如下指令：

> java -jar beehive-demo/consumer/target/consumer-1.0-SNAPSHOT-jar-with-dependencies.jar

### 服务切换的测试
示例程序中提供了两个服务端程序，表示两个服务提供者，测试服务切换需要同时启动这两个程序，指令如下：

> java -cp beehive-demo/provider/target/provider-1.0-SNAPSHOT-jar-with-dependencies.jar top.aprilyolies.provider.Provider

> java -cp beehive-demo/provider/target/provider-1.0-SNAPSHOT-jar-with-dependencies.jar top.aprilyolies.provider.AnotherProvider

启动客户端，没有修改代码的情况下，会从注册中心获取服务信息，输入如下指令：

> java -jar beehive-demo/consumer/target/consumer-1.0-SNAPSHOT-jar-with-dependencies.jar

实例程序默认是使用的轮询负载均衡机制，如果过程没错的话，那么你将会看到客户端会交替的从两个 provider 进行 rpc 调用。

尝试关掉其中一个 provider，客户端会侦测到这个变化，随即将这个下线的 provider 剔除，仅仅从剩下的 provider 中进行 rpc 调用。

再尝试重启这个 provider，客户端也会侦测到这个变化，随即将这个 provider 加入到可调用的 providers 列表中，进而进行 rpc 调用。

## TODO-LIST
* 底层通信框架的支持有待完善，比如 Mina（我没接触过）

* 缺少一个服务的管理模块，框架当前提供了相应的 filter 接口，通过实现该接口即可对 rpc 请求进行拦截，由此来获取部分监控信息

* SPI 拓展机制没有完成相应的组件筛选功能，比如 filter 接口实现类，框架没有提供基础的选择性获取方式

* 异步消息处理的线程池的构建应该更加灵活，相应的拒绝执行策略有待完善

* 负载均衡策略缺陷严重，应该需要根据实际的 provider 负载情况来动态的调整