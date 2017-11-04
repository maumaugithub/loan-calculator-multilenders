package finance.logic;

import finance.Configuration;
import finance.lender.Lender;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class WeightedLoanCalculator implements LoanCalculator {

	private WeightedLoanAlgo loanAlgorithm;
	private Map<Lender, MonetaryAmount> amountsToBorrowPerLender = new HashMap<Lender, MonetaryAmount>();
	
	public WeightedLoanCalculator(WeightedLoanAlgo loanAlgorithm) {
		this.loanAlgorithm  = loanAlgorithm;
	}
	
	public Map<Lender, MonetaryAmount> calcAmountToBorrowPerLender(MonetaryAmount amountToBorrow) {
		Map<Lender, BigDecimal> lenderRatios = loanAlgorithm.calcLenderRatios();
		Map<Lender, MonetaryAmount> amountsToBorrowPerLender = loanAlgorithm.calcAmountsToBorrowPerLender(amountToBorrow, lenderRatios);
		MonetaryAmount leftOverAmount = loanAlgorithm.updateLenderAmountsAndReturnLeftOverAmount(amountsToBorrowPerLender);
		this.amountsToBorrowPerLender = loanAlgorithm.mergeMaps(this.amountsToBorrowPerLender, amountsToBorrowPerLender);
		if (leftOverAmount.isGreaterThan(Configuration.MINIMUM_REMAINER)) {
			calcAmountToBorrowPerLender(leftOverAmount);
		}
		return amountsToBorrowPerLender;
	}

	public BigDecimal calcLoanRate(Map<Lender, MonetaryAmount> amountsToBorrowPerLender) {
		return loanAlgorithm.calcLoanRate(amountsToBorrowPerLender);
	}

	public MonetaryAmount calcMonthlyPayment(MonetaryAmount amount, BigDecimal rate, int repaymentPeriod) {
		return loanAlgorithm.calcMonthlyRepayment(amount, rate, repaymentPeriod);
	}

	public MonetaryAmount calcTotalPayment(MonetaryAmount monthlyRepayment) {
		return monthlyRepayment.multiply(Configuration.LOAN_DURATION);
	}

}
