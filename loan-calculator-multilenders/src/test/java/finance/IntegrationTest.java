package finance;


import org.junit.Before;
import org.junit.Test;

import finance.Main;
import finance.Quote;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class IntegrationTest {
	@Before
	public void setUp() throws Exception {
		String requirement = "Lender,Rate,Available\n" +
				"Bob,0.075,640\n" +
				"Jane,0.069,480\n" +
				"Fred,0.071,520\n" +
				"Mary,0.104,170\n" +
				"John,0.081,320\n" +
				"Dave,0.074,140\n" +
				"Angela,0.071,60\n";
		writeRequirementToTestFile(requirement, "requirement.input");
		
	}
	
	@Before
	public void setUpNonUniqueLenderNameReq() throws Exception {
		String notUniqueLenderName = "Lender,Rate,Available\n" +
				"Bob,0.075,640\n" +
				"Jane,0.069,480\n" +
				"Mary,0.071,520\n" +
				"Mary,0.104,170\n" +
				"John,0.081,320\n" +
				"Dave,0.074,140\n" +
				"Angela,0.071,60\n";
		writeRequirementToTestFile(notUniqueLenderName, "nonUniqueLenderNameReq.input");
		
	}

	@Test
	public void Given_MultipleLenders_And_ValidRequestedAmount_ThenReturn_CorrectQuote() throws IOException {
		Quote quote = Main.run("requirement.input", "1000");
		String expectedQuote = "Requested amount: £1,000.00\nRate: 7.8%\nMonthly repayment: £31.26\nTotal repayment: £1,125.45";
		assertThat(quote.toString(), is(expectedQuote));
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_MultipleLenders_WithRequestedAmountUnder1k_Then_ThrowException() throws IOException {
		Quote quote = Main.run("requirement.input", "900");
		String expectedQuote = "Requested amount: £1,000.00\nRate: 7.8%\nMonthly repayment: £31.26\nTotal repayment: £1,125.45";
		assertThat(quote.toString(), is(expectedQuote));
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_MultipleLenders_RequestedAmountOver15k_Then_ThrowException() throws IOException {
		Quote quote = Main.run("requirement.input", "15100");
		String expectedQuote = "Requested amount: £1,000.00\nRate: 7.8%\nMonthly repayment: £31.26\nTotal repayment: £1,125.45";
		assertThat(quote.toString(), is(expectedQuote));
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_MultipleLenders_RequestedAmountNotIncrementOf100_Then_ThrowException() throws IOException {
		Quote quote = Main.run("requirement.input", "1001");
		String expectedQuote = "Requested amount: £1,000.00\nRate: 7.8%\nMonthly repayment: £31.26\nTotal repayment: £1,125.45";
		assertThat(quote.toString(), is(expectedQuote));
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_MultipleLenders_RequestedAmountMoreThanLendersHaveAvailable_Then_ThrowException() throws IOException {
		Quote quote = Main.run("requirement.input", "10000");
		String expectedQuote = "Requested amount: £1,000.00\nRate: 7.8%\nMonthly repayment: £31.26\nTotal repayment: £1,125.45";
		assertThat(quote.toString(), is(expectedQuote));
	}

	@Test(expected = IllegalArgumentException.class)
	public void Given_MultipleLenders_NonUniqueLenderName_Then_ThrowException() throws IOException {
		Quote quote = Main.run("nonUniqueLenderNameReq.input", "1000");
		String expectedQuote = "Requested amount: £1,000.00\nRate: 7.4%\nMonthly repayment: £31.06\nTotal repayment: £1,118.18";
		assertThat(quote.toString(), is(expectedQuote));
	}

	private void writeRequirementToTestFile(String requirement, String reqfilePath) throws IOException {
		Path path = Paths.get(reqfilePath);
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			writer.write(requirement);
		}
	}

}
