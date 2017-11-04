package finance;



import finance.Configuration;
import finance.Quote;
import finance.lender.Lender;
import finance.logic.LoanCalculator;
import org.javamoney.moneta.Money;
import org.junit.Test;

import javax.money.MonetaryAmount;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuoteTest {

	@Test
	public void Given_MultipleOf100Amount_And_LoanCalculator_ThenReturn_QuoteOutput() {
		Map<Lender, MonetaryAmount> amountToBorrowPerLender = mock(Map.class);
		LoanCalculator loanCalculator = mock(LoanCalculator.class);
		
		BigDecimal rate = new BigDecimal("0.07");
		MonetaryAmount amount = Money.of(1000, Configuration.CURRENCY);	
		MonetaryAmount monthlyPayment = Money.of(30.781, Configuration.CURRENCY);
		MonetaryAmount totalPayment = Money.of(1108.1, Configuration.CURRENCY);
		
		when(loanCalculator.calcAmountToBorrowPerLender(amount)).thenReturn(amountToBorrowPerLender);
		
		when(loanCalculator.calcLoanRate(amountToBorrowPerLender)).thenReturn(rate);

		when(loanCalculator.calcMonthlyPayment(amount, rate, Configuration.LOAN_DURATION)).thenReturn(monthlyPayment);

		when(loanCalculator.calcTotalPayment(monthlyPayment)).thenReturn(totalPayment);

		Quote quote = new Quote(amount, loanCalculator);
		String expectedOutput = "Requested amount: £1,000.00\nRate: 7.0%\nMonthly repayment: £30.78\nTotal repayment: £1,108.10";
		assertThat(quote.toString(), is(expectedOutput));
	}

}