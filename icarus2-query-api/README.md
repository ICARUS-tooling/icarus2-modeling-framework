# ICARUS2 Query API

## Overview


## Developer Notes

Due to some obvious flaws in the ANTLR components for gradle and IDEs such as Eclipse, additional steps can be necessary in order to get the automatic processing of grammar files working. For Eclipse (using the ANTLR 4 IDE plugin), the following needed to be done manually:

 - Run As "Generate ANTLR Recognizer" for every grammar file in the ``serc/main/antlr`` and ``src/test/antlr`` folders to have Eclipse create the launch configurations.
 - Modify the "External Tool Configuration" entries related to test grammars to also include ``-o build\generated-src\antlr\test\de\ims\icarus2\query\api\iql\antlr`` as argument.
 
 After this, the ANTLR 4 IDE plugin for Eclipse should be able to properly process and place the grammar files and also its most important feature, the ``Parse Tree`` view, will provide invaluable help in debugging grammar files ;)
 To initiate creation of the lexers and parsers, use the gradle command ``gradlew.bat icarus2-query-api:clean icarus2-query-api:generateGrammarSource icarus2-query-api:generateTestGrammarSource --no-scan``.