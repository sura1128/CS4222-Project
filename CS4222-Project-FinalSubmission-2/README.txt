CS4222 Project: User Activity Tracking 
Group 2
Akshat Dubey
  A0103516U

Suranjana Sengupta 
A0102800A

Changes made in source file ActivityDetection.java

## Significant Changes made in Stage 2: 

1. Autocorrelation Algorithm for walking
2. Barometer sensing for altitude changes when going into MRT
3. No changes were made in ActivityTrackApp. 

Important Flags:

1. isWalking: set to TRUE if autocorrelation is greater than WALK_THRESHOLD_CORRELATION.   
2. isMoving: set to TRUE if the user speed (from GPS) is greater than a certain SPEED_THRESHOLD.
3. isUserOutside: set to TRUE if the light sensor reading is greater than a certain LIGHT_THRESHOLD_OUTDOOR.
4. isInPocket: set to TRUE if the proximity sensor records a proximity of less than 0.
5. isUnderground: set to TRUE is the barometer alttude recorded is lesser than a certain HEIGHT_THRESHOLD. 
6. isLocationGPS: set to TRUE is location provider is GPS else FALSE if provider is network. 

Values for important THRESHOLDS:
1. WALK_THRESHOLD_CORRELATION = 0.65
2. SPEED_THRESHOLD = 2.7
3. LIGHT_THRESHOLD_INDOOR = 220
4. LIGHT_THRESHOLD_OUTDOOR = 350
5. UNDER_THRESH = -5
6. OVER_THRESH = -3
7. WALK_THRESHOLD = 1.3

Functions Modified:

1. onLinearAcclSensorChanged(...)
Stage One calculation of Threshold remains. But a feature for autocorrelation is added. An acclFilter is created to find the median values ranging over 15 acceleration values obtained and store the median in an acclBuffer. Following this, an autocorrelation is obtained and compared to the WALK_THRESHOLD_CORRELATION value, and if it is greater than this value, it is assumed that a repetitive pattern has occured and the user is now walking.

2. onProximitySensorChanged(...)
A check is done on the proximity of the phone to the user, and based on this a variable called isInPocket is updated, so as to add an additional check for the user's location (inside or outside). We assume that if the phone is in the pocket (proximity < 0) then the user is outside.

3. onLightSensorChanged(...)
A light sensor filter is created to record the light sensor readings and find the median every 20 readings. Following this, if the phone is not inside the pocket, and the light sensor reading is less than the LIGHT_THRESHOLD_INDOOR and if the user is outside, in which case we set an isUserOutside variable to false, i.e. the user must be indoors. Similarly, if the user is inside and the light sensor reading exceeds the LIGHT_THRESHOLD_OUTDOOR then the user is definitely outside. If the phone is in pocket, then we automatically assume the user is outside.

4. onBarometerSensorChanged(...)
A barometer filter is created to record the barometer height readings and find the median every 20 readings. If the height obtained is lesser than UNDER_THRESH then we set isUnderground to TRUE, because if the user is below 5 meters, they are underground. If the height is over OVER_THRESH then the user is not underground and isUnderground is set to FALSE. 

4. onLocationSensorChanged(...)
There are two conditions. If the provider is network, we do not store the speed (as it is recorded as -1) and simply set the isLocationGPS flag to FALSE. If the provider is GPS, we perform the following calculations. A location buffer is created. The user speed is taken as the speed recorded by the location sensor. It is compared to the SPEED_THRESHOLD (which is set to 2.72) and if it is greater than the threshold, we set an isMoving variable to true, which signifies that the user is in a VEHICLE (example: we have assumed this to be BUS).  
Also, we update a calculateGPS flag to True if and only if there are a minimum of 2 readings from GPS, so that we only initialize the current activity after the first location change and user speed is detected. 

5. run()
A flag called initDone is set to false initially, so as to run an initialization the first time the program is run and set a currentActivity variable to an initial default state. Here, we implement a simple if...else to check if the flags isMoving, isWalking, isUserOutside are true or false, before we set the currentActivity. Once this initalization is run, we call the changeActivity() function, for as many times as the program is run. 

Functions Added:

1. changeActivity()
A switch-case is implemented to check for change in states. The cases IDLE_INDOOR and IDLE_OUTDOOR are considered to have the same case conditions as the user can either switch to walking or vehicle from these states. Therefore, the first check is done for isMoving and if the user isn't moving a check is done to see if the location provider is network. If the user isUnderground this will automatically mean that they are inside the MRT. Following this, the isWalking flag is checked to check if the user is walking before changing state to walking. Similarly, the cases BUS, TRAIN, CAR and WALKING are checked to test if the user isMoving before checking isWalking.  


2. getStandardDeviation()
Returns the standard deviation for linear acceleration values in the acclBuffer based on BUFFER_SIZE. 

3. getMean()
Returns the mean of linear acceleration values in the acclBuffer based on BUFFER_SIZE. 

4. getMagnitude(x,y,z)
Returns the magnitude of acceleration values, based on x,y and z values.

6. getMedian(array[])
Returns the median of an array of values passed into the arguments. 

## STAGE TWO RESULTS FOR OUR TRACE##
ActivityEval Results:
We received the following results after running ActivityEval on our code.


Based on this we can note that the state with highest accuracy is VEHICLE (89.05%) and least is IDLE_INDOOR (63.21%). However, the overall accuracy stands at 79.89%. 


## STAGE TWO RESULTS 

** Evaluating trace in folder '../Traces/../Traces/T1' :
* DURATION of the trace: 2.17 hrs (130.12 min)
* CONFUSION MATRIX (Rows are ground truth, columns are detected states):
Truth/Detect  IDLE_INDOOR IDLE_OUTDOOR      WALKING      VEHICLE        OTHER
 IDLE_INDOOR       58.50%        0.00%       39.64%        1.10%        0.76%
IDLE_OUTDOOR        0.15%       59.44%       40.40%        0.00%        0.00%
     WALKING       22.11%       28.67%       49.22%        0.00%        0.00%
     VEHICLE       45.17%        3.12%       32.63%       19.08%        0.00%
* ACCURACY:
	IDLE_INDOOR (48.35 min [0.81 hrs]): 58.50%
	IDLE_OUTDOOR (10.77 min [0.18 hrs]): 59.44%
	WALKING (36.33 min [0.61 hrs]): 49.22%
	VEHICLE (34.68 min [0.58 hrs]): 19.08%
	OVERALL Accuracy: 45.48%
* LATENCY:
	IDLE_INDOOR: 0.12 min (7.28 sec) [missed 0 out of 3]
	IDLE_OUTDOOR: 0.00 min (0.00 sec) [missed 0 out of 1]
	WALKING: 0.00 min (0.00 sec) [missed 0 out of 3]
	VEHICLE: 0.11 min (6.75 sec) [missed 0 out of 2]

.... EVALUATING ../Traces/T2 ....

Found a data collection trace in folder '../Traces/../Traces/T2'

** Evaluating trace in folder '../Traces/../Traces/T2' :
* DURATION of the trace: 2.21 hrs (132.55 min)
* CONFUSION MATRIX (Rows are ground truth, columns are detected states):
Truth/Detect  IDLE_INDOOR IDLE_OUTDOOR      WALKING      VEHICLE        OTHER
 IDLE_INDOOR       60.35%        0.00%       39.65%        0.00%        0.00%
IDLE_OUTDOOR        0.00%       56.53%       41.90%        1.56%        0.00%
     WALKING       20.78%       39.59%       37.45%        1.12%        1.05%
     VEHICLE       28.41%        8.23%       36.52%       26.84%        0.00%
* ACCURACY:
	IDLE_INDOOR (18.03 min [0.30 hrs]): 60.35%
	IDLE_OUTDOOR (24.50 min [0.41 hrs]): 56.53%
	WALKING (47.48 min [0.79 hrs]): 37.45%
	VEHICLE (42.53 min [0.71 hrs]): 26.84%
	OVERALL Accuracy: 40.69%
* LATENCY:
	IDLE_INDOOR: 0.00 min (0.00 sec) [missed 0 out of 1]
	IDLE_OUTDOOR: 0.00 min (0.00 sec) [missed 0 out of 2]
	WALKING: 0.18 min (10.66 sec) [missed 0 out of 3]
	VEHICLE: 0.00 min (0.00 sec) [missed 0 out of 2]

.... EVALUATING ../Traces/T3 ....

Found a data collection trace in folder '../Traces/../Traces/T3'

** Evaluating trace in folder '../Traces/../Traces/T3' :
* DURATION of the trace: 1.51 hrs (90.89 min)
* CONFUSION MATRIX (Rows are ground truth, columns are detected states):
Truth/Detect  IDLE_INDOOR IDLE_OUTDOOR      WALKING      VEHICLE        OTHER
 IDLE_INDOOR       23.54%       33.25%       43.21%        0.00%        0.00%
IDLE_OUTDOOR        0.00%       46.48%       36.57%        0.00%       16.95%
     WALKING       15.27%       35.63%       49.10%        0.00%        0.00%
     VEHICLE        6.31%       21.45%       27.33%       44.91%        0.00%
* ACCURACY:
	IDLE_INDOOR (18.90 min [0.32 hrs]): 23.54%
	IDLE_OUTDOOR (16.82 min [0.28 hrs]): 46.48%
	WALKING (20.30 min [0.34 hrs]): 49.10%
	VEHICLE (34.88 min [0.58 hrs]): 44.91%
	OVERALL Accuracy: 41.69%
* LATENCY:
	IDLE_INDOOR: 0.10 min (6.20 sec) [missed 0 out of 2]
	IDLE_OUTDOOR: 1.42 min (85.01 sec) [missed 0 out of 2]
	WALKING: 0.00 min (0.00 sec) [missed 0 out of 3]
	VEHICLE: 0.61 min (36.68 sec) [missed 0 out of 2]

.... EVALUATING ../Traces/T4 ....

Found a data collection trace in folder '../Traces/../Traces/T4'

** Evaluating trace in folder '../Traces/../Traces/T4' :
* DURATION of the trace: 2.02 hrs (121.03 min)
* CONFUSION MATRIX (Rows are ground truth, columns are detected states):
Truth/Detect  IDLE_INDOOR IDLE_OUTDOOR      WALKING      VEHICLE        OTHER
 IDLE_INDOOR       58.46%        0.00%       41.54%        0.00%        0.00%
IDLE_OUTDOOR        0.06%       61.73%       35.36%        1.49%        1.36%
     WALKING        7.75%       56.51%       35.75%        0.00%        0.00%
     VEHICLE        2.83%       21.81%       21.88%       53.48%        0.00%
* ACCURACY:
	IDLE_INDOOR (20.58 min [0.34 hrs]): 58.46%
	IDLE_OUTDOOR (26.92 min [0.45 hrs]): 61.73%
	WALKING (22.80 min [0.38 hrs]): 35.75%
	VEHICLE (50.73 min [0.85 hrs]): 53.48%
	OVERALL Accuracy: 52.82%
* LATENCY:
	IDLE_INDOOR: 0.00 min (0.00 sec) [missed 0 out of 1]
	IDLE_OUTDOOR: 0.18 min (10.96 sec) [missed 0 out of 2]
	WALKING: 0.00 min (0.00 sec) [missed 0 out of 3]
	VEHICLE: 0.00 min (0.00 sec) [missed 0 out of 2]

.... EVALUATING ../Traces/T5 ....

Found a data collection trace in folder '../Traces/../Traces/T5'

** Evaluating trace in folder '../Traces/../Traces/T5' :
* DURATION of the trace: 2.09 hrs (125.18 min)
* CONFUSION MATRIX (Rows are ground truth, columns are detected states):
Truth/Detect  IDLE_INDOOR IDLE_OUTDOOR      WALKING      VEHICLE        OTHER
 IDLE_INDOOR       37.36%        0.00%       23.43%        0.00%       39.21%
IDLE_OUTDOOR        0.00%       53.51%       46.49%        0.00%        0.00%
     WALKING       10.89%       53.36%       35.70%        0.05%        0.00%
     VEHICLE       26.01%        3.74%       29.52%       40.73%        0.00%
* ACCURACY:
	IDLE_INDOOR (33.37 min [0.56 hrs]): 37.36%
	IDLE_OUTDOOR (17.35 min [0.29 hrs]): 53.51%
	WALKING (30.77 min [0.51 hrs]): 35.70%
	VEHICLE (43.70 min [0.73 hrs]): 40.73%
	OVERALL Accuracy: 40.37%
* LATENCY:
	IDLE_INDOOR: 13.07 min (784.42 sec) [missed 0 out of 1]
	IDLE_OUTDOOR: 0.00 min (0.00 sec) [missed 0 out of 1]
	WALKING: 0.00 min (0.00 sec) [missed 0 out of 3]
	VEHICLE: 0.67 min (40.15 sec) [missed 0 out of 2]

.... EVALUATING ../Traces/T6 ....

Found a data collection trace in folder '../Traces/../Traces/T6'

** Evaluating trace in folder '../Traces/../Traces/T6' :
* DURATION of the trace: 2.23 hrs (133.59 min)
* CONFUSION MATRIX (Rows are ground truth, columns are detected states):
Truth/Detect  IDLE_INDOOR IDLE_OUTDOOR      WALKING      VEHICLE        OTHER
 IDLE_INDOOR       44.84%        0.00%       28.94%        0.00%       26.22%
IDLE_OUTDOOR        0.00%       60.30%       39.70%        0.00%        0.00%
     WALKING       30.93%       22.20%       43.54%        0.70%        2.63%
     VEHICLE       22.10%       18.90%       38.40%       20.61%        0.00%
* ACCURACY:
	IDLE_INDOOR (20.85 min [0.35 hrs]): 44.84%
	IDLE_OUTDOOR (28.63 min [0.48 hrs]): 60.30%
	WALKING (45.05 min [0.75 hrs]): 43.54%
	VEHICLE (39.07 min [0.65 hrs]): 20.61%
	OVERALL Accuracy: 40.63%
* LATENCY:
	IDLE_INDOOR: 0.00 min (0.00 sec) [missed 1 out of 2]
	IDLE_OUTDOOR: 0.00 min (0.00 sec) [missed 0 out of 3]
	WALKING: 0.33 min (19.69 sec) [missed 0 out of 4]
	VEHICLE: 0.61 min (36.70 sec) [missed 0 out of 3]

.... EVALUATING ../Traces/T7 ....

Found a data collection trace in folder '../Traces/../Traces/T7'

** Evaluating trace in folder '../Traces/../Traces/T7' :
* DURATION of the trace: 2.14 hrs (128.67 min)
* CONFUSION MATRIX (Rows are ground truth, columns are detected states):
Truth/Detect  IDLE_INDOOR IDLE_OUTDOOR      WALKING      VEHICLE        OTHER
 IDLE_INDOOR       56.61%        3.04%       39.68%        0.66%        0.00%
IDLE_OUTDOOR        0.00%       49.95%       47.98%        0.00%        2.07%
     WALKING       53.65%        3.34%       41.88%        1.13%        0.00%
     VEHICLE       22.81%        6.94%       31.18%       39.07%        0.00%
* ACCURACY:
	IDLE_INDOOR (30.12 min [0.50 hrs]): 56.61%
	IDLE_OUTDOOR (17.75 min [0.30 hrs]): 49.95%
	WALKING (29.45 min [0.49 hrs]): 41.88%
	VEHICLE (51.37 min [0.86 hrs]): 39.07%
	OVERALL Accuracy: 45.32%
* LATENCY:
	IDLE_INDOOR: 0.00 min (0.00 sec) [missed 0 out of 1]
	IDLE_OUTDOOR: 0.18 min (10.75 sec) [missed 0 out of 2]
	WALKING: 0.00 min (0.00 sec) [missed 0 out of 2]
	VEHICLE: 0.49 min (29.39 sec) [missed 0 out of 2]


STAGE 2 RESULTS FOR OUR TRACES
.... EVALUATING ../Traces/nexus4 ....

Found a data collection trace in folder '../Traces/../Traces/nexus4'

** Evaluating trace in folder '../Traces/../Traces/nexus4' :
* DURATION of the trace: 2.26 hrs (135.80 min)
* CONFUSION MATRIX (Rows are ground truth, columns are detected states):
Truth/Detect  IDLE_INDOOR IDLE_OUTDOOR      WALKING      VEHICLE        OTHER
 IDLE_INDOOR       36.16%        0.00%       31.23%       32.62%        0.00%
IDLE_OUTDOOR       23.76%       20.68%       55.56%        0.00%        0.00%
     WALKING        4.33%        6.04%       80.01%        8.36%        1.25%
     VEHICLE       11.20%        1.97%       13.44%       73.39%        0.00%
* ACCURACY:
	IDLE_INDOOR (13.18 min [0.22 hrs]): 36.16%
	IDLE_OUTDOOR (20.55 min [0.34 hrs]): 20.68%
	WALKING (54.60 min [0.91 hrs]): 80.01%
	VEHICLE (47.48 min [0.79 hrs]): 73.39%
	OVERALL Accuracy: 64.46%
* LATENCY:
	IDLE_INDOOR: 0.00 min (0.00 sec) [missed 0 out of 3]
	IDLE_OUTDOOR: 0.00 min (0.00 sec) [missed 1 out of 3]
	WALKING: 0.15 min (8.71 sec) [missed 0 out of 7]
	VEHICLE: 0.30 min (18.28 sec) [missed 1 out of 5]

.... EVALUATING ../Traces/nexus5 ....

Found a data collection trace in folder '../Traces/../Traces/nexus5'

** Evaluating trace in folder '../Traces/../Traces/nexus5' :
* DURATION of the trace: 3.36 hrs (201.36 min)
* CONFUSION MATRIX (Rows are ground truth, columns are detected states):
Truth/Detect  IDLE_INDOOR IDLE_OUTDOOR      WALKING      VEHICLE        OTHER
 IDLE_INDOOR       90.12%        0.00%        6.03%        3.85%        0.00%
IDLE_OUTDOOR       18.33%       21.09%       55.21%        5.37%        0.00%
     WALKING        0.59%        5.82%       76.69%       14.67%        2.23%
     VEHICLE        7.70%        3.83%       30.19%       58.27%        0.00%
* ACCURACY:
	IDLE_INDOOR (79.27 min [1.32 hrs]): 90.12%
	IDLE_OUTDOOR (21.73 min [0.36 hrs]): 21.09%
	WALKING (53.83 min [0.90 hrs]): 76.69%
	VEHICLE (46.53 min [0.78 hrs]): 58.27%
	OVERALL Accuracy: 71.72%
* LATENCY:
	IDLE_INDOOR: 0.00 min (0.00 sec) [missed 0 out of 3]
	IDLE_OUTDOOR: 0.00 min (0.00 sec) [missed 1 out of 3]
	WALKING: 0.17 min (10.16 sec) [missed 0 out of 7]
	VEHICLE: 0.08 min (4.91 sec) [missed 0 out of 5]


In conclusion, we can say that by carrying out the changes in Stage 2 as noted, we were able to achieve higher accuracies for other traces. Our overall accuracy dropped from 79% to 64.46%. However, in our previous test with other traces our accuracy dropped to 47% for other traces, which was a difference of 32%. But, this time our average accuracy observed from other traces is around 50%. The difference is now 14%. The WALKING state is the main difficulty faced as the change in idle state is often mistaken for walking. The highest accuracy observed is WALKING which is 80% and lowest is IDLE_OUTDOOR which is 20.68%, this is probably because of the interchangeability between walking and idle states. 
Moreover, the barometer values helped to detect the presence of the user inside  MRT, which greatly improved our vehicle accuracy. The IDLE_INDOOR also improved due to this feature, as now all the MRT rides will be detected correctly. 



STUDENT CONTRIBUTION:

1. Akshat Dubey: Autocorrelation, changing activity detection, location sensor changing, proximity sensor changing
2. Suranjana Sengupta: Barometer sensor changing, changing activity detection, light sensor changing, acceleration changing
