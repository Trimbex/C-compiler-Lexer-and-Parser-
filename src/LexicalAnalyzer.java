import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexicalAnalyzer {

    public static enum TokenType {
        NUMBER("[+-]?(?:0[bB][01]+|0[xX][0-9a-fA-F]+|0[0-7]*|[1-9][0-9]*|0)(?:\\.[0-9]+)?(?:[eE][-+]?[0-9]+)?"),
        OPERATOR("<<=|>>=|\\+=|\\-=|\\*=|/=|&=|\\|=|\\^=|\\+\\+|--|==|!=|=>|=<|<=|>=|&&|\\|\\||<<|>>|[+\\-*/%<>&|^=]|#|<|>"),
        STRINGFORMAT("%[ds]"),
        KEYWORD("if|while|for|switch|case|default|break|continue|goto|sizeof|typedef|extern|static|register|const|" +
                "volatile|" +
                "return|auto|void|short|long|long long|signed|unsigned|float|double|struct|enum|do|else|char|" +
                "int|void"),

        ID("[a-zA-Z_][a-zA-Z0-9_]*(::[a-zA-Z_][a-zA-Z0-9_]*)*"),
        STRINGLITERAL("\"[^\"]*\""),
        CHARACTERLITERAL("'.'"),
        PUNCTUATION("[(){};,]");

        public final String pattern;

        private TokenType(String pattern) {
            this.pattern = pattern;
        }
    }

    public static class Token {
        public TokenType type;
        public String data;
        public int index;
        public boolean isFloat;

        public Token(TokenType type, String data, int index, boolean isFloat) {
            this.type = type;
            this.data = data;
            this.index = index;
            this.isFloat = isFloat;
        }

        @Override
        public String toString() {
            if (type == TokenType.ID && index != -1) {
                return String.format("Token: <%s , %d>", type.name(), index);
            } else if (type == TokenType.NUMBER)
            {

                return String.format("Token: <%s , %s value %s>", type.name(), isFloat ? "FLOAT" : "INTEGER", data);
            } else
            {
                return String.format("Token: <%s , %s>", type.name(), data);
            }
        }
    }

    public static ArrayList<Token> tokenize(String input) {
        ArrayList<Token> tokens = new ArrayList<>();
        StringBuilder tokenpatterns = new StringBuilder();
        for (TokenType tokenType : TokenType.values())
            tokenpatterns.append(String.format("|(?<%s>%s)", tokenType.name(), tokenType.pattern));
        Pattern tokenPatterns = Pattern.compile(tokenpatterns.substring(1));

        Matcher matcher = tokenPatterns.matcher(input);
        while (matcher.find()) {
            if (matcher.group(TokenType.NUMBER.name()) != null) {
                String numberString = matcher.group(TokenType.NUMBER.name());
                boolean isFloat = numberString.contains(".");

                tokens.add(new Token(TokenType.NUMBER, numberString, -1, isFloat));
            } else {
                for (TokenType tokenType : TokenType.values())
                {
                    if (matcher.group(tokenType.name()) != null) {
                        tokens.add(new Token(tokenType, matcher.group(tokenType.name()), -1, false));
                        break;
                    }
                }
            }
        }
        return tokens;
    }

    private static final String FILE_PATH = "src/Lexical.c";

    public void lexer() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(FILE_PATH));
        StringBuilder multilineComment = new StringBuilder();
        ArrayList<Token> symbolTable = new ArrayList<>();
        int index = 0;

        String currentLine;
        boolean skipNext = false;

        while ((currentLine = bufferedReader.readLine()) != null) {
            int commentIndex = currentLine.indexOf("//");
            if (commentIndex != -1)
            {
                currentLine = currentLine.substring(0, commentIndex);
            }

            int startCommentIndex = currentLine.indexOf("/*");
            int endCommentIndex = currentLine.indexOf("*/");
            if (startCommentIndex != -1 && endCommentIndex != -1 && startCommentIndex < endCommentIndex) {
                currentLine = currentLine.substring(0, startCommentIndex) + currentLine.substring(endCommentIndex + 2);
            } else if (startCommentIndex != -1 && endCommentIndex == -1) {
                multilineComment.append(currentLine.substring(startCommentIndex + 2)).append("\n");
                continue;
            } else if (startCommentIndex == -1 && endCommentIndex != -1) {
                multilineComment.setLength(0);
                currentLine = currentLine.substring(endCommentIndex + 2);
            }

            if (!multilineComment.isEmpty())
            {
                multilineComment.append(currentLine).append("\n");
                continue;
            }

            if (currentLine.contains("#") && currentLine.contains(">")) {
                skipNext = true;
            }

            if (!skipNext) {
                ArrayList<Token> tokens = tokenize(currentLine);
                for (Token token : tokens) {
                    if (token.type == TokenType.ID) {
                        boolean found = false;
                        for (Token existingToken : symbolTable) {
                            if (existingToken.data.equals(token.data)) {
                                token.index = existingToken.index;
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            token.index = index++;
                            symbolTable.add(token);
                        }
                    }
                    System.out.println(token);
                }
            } else {
                skipNext = false;
            }
        }

        for (Token token : symbolTable) {

            System.out.println("Symbol Table Token: <" + token.type.name() + " , " + token.data + "> Index: " + token.index);

        }

        bufferedReader.close();
    }


}
