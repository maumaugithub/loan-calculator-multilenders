package finance.lender;

import finance.Configuration;
import finance.lender.Lender;

import org.javamoney.moneta.Money;
import org.junit.Test;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class LenderTest {

	private MonetaryAmount onePound = Money.of(1, Configuration.CURRENCY);

	@Test
	public void Return_Lender_Name() {
		Lender lender = new Lender("bob", null, onePound);
		assertThat(lender.getName(), is("bob"));
	}

	@Test
	public void Return_Weighted_Lender_Rate() {
		Lender lender = new Lender(null, new BigDecimal("0.8"), onePound);
		assertThat(lender.getWeightedRate(), is(new BigDecimal("0.2")));

		lender = new Lender(null, new BigDecimal("0.3"), onePound);
		assertThat(lender.getWeightedRate(), is(new BigDecimal("0.7")));
	}

	@Test
	public void Return_Lender_AvailableAmount() {
		Lender lender = new Lender(null, null, Money.of(100, Configuration.CURRENCY));
		assertThat(lender.getAvailable(), is(Money.of(100, Configuration.CURRENCY)));
	}

	@Test
	public void Subtract_ThenReturn_AvailableAmount() {
		Lender lender = new Lender(null, null, Money.of(100, Configuration.CURRENCY));
		lender.sub(Money.of(10, Configuration.CURRENCY));
		assertThat(lender.getAvailable(), is(Money.of(90, Configuration.CURRENCY)));
	}

}