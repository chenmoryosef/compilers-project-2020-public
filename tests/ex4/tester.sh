## assumptions:
# 1. in order to reach the compiled jar you need to run ../../mjavac
# 2. tests folder 'Test' has xml and 'Test.res' file (expected result)


## results:
# if the script & your code are working properly
# you should see the names of all of the tests with "Success"


COMMAND="java -jar ../../mjavac.jar parse print"
COMMAND_XML="java -jar ../../mjavac.jar parse marshal"
COMMAND_GOLDEN="java -jar ../../mjavac.jar unmarshal print"
FILEPATH="."
function test()
{
	JAVAFILE=$2
	TESTFOLDER=$1
	echo "Running Test:" $TESTFOLDER / $JAVAFILE
	result_file=$TESTFOLDER/$JAVAFILE.java.ours
	golden_file=$TESTFOLDER/$JAVAFILE.java
	log_file=$TESTFOLDER/$JAVAFILE.log
	if [ -f $result_file ]; then
	   rm $TESTFOLDER/$JAVAFILE.java.ours
	fi
	if [ -f $log_file ]; then
	   rm $TESTFOLDER/$JAVAFILE.log
	fi
	$COMMAND $TESTFOLDER/$JAVAFILE.java $result_file > $log_file
	diff -b -w -E -Z -B $result_file $golden_file
	if [ $? -eq 0 ]
	then
		echo Success
	else
		echo Fail
	fi
}

function test_xml()
{
	JAVAFILE=$2
	TESTFOLDER=$1
	echo "Running Test:" $TESTFOLDER / $JAVAFILE
	result_file=$TESTFOLDER/$JAVAFILE.java.ours
	golden_file=$TESTFOLDER/$JAVAFILE.java.xml
	log_file=$TESTFOLDER/$JAVAFILE.log
	if [ -f $result_file ]; then
	   rm $result_file
	fi
	if [ -f $log_file ]; then
	   rm $log_file
	fi
	# create java with out parser
	$COMMAND $TESTFOLDER/$JAVAFILE.java $result_file.java > $log_file
	# create java with unmarshal - for debug
#	$COMMAND_GOLDEN $golden_file $result_file.real.java > $log_file
	# create xml with marshal
	$COMMAND_XML $TESTFOLDER/$JAVAFILE.java $result_file > $log_file
	# ignore spaces, tabs, new lines and lineNumber lines
	diff -b -w -E -Z -B -I '^.*<lineNumber>' $result_file $golden_file
	if [ $? -eq 0 ]
	then
		echo Success
	else
		echo Fail
	fi
}


function test_invalid()
{
	JAVAFILE=$2
	TESTFOLDER=$1
	echo "Running Test:" $TESTFOLDER / $JAVAFILE
	result_file=$TESTFOLDER/$JAVAFILE.java.ours
	golden_file=$TESTFOLDER/$JAVAFILE.err
	log_file=$TESTFOLDER/$JAVAFILE.log
	if [ -f $result_file ]; then
	   rm $result_file
	fi
	if [ -f $log_file ]; then
	   rm $log_file
	fi
	# create java with out parser
	$COMMAND $TESTFOLDER/$JAVAFILE.java $result_file.java 2>> $log_file

	diff -w $log_file $golden_file
	if [ $? -eq 0 ]
	then
		echo Success
	else
		echo Fail
	fi
}

#ex4:
test main-class main-class
test class_decl class_decl
test class_decls class_decls

test_xml ast BubbleSort
test_xml ast LinearSearch
test_xml ast QuickSort
test_xml ast TreeVisitor
test_xml ast Factorial
test_xml ast LinkedList

test_invalid Invalid InvalidLexing
test_invalid Invalid InvalidLexing2
test_invalid Invalid InvalidParsing
test_invalid class_decls_fail2 class_decls_fail2
test_invalid class_decls_fail class_decls_fail
 

