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

#ex3 examples in yotam's git:
test AssignmentInvalid.java AssignmentInvalid
test AssignmentValid.java AssignmentValid
test InitVarInvalid.java InitVarInvalid
test InitVarValid.java InitVarValid
test OwnerExprInvalid.java OwnerExprInvalid
test OwnerExprValid.java OwnerExprValid
test MethodInvalid MethodInvalid
