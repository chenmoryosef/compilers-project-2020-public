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
	echo "Running Test:" $TESTFOLDER
	result_file=$TESTFOLDER/$TESTFOLDER.our-result
	log_file=$TESTFOLDER/$TESTFOLDER.log
	if [ -f $result_file ]; then
	   rm $TESTFOLDER/$TESTFOLDER.our-result
	fi
	if [ -f $log_file ]; then
	   rm $TESTFOLDER/$TESTFOLDER.log
	fi

	$COMMAND $TESTFOLDER/$XMLFILE.xml $result_file > $log_file
	diff $result_file $TESTFOLDER/$TESTFOLDER.res
	if [ $? -eq 0 ]
	then
		echo Success
	else
		echo Fail
	fi
}

#ex3:
test AssignmentInvalid.java AssignmentInvalid
test AssignmentValid.java AssignmentValid
test InitVarInvalid.java InitVarInvalid
test InitVarValid.java InitVarValid
test OwnerExprInvalid.java OwnerExprInvalid
test OwnerExprValid.java OwnerExprValid
test MethodInvalid MethodInvalid
test 1orderOfClassesDecls 1orderOfClassesDecls
test 2mainInheritance 2mainInheritance
test 3classesWithSameName 3classesWithSameName
test 4FieldInheritClassSameName 4FieldInheritClassSameName
test 4FieldWithSameDecl 4FieldWithSameDecl
test 5methodsWithSameName 5methodsWithSameName
test 6declOfOverrodeMethodRetVal 6declOfOverrodeMethodRetVal
test 6declOfOverrideMethodFotmalVal 6declOfOverrideMethodFotmalVal
test 6declOfOverrideMethodSubTypeValid 6declOfOverrideMethodSubTypeValid
test 8declareObjectOfUndeclaredClass 8declareObjectOfUndeclaredClass
test 9Invalid 9Invalid
test 9Valid 9Valid
test 10methodCallOnlyFromObject 10methodCallOnlyFromObject
test 11methodCallStaticType 11methodCallStaticType
test 12OwnerExprInvalid 12OwnerExprInvalid
test 12OwnerExprThisValid 12OwnerExprThisValid
test 13LengthValid 13LengthValid
test 13LengthInvalid 13LengthInvalid
test 13LengthValid2 13LengthValid2
test 14notDefined 14notDefined
test 14definedInDiffClass 14definedInDiffClass
test 15IfInitialized 15IfInitialized
test 16assignmentInvalid 16assignmentInvalid
test 16assignmentValid 16assignmentValid
test 17ifCond 17ifCond
test 17whileCond 17whileCond
test 18returnValueInvalid 18returnValueInvalid
test 18returnValueValid 18returnValueValid
test 20printInt 20printInt
test 21multInvalid 21multInvalid
test 21notInvalid 21notInvalid
test 21addInvalid 21addInvalid
test 22arrayInvalid 22arrayInvalid
test 24varsWithSameName 24varsWithSameName

