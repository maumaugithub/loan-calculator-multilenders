package finance.inbound;

import finance.Configuration;
import finance.lender.Lender;
import finance.lender.LenderPool;
import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ClientInputParser implements InputParser {

	private InputValidator inputValidator;

	public ClientInputParser(InputValidator inputValidator) {
		this.inputValidator = inputValidator;
	}

	public List<Lender> parseLenders(List<String> content) {
		List<Lender> lenders = new ArrayList<>();
		content = removeFirstLine(content);
		for (String string : content) {
			String[] spl = string.split(",");
			Lender lender = new Lender(spl[0],
					new BigDecimal(spl[1]),
					Money.of(new BigDecimal(spl[2]), Configuration.CURRENCY));
			inputValidator.validateLender(lender);
			lenders.add(lender);
		}
		inputValidator.lenderUniqueness(lenders);
		return lenders;
	}

	private List<String> removeFirstLine(List<String> content) {
		return content.subList(1, content.size());
	}

	public MonetaryAmount parseAmount(String amount, LenderPool lenderPool) {
		BigDecimal convertedAmount = new BigDecimal(amount);
		inputValidator.validateAmountRequested(convertedAmount, lenderPool);
		MonetaryAmount parsedAmount = Money.of(convertedAmount, Configuration.CURRENCY);
		return  parsedAmount;
	}

}
