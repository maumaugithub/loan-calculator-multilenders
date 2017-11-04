package finance;


import java.math.BigDecimal;
import java.util.Locale;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;

public class Configuration {

	public static final String CURRENCY = "GBP";
	public static final Locale LOCALE = Locale.UK;
	public static final int LOAN_DURATION = 36;
	public static final MonetaryAmount MINIMUM_REMAINER = Money.of(0.00001, Configuration.CURRENCY);
	public static final int SCALE = 6;
	public static final BigDecimal NUMBER_OF_MONTHS = new BigDecimal("12");



}
