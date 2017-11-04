package finance.inbound;

import finance.lender.Lender;
import finance.lender.LenderPool;

import javax.money.MonetaryAmount;
import java.util.List;

public interface InputParser {

	List<Lender> parseLenders(List<String> content);

	MonetaryAmount parseAmount(String amount, LenderPool lenderPool);
}
