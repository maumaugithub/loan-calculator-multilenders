package finance.inbound;

import finance.Configuration;
import finance.inbound.ClientInputParser;
import finance.inbound.InputValidator;
import finance.lender.Lender;
import finance.lender.LenderPool;
import org.javamoney.moneta.Money;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ClientInputParserTest {

	@Test
	public void Given_OneLender_ThenReturn_ListOfLenders() throws IOException {
		List<String> content = Arrays.asList("Lender,Rate,Available", "Angela,0.071,60");
		List<Lender> expectedLenders = Arrays.asList(
				new Lender("Angela", new BigDecimal("0.071"), Money.of(60, Configuration.CURRENCY))
		);
		ClientInputParser inputParser = new ClientInputParser(new InputValidator());
		assertThat(inputParser.parseLenders(content).toString(),
				is(expectedLenders.toString()));
	}

	@Test
	public void Given_TwoLenders_ThenReturn_ListOfLenders() throws IOException {
		List<String> content = Arrays.asList("Lender,Rate,Available", "Angela,0.071,60", "Jane,0.069,480");
		List<Lender> expectedLenders = Arrays.asList(
				new Lender("Angela", new BigDecimal("0.071"), Money.of(60, Configuration.CURRENCY)),
				new Lender("Jane", new BigDecimal("0.069"), Money.of(480, Configuration.CURRENCY))
		);
		ClientInputParser inputParser = new ClientInputParser(new InputValidator());
		assertThat(inputParser.parseLenders(content).toString(),
				is(expectedLenders.toString()));
	}

	@Test
	public void Given_TwoLenders_ThenReturn_ValidLenders() throws IOException {
		List<String> content = Arrays.asList("Lender,Rate,Available", "Angela,0.071,60", "Jane,0.069,480");
		InputValidator inputValidator = mock(InputValidator.class);
		ClientInputParser inputParser = new ClientInputParser(inputValidator);

		inputParser.parseLenders(content);

		verify(inputValidator, times(2)).validateLender(any(Lender.class));
		verify(inputValidator, times(1)).lenderUniqueness(any(List.class));
	}

	@Test
	public void Given_AmountRequested_ThenReturn_ParsedAmount() {
		ClientInputParser inputParser = new ClientInputParser(mock(InputValidator.class));
		assertThat(inputParser.parseAmount("1000", null), is(Money.of(1000, Configuration.CURRENCY)));
	}

	@Test
	public void Given_AmountRequested_ThenReturn_ValidAmount() {
		InputValidator inputValidator = mock(InputValidator.class);
		ClientInputParser inputParser = new ClientInputParser(inputValidator);
		LenderPool lenderPool = new LenderPool(null);
		inputParser.parseAmount("1000", lenderPool);
		verify(inputValidator, times(1)).validateAmountRequested(new BigDecimal("1000"), lenderPool);
	}

}