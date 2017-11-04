# Loan Calculator for Multiple Lenders : loan-calculator-multilenders

## How to Run via IDE:

Run As: Java Application
Main Class: finance.Main
Arguments: marketTest.csv 1500

## How to Run on Command Line:

> java -jar loan-calculator-multilenders-0.0.1-SNAPSHOT.jar marketTest.csv 1500

## How to compile with Maven:

> mvn clean install -X -e

# Design

The formula picked to calculate compound interest was implemented from a Mortgage Calculator (source: https://en.wikipedia.org/wiki/Mortgage_calculator)

In order to ensure the model was fitting the requirements an integration test was created and standard JUnit tests for each of the classes implemented.

For the Available Amounts on the Model a library was used, Money and Currency API (JSR 354).

# ALGO
formula used is c = ((P * r) / 1 - (1 / (1 + r) ^ n))
	 
 where:
	  c = monthly repayment
	  P = principal (amount)
	  r = monthly interest rate
	  n = number of payment periods
source: https://en.wikipedia.org/wiki/Mortgage_calculator
	 
