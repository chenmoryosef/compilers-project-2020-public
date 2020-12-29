## assumptions:
# 1. in order to reach the compiled jar you need to run ../../mjavac
# 2. tests folder 'Test' has xml and 'Test.res' file (expected result)


## results:
# if the script & your code are working properly
# you should see the names of all of the tests with "Success"


COMMAND="java -jar ../../mjavac.jar unmarshal semantic"
FILEPATH="."
function test()
{
	XMLFILE=$1
	TESTFOLDER=$2
	echo "Running Test:" $TESTFOLDER/$XMLFILE.java.xml
	result_file=$TESTFOLDER/$TESTFOLDER.our-result
	log_file=$TESTFOLDER/$XMLFILE.log
	if [ -f $result_file ]; then
	   rm $TESTFOLDER/$TESTFOLDER.our-result
	fi
	if [ -f $log_file ]; then
	   rm $TESTFOLDER/$XMLFILE.log
	fi

	$COMMAND $TESTFOLDER/$XMLFILE.java.xml $result_file > $log_file
	diff $result_file $TESTFOLDER/$XMLFILE.res
	if [ $? -eq 0 ]
	then
		echo Success
	else
		echo Fail
	fi
}

#ex3 examples in yotam's git:

test arraysValid arrays
test arraysInvalidLengthNotOnArray arrays
test arraysInvalidAccess arrays
test arraysInvalidAssignment arrays

test BinarySearch ast
test BinaryTree ast
test BubbleSort ast
test LinearSearch ast
test LinkedList ast
test QuickSort ast
test TreeVisitor ast

test inheritanceValid cyclicInheritance
test inheritanceInvalidCyclic cyclicInheritance
test inheritanceInvalidClassNotExist cyclicInheritance
test inheritanceInvalidBadClassOrder cyclicInheritance

test IfValid generalTypeChecks
test IfInvalidNonBooleanCond generalTypeChecks
test sysoutValid generalTypeChecks
test sysoutInvalidNonIntCond generalTypeChecks
test whileValid generalTypeChecks

test arrayCallInvalid IdentifierChecks
test booleanCallInvalid IdentifierChecks
test declRefTypeInvalid IdentifierChecks
test declRefTypeValid IdentifierChecks
test intCallInvalid IdentifierChecks
test NewClassInvalid IdentifierChecks
test NewClassValid IdentifierChecks

test initbothifelsinvalid InitializationCheck
test initonlywhileInvalid InitializationCheck

test OwnerExprInvalid methodCall
test OwnerExprValid methodCall
test OwnerExprValidField methodCall
test OwnerExprValidNew methodCall
test OwnerExprValidThis methodCall

test duplicated_formal_args ClassChecks
test duplicated_vars_in_method ClassChecks
test method_ret_type_invalid ClassChecks
test method_same_name_in_inheritance_class ClassChecks
test method_same_name_in_inheritance_class2 ClassChecks
test method_same_name_in_same_class ClassChecks
test several_fields_in_the_inheritance_class ClassChecks
test several_fields_in_the_same_class ClassChecks
test main_name_duplicate ClassChecks

test MethodDoesNotExist methodCallSignature
test MethodOverrideValid methodCallSignature
test MethodOverloadingDiffType methodCallSignature
test MethodOverloadingExtraParam methodCallSignature
test MethodCallWrongActual methodCallSignature
test MethodCallWrongActual2 methodCallSignature


test arrayNameInvalid varDeclarationChecks
test arraysAssignInvalid varDeclarationChecks
test AssignLvInvalid varDeclarationChecks
test AssignRvInvalid varDeclarationChecks
test BinaryAddInvalid varDeclarationChecks
test CallInvalid varDeclarationChecks
test IfInvalid varDeclarationChecks
test IfValid varDeclarationChecks
test OwnerExprNameInvalid varDeclarationChecks
test sysoutInvalid varDeclarationChecks
test whileValid varDeclarationChecks

test AssignmentInvalid yotam
test AssignmentValid yotam
test InitVarInvalid yotam
test InitVarValid yotam
test OwnerExprInvalid yotam
test OwnerExprValid yotam
