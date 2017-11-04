package finance;


import finance.inbound.ClientInputParser;
import finance.inbound.InputParser;
import finance.inbound.InputValidator;
import finance.lender.Lender;
import finance.lender.LenderPool;
import finance.logic.LoanCalculator;
import finance.logic.WeightedLoanAlgo;
import finance.logic.WeightedLoanCalculator;

import javax.money.MonetaryAmount;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

	public static void main(String... args) throws IOException {
		System.out.println(run(args[0], args[1]));
	}

	public static Quote run(String filePath, String amount) throws IOException {
		
		InputParser inputParser = new ClientInputParser(new InputValidator());
		List<Lender> lenders = inputParser.parseLenders(Files.lines(Paths.get(filePath)).collect(Collectors.toList()));
		LenderPool lenderPool = new LenderPool(lenders);
		MonetaryAmount amountRequested = inputParser.parseAmount(amount, lenderPool);
		
		LoanCalculator loanCalculator = new WeightedLoanCalculator(new WeightedLoanAlgo(lenderPool));
		
		return 
				new Quote(amountRequested, loanCalculator);
	}

}
