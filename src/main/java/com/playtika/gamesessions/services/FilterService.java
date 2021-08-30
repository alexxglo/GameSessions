package com.playtika.gamesessions.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Service
public class FilterService {

    private List<String> getExpression(String input, int index) {
        List<String> newExpressions = new ArrayList<>();
        char nextSymbolChar = input.charAt(index+1);
        int currentWord = 0;
        String nextSymbol = String.valueOf(nextSymbolChar);
        if("(".equals(nextSymbol)) {
            return null;
        }
        for(int i = index+1; i < input.length(); i++) {
            nextSymbolChar = input.charAt(i);
            nextSymbol = String.valueOf(nextSymbolChar);

            if(" ".equals(nextSymbol)) {
                currentWord++;
                List<String> cloneExpressions = new ArrayList<>(newExpressions.size()+1);
                cloneExpressions.addAll(newExpressions);
                newExpressions = new ArrayList<>();
                newExpressions.addAll(cloneExpressions);
                newExpressions.add("");
            }
            if(")".equals(nextSymbol)) {
                return newExpressions;
            }
            else if(currentWord == 0 && i == index+1) {
                newExpressions.add(nextSymbol);
            }
            else {
                String newEx = newExpressions.get(currentWord);
                newEx = newEx.concat(nextSymbol);
                newExpressions.remove(currentWord);
                newExpressions.add(newEx);
            }
        }
        return null; }

    private String getOperator(String input, int j) {
        String operator = new String();
        while(j < input.length() && input.charAt(j) != '(' && input.charAt(j) != ')') {
            operator = operator + input.charAt(j);
            j++;
        }
        return operator;
    }

    private List<String> trimExpressions(List<String> expressions) {
        List<String> exCopy = new ArrayList<>();
        for(String ex : expressions) {
            if(ex.startsWith(" ")) {
                exCopy.add(ex.substring(1));
            }
            else {
                exCopy.add(ex);
            }
        }
        return exCopy;
    }
    public List<String> resolveQuery(String input){
        if(input==null)
            return null;
        List<String> expressions = new ArrayList<>();
        Stack<Character> stack = new Stack<>();
        String operator = new String();
        for (int i = 0; i <input.length() ; i++) {
            char c = input.charAt(i);
            if(c=='('){
                expressions.add(String.valueOf(c));
                if(getExpression(input, i) != null) {
                    expressions.addAll(getExpression(input,i));
                }
                stack.push(')');
            }
            else if (c==')'){
                expressions.add(String.valueOf(c));
                int j = i+1;
                operator = getOperator(input, j);
                if(!operator.equals("")) {
                    expressions.add(operator);
                }
                if(stack.isEmpty() || stack.pop()!=c)
                    return null;
            }
        }

        if (stack.isEmpty()) {
//            return trimExpressions(expressions);
            return expressions;
        }
        return null;
    }
}
