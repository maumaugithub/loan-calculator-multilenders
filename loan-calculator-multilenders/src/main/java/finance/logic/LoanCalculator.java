package finance.logic;

import finance.lender.Lender;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Map;

public interface LoanCalculator {

	
	Map<Lender, MonetaryAmount> calcAmountToBorrowPerLender(MonetaryAmount amountToBorrow);
	BigDecimal calcLoanRate(Map<Lender, MonetaryAmount> amountsToBorrowPerLender);
	MonetaryAmount calcMonthlyPayment(MonetaryAmount amount, BigDecimal rate, int repaymentPeriod);
	MonetaryAmount calcTotalPayment(MonetaryAmount monthlyRepayment);

}
