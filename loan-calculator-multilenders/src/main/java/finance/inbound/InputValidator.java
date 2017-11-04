package finance.inbound;

import finance.Configuration;
import finance.lender.Lender;
import finance.lender.LenderPool;
import org.javamoney.moneta.Money;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class InputValidator {

	public void generalValidate(String stringInput) {
		if (stringInput == null || stringInput.trim().length() == 0) {
		 throw new IllegalArgumentException("row is empty: " + stringInput);
		}
	}

	public void lenderUniqueness(List<Lender> lenders) {
		List<String> lenderNames = lenders.stream().map(Lender::getName).collect(Collectors.toList());
		HashSet<String> uniqueLenders = new HashSet<>(lenderNames);
		if (lenderNames.size() != uniqueLenders.size()) {
			throw new IllegalArgumentException("Two or more lenders with the same name.");
		}
	}

	public void validateLender(Lender lender) {
		validateUniqueName(lender);
		validateAmount(lender);
		validateRate(lender);
	}

	public void validateAmountRequested(BigDecimal amount, LenderPool lenderPool) {
		BigDecimal remainder = amount.remainder(new BigDecimal(100));
		if (BigDecimal.ZERO.compareTo(remainder) != 0) {
			throw new IllegalArgumentException("Amount requested: £" + amount + " must be a £100 increment");
		}
		if (isLessThan(amount, new BigDecimal("1000")) || isMoreThanOrEqualTo(amount, new BigDecimal("15000.01"))) {
			throw new IllegalArgumentException("Amount requested: £" + amount + " must be more than or equal to £1000");
		}
		if (Money.of(amount, Configuration.CURRENCY).isGreaterThan(lenderPool.sumAllAvailableAmounts())) {
			throw new IllegalArgumentException("Amount requested: £" + amount +
					" must be bigger than or same of the lenders' available amount: £" + lenderPool.sumAllAvailableAmounts());
		}
	}

	private void validateUniqueName(Lender lender) {
		generalValidate(lender.getName());
	}

	private void validateAmount(Lender lender) {
		if (lender.getAvailable().isLessThanOrEqualTo(Money.of(0, Configuration.CURRENCY))) {
			throw new IllegalArgumentException("Lender had zero or negative amount of money:\n" + lender);
		}
	}

	private void validateRate(Lender lender) {
		if (isMoreThanOrEqualTo(lender.getRate(), BigDecimal.ONE) || isLessThan(lender.getRate(), BigDecimal.ZERO)) {
			throw new IllegalArgumentException("Lender has a rate of 100% or more:\n" + lender);
		}
	}

	private Boolean isMoreThanOrEqualTo(BigDecimal x, BigDecimal y) {
		return x.compareTo(y) >= 0;
	}

	private Boolean isLessThan(BigDecimal x, BigDecimal y) {
		return x.compareTo(y) < 0;
	}

}