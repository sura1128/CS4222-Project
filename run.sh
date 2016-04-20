cd ./ActivitySim
echo "\n....jarifying sim....\n"
ant jarify
echo "\n....running sim....\n"

for entry in "../Traces"/*
do
	echo "\n.... SIMULATING $entry ....\n"
	java -jar ActivitySim.jar ../Traces/$entry
done

cd ../ActivityEval
echo "\n....jarifying eval....\n"
ant jarify
echo "\n....running eval....\n"

for entry in "../Traces"/*
do
	echo "\n.... EVALUATING $entry ....\n"
	java -jar ActivityEval.jar ../Traces/$entry
done

