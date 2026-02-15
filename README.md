<!-- TOC -->
* [java-clean-finance](#java-clean-finance)
  * [Project Overview](#project-overview)
* [Requirements](#requirements)
* [Architectural Philosophy](#architectural-philosophy)
* [Design](#design)
  * [Core Design Patterns](#core-design-patterns)
    * [1. Command Pattern: Decoupling & Transactional Integrity](#1-command-pattern-decoupling--transactional-integrity)
    * [2. Strategy Pattern: Dynamic Business Rules](#2-strategy-pattern-dynamic-business-rules)
    * [3. Visitor Pattern: Separation of Concerns in Hierarchies](#3-visitor-pattern-separation-of-concerns-in-hierarchies)
    * [4. Composite Pattern: Category Management](#4-composite-pattern-category-management)
* [Diagrams](#diagrams)
  * [Domain Class Architecture](#domain-class-architecture)
  * [Behavioral Logic: Transaction Execution](#behavioral-logic-transaction-execution)
  * [Command Implementation Detail](#command-implementation-detail)
  * [Transactional Integrity: Transfer Workflow](#transactional-integrity-transfer-workflow)
  * [Complete Overview](#complete-overview)
* [Development Status & Roadmap](#development-status--roadmap)
* [Note](#note)
<!-- TOC -->

# java-clean-finance
A reference implementation of a Personal Finance System, engineered with a strict adherence to Clean Architecture and Domain-Driven Design (DDD) principles.
This project serves as a showcase for decoupling business logic from infrastructure, ensuring maintainability, testability, and architectural evolution.

## Project Overview
The objective of java-clean-finance is to manage financial lifecycles across heterogeneous wallet types and transaction modalities.
Unlike traditional CRUD applications, this system enforces complex domain invariants and business rules at the core level, remaining entirely agnostic of the persistence layer or UI framework.

# Requirements
The software must **handle different wallet** *(CREDITCARD, DEBITCARD, CHECKINGACCOUNT, ...)*

The software must **handle different transactions** *(DEPOSIT, WITHDRAWAL and TRANSFER)*

The software must **execute only allowed operation**: for example, a DEBITCARD can't have a negative amount of money

Each transaction must **have a category**, each category can have **subcategory**.

The software, due to internal need, must **allow connection and data saving to different type of Database**.

The software must **allow undo/redo operations**.

Here is given an example of category and subcategory:

|  Category  | Subcategory  |
|:----------:|:------------:|
| University |    Taxes     |
|            |    Books     |
| Transport  |     Bus      |
|            |     Fuel     |
|            |     RCA      |
|    Food    | Lunch&Dinner |
|            | Supermarket  |
|  Fitness   |    Sport     |
|            |     Gym      |
|            |     Pool     |

# Architectural Philosophy
This system is built upon the **Hexagonal (Ports & Adapters)** pattern.
The dependency rule is absolute: dependencies only point inwards toward the Domain Layer.

* **Domain Layer**: Contains Entities (Wallet, Transaction), Value Objects (Money) and Domain Exceptions.
* **Application Layer**: Orchestrates the flow of data via the Command Pattern and defines Interfaces (Ports) for external communication.
* **Infrastructure Layer**: Implements the Ports (Adapters) for different databases and external frameworks.


# Design
To address the rigorous requirements of financial software, the following GoF (Gang of Four) patterns were strategically implemented:

|  Pattern  |  Category  |                           Motivation                            |
|:---------:|:----------:|:---------------------------------------------------------------:|
|  Command  | Behavioral |      To enable Undo/Redo actions and decouple operations.       |
| Strategy  | Behavioral | To dynamically apply different business constraints to wallets. |
|  Visitor  | Behavioral | To separate algorithms from the object structures they work on. |
|  Factory  | Creational |   To standardize the creation of wallets with specific rules.   |
|  Builder  | Creational |   To handle the complex construction of transaction entities.   |
| Composite | Structural |              To handle Category and Subcategories               |

A critical design decision was the implementation of the Money Value Object.
By using BigDecimal with explicit rounding modes and currency validation, the system eliminates the precision errors and "currency-mixing" bugs common in financial systems using floating-point primitives.

## Core Design Patterns

### 1. Command Pattern: Decoupling & Transactional Integrity
The **Command Pattern** is the backbone of the application's operations. Beyond simple execution, it provides:
* **Undo/Redo Functionality:** By encapsulating operations (e.g., `AddTransactionCommand`, `MakeTransferCommand`) into objects, the system maintains an execution history for seamless state reversal.
* **Atomicity:** Complex operations like transfers between wallets are handled within a single command context, ensuring that a failure in one step (e.g., deposit) triggers a rollback or prevents the initial step (e.g., withdrawal), maintaining financial consistency.

### 2. Strategy Pattern: Dynamic Business Rules
Wallet validation logic (e.g., checking for insufficient funds or withdrawal limits) is delegated to an interchangeable family of algorithms via the **Strategy Pattern**.
* **Compliance:** Different wallet types (Debit vs. Credit) can have different `IRuleStrategy` implementations.
* **Open/Closed Principle:** New financial constraints can be added at runtime by injecting new strategies without modifying the existing `Wallet` entity code.

### 3. Visitor Pattern: Separation of Concerns in Hierarchies
The system utilizes the **Visitor Pattern** to navigate the complex tree structures of `Category` and `Wallet` objects.
* **Extensibility:** It allows adding new operations—such as generating financial reports, exporting data to JSON without polluting the domain model with infrastructure-specific logic.

### 4. Composite Pattern: Category Management
The hierarchical relationship between **Categories** and **Subcategories** is managed through the **Composite Pattern**. This treats individual categories and groups of categories uniformly, enabling infinite nesting as required by the user's personal finance organization.

# Diagrams

## Domain Class Architecture
The following diagram illustrates the structural relationships between core entities and the strategy-based rule engine.

```mermaid
classDiagram
%% =========================================================
%% 1. CORE DOMAIN (Models & Values)
%% =========================================================

    class Money {
        <<ValueObject>>
        -BigDecimal amount
        -String currency
        +of(amount, currency) Money$
        +zero(currency) Money$
        ~add(Money) Money
        ~subtract(Money) Money
        +isPositive() boolean
        +isNegative() boolean
    }

    class Wallet {
        <<Entity>>
        -UUID id
        -String name
        -WalletType type
        -Money balance
        -Collection~IRuleStrategy~ ruleStrategies
        -Collection~Transaction~ transactions
        -deposit(Money amount)
        -withdraw(Money amount)
        +addTransaction(Transaction)
        +removeTransaction(Transaction)
        +transferWithdraw(Transaction)
        +transferDeposit(Transaction)
        +rollbackDeposit(Transaction)
        +rollbackWithdraw(Transaction)
        +accept(IVisitor)
    }

    class Transaction {
        <<Entity>>
        -UUID id
        -Money money
        -TransactionType type
        -Category category
        -LocalDateTime date
        -String note
        +accept(IVisitor)
    }

    class Category {
        <<Entity>>
        -UUID id
        -String name
        -Category parent
        -Collection~Category~ children
        +add(Category)
        +remove(Category)
        +accept(IVisitor)
    }

    class WalletType {
        <<Enumeration>>
        DEBITCARD
        CREDITCARD
        CHECKINGACCOUNT
    }

    class TransactionType {
        <<Enumeration>>
        DEPOSIT
        WITHDRAW
        TRANSFER
    }

%% Relazioni Core
    Wallet --> Money
    Wallet o-- Transaction : contains
    Wallet --> WalletType
    Wallet ..|> IVisitable

    Transaction *-- Money
    Transaction --> TransactionType
    Transaction --> Category
    Transaction ..|> IVisitable

    Category o-- Category : parent/children
    Category ..|> IVisitable

%% =========================================================
%% 2. STRATEGY PATTERN (Business Rules)
%% =========================================================

    class IRuleStrategy {
        <<Interface>>
        +check(Wallet w, Transaction t)
    }

    Wallet o-- IRuleStrategy

%% =========================================================
%% 3. FACTORY & BUILDER
%% =========================================================

    class WalletFactory {
        +create(name, WalletType, initialBalance) Wallet$
    }

    class TransactionBuilder {
        -UUID id;
        -Money amount
        -TransactionType type
        -Category category
        -String note
        -LocalDateTime date;
        +withCategory(Category)
        +withNote(String)
        +withDate(LocalDateTime)
        +build() Transaction
    }

    WalletFactory ..> Wallet : creates
    TransactionBuilder ..> Transaction : builds

%% =========================================================
%% 4. APPLICATION LAYER (Manager & Commands)
%% =========================================================

    class FinanceManager {
        -IWalletRepository walletRepository
        -ICategoryRepository categoryRepository
        -WalletFactory walletFactory
        +addWallet(Wallet)
        +removeWallet(UUID)
        +addCategory(Category)
        +removeCategory(Category)
        +getWallet(UUID)
    }

    class CommandInvoker {
        -Stack~ICommand~ commandHistory
        -Stack~ICommand~ redoStack
        +createWallet(FinanceManager, String name, WalletType, Money)
        +addTransaction(Wallet, Transaction, Money, TransactionType, Category, LocalDateTime, String note)
        +transfer(Wallet from, Wallet to, Transaction, Money, Category, LocalDateTime, String note)
        +createCategory(FinanceManager, Category)
        +undo()
        +redo()
    }

    class ICommand {
        <<Interface>>
        +execute()
        +undo()
    }

%% Relazioni Command
    CommandInvoker o-- ICommand


%% =========================================================
%% 5. PORTS & ADAPTERS (Repository & Persistence)
%% =========================================================

    class IWalletRepository {
        <<Interface>>
        +save(Wallet)
        +findById(UUID)
        +removeWallet(UUID)
        +update(Wallet)
    }

    class ICategoryRepository {
        <<Interface>>
        +save(Category)
        +delete(Category)
        +findById(UUID)
        +findByName(String)
    }

    FinanceManager --> IWalletRepository
    FinanceManager --> ICategoryRepository
    FinanceManager --> WalletFactory

%% =========================================================
%% 6. VISITOR PATTERN
%% =========================================================

    class IVisitor {
        <<Interface>>
        +visit(Wallet)
        +visit(Transaction)
        +visit(Category)
    }

    class IVisitable {
        <<Interface>>
        +accept(IVisitor)
    }
```

## Behavioral Logic: Transaction Execution

The sequence diagram below details the lifecycle of a transaction command, from user input through validation strategies and persistence update. 

```mermaid
sequenceDiagram
    autonumber

    actor User as Utente
    participant Invoker as CommandInvoker
    participant Cmd as AddTransactionCommand
    participant Builder as TransactionBuilder
    participant Wallet as Wallet (Entity)
    participant Repo as IWalletRepository

    User->>Invoker: addTransaction(wallet, amount, ...)
    activate Invoker

    Invoker->>Cmd: new AddTransactionCommand(wallet, inputs)
    activate Cmd

    Cmd->>Builder: new TransactionBuilder(...)
    activate Builder
    Builder-->>Cmd: build() -> Transaction
    deactivate Builder

    Invoker->>Cmd: execute()

    Cmd->>Wallet: addTransaction(transaction)
    activate Wallet
    Note over Wallet: Validation Strategy and balance update

    deactivate Wallet

    Cmd->>Repo: update(wallet)
    activate Repo
    Repo-->>Cmd: void
    deactivate Repo

    Cmd-->>Invoker: void
    deactivate Cmd

    Invoker->>Invoker: history.push(command)
    Invoker-->>User: "Operation completed"
    deactivate Invoker
```

## Command Implementation Detail

```mermaid
classDiagram
    class Wallet {
        <<Entity>>
        -UUID id
        -String name
        -WalletType type
        -Money balance
        -Collection~IRuleStrategy~ ruleStrategies
        -Collection~Transaction~ transactions
        -deposit(Money amount)
        -withdraw(Money amount)
        +addTransaction(Transaction)
        +removeTransaction(Transaction)
        +transferWithdraw(Transaction)
        +transferDeposit(Transaction)
        +rollbackDeposit(Transaction)
        +rollbackWithdraw(Transaction)
        +accept(IVisitor)
    }

    class FinanceManager {
        -IWalletRepository walletRepository
        -ICategoryRepository categoryRepository
        -WalletFactory walletFactory
        +addWallet(Wallet)
        +removeWallet(UUID)
        +addCategory(Category)
        +removeCategory(Category)
        +getWallet(UUID)
    }

    class CommandInvoker {
        -Stack~ICommand~ commandHistory
        -Stack~ICommand~ redoStack
        +createWallet(FinanceManager, String name, WalletType, Money)
        +addTransaction(Wallet, Transaction, Money, TransactionType, Category, LocalDateTime, String note)
        +transfer(Wallet from, Wallet to, Transaction, Money, Category, LocalDateTime, String note)
        +createCategory(FinanceManager, Category)
        +undo()
        +redo()
    }

    class ICommand {
        <<Interface>>
        +execute()
        +undo()
    }

    class AddTransactionCommand {
        -Wallet wallet
        -Transaction transaction
        +execute()
        +undo()
    }

    class MakeTransferCommand {
        -Wallet walletForWithdraw
        -Wallet walletForDeposit
        -Transaction transactionWithdraw
        -Transaction transactionDeposit
        +execute()
        +undo()
    }

    class NewWalletCommand {
        -FinanceManager financeManager
        -Wallet wallet
        +execute()
        +undo()
    }

    class NewCategoryCommand {
        -FinanceManager financeManager
        -Category category
        +execute()
        +undo()
    }

%% Relazioni Command
    CommandInvoker o-- ICommand
    AddTransactionCommand ..|> ICommand
    MakeTransferCommand ..|> ICommand
    NewWalletCommand ..|> ICommand
    NewCategoryCommand ..|> ICommand

%% Command Dependencies (Receivers)
    AddTransactionCommand --> Wallet : receiver
    MakeTransferCommand --> Wallet : receiver
    NewWalletCommand --> FinanceManager : receiver
    NewCategoryCommand --> FinanceManager : receiver
```

## Transactional Integrity: Transfer Workflow

This flowchart illustrates the internal rollback mechanism ensuring atomicity during wallet transfers.

```mermaid
flowchart TD
    Start([Start]) --> Withdraw[Execute: walletForWithdraw.transferWithdraw]

    Withdraw --> CheckWithdraw{Exception Thrown?}

    CheckWithdraw -- Yes --> EndFail([End: Exception / Fail])

    CheckWithdraw -- No --> TryDeposit[Try: walletForDeposit.transferDeposit]

    TryDeposit --> CheckDeposit{Exception Thrown?}

    CheckDeposit -- No --> EndSuccess([End: Success])

    CheckDeposit -- Yes --> CatchBlock[Catch Exception]

    CatchBlock --> Rollback[Execute: walletForWithdraw.rollbackTransferWithdraw]

    Rollback --> ThrowRuntime[Throw RuntimeException]

    ThrowRuntime --> EndFail
```
## Complete Overview
Last but not least, here is the comprehensive project structure (detailed view):

```mermaid
classDiagram
%% =========================================================
%% 1. CORE DOMAIN (Models & Values)
%% =========================================================

    class Money {
        <<ValueObject>>
        -BigDecimal amount
        -String currency
        +of(amount, currency) Money$
        +zero(currency) Money$
        ~add(Money) Money
        ~subtract(Money) Money
        +isPositive() boolean
        +isNegative() boolean
    }

    class Wallet {
        <<Entity>>
        -UUID id
        -String name
        -WalletType type
        -Money balance
        -Collection~IRuleStrategy~ ruleStrategies
        -Collection~Transaction~ transactions
        -deposit(Money amount)
        -withdraw(Money amount)
        +addTransaction(Transaction)
        +removeTransaction(Transaction)
        +transferWithdraw(Transaction)
        +transferDeposit(Transaction)
        +rollbackDeposit(Transaction)
        +rollbackWithdraw(Transaction)
        +accept(IVisitor)
    }

    class Transaction {
        <<Entity>>
        -UUID id
        -Money money
        -TransactionType type
        -Category category
        -LocalDateTime date
        -String note
        +accept(IVisitor)
    }

    class Category {
        <<Entity>>
        -UUID id
        -String name
        -Category parent
        -Collection~Category~ children
        +add(Category)
        +remove(Category)
        +accept(IVisitor)
    }

    class WalletType {
        <<Enumeration>>
        DEBITCARD
        CREDITCARD
        CHECKINGACCOUNT
    }

    class TransactionType {
        <<Enumeration>>
        DEPOSIT
        WITHDRAW
        TRANSFER
    }

%% Relazioni Core
    Wallet --> Money
    Wallet o-- Transaction : contains
    Wallet --> WalletType
    Wallet ..|> IVisitable

    Transaction *-- Money
    Transaction --> TransactionType
    Transaction --> Category
    Transaction ..|> IVisitable

    Category o-- Category : parent/children
    Category ..|> IVisitable

%% =========================================================
%% 2. STRATEGY PATTERN (Business Rules)
%% =========================================================

    class IRuleStrategy {
        <<Interface>>
        +check(Wallet w, Transaction t)
    }

    class MaxWithdraw {
        -double maxAmount
        +check(Wallet w, Transaction t)
    }

    class NegativeBalanceNotAllowed {
        +check(Wallet w, Transaction t)
    }

    Wallet o-- IRuleStrategy
    MaxWithdraw ..|> IRuleStrategy
    NegativeBalanceNotAllowed ..|> IRuleStrategy

%% =========================================================
%% 3. FACTORY & BUILDER
%% =========================================================

    class WalletFactory {
        +create(name, WalletType, initialBalance) Wallet$
    }

    class TransactionBuilder {
        -UUID id;
        -Money amount
        -TransactionType type
        -Category category
        -String note
        -LocalDateTime date;
        +withCategory(Category)
        +withNote(String)
        +withDate(LocalDateTime)
        +build() Transaction
    }

    WalletFactory ..> Wallet : creates
    TransactionBuilder ..> Transaction : builds

%% =========================================================
%% 4. APPLICATION LAYER (Manager & Commands)
%% =========================================================

    class FinanceManager {
        -IWalletRepository walletRepository
        -ICategoryRepository categoryRepository
        -WalletFactory walletFactory
        +addWallet(Wallet)
        +removeWallet(UUID)
        +addCategory(Category)
        +removeCategory(Category)
        +getWallet(UUID)
    }

    class CommandInvoker {
        -Stack~ICommand~ commandHistory
        -Stack~ICommand~ redoStack
        +createWallet(FinanceManager, String name, WalletType, Money)
        +addTransaction(Wallet, Transaction, Money, TransactionType, Category, LocalDateTime, String note)
        +transfer(Wallet from, Wallet to, Transaction, Money, Category, LocalDateTime, String note)
        +createCategory(FinanceManager, Category)
        +undo()
        +redo()
    }

    class ICommand {
        <<Interface>>
        +execute()
        +undo()
    }

    class AddTransactionCommand {
        -Wallet wallet
        -Transaction transaction
        +execute()
        +undo()
    }

    class MakeTransferCommand {
        -Wallet walletForWithdraw
        -Wallet walletForDeposit
        -Transaction transactionWithdraw
        -Transaction transactionDeposit
        +execute()
        +undo()
    }

    class NewWalletCommand {
        -FinanceManager financeManager
        -Wallet wallet
        +execute()
        +undo()
    }

    class NewCategoryCommand {
        -FinanceManager financeManager
        -Category category
        +execute()
        +undo()
    }

%% Relazioni Command
    CommandInvoker o-- ICommand
    AddTransactionCommand ..|> ICommand
    MakeTransferCommand ..|> ICommand
    NewWalletCommand ..|> ICommand
    NewCategoryCommand ..|> ICommand

%% Command Dependencies (Receivers)
    AddTransactionCommand --> Wallet : receiver
    MakeTransferCommand --> Wallet : receiver
    NewWalletCommand --> FinanceManager : receiver
    NewCategoryCommand --> FinanceManager : receiver

%% =========================================================
%% 5. PORTS & ADAPTERS (Repository & Persistence)
%% =========================================================

    class IWalletRepository {
        <<Interface>>
        +save(Wallet)
        +findById(UUID)
        +removeWallet(UUID)
        +update(Wallet)
    }

    class ICategoryRepository {
        <<Interface>>
        +save(Category)
        +delete(Category)
        +findById(UUID)
        +findByName(String)
    }

    class MariaDBWalletPersistence {
        -EntityManagerFactory emf
    }

    class MariaDBCategoryPersistence {
        -EntityManagerFactory emf
    }

    FinanceManager --> IWalletRepository
    FinanceManager --> ICategoryRepository
    FinanceManager --> WalletFactory
    MariaDBWalletPersistence ..|> IWalletRepository
    MariaDBCategoryPersistence ..|> ICategoryRepository

%% =========================================================
%% 6. VISITOR PATTERN
%% =========================================================

    class IVisitor {
        <<Interface>>
        +visit(Wallet)
        +visit(Transaction)
        +visit(Category)
    }

    class IVisitable {
        <<Interface>>
        +accept(IVisitor)
    }
```

# Development Status & Roadmap

This project is currently in a **Reference Implementation** state.
The Core Domain and Application Logic are not fully implemented.

**Current Limitations**
* **Persistence**: the interfaces in the **ports package** are not implemented in **persistence package**.

# Note
The project implementation focuses on demonstrating Clean Architecture principles and is not intended for production use.
Consequently, the persistence layer is currently incomplete, and end-to-end database connectivity has not been established.
Furthermore, the system does not yet support persistent storage for financial transactions, as the AddTransactionCommand has not been integrated with its respective repository port.
These components are slated for implementation in subsequent refactoring phases.