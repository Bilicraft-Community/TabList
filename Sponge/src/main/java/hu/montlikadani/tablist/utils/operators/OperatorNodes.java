package hu.montlikadani.tablist.utils.operators;

public class OperatorNodes implements ExpressionNode {

	private Condition condition;
	private String parseExpression;

	private final String[] expressions = { ">", ">=", "<", "<=", "==", "!=" };

	public OperatorNodes(String str) {
		setParseExpression(str);
	}

	@Override
	public void setParseExpression(String parseExpression) {
		if (parseExpression != null && !parseExpression.isEmpty()) {
			this.parseExpression = parseExpression;
			condition = makeConditionFromInput(parseExpression);
		}
	}

	@Override
	public String getParseExpression() {
		return parseExpression;
	}

	@Override
	public String[] getExpressions() {
		return expressions;
	}

	@Override
	public Condition getCondition() {
		return condition;
	}

	private Condition makeConditionFromInput(final String str) {
		String operator = "";

		for (int i = 0; i < expressions.length; i++) {
			String s = str.replaceAll("[^" + expressions[i] + "]", "");

			if (s.contentEquals(expressions[i])) {
				operator = s;
			}
		}

		if (operator.isEmpty()) {
			return null;
		}

		String[] array = String.valueOf(str.replace(" ", "").replace(operator, ";").toCharArray()).split(";");

		if (array.length > 1 && array[1].replaceAll("[^\\d]", "").matches("[0-9]+")) {
			return new Condition(operator, array);
		}

		return null;
	}

	@Override
	public boolean parse(int firstCondition) {
		if (condition == null) {
			return false;
		}

		int secondCondition = condition.getSecondCondition();
		if (secondCondition < 0 || firstCondition < 0)
			return false;

		switch (condition.getOperator()) {
		case ">":
			return firstCondition > secondCondition;
		case ">=":
			return firstCondition >= secondCondition;
		case "<":
			return firstCondition < secondCondition;
		case "<=":
			return firstCondition <= secondCondition;
		case "==":
			return firstCondition == secondCondition;
		case "!=":
			return firstCondition != secondCondition;
		default:
			return false;
		}
	}
}
