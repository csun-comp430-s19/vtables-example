# Virtual Tables Example #

Simple language that understands subtyping and virtual dispatch (vtables).
Syntax is below:

```
T is a type variable
i is an integer
x is a variable
cname is a class name
mname is a method name

exp ::= i | lhs
lhs ::= x | lhs.x | this
type ::= int | cname<type*> | T
vdec ::= type x
stmt ::= vdec = new cname<type*>(exp*) |
         vdec = exp.mname<type*>(exp*) |
         print(exp) |
         return exp |
         lhs = exp |
         super(exp*) |
         stmt; stmt |
         empty
mdef ::= [virtual] <T*> type mname(vdec*) { stmt }
cdef ::= class cname<T*> [extends cname<type*>] {
           vdec*
           init(vdec*) { stmt }
           mdef*
         }
program ::= cdef* stmt
```

## Intentional Limitations ##

- Most useful kinds of expressions and statements are missing, to dramatically simplify the language.
  See other examples in the organization for how to handle those.
- Most expressions do not nest (e.g., `new` and method calls are statements, not expressions).
  This is to simplify codegen so that every expression can be compiled with a single register, without needing the stack.
- Overriding methods must be marked `virtual`, unlike C++
- There is no lexer or parser.
- The typechecker is not well-tested.

