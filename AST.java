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
        String listString = "";

        for (DataTypeDef s : this.datatypedefs) {
            listString += s.compile() + "\t";
        }
        return "Xavi Start" + listString;
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

    public String compile() {
        String listString = "abstract class " + this.dataTypeName + "{};\n";
        System.out.println("This is where Thiago is supposed to ");


        for (Alternative a : this.alternatives) {
            //System.out.println("ASyssOut"+a.compile(this.dataTypeName));

            listString += a.compile(this.dataTypeName);
        }
        return " " + listString;
    }
}

class Alternative extends AST {
    public String constructor;  // Constant -  Variable  -  Mult     -  Add
    public List<Argument> arguments; // Type: NUM, name: -  Type: ID, name: name  -  Type: expr, name: e1
    public List<Token> tokens;// det der står efter :

    Alternative(String constructor, List<Argument> arguments, List<Token> tokens) {
        this.constructor = constructor;
        this.arguments = arguments;
        this.tokens = tokens;
    }


    String compileConstructor(){
        String constInit = "";

        String argsCompiled = "";
        int index = 1;

        for (Argument a : this.arguments) {
            String addComma = index == this.arguments.size() ? "" : ",";

            argsCompiled += a.compileArguments() + addComma;
            index++;

            constInit += a.compileConstructorArguments()+";\n";
        }

        return  ""+
                this.constructor +
                "  ( " + argsCompiled + "){\n" +
                constInit+
                "  }\n" ;
    }


    public String compile(String extendsClass) {
        String variables = "";
        String extendsString = (extendsClass == null) ? "" : " extends " + extendsClass;

        String compiledConstructer = this.compileConstructor();
        for (Argument a : this.arguments) {
            variables+=a.compileVariables();

        }





        return "" + "class " + this.constructor + extendsString + "{\n" +
                variables+
                compiledConstructer+

                "  public String toString(){\n" +
                "    return \"\"+v;\n" +
                "  }\n" +
                "}\n";
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
        return "public " + type + " " + name+";\n";
    }
    public String compileArguments() {
        return "" + type + " " + name;
    }

    public String compileConstructorArguments() {
        return "this." + name + " = " + name;
    }

}

abstract class Token extends AST {

    public String compile() {
        return "token: ";
    }
}

class Nonterminal extends Token {//v,name,e1,e2
    public String name;

    Nonterminal(String name) {
        this.name = name;
    }

    @Override
    public String compile() {
        return "Nonterminal: " + this.name;
    }
}

class Terminal extends Token {// '(' , '*' , '+', ')'
    public String token;

    Terminal(String token) {
        this.token = token;
    }

    @Override
    public String compile() {
        return "Terminal: " + this.token;
    }
}

/*
- variable declaration
- toString
-
* */