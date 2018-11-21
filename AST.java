import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;

class faux { // collection of non-OO auxiliary functions (currently just error)
    public static void error(String msg) {
        System.err.println("Interpreter error: " + msg);
        System.exit(-1);
    }
}

abstract class AST {
}

class Start extends AST {
    public List<TokenDef> tokendefs;
    public List<DataTypeDef> datatypedefs;

    Start(List<TokenDef> tokendefs, List<DataTypeDef> datatypedefs) {
        this.tokendefs = tokendefs;
        this.datatypedefs = datatypedefs;
    }

    public String compile() {
        try {
            String listString = "";

            for (DataTypeDef s : this.datatypedefs) {
                listString += s.compile(this.tokendefs) + "\t";
            }
            return "" + listString;
        } catch (Exception e) {
            return "" + e;
        }

    }
}

class TokenDef extends AST {
    public String tokenname;// NUM and ID
    public String ANTLRCODE;//#: ('0'..'9')+ ('.'('0'..'9')+)? ;#   #: ('A'..'Z'|'a'..'z'|'_')+ ;     #

    TokenDef(String tokenname, String ANTLRCODE) {
        this.tokenname = tokenname;
        this.ANTLRCODE = ANTLRCODE;
    }

    public String compile() {
        return this.ANTLRCODE;
    }
}

class DataTypeDef extends AST {
    public String dataTypeName; //exp
    public List<Alternative> alternatives;

    DataTypeDef(String dataTypeName, List<Alternative> alternatives) {
        this.dataTypeName = dataTypeName;
        this.alternatives = alternatives;
    }

    public String compile(List<TokenDef> tokendefs) throws Exception {
        String listString = "abstract class " + this.dataTypeName + "{};\n\n";
        for (Alternative a : this.alternatives) {
            a.mapArguments(tokendefs);

            listString += a.compile(this.dataTypeName);
        }
        return "" + listString;
    }
}

class Alternative extends AST { //laver klasser
    public String constructor;  // Constant -  Variable  -  Mult     -  Add
    public List<Argument> arguments; // Type: NUM, name: -  Type: ID, name: name  -  Type: expr, name: e1
    public List<Token> tokens;// det der står efter :

    Alternative(String constructor, List<Argument> arguments, List<Token> tokens) {
        this.constructor = constructor;
        this.arguments = arguments;
        this.tokens = tokens;
    }

    public void mapArguments(List<TokenDef> tokendefs) { //map arguments types to valid Java types
        for (Argument arg : this.arguments) {
            for (TokenDef tokenDef : tokendefs) {
                if (arg.type.equals(tokenDef.tokenname)) {
                    arg.type = "String";
                }
            }
        }
    }

    void typeCheck() throws Exception {
        Boolean error = false;
        String notFoundVar = "";
        for (Token token : this.tokens) {//Task 3.1: check for undefined var
            error = false;
            for (Argument arg : this.arguments) {
                if (token.getType().equals("nonTerminal")) {
                    if (((Nonterminal) token).getName().equals(arg.name)) {
                        error = false;
                        break;
                    } else {
                        notFoundVar = ((Nonterminal) token).getName();
                        error = true;
                    }
                }
            }

            if (error) {
                throw new Exception("Symbol \"" + notFoundVar + "\" is undefined in " + this.constructor);
            }
        }
        for (Argument a : this.arguments) {
            if (a.name.equals(a.type)) {//Task 3.2: check for double use of symbol
                throw new Exception("Symbol \"" + a.name + "\" is used as a type and a name in class " + this.constructor);
            }

            int occuredCount = 0;

            for (Argument arg2 : this.arguments) {//task 3.3
                if (arg2.name.equals(a.name)) {
                    occuredCount++;
                    if (occuredCount > 1) {
                        throw new Exception("Variable name \"" + a.name + "\" occurs more than once in " + this.constructor);

                    }
                }
            }
        }

        for (Argument a : this.arguments) {//Task 3.4: check for unused arguments
            Boolean argumentIsUsed = false;
            for (Token token : this.tokens) {
                if (token.getType().equals("nonTerminal")) {
                    if (((Nonterminal) token).getName().equals(a.name)) {
                        argumentIsUsed = true;
                        break;
                    }
                }
            }

            if (!argumentIsUsed) {//throw Error if an argument is not used
                throw new Exception("Argument \"" + a.name + "\" in " + this.constructor + " is not used. ");
            }
        }
    }

    String compileToString() {//task 2
        int index = 1;
        String tokensCompiled = "";
        for (Token t : this.tokens) {
            String addPlus = (index == this.tokens.size()) ? "" : "+";
            tokensCompiled += t.compile() + addPlus;
            index++;
        }
        return "public String toString(){" +
                "\n return \"\"+" + tokensCompiled +
                "\n}\n";
    }

    String compileConstructor() {//laver constructer
        String constInit = "";
        String argsCompiled = "";
        int index = 1;
        for (Argument a : this.arguments) {
            String addComma = index == this.arguments.size() ? "" : ", ";
            argsCompiled += a.compileArguments() + addComma;
            index++;
            constInit += a.compileConstructorArguments();
        }
        return "" +
                this.constructor +
                "(" + argsCompiled + "){\n" +
                constInit +
                "}\n";
    }

    public String compile(String extendsClass) throws Exception {
        String variables = "";
        String extendsString = (extendsClass == null) ? "" : " extends " + extendsClass;
        String toStringMethod = this.compileToString();
        String compiledConstructer = this.compileConstructor();
        this.typeCheck();
        for (Argument a : this.arguments) {
            variables += a.compileVariables();
        }
        return "" + "class " + this.constructor + extendsString + "{\n" +
                variables +
                "\n" + compiledConstructer +
                "\n" + toStringMethod +
                "\n}\n\n";
    }
}

class Argument extends AST {
    public String type;//expr
    public String name;//e1 or e2

    Argument(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String compileVariables() {
        return "public " + type + " " + name + ";\n";
    }

    public String compileArguments() {
        return "" + type + " " + name;
    }

    public String compileConstructorArguments() {
        return "this." + name + " = " + name + ";\n";
    }

}

abstract class Token extends AST {
    public String type;

    public String compile() {
        return ": ";
    }

    public String getType() {
        return type;
    }
}

class Nonterminal extends Token {//v,name,e1,e2
    public String name;

    Nonterminal(String name) {
        this.name = name;
        this.type = "nonTerminal";
    }

    public String getName() {
        return name;
    }

    @Override
    public String compile() {
        return this.name;
    }
}

class Terminal extends Token {// '(' , '*' , '+', ')'
    public String token;

    Terminal(String token) {
        this.token = token;
        this.type = "Terminal";

    }

    @Override
    public String compile() {
        return this.token.replaceAll("'", "\"");
    }
}

