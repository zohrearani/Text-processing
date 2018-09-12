package ir.malek.textanalysis.srl;

import java.util.List;

import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.textanalysis.srl.SRLRelation.Argument;

public class IERelation {
	public String str1;
	public String str2;
	public String str3;
	public String confidence1;
	public String confidence2;

	public IERelation(List<Token> tokensOfSen, Argument arg1, Argument arg2, Argument arg3) {
		if (arg1.firstIndex() < arg2.firstIndex()) {
			if (arg2.firstIndex() < arg3.firstIndex()) {
				str1 = arg1.getLemma();
				str2 = arg2.getLemma();
				str3 = arg3.getLemma();
			} else if (arg3.firstIndex() < arg1.firstIndex()) {
				str1 = arg3.getLemma();
				str2 = arg1.getLemma();
				str3 = arg2.getLemma();
			} else if (arg3.firstIndex() < arg2.firstIndex()) {
				str1 = arg1.getLemma();
				str2 = arg3.getLemma();
				str3 = arg2.getLemma();
			}
		} else {
			if (arg1.firstIndex() < arg3.firstIndex()) {

				str1 = arg2.getLemma();
				str2 = arg1.getLemma();
				str3 = arg3.getLemma();
			} else if (arg3.firstIndex() < arg2.firstIndex()) {
				str1 = arg3.getLemma();
				str2 = arg2.getLemma();
				str3 = arg1.getLemma();
			} else if (arg3.firstIndex() < arg1.firstIndex()) {
				str1 = arg2.getLemma();
				str2 = arg3.getLemma();
				str3 = arg1.getLemma();
			}
		}
	}

	public String toString() {
		return str1 + "\t\t" + str2 + "\t\t" + str3 + "\t\t" + confidence1 + "\t\t" + confidence2;

	}
}
