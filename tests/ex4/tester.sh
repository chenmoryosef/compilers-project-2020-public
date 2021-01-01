## assumptions:
# 1. in order to reach the compiled jar you need to run ../../mjavac
# 2. tests folder 'Test' has xml and 'Test.res' file (expected result)


## results:
# if the script & your code are working properly
# you should see the names of all of the tests with "Success"


COMMAND="java -jar ../../mjavac.jar parse print"
FILEPATH="."
function test()
{
	JAVAFILE=$1
	TESTFOLDER=$1
	echo "Running Test:" $TESTFOLDER
	result_file=$TESTFOLDER/$TESTFOLDER.java.ours
	log_file=$TESTFOLDER/$TESTFOLDER.log
	if [ -f $result_file ]; then
	   rm $TESTFOLDER/$TESTFOLDER.java.ours
	fi
	if [ -f $log_file ]; then
	   rm $TESTFOLDER/$TESTFOLDER.log
	fi

	$COMMAND $TESTFOLDER/$JAVAFILE.java $result_file > $log_file
	diff -b -w -E -Z -B $result_file $TESTFOLDER/$JAVAFILE.java
	if [ $? -eq 0 ]
	then
		echo Success
	else
		echo Fail
	fi
}

#ex4:
test main-class

