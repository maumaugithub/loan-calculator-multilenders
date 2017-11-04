package finance.logic;

import finance.Configuration;
import finance.lender.Lender;
import finance.lender.LenderPool;
import finance.logic.WeightedLoanAlgo;

import org.javamoney.moneta.Money;
import org.junit.Test;

import javax.money.MonetaryAmount;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WeightedLoanAlgoTest {

	private MonetaryAmount onePound = Money.of(1, Configuration.CURRENCY);

	@Test
	public void Given_OneLender_Return_LenderRatioMap() {
		Lender lender = new Lender("lenderOne", new BigDecimal("0.1"), onePound);
		LenderPool lenderPool = new LenderPool(Collections.singletonList(lender));
		Map<Lender, BigDecimal> expectedRatios = Collections.singletonMap(lender, new BigDecimal("1.000000"));
		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(lenderPool);

		assertThat(loanAlgo.calcLenderRatios(), is(expectedRatios));
	}

	@Test
	public void Given_TwoLenders_With_DifferentRates_ThenReturn_LenderRatioMap() {
		Lender lender1 = new Lender("lenderOne", new BigDecimal("0.2"), onePound);
		Lender lender2 = new Lender("lenderTwo", new BigDecimal("0.4"), onePound);
		List<Lender> lenders = Arrays.asList(lender1, lender2);

		Map<Lender, BigDecimal> expectedRatios = new HashMap<>();
		expectedRatios.put(lender1, new BigDecimal("0.571429"));
		expectedRatios.put(lender2, new BigDecimal("0.428571"));

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(new LenderPool(lenders));

		assertThat(loanAlgo.calcLenderRatios(), is(expectedRatios));
	}

	@Test
	public void Given_OneLender_WithLenderRatio_ThenReturn_AmountsAvailableToBorrowPerLender() {
		MonetaryAmount borrowerAmount = Money.of(10, Configuration.CURRENCY);

		Lender lender = new Lender("lenderOne", null, onePound);
		List<Lender> lenders = Collections.singletonList(lender);

		Map<Lender, BigDecimal> lenderRatios = Collections.singletonMap(lender, new BigDecimal("1.000"));
		Map<Lender, MonetaryAmount> expectedResult = Collections.singletonMap(lender, borrowerAmount);

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(new LenderPool(lenders));

		assertThat(loanAlgo.calcAmountsToBorrowPerLender(borrowerAmount, lenderRatios), is(expectedResult));
	}

	@Test
	public void Given_TwoLenders_WithEqualLenderRatio_ThenReturn_AmountsAvailableToBorrowPerLender() {
		MonetaryAmount borrowerAmount = Money.of(10, Configuration.CURRENCY);

		Lender lender1 = new Lender("lenderOne", null, onePound);
		Lender lender2 = new Lender("lenderTwo", null, onePound);
		LenderPool lenderPool = new LenderPool(Arrays.asList(lender1, lender2));

		Map<Lender, BigDecimal> lenderRatios = new HashMap<>();
		lenderRatios.put(lender1, new BigDecimal("0.500"));
		lenderRatios.put(lender2, new BigDecimal("0.500"));

		Map<Lender, MonetaryAmount> expectedResult = new HashMap<>();
		expectedResult.put(lender1, Money.of(5, Configuration.CURRENCY));
		expectedResult.put(lender2, Money.of(5, Configuration.CURRENCY));

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(lenderPool);

		assertThat(loanAlgo.calcAmountsToBorrowPerLender(borrowerAmount, lenderRatios), is(expectedResult));
	}

	@Test
	public void Given_TwoLenders_WithDifferentLenderRatio_ThenReturn_AmountsAvailableToBorrowPerLender() {
		MonetaryAmount borrowerAmount = Money.of(10, Configuration.CURRENCY);

		Lender lender1 = new Lender("lenderOne", null, onePound);
		Lender lender2 = new Lender("lenderTwo", null, onePound);
		LenderPool lenderPool = new LenderPool(Arrays.asList(lender1, lender2));

		Map<Lender, BigDecimal> lenderRatios = new HashMap<>();
		lenderRatios.put(lender1, new BigDecimal("0.667"));
		lenderRatios.put(lender2, new BigDecimal("0.333"));

		Map<Lender, MonetaryAmount> expectedResult = new HashMap<>();
		expectedResult.put(lender1, Money.of(6.67, Configuration.CURRENCY));
		expectedResult.put(lender2, Money.of(3.33, Configuration.CURRENCY));

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(lenderPool);

		assertThat(loanAlgo.calcAmountsToBorrowPerLender(borrowerAmount, lenderRatios), is(expectedResult));
	}

	@Test
	public void Given_TwoLenders_WithAvailability_Warn_ThenReturn_ZeroLeftoverAmount() {
		Lender lender1 = new Lender("lenderOne", new BigDecimal("0.1"), Money.of(2.00, Configuration.CURRENCY));
		Lender lender2 = new Lender("lenderTwo", new BigDecimal("0.1"), Money.of(4.00, Configuration.CURRENCY));
		LenderPool lenderPool = new LenderPool(Arrays.asList(lender1, lender2));

		Map<Lender, MonetaryAmount> amountsToBorrowPerLender = new HashMap<>();
		amountsToBorrowPerLender.put(lender1, Money.of(2.00, Configuration.CURRENCY));
		amountsToBorrowPerLender.put(lender2, Money.of(4.00, Configuration.CURRENCY));

		Map<Lender, MonetaryAmount> expectedResult = new HashMap<>();
		expectedResult.put(lender1, Money.of(2.00, Configuration.CURRENCY));
		expectedResult.put(lender2, Money.of(4.00, Configuration.CURRENCY));

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(lenderPool);
		MonetaryAmount leftOverMoney = loanAlgo.updateLenderAmountsAndReturnLeftOverAmount(amountsToBorrowPerLender);

		assertThat(amountsToBorrowPerLender, is(expectedResult));
		assertThat(leftOverMoney, is(Money.of(0, Configuration.CURRENCY)));
	}

	@Test
	public void Given_OneLender_Then_UpdateLenderAmountsToBorrow_ThenReturn_LeftOverAmount() {
		Lender lender = new Lender("lenderOne", new BigDecimal("0.1"), Money.of(10, Configuration.CURRENCY));
		LenderPool lenderPool = new LenderPool(Collections.singletonList(lender));

		Map<Lender, MonetaryAmount> amountsToBorrowPerLender = new HashMap<>();
		amountsToBorrowPerLender.put(lender, Money.of(20, Configuration.CURRENCY));

		Map<Lender, MonetaryAmount> expectedResult = new HashMap<>();
		expectedResult.put(lender, Money.of(10, Configuration.CURRENCY));

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(lenderPool);
		MonetaryAmount leftOverMoney = loanAlgo.updateLenderAmountsAndReturnLeftOverAmount(amountsToBorrowPerLender);

		assertThat(amountsToBorrowPerLender, is(expectedResult));
		assertThat(leftOverMoney, is(Money.of(10, Configuration.CURRENCY)));
	}

	@Test
	public void Given_TwoLenders_AndOneLenderOverdraft_ThenUpdateLenderAmountsToBorrow_AndReturnLeftOverAmount() {
		Lender lender1 = new Lender("lenderOne", new BigDecimal("0.1"), Money.of(10, Configuration.CURRENCY));
		Lender lender2 = new Lender("lenderTwo", new BigDecimal("0.1"), Money.of(10, Configuration.CURRENCY));
		LenderPool lenderPool = new LenderPool(Arrays.asList(lender1, lender2));

		Map<Lender, MonetaryAmount> amountsToBorrowPerLender = new HashMap<>();
		amountsToBorrowPerLender.put(lender1, Money.of(10, Configuration.CURRENCY));
		amountsToBorrowPerLender.put(lender2, Money.of(20, Configuration.CURRENCY));

		Map<Lender, MonetaryAmount> expectedResult = new HashMap<>();
		expectedResult.put(lender1, Money.of(10, Configuration.CURRENCY));
		expectedResult.put(lender2, Money.of(10, Configuration.CURRENCY));

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(lenderPool);
		MonetaryAmount leftOverMoney = loanAlgo.updateLenderAmountsAndReturnLeftOverAmount(amountsToBorrowPerLender);

		assertThat(amountsToBorrowPerLender, is(expectedResult));
		assertThat(leftOverMoney, is(Money.of(10, Configuration.CURRENCY)));
	}

	@Test
	public void Given_FourLenders_AndTwoLendersOverdraft_ThenUpdateLenderAmountsToBorrow_AndReturnLeftOverAmount() {
		Lender lender1 = new Lender("lenderOne", new BigDecimal("0.1"), Money.of(10, Configuration.CURRENCY));
		Lender lender2 = new Lender("lenderTwo", new BigDecimal("0.1"), Money.of(11, Configuration.CURRENCY));
		Lender lender3 = new Lender("lenderThree", new BigDecimal("0.1"), Money.of(12, Configuration.CURRENCY));
		Lender lender4 = new Lender("lenderFour", new BigDecimal("0.1"), Money.of(20, Configuration.CURRENCY));
		LenderPool lenderPool = new LenderPool(Arrays.asList(lender1, lender2, lender3, lender4));

		Map<Lender, MonetaryAmount> amountsToBorrowPerLender = new HashMap<>();
		amountsToBorrowPerLender.put(lender1, Money.of(8, Configuration.CURRENCY));
		amountsToBorrowPerLender.put(lender2, Money.of(12, Configuration.CURRENCY));
		amountsToBorrowPerLender.put(lender3, Money.of(16, Configuration.CURRENCY));
		amountsToBorrowPerLender.put(lender4, Money.of(20, Configuration.CURRENCY));

		Map<Lender, MonetaryAmount> expectedResult = new HashMap<>();
		expectedResult.put(lender1, Money.of(8, Configuration.CURRENCY));
		expectedResult.put(lender2, Money.of(11, Configuration.CURRENCY));
		expectedResult.put(lender3, Money.of(12, Configuration.CURRENCY));
		expectedResult.put(lender4, Money.of(20, Configuration.CURRENCY));

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(lenderPool);
		MonetaryAmount leftOverMoney = loanAlgo.updateLenderAmountsAndReturnLeftOverAmount(amountsToBorrowPerLender);

		assertThat(leftOverMoney, is(Money.of(5, Configuration.CURRENCY)));
		assertThat(amountsToBorrowPerLender, is(expectedResult));
	}

	@Test
	public void Given_TwoLenders_ThenSubtract_EachLendingAvailableAmounts() {
		Lender lender1 = mock(Lender.class);
		Lender lender2 = mock(Lender.class);
		when(lender1.getAvailable()).thenReturn(Money.of(8, Configuration.CURRENCY));
		when(lender2.getAvailable()).thenReturn(Money.of(4, Configuration.CURRENCY));
		when(lender1.getName()).thenReturn("lenderOne");
		when(lender2.getName()).thenReturn("lenderTwo");
		LenderPool lenderPool = new LenderPool(Arrays.asList(lender1, lender2));

		Map<Lender, MonetaryAmount> amountsToBorrowPerLender = new HashMap<>();
		amountsToBorrowPerLender.put(lender1, Money.of(8, Configuration.CURRENCY));
		amountsToBorrowPerLender.put(lender2, Money.of(12, Configuration.CURRENCY));

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(lenderPool);
		loanAlgo.updateLenderAmountsAndReturnLeftOverAmount(amountsToBorrowPerLender);

		verify(lender1, times(1)).sub(Money.of(8, Configuration.CURRENCY));
		verify(lender2, times(1)).sub(Money.of(4, Configuration.CURRENCY));
	}

	@Test
	public void Merges_Maps_By_Adding_Money_From_Same_Lender() {
		Lender lender = mock(Lender.class);
		Map<Lender, MonetaryAmount> map1 = Collections.singletonMap(lender, Money.of(1, Configuration.CURRENCY));
		Map<Lender, MonetaryAmount> map2 = Collections.singletonMap(lender, Money.of(2, Configuration.CURRENCY));

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(mock(LenderPool.class));

		Map<Lender, MonetaryAmount> expectedMergedMap = Collections.singletonMap(lender, Money.of(3, Configuration.CURRENCY));

		assertThat(loanAlgo.mergeMaps(map1, map2), is(expectedMergedMap));
	}

	@Test
	public void Given_OneLender_ThenReturn_LoanRate() {
		Lender lender = new Lender("lenderOne", new BigDecimal("0.1"), Money.of(10, Configuration.CURRENCY));

		Map<Lender, MonetaryAmount> amountToBorrowPerLender = new HashMap<>();
		amountToBorrowPerLender.put(lender, Money.of(8, Configuration.CURRENCY));

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(null);

		assertThat(loanAlgo.calcLoanRate(amountToBorrowPerLender), is(new BigDecimal("0.1000")));
	}

	@Test
	public void Given_MultipleLendersWithDifferentAmounts_ThenReturn_WeightedAverageLoanRate() {
		Lender lender1 = new Lender("lenderOne", new BigDecimal("0.1"), Money.of(20, Configuration.CURRENCY));
		Lender lender2 = new Lender("lenderTwo", new BigDecimal("0.3"), Money.of(20, Configuration.CURRENCY));
		Lender lender3 = new Lender("lenderThree", new BigDecimal("0.5"), Money.of(20, Configuration.CURRENCY));

		Map<Lender, MonetaryAmount> amountToBorrowPerLender = new HashMap<>();
		amountToBorrowPerLender.put(lender1, Money.of(20, Configuration.CURRENCY));
		amountToBorrowPerLender.put(lender2, Money.of(10, Configuration.CURRENCY));
		amountToBorrowPerLender.put(lender3, Money.of(5, Configuration.CURRENCY));

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(null);

		assertThat(loanAlgo.calcLoanRate(amountToBorrowPerLender), is(new BigDecimal("0.2143")));
	}

	@Test
	public void Given_MultipleOf100Amount_AndRate_AndPaymentPeriods_ThenReturn_MonthlyPayment() {
		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(null);
		MonetaryAmount principalAmount = Money.of(1000, Configuration.CURRENCY);
		BigDecimal rate = new BigDecimal("0.07");
		MonetaryAmount monthlyPayment = loanAlgo.calcMonthlyRepayment(principalAmount, rate, 36);
		String expectedAmount = Configuration.CURRENCY + " 30.87";
		assertTrue(monthlyPayment.toString().startsWith(expectedAmount));
	}

	@Test
	public void Merges_Maps_With_Multiple_Lenders() {
		Lender lender1 = mock(Lender.class);
		Lender lender2 = mock(Lender.class);

		Map<Lender, MonetaryAmount> map1 = new HashMap<>();
		map1.put(lender1, Money.of(5, Configuration.CURRENCY));
		map1.put(lender2, Money.of(10, Configuration.CURRENCY));

		Map<Lender, MonetaryAmount> map2 = new HashMap<>();
		map2.put(lender1, Money.of(20, Configuration.CURRENCY));
		map2.put(lender2, Money.of(30, Configuration.CURRENCY));

		WeightedLoanAlgo loanAlgo = new WeightedLoanAlgo(mock(LenderPool.class));

		Map<Lender, MonetaryAmount> expectedMergedMap = new HashMap<>();
		expectedMergedMap.put(lender1, Money.of(25, Configuration.CURRENCY));
		expectedMergedMap.put(lender2, Money.of(40, Configuration.CURRENCY));

		assertThat(loanAlgo.mergeMaps(map1, map2), is(expectedMergedMap));
	}

}