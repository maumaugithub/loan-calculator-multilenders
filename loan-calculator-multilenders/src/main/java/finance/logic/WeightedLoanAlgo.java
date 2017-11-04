package finance.logic;

import finance.Configuration;
import finance.lender.Lender;
import finance.lender.LenderPool;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.function.MonetaryFunctions;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WeightedLoanAlgo {

	private LenderPool lenderPool;

	public WeightedLoanAlgo(LenderPool lenderPool) {
		this.lenderPool = lenderPool;
	}

	public Map<Lender, BigDecimal> calcLenderRatios() {
		BigDecimal summedRates = lenderPool.sumAllWeightedRates();
		Map<Lender, BigDecimal> ratioToBorrowPerLender = new HashMap<Lender, BigDecimal>();
		for (Lender lender : lenderPool.getLenders()) {
			BigDecimal ratio = lender.getWeightedRate().divide(summedRates, Configuration.SCALE, BigDecimal.ROUND_HALF_UP);
			ratioToBorrowPerLender.put(lender, ratio);
		}
		return ratioToBorrowPerLender;
	}

	public Map<Lender, MonetaryAmount> calcAmountsToBorrowPerLender(MonetaryAmount borrowerAmount, Map<Lender, BigDecimal> lenderRatios) {
		Map<Lender, MonetaryAmount> amountToBorrowPerLender = new HashMap<Lender, MonetaryAmount>();
		for (Lender lender : lenderPool.getLenders()) {
			MonetaryAmount amountToBorrowFromLender = borrowerAmount.multiply(lenderRatios.get(lender));
			amountToBorrowPerLender.put(lender, amountToBorrowFromLender);
		}
		return amountToBorrowPerLender;
	}

	public MonetaryAmount updateLenderAmountsAndReturnLeftOverAmount(Map<Lender, MonetaryAmount> amountToBorrowPerLender) {
		MonetaryAmount leftOverAmount = Money.of(0, Configuration.CURRENCY);
		for (Lender lender : amountToBorrowPerLender.keySet()) {
			MonetaryAmount amountToBorrow = amountToBorrowPerLender.get(lender);
			MonetaryAmount maxAvail = lender.getAvailable();
			if (amountToBorrow.isGreaterThan(maxAvail)) {
				MonetaryAmount amountCantLend = amountToBorrow.subtract(maxAvail);
				leftOverAmount = leftOverAmount.add(amountCantLend);
				amountToBorrow = amountToBorrow.subtract(amountCantLend);
				amountToBorrowPerLender.put(lender, amountToBorrow);
			}
			lender.sub(amountToBorrow);
		}
		return leftOverAmount;
	}

	public BigDecimal calcLoanRate(Map<Lender, MonetaryAmount> amountsToBorrowPerLender) {
		List<BigDecimal> rates = new ArrayList<BigDecimal>();
		MonetaryAmount total = calcTotalAmountToBorrow(amountsToBorrowPerLender);
		for (Map.Entry<Lender, MonetaryAmount> mapEntry: amountsToBorrowPerLender.entrySet()) {
			MonetaryAmount divided = mapEntry.getValue().divide(total.getNumber());
			BigDecimal dividedAsBigDec = new BigDecimal(divided.getNumber().toString());
			BigDecimal weightedRate = dividedAsBigDec.multiply(mapEntry.getKey().getRate());
			rates.add(weightedRate);
		}
		return rates.stream().reduce(BigDecimal::add).get().setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public MonetaryAmount calcTotalAmountToBorrow(Map<Lender, MonetaryAmount> amountsToBorrowPerLender) {
		return amountsToBorrowPerLender.entrySet().stream().
				map(it -> it.getValue()).
				reduce(MonetaryFunctions.sum()).get();
	}

	public MonetaryAmount calcMonthlyRepayment(MonetaryAmount amount, BigDecimal rate, int repaymentPeriod) {
		BigDecimal monthlyInterest = rate.divide(Configuration.NUMBER_OF_MONTHS, 6, BigDecimal.ROUND_HALF_UP);
		MonetaryAmount pr = amount.multiply(monthlyInterest);
		BigDecimal onePlusR = BigDecimal.ONE.add(monthlyInterest);
		Double onePlusPowN = Math.pow(onePlusR.doubleValue(), (new BigDecimal(-repaymentPeriod)).doubleValue());
		return pr.divide(BigDecimal.ONE.subtract(new BigDecimal(onePlusPowN)));
	}

	public Map<Lender, MonetaryAmount> mergeMaps(Map<Lender, MonetaryAmount> map1, Map<Lender, MonetaryAmount> map2) {
		return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						MonetaryAmount::add
				));
	}

}
