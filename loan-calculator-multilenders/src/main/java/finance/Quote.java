package finance;


import finance.lender.Lender;
import finance.logic.LoanCalculator;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

import static finance.Configuration.CURRENCY;
import static finance.Configuration.LOCALE;

public class Quote {

	private BigDecimal rate;
	private MonetaryAmount amount;
	private MonetaryAmount monthlyPaymentAmount;
	private MonetaryAmount totalPaymentAmount;
	
	public Quote(MonetaryAmount amount, LoanCalculator loanCalculator) {
		this.amount = amount;
		Map<Lender, MonetaryAmount> amountToBorrowPerLender = loanCalculator.calcAmountToBorrowPerLender(amount);
		rate = loanCalculator.calcLoanRate(amountToBorrowPerLender);
		monthlyPaymentAmount = loanCalculator.calcMonthlyPayment(amount, rate, Configuration.LOAN_DURATION);
		totalPaymentAmount = loanCalculator.calcTotalPayment(monthlyPaymentAmount);
	}

	@Override
	public String toString() {
		return "Requested amount: " + format(amount) + "\n" +
				"Rate: " + format(rate) + "%\n" +
				"Monthly repayment: " + format(monthlyPaymentAmount) + "\n" +
				"Total repayment: " + format(totalPaymentAmount);
	}

	private String format(BigDecimal toPrint) {
		return toPrint.multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_EVEN).toString();
	}

	private String format(MonetaryAmount unformatted) {
		MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(LOCALE);
		String formatted = format.format(unformatted);
		return  formatted.replace(CURRENCY,
				Currency.getInstance(CURRENCY).getSymbol());
	}

}
