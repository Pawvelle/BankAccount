# 银行账户管理系统 (Bank Account Management System)

> **项目类型**：面向对象编程 (OOP) 综合实践项目 / 课程设计作业  
> **开发语言**：Java (JDK 8+)  
> **运行环境**：Command Line Interface (CLI)

---

## 1. 项目简介 (Project Overview)

本项目是一个基于纯 Java 编写的控制台交互式银行账户管理系统。项目旨在通过模拟真实世界的银行业务逻辑，深度实践并展示 Java 面向对象编程的核心理念（如**封装**、**继承**、**多态**、**抽象**），以及**接口编程**和**Java IO 流数据持久化**技术。

系统支持多用户注册与管理、多账户体系（储蓄卡与信用卡并存）、基础金融交易（存取款、在线支付、信用还款），并配备了完善的输入校验与异常处理机制，保证了系统的高内聚、低耦合与健壮性。

---

## 2. 系统架构与设计 (System Architecture)

经过工程化重构，本项目严格遵循标准的 Java 包管理规范，采用经典的 **MVC 变体**分层架构。

### 2.1 目录结构

```text
src/main/java/com/bank/
├── model/        # 数据模型层 (Data Models)
│   ├── BankAccount.java     (银行账户抽象基类)
│   ├── BankUser.java        (系统用户实体类)
│   ├── CreditAccount.java   (信用卡账户，继承 BankAccount)
│   └── SavingsAccount.java  (储蓄卡账户，继承 BankAccount)
├── service/      # 业务逻辑层 (Business Services)
│   └── BankUserManager.java (用户与数据管理器)
├── interfaces/   # 抽象接口层 (Interfaces)
│   ├── CurrencyConvertible.java (多币种转换能力接口)
│   └── OnlinePayable.java       (在线支付能力接口)
└── ui/           # 用户交互层 (User Interface)
    └── Main.java            (CLI 交互主入口)
```

### 2.2 核心设计思想 (Design Principles)
- **封装性 (Encapsulation)**：所有的实体类属性均声明为 `private`，通过严格校验的 `getter/setter` 或业务方法（如 `withdraw`，`deposit`）对外提供访问，确保资金和密码等敏感数据的安全性。
- **继承与多态 (Inheritance & Polymorphism)**：`BankAccount` 作为抽象基类（Abstract Class），将 `withdraw` 定义为抽象方法，由其子类 `SavingsAccount` 和 `CreditAccount` 根据各自的业务规则（如余额检查、透支逻辑）进行重写，从而实现多态调用。
- **接口分离原则 (Interface Segregation)**：通过抽取 `OnlinePayable` 和 `CurrencyConvertible` 接口，赋予信用卡特定的扩展能力，使得代码结构更具扩展性。

---

## 3. 核心功能特性 (Key Features)

1. **安全的用户鉴权体系**
   - 支持用户的注册、登录。
   - 包含严格的输入校验（如 6 位纯数字密码校验、正则验证邮箱/手机号格式）。
   - 用户唯一标识（USER_ID）全局自增防重。

2. **多元化的账户管理**
   - **储蓄卡 (Savings Account)**：支持存款、取款，提供基于固定利率的计息（Apply Interest）功能，并限制取款后的最低余额。
   - **信用卡 (Credit Account)**：引入信用额度（Credit Limit）概念，支持信用透支取款、还款功能，同时接入在线支付通道和美元汇率转换计算。

3. **稳定可靠的数据持久化 (Data Persistence)**
   - 抛弃了传统内存运行时存储的易失性，系统接入了本地持久化机制。
   - 依赖 Java 原生序列化流 (`ObjectOutputStream` / `ObjectInputStream`)。
   - 数据文件统一存放在 `data/bank_data.dat` 中。系统自动在每次核心状态变更（如交易发生、修改资料）以及退出时进行落盘，确保用户资产数据 0 丢失。

4. **数据统计与排行**
   - 系统支持纵向计算单个用户名下所有绑定的实体卡片余额，并根据总资产进行全站用户的财富排行榜打印。

---

## 4. 运行指南 (Run Instructions)

本项目无任何第三方依赖（如 Maven 或 Gradle 等外置框架），仅需基础的 Java 编译环境即可运行。

### 4.1 编译项目 (Compile)
在项目根目录（即 `BankAccount` 文件夹下）打开终端，执行以下命令将源码编译到 `bin` 目录：
```bash
javac -d bin -sourcepath src/main/java src/main/java/com/bank/ui/Main.java
```

### 4.2 运行系统 (Run)
编译完成后，执行以下命令启动交互式命令行终端：
```bash
java -cp bin com.bank.ui.Main
```

### 4.3 故障排除
如果在 IDE（如 VSCode）中遇到包名报错提示，请确保 IDE 的源文件目录 (Source Path) 已正确指向 `src/main/java` 而非 `src`。

---

## 5. 项目总结 (Conclusion)

通过本项目的开发，深刻体会到了 OOP 面向对象思想在解耦复杂业务场景时的巨大威力。抽象类的使用完美地复用了公共代码，接口的引入则保证了程序极高的扩展能力；同时，文件 IO 流的实战应用，为系统注入了真正的实用价值。后续可考虑引入真正的关系型数据库（如 MySQL）进行数据托管以支持并发处理。