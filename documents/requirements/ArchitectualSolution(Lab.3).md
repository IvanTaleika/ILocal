# Содержание
1. [Часть 1](#part1)  
1.1. [Тип приложения](#type_app)  
1.2. [Стратегия развертывания](#strategy)   
1.3. [Выбор технологии](#technology)  
1.4. [Показатели качества](#quality_indicator)  
1.5. [Пути реализации сквозной функциональности](#way_implimintation)   
1.6. ["To Be" архитектура](#to_be)  
2. [Часть 2](#part2)      
2.1. ["As Is" архитектура](#as_is)
3. [Часть 3](#part3)   
3.1. [Сравнение](#compare)  
3.2. [Анализ](#analysis)   
3.3. [Пути улучшения архитектуры](#way_upgrade)  


<a name="part1"/>

# Часть 1

<a name="type_app"/>

## 1.	Тип приложения
Вэб приложение.

<a name="strategy"/>

## 2.	Стратегия развёртывания 
Распределенное развертывание.

<a name="technology"/>

## 3. Выбор технологии
  - Java - сильно типизированный объектно-ориентированный язык программирования, разработанный компанией Sun Microsystems. 
  - Angular - это открытая и свободная платформа для разработки веб-приложений, написанная на языке TypeScript, разрабатываемая командой из компании Google. 
  - Spring Framework - универсальный фреймворк с открытым исходным кодом для Java-платформы. Также существует форк для платформы .NET Framework, названный Spring.NET.
  - MySQL - свободная реляционная система управления базами данных. Разработку и поддержку MySQL осуществляет корпорация Oracle.

<a name="quality_indicator"/>

## 4. Показатели качества
  - Концептуальная целостность
  - Доступность
  - Производительность
  - Надежность
  - Безопасность
  - Тестируемость
  - Удобство и простота использования
  
  <a name="way_implimintation"/>
  
## 5.  Пути реализации сквозной функциональности: 
  - Сетевое взаимодействие: использовать асинхронные взаимодействия; использовать HTTP протоколы.
  - Управление конфигурацией: продумать, какие параметры должны быть конфигурируемыми извне.
  - Управление исключениями: обеспечить стабильность состояния приложения после сбоя.
  
  <a name="to_be"/>
  
 ## "To be" архитектура:
 1. Диаграмма компонентов.
 ![]()
 2. Диаграмма развертывания.
 ![]()
 3. Диаграмма классов
 
 <a name="part2"/>
 
 # Часть 2
 
 <a name="as_is"/>
 
 ## "As is" архитектура:
 1. Диаграмма компонентов.
 ![]()
 2. Диаграмма развертывания.
 ![]()
 
 <a name="part3"/>
 
 # Часть 3
 
  <a name="compare"/>
  
**1.** Во время разработки приложения возникли проблемы с хранением и последующим использованием данных
Для исправления проблем наша команда прибегла к использованию базы данных Mysql, что значительно облегчило последующее взаимдоействие с данными и их хранение.

 <a name="analysis"/>
 
**2.** Проанализировав архитектуры "As is" и "To be" , наша команда пришла к выводу, что использование базы данных дало больше преимуществ нашему приложению.

 <a name="way_upgrade"/>
 
**3.** В связи с вышеперечисленными причинами мы будет улучшать архитектуру согласно следующим принципам:

- принцип единственности;
- принцип минимального знания.