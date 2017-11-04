package finance.inbound;

import finance.Configuration;
import finance.inbound.InputValidator;
import finance.lender.Lender;
import finance.lender.LenderPool;
import org.javamoney.moneta.Money;
import org.junit.Test;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InputValidatorTest {

	private MonetaryAmount onePound = Money.of(1, Configuration.CURRENCY);

	@Test(expected = IllegalArgumentException.class)
	public void Given_EmptyContent_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		inputValidator.generalValidate("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_ContentWithEmptySpace_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		inputValidator.generalValidate(" ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_NullContent_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		inputValidator.generalValidate(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_LenderWithZeroAmount_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		Lender lender = new Lender("bob", new BigDecimal("0.1"), Money.of(0, Configuration.CURRENCY));
		inputValidator.validateLender(lender);
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_LenderWithNegativeAmount_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		Lender lender = new Lender("bob", new BigDecimal("0.1"), Money.of(-1, Configuration.CURRENCY));
		inputValidator.validateLender(lender);
	}

	@Test
	public void Given_LenderWithValidAmount_Then_DoNotThrowException() {
		InputValidator inputValidator = new InputValidator();
		Lender lender = new Lender("bob", new BigDecimal("0.1"), Money.of(1, Configuration.CURRENCY));
		inputValidator.validateLender(lender);
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_LenderWithEmptyName_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		Lender lender = new Lender(" ", new BigDecimal("0.1"), Money.of(1, Configuration.CURRENCY));
		inputValidator.validateLender(lender);
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_LenderWithNullName_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		Lender lender = new Lender(null, new BigDecimal("0.1"), Money.of(1, Configuration.CURRENCY));
		inputValidator.validateLender(lender);
	}

	@Test
	public void Given_LenderWithValidName_Then_DoNotThrowException() {
		InputValidator inputValidator = new InputValidator();
		Lender lender = new Lender("x", new BigDecimal("0.1"), Money.of(1, Configuration.CURRENCY));
		inputValidator.validateLender(lender);
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_LenderWithRateOf100Percent_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		Lender lender = new Lender("x", new BigDecimal("1"), Money.of(1, Configuration.CURRENCY));
		inputValidator.validateLender(lender);
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_LenderWithRateOfLessThanZero_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		Lender lender = new Lender("x", new BigDecimal("-0.01"), Money.of(1, Configuration.CURRENCY));
		inputValidator.validateLender(lender);
	}

	@Test
	public void Given_LenderWithValidRate_Then_DoNotThrowException() {
		InputValidator inputValidator = new InputValidator();
		Lender lender = new Lender("x", new BigDecimal("0.01"), Money.of(1, Configuration.CURRENCY));
		inputValidator.validateLender(lender);
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_LendersWithNonUniqueNames_Then_ThrowExceptions() {
		InputValidator inputValidator = new InputValidator();
		List<Lender> lenders = Arrays.asList(
				new Lender("l1", null, onePound),
				new Lender("l1", null, onePound),
				new Lender("l3", null, onePound));

		inputValidator.lenderUniqueness(lenders);
	}

	@Test
	public void Given_LendersWithUniqueNames_Then_DoNotThrowException() {
		InputValidator inputValidator = new InputValidator();
		List<Lender> lenders = Arrays.asList(
				new Lender("l1", null, onePound),
				new Lender("l2", null, onePound),
				new Lender("l3", null, onePound));
		inputValidator.lenderUniqueness(lenders);
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_AmountNotDivisibleBy100_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		inputValidator.validateAmountRequested(new BigDecimal("99"), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_AmountLessThan1000_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		inputValidator.validateAmountRequested(new BigDecimal("100"), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_AmountMoreThan15000_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		inputValidator.validateAmountRequested(new BigDecimal("15000.02"), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_AmountMoreThanLenderPoolAvailableAmount_Then_ThrowException() {
		InputValidator inputValidator = new InputValidator();
		LenderPool lenderPool = mock(LenderPool.class);
		when(lenderPool.sumAllAvailableAmounts()).thenReturn(Money.of(1000, Configuration.CURRENCY));
		inputValidator.validateAmountRequested(new BigDecimal("2000"), lenderPool);
	}

	@Test
	public void Given_ValidAmounts_Then_DoNotThrowException() {
		InputValidator inputValidator = new InputValidator();
		LenderPool lenderPool = mock(LenderPool.class);
		when(lenderPool.sumAllAvailableAmounts()).thenReturn(Money.of(15000, Configuration.CURRENCY));
		inputValidator.validateAmountRequested(new BigDecimal("1000"), lenderPool);
		inputValidator.validateAmountRequested(new BigDecimal("15000"), lenderPool);
		inputValidator.validateAmountRequested(new BigDecimal("3000"), lenderPool);
		inputValidator.validateAmountRequested(new BigDecimal("1300"), lenderPool);
	}
}