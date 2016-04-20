cd ./ActivitySim
echo "\n....jarifying sim....\n"
ant jarify
echo "\n....running sim....\n"
java -jar ActivitySim.jar ../nexus4
java -jar ActivitySim.jar ../nexus5
cd ../ActivityEval
echo "\n....jarifying eval....\n"
ant jarify
echo "\n....running eval....\n"
java -jar ActivityEval.jar ../nexus4 
echo "\n....FOR NEXUS 5 NOW....\n"
java -jar ActivityEval.jar ../nexus5 

