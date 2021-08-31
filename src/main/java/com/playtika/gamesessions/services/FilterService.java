package com.playtika.gamesessions.services;

import com.playtika.gamesessions.models.FilterInterface;
import com.playtika.gamesessions.models.GameSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FilterService implements FilterInterface {

    @Autowired
    private GameSessionQueriesService gameSessionQueriesService;

    private List<String> getExpression(String input, int index) {
        List<String> newExpressions = new ArrayList<>();
        char nextSymbolChar = input.charAt(index + 1);
        int currentWord = 0;
        String nextSymbol = String.valueOf(nextSymbolChar);
        if ("(".equals(nextSymbol)) {
            return null;
        }
        for (int i = index + 1; i < input.length(); i++) {
            nextSymbolChar = input.charAt(i);
            nextSymbol = String.valueOf(nextSymbolChar);

            if (" ".equals(nextSymbol)) {
                currentWord++;
                List<String> cloneExpressions = new ArrayList<>(newExpressions.size() + 1);
                cloneExpressions.addAll(newExpressions);
                newExpressions = new ArrayList<>();
                newExpressions.addAll(cloneExpressions);
                newExpressions.add("");
            }
            if (")".equals(nextSymbol)) {
                return newExpressions;
            } else if (currentWord == 0 && i == index + 1) {
                newExpressions.add(nextSymbol);
            } else {
                String newEx = newExpressions.get(currentWord);
                newEx = newEx.concat(nextSymbol);
                newExpressions.remove(currentWord);
                newExpressions.add(newEx);
            }
        }
        return null;
    }

    private String getOperator(String input, int j) {
        String operator = new String();
        while (j < input.length() && input.charAt(j) != '(' && input.charAt(j) != ')') {
            operator = operator + input.charAt(j);
            j++;
        }
        return operator;
    }

    public List<String> resolveQuery(String input) {
        if (input == null)
            return null;
        List<String> expressions = new ArrayList<>();
        Stack<Character> stack = new Stack<>();
        String operator = new String();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(') {
                expressions.add(String.valueOf(c));
                if (getExpression(input, i) != null) {
                    expressions.addAll(getExpression(input, i));
                }
                stack.push(')');
            } else if (c == ')') {
                expressions.add(String.valueOf(c));
                int j = i + 1;
                operator = getOperator(input, j);
                if (!operator.equals("")) {
                    expressions.add(operator);
                }
                if (stack.isEmpty() || stack.pop() != c)
                    return null;
            }
        }

        if (stack.isEmpty()) {
            return expressions;
        }
        return null;
    }

    private String checkIfKeywordExists(String exp) {
        if (" eq".equals(exp)) {
            return "=";
        } else if (" ne".equals(exp)) {
            return "!=";
        } else if (" gt".equals(exp)) {
            return ">";
        } else if (" lt".equals(exp)) {
            return "<";
        } else return null;
    }

    @Override
    public List<GameSession> getListFromFilterQuery(String query) {
        List<String> expressionsFromInput;
        List<String> safeExpressions = new ArrayList<>();
        String regexedString = new String();
        expressionsFromInput = resolveQuery(query);

        String sqlQuery = new String();
        Pattern pattern = Pattern.compile("[^;?\']");
        for (String exp : expressionsFromInput) {
            Matcher matcher = pattern.matcher(exp);
            while (matcher.find()) {
                regexedString = regexedString + matcher.group();
            }
            safeExpressions.add(regexedString);
            regexedString = new String();
        }
        for (String exp : safeExpressions) {
            String translatedOperation = checkIfKeywordExists(exp);
            if (translatedOperation != null) {
                sqlQuery = sqlQuery.concat(translatedOperation);
            } else {
                sqlQuery = sqlQuery.concat(exp);
            }
        }
        return gameSessionQueriesService.getListFromFilterQuery(sqlQuery);
    }
}
