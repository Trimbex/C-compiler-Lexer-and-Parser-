import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexicalAnalyzer
{

    public static enum TokenType {
        NUMBER("[+-]?(?:0[bB][01]+|0[xX][0-9a-fA-F]+|0[0-7]*|[1-9][0-9]*|0)(?:\\.[0-9]+)?(?:[eE][-+]?[0-9]+)?"),
        COMPARISON_OP("==|!=|>=|<=|>|<|&&|\\|\\|"),
        COMPOUND_OP("-=|\\+=|\\*=|/=|%=|<<=|>>=|&=|\\^=|\\|="),
        ASSIGNMENT_OP("="),

      //  SCOPE_OP("::"),
        TERNARY_OP("\\?"),
        COLON(":"),
        SHIFT_OP("<<|>>"),
        UNARY_OP("\\+\\+|--"),
        ARITHMETIC_OP("[+\\-*/%]"),
      //  LOGICAL_OP("[=!&|]"),
        BITWISE_OP("[<>&|^]"),

       // HASH("#"),
        RELATIONAL_OP("[<>]"),
        LEFT_PAREN("\\("),
        RIGHT_PAREN("\\)"),
        LEFT_BRACKET("\\["),
        RIGHT_BRACKET("\\]"),
        LEFT_BRACE("\\{"),
        RIGHT_BRACE("\\}"),
        SEMICOLON(";"),
        COMMA(","),
        DOT("\\."),
      //  STRING_FORMAT("%[ds]"),

        TYPE_SPECIFIER("int|float|char|double|short|long|signed|unsigned|void|string|bool"),

        KEYWORD("if|while|for|switch|case|default|break|continue|goto|sizeof|typedef|extern|static|const|volatile|return|auto|struct|enum|do|else"),

        STRING_LITERAL("\"[^\"]*\""),

        CHARACTER_LITERAL("'.'"),


        ID("[a-zA-Z_][a-zA-Z0-9_]*(::[a-zA-Z_][a-zA-Z0-9_]*)*"),



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
                return String.format("Token: <%s , (%d, %s)>", type.name(), index, data);
            } else if (type == TokenType.NUMBER) {
                return String.format("Token: <%s , %s value %s>", type.name(), isFloat ? "FLOAT" : "INTEGER", data);
            } else {
                return String.format("Token: <%s , %s>", type.name(), data);
            }
        }
    }

    public static ArrayList<Token> tokenize(String input) {
        ArrayList<Token> tokens = new ArrayList<>();
        StringBuilder tokenpatterns = new StringBuilder();
        for (TokenType tokenType : TokenType.values())
            tokenpatterns.append(String.format("|(%s)", tokenType.pattern));

        // Adding pattern for single-line comments (//)
        tokenpatterns.append("|(//[^\\n]*)");

        // Adding pattern for multi-line comments (/* */)
        tokenpatterns.append("|(/\\*(?:.|[\\n\\r])*?\\*/)");

        Pattern tokenPatterns = Pattern.compile(tokenpatterns.substring(1));

        Matcher matcher = tokenPatterns.matcher(input);
        int currentIndex = 0; // To keep track of the current position in the input string
        while (matcher.find()) {
            boolean isInComment = false;

            // Check if the match is within a single-line comment
            if (matcher.group().startsWith("//")) {
                isInComment = true;
            } else {
                // Check if the match is within a multi-line comment
                for (int i = matcher.start(); i < matcher.end(); i++) {
                    if (input.charAt(i) == '/' && i + 1 < input.length() && input.charAt(i + 1) == '*') {
                        isInComment = true;
                        break;
                    }
                }
            }

            // If the match is not within a comment, add it to tokens
            if (!isInComment) {
                for (TokenType tokenType : TokenType.values()) {
                    if (matcher.group(tokenType.ordinal() + 1) != null) {
                        tokens.add(new Token(tokenType, matcher.group(tokenType.ordinal() + 1), currentIndex, false));
                        break;
                    }
                }
            }

            // Update the current index after each match
            currentIndex = matcher.end();
        }

        return tokens;
    }


    public static final String FILE_PATH = "C:\\Users\\saifh\\Desktop\\Github Compilers Project\\Compilers-Project\\src\\Lexical.c";

    public static ArrayList<Token> lexer() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(FILE_PATH));
        StringBuilder multilineComment = new StringBuilder();
        ArrayList<Token> symbolTable = new ArrayList<>();
        ArrayList<Token> allTokens = new ArrayList<>();
        int index = 0;

        String currentLine;
        boolean skipNext = false;

        while ((currentLine = bufferedReader.readLine()) != null) {
            int commentIndex = currentLine.indexOf("//");
            if (commentIndex != -1) {
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

            if (!multilineComment.isEmpty()) {
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
                    allTokens.add(token);
                    System.out.println(token); // Print tokens
                }
            } else {
                skipNext = false;
            }
        }

        // Print symbol table
        for (Token token : symbolTable) {
            System.out.println("Symbol Table Token: <" + token.type.name() + " , " + token.data + "> Index: " + token.index);
        }

        bufferedReader.close();
        return allTokens;
    }



    public static ArrayList<Token> lexer (String filepath) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath));
        StringBuilder multilineComment = new StringBuilder();
        ArrayList<Token> symbolTable = new ArrayList<>();
        ArrayList<Token> allTokens = new ArrayList<>();
        int index = 0;

        String currentLine;
        boolean skipNext = false;

        while ((currentLine = bufferedReader.readLine()) != null) {
            int commentIndex = currentLine.indexOf("//");
            if (commentIndex != -1) {
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

            if (!multilineComment.isEmpty()) {
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
                    allTokens.add(token);
                    System.out.println(token); // Print tokens
                }
            } else {
                skipNext = false;
            }
        }

        // Print symbol table
        for (Token token : symbolTable) {
            System.out.println("Symbol Table Token: <" + token.type.name() + " , " + token.data + "> Index: " + token.index);
        }

        bufferedReader.close();
        return allTokens;
    }



  /*  public static void main(String[] args) throws IOException
    {


     //   lexer("Compilers-Project/src/Lexical.c");


          /*
        // Tokenize the C code
        List<LexicalAnalyzer.Token> tokens = LexicalAnalyzer.tokenize(
                """  
                        
                                                
                      static int x = 4;
                      
                      int main()
                      {
                      }
                      
                
               
                """);

        for(Token token : tokens)
        {
            System.out.println(token);
        }

        // Create SyntaxAnalyzer instance
        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(tokens);


        // Parse the tokens
        syntaxAnalyzer.parse();

    } */

}