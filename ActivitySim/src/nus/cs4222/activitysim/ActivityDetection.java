package nus.cs4222.activitysim;

import java.io.*;
import java.util.*;
import java.text.*;

import android.hardware.*;
import android.util.*;

/**
 * Class containing the activity detection algorithm.
 *
 * <p>
 * You can code your activity detection algorithm in this class. (You may add
 * more Java class files or add libraries in the 'libs' folder if you need). The
 * different callbacks are invoked as per the sensor log files, in the
 * increasing order of timestamps. In the best case, you will simply need to
 * copy paste this class file (and any supporting class files and libraries) to
 * the Android app without modification (in stage 2 of the project).
 *
 * <p>
 * Remember that your detection algorithm executes as the sensor data arrives
 * one by one. Once you have detected the user's current activity, output it
 * using the {@link ActivitySimulator.outputDetectedActivity(UserActivities)}
 * method. If the detected activity changes later on, then you need to output
 * the newly detected activity using the same method, and so on. The detected
 * activities are logged to the file "DetectedActivities.txt", in the same
 * folder as your sensor logs.
 *
 * <p>
 * To get the current simulator time, use the method
 * {@link ActivitySimulator.currentTimeMillis()}. You can set timers using the
 * {@link SimulatorTimer} class if you require. You can log to the console/DDMS
 * using either {@code System.out.println()} or using the
 * {@link android.util.Log} class. You can use the
 * {@code SensorManager.getRotationMatrix()} method (and any other helpful
 * methods) as you would normally do on Android.
 *
 * <p>
 * Note: Since this is a simulator, DO NOT create threads, DO NOT sleep(), or do
 * anything that can cause the simulator to stall/pause. You can however use
 * timers if you require, see the documentation of the {@link SimulatorTimer}
 * class. In the simulator, the timers are faked. When you copy the code into an
 * actual Android app, the timers are real, but the code of this class does not
 * need not be modified.
 */
public class ActivityDetection {
	/*init */
	private boolean initDone = false;

	/** To format the UNIX millis time as a human-readable string. */
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-h-mm-ssa");
	private boolean isFirstAcclReading = true;
	private int numberTimers = 1;

	// String for Current Activity
	private UserActivities currentActivity = UserActivities.IDLE_INDOOR;

	// ACCELEROMETER
	private int BUFFER_SIZE = 200;
	private int BUFFER_SIZE_ACCL_FILTER = 10;
	private double acclBuffer[] = new double[BUFFER_SIZE];
	private double acclFilter[] = new double[BUFFER_SIZE_ACCL_FILTER];
	private int accFilterIndex = 0;
	private int accIndex = 0;
	private double walkSdev = 0;
	private double walkAutoC = 0;
	private double WALK_THRESHOLD = 1.3;
	private double WALK_THRESHOLD_correlation = 0.65;

	private boolean isWalking = false; //WALKING FLAG

	//Location
	private boolean calculateGPS=false;
	private int LOCATION_BUFF_SIZE=10;
	private double locationBuffer[][] = new double[LOCATION_BUFF_SIZE][3];
	private boolean hasMoved = false;
	private int locationIndex=0;
	private double latStdDev = 0.0;
	private double longStdDev = 0.0;
	private boolean gpsEvent = false;

	//VEHICLE
	private double userSpeed=0;
	private double SPEED_THRESHOLD = 2.7;
	private boolean isMoving = false; //VEHICLE FLAG
	private boolean isLocationGPS = false;

	// LIGHT
	private float LIGHT_THRESHOLD_INDOOR = 220;
	private float LIGHT_THRESHOLD_OUTDOOR = 350;
	private int LIGHT_BUFF_SIZE = 20;
	private double lightSensorFilter[] = new double[LIGHT_BUFF_SIZE];
	private int lightIndex = 0;
	private double lightSensorClean = 0;

	private boolean isUserOutside = false;

	//PROXIMITY
	private boolean isInPocket = false;

	//BAROMETER
	private double UNDER_THRESH = -5;
	private double OVER_THRESH = -3;
	private boolean isUnderground = false;
	private int BAROMETER_BUFFER_SIZE = 20;
	private double barometerFilter[] = new double[BAROMETER_BUFFER_SIZE];
	private int baroIndex = 0;
	private double medianHeight = 0;
	private float HEIGHT_THRESHOLD = -5;

	/**
	 * Called when the accelerometer sensor has changed.
	 *
	 * @param timestamp
	 *            Timestamp of this sensor event
	 * @param x
	 *            Accl x value (m/sec^2)
	 * @param y
	 *            Accl y value (m/sec^2)
	 * @param z
	 *            Accl z value (m/sec^2)
	 * @param accuracy
	 *            Accuracy of the sensor data (you can ignore this)
	 */
	public void onAcclSensorChanged(long timestamp, float x, float y, float z, int accuracy) {

		// Process the sensor data as they arrive in each callback,
		// with all the processing in the callback itself (don't create
		// threads).

		// You will most likely not need to use Timers at all, it is just
		// provided for convenience if you require.

		// Here, we just show a dummy example of creating a timer
		// to execute a task 10 minutes later.
		// Be careful not to create too many timers!
		if (isFirstAcclReading) {
			isFirstAcclReading = false;
			SimulatorTimer timer = new SimulatorTimer();
			timer.schedule(this.task, // Task to be executed
					1 * 1000); // Delay in millisec (10 min)
		}
	}

	/**
	 * Called when the gravity sensor has changed.
	 *
	 * @param timestamp
	 *            Timestamp of this sensor event
	 * @param x
	 *            Gravity x value (m/sec^2)
	 * @param y
	 *            Gravity y value (m/sec^2)
	 * @param z
	 *            Gravity z value (m/sec^2)
	 * @param accuracy
	 *            Accuracy of the sensor data (you can ignore this)
	 */
	public void onGravitySensorChanged(long timestamp, float x, float y, float z, int accuracy) {
	}

	/**
	 * Called when the linear accelerometer sensor has changed.
	 *
	 * @param timestamp
	 *            Timestamp of this sensor event
	 * @param x
	 *            Linear Accl x value (m/sec^2)
	 * @param y
	 *            Linear Accl y value (m/sec^2)
	 * @param z
	 *            Linear Accl z value (m/sec^2)
	 * @param accuracy
	 *            Accuracy of the sensor data (you can ignore this)
	 */
	public void onLinearAcclSensorChanged(long timestamp, float x, float y, float z, int accuracy) {

		accIndex = accIndex % BUFFER_SIZE;		
		accFilterIndex = accFilterIndex % BUFFER_SIZE_ACCL_FILTER;
		acclFilter[accFilterIndex] = getMagnitude(x,y,z);
		acclBuffer[accIndex] = getMedian(acclFilter);
		accFilterIndex++;
		accIndex++;

		// just to init everything
		if (isFirstAcclReading) {
			isFirstAcclReading = false;
			SimulatorTimer timer = new SimulatorTimer();
			timer.schedule(this.task, // Task to be executed
					1* 1000); // Delay in millisec (10 min)
		}
		walkSdev = getStandardDeviation();
		walkAutoC = getAutoCorrelation();
		//System.out.println("Autocorrelation = " + walkAutoC);

		//if(walkAutoC > WALK_THRESHOLD_correlation){
		if (walkSdev > WALK_THRESHOLD || walkAutoC > WALK_THRESHOLD_correlation){	
			isWalking = true;
		}else {
			isWalking = false;
		}

		}

	double getStandardDeviation() {
		double mean = getMean();
		double sum = 0.0;
		for (int i = 0; i < BUFFER_SIZE; i++) {
			sum += Math.pow((acclBuffer[i] - mean), 2);
		}
		double stdDev = Math.sqrt((1.0 / (double)BUFFER_SIZE) * sum);
		return stdDev;
	}

	double getMean() {
		double sum = 0.0;
		for (int j = 0; j < BUFFER_SIZE; j++) {
			sum += acclBuffer[j];
		}
		return sum / (float) BUFFER_SIZE;
	}

	double getMagnitude(float x, float y, float z) {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
	}

	double getAutoCorrelation(){
		double maxCorrelation = 0;
		double tempCorrelation = 0;
		double denominator = 0;
		double numerator = 0;
		double doubleWindow = 0;
		double mean1 = 0;
		double mean2 = 0;
		double variance1 = 0;
		double variance2 = 0;
		double shiftedArray[] = new double[BUFFER_SIZE];
		int actualIndex = (accIndex+1)%BUFFER_SIZE;
		// iterate over the buffer to create the adjusted array
		for (int i = 0; i<BUFFER_SIZE; i++) { 
			shiftedArray[i] = acclBuffer[actualIndex];
			actualIndex = (actualIndex+1)%BUFFER_SIZE;
		}
		for(int window_size=30; window_size<(BUFFER_SIZE/2); window_size++){ //k is the window size
			doubleWindow = (double)window_size;
			mean1 = 0;
			mean2 = 0;
			variance1 = 0;
			variance2 = 0;
			/// calculate mean
			for(int a = 0; a<window_size; a++){
				mean1 += shiftedArray[a];
				mean2 += shiftedArray[window_size+a];
			}
			mean1 /= doubleWindow;
			mean2 /= doubleWindow;
			// calculate variance
			for (int b = 0; b<window_size; b++){
				variance1 += Math.pow((shiftedArray[b] - mean1), 2);
				variance2 += Math.pow((shiftedArray[window_size+b] - mean2), 2);
			}
			// not doing this since will need to divide the numerator by this anyways
			// variance1 /= window_size;
			// variance2 /= window_size;

			numerator = 0;
			denominator = 0;
			// now calculate the numerator and denominator
			for(int c=0; c<window_size; c++) {
				numerator += (shiftedArray[c]-mean1) * (shiftedArray[window_size+c]-mean2);
			};
			denominator = Math.sqrt(variance1)*Math.sqrt(variance2);
			//System.out.println("numerator = "+ numerator + ", denominator = " + denominator);
			tempCorrelation = (numerator/denominator);
			//System.out.println("window size = " + window_size + " AutoCorrelation = " + tempCorrelation);
			if (tempCorrelation > maxCorrelation) {
				maxCorrelation = tempCorrelation;
			}
		}
		return maxCorrelation;
	}

	/**
	 * Called when the magnetic sensor has changed.
	 *
	 * @param timestamp
	 *            Timestamp of this sensor event
	 * @param x
	 *            Magnetic x value (microTesla)
	 * @param y
	 *            Magnetic y value (microTesla)
	 * @param z
	 *            Magnetic z value (microTesla)
	 * @param accuracy
	 *            Accuracy of the sensor data (you can ignore this)
	 */
	public void onMagneticSensorChanged(long timestamp, float x, float y, float z, int accuracy) {
	}

	/**
	 * Called when the gyroscope sensor has changed.
	 *
	 * @param timestamp
	 *            Timestamp of this sensor event
	 * @param x
	 *            Gyroscope x value (rad/sec)
	 * @param y
	 *            Gyroscope y value (rad/sec)
	 * @param z
	 *            Gyroscope z value (rad/sec)
	 * @param accuracy
	 *            Accuracy of the sensor data (you can ignore this)
	 */
	public void onGyroscopeSensorChanged(long timestamp, float x, float y, float z, int accuracy) {
	}

	/**
	 * Called when the rotation vector sensor has changed.
	 *
	 * @param timestamp
	 *            Timestamp of this sensor event
	 * @param x
	 *            Rotation vector x value (unitless)
	 * @param y
	 *            Rotation vector y value (unitless)
	 * @param z
	 *            Rotation vector z value (unitless)
	 * @param scalar
	 *            Rotation vector scalar value (unitless)
	 * @param accuracy
	 *            Accuracy of the sensor data (you can ignore this)
	 */
	public void onRotationVectorSensorChanged(long timestamp, float x, float y, float z, float scalar, int accuracy) {
	}

	/**
	 * Called when the barometer sensor has changed.
	 *
	 * @param timestamp
	 *            Timestamp of this sensor event
	 * @param pressure
	 *            Barometer pressure value (millibar)
	 * @param altitude
	 *            Barometer altitude value w.r.t. standard sea level reference
	 *            (meters)
	 * @param accuracy
	 *            Accuracy of the sensor data (you can ignore this)
	 */
	public void onBarometerSensorChanged(long timestamp, float pressure, float altitude, int accuracy) {
		baroIndex = baroIndex % BAROMETER_BUFFER_SIZE;
		barometerFilter[baroIndex] = altitude;
		medianHeight = getMedian(barometerFilter);
		baroIndex++;

		if(isUnderground){
			if(medianHeight > OVER_THRESH){
				isUnderground = false;
			}
		}else{
			if(medianHeight < UNDER_THRESH)
				isUnderground = true;
		}
		//if (medianHeight < HEIGHT_THRESHOLD) {
		//	isUnderground = true;
		//} else {
		//	isUnderground = false;
		//}
	}

	/**
	 * Called when the light sensor has changed.
	 *
	 * @param timestamp
	 *            Timestamp of this sensor event
	 * @param light
	 *            Light value (lux)
	 * @param accuracy
	 *            Accuracy of the sensor data (you can ignore this)
	 */
	public void onLightSensorChanged(long timestamp, float light, int accuracy) {
		lightIndex = lightIndex % LIGHT_BUFF_SIZE;
		lightSensorFilter[lightIndex] = light;
		lightSensorClean = getMedian(lightSensorFilter);
		lightIndex++;

		if (!isInPocket && isUserOutside && (lightSensorClean < LIGHT_THRESHOLD_INDOOR)) {
			isUserOutside = false;
		} else if (!isUserOutside && (lightSensorClean > LIGHT_THRESHOLD_OUTDOOR)) {
			isUserOutside = true;
		}
	}

	/**
	 * Called when the proximity sensor has changed.
	 *
	 * @param timestamp
	 *            Timestamp of this sensor event
	 * @param proximity
	 *            Proximity value (cm)
	 * @param accuracy
	 *            Accuracy of the sensor data (you can ignore this)
	 */
	public void onProximitySensorChanged(long timestamp, float proximity, int accuracy) {
		if(proximity > 0.0){
			isInPocket = false;
		}else{
			isInPocket = true;
		}
	}

	/**
	 * Called when the location sensor has changed.
	 *
	 * @param timestamp
	 *            Timestamp of this location event
	 * @param provider
	 *            "gps" or "network"
	 * @param latitude
	 *            Latitude (deg)
	 * @param longitude
	 *            Longitude (deg)
	 * @param accuracy
	 *            Accuracy of the location data (you may use this) (meters)
	 * @param altitude
	 *            Altitude (meters) (may be -1 if unavailable)
	 * @param bearing
	 *            Bearing (deg) (may be -1 if unavailable)
	 * @param speed
	 *            Speed (m/sec) (may be -1 if unavailable)
	 */
	public void onLocationSensorChanged(long timestamp, String provider, double latitude, double longitude,
			float accuracy, double altitude, float bearing, float speed) {

		if (provider.equalsIgnoreCase("gps")) {
			isLocationGPS = true;

			locationIndex++;
			locationIndex = locationIndex % LOCATION_BUFF_SIZE;

			locationBuffer[locationIndex][0] = latitude;
			locationBuffer[locationIndex][1] = longitude;
			locationBuffer[locationIndex][2] = (double)timestamp;

			// latStdDev = getStdDevLocation(0); //locationIndex = 0 for latitude, 1 for longitude
			// longStdDev = getStdDevLocation(1);
			int prevIndex = (locationIndex-1)%LOCATION_BUFF_SIZE;
			if(prevIndex < 0){
				prevIndex += LOCATION_BUFF_SIZE;
			}

			if(speed >= 0){
				userSpeed = speed;
			}
			// else{
			//   double userSpeedCoarse = 0;
			//   userSpeedCoarse = distance_on_geoid(locationBuffer[locationIndex][0], locationBuffer[locationIndex][1], locationBuffer[prevIndex][0], locationBuffer[prevIndex][1]);
			//   userSpeedCoarse = userSpeedCoarse / (locationBuffer[locationIndex][2] - locationBuffer[prevIndex][2]) * 1000;

			//   userSpeed = userSpeedCoarse;
			// }

			if(userSpeed > SPEED_THRESHOLD){
				isMoving = true;
			}else{
				isMoving = false;
			}

			if(locationIndex > 1){
				calculateGPS = true;
			}

		} else {
			isLocationGPS = false;
		}

	}

	private double distance_on_geoid(double lat1, double lon1, double lat2, double lon2) {

		// Convert degrees to radians
		lat1 = lat1 * Math.PI / 180.0;
		lon1 = lon1 * Math.PI / 180.0;

		lat2 = lat2 * Math.PI / 180.0;
		lon2 = lon2 * Math.PI / 180.0;

		// radius of earth in metres
		double r = 6378100;

		// P
		double rho1 = r * Math.cos(lat1);
		double z1 = r * Math.sin(lat1);
		double x1 = rho1 * Math.cos(lon1);
		double y1 = rho1 * Math.sin(lon1);

		// Q
		double rho2 = r * Math.cos(lat2);
		double z2 = r * Math.sin(lat2);
		double x2 = rho2 * Math.cos(lon2);
		double y2 = rho2 * Math.sin(lon2);

		// Dot product
		double dot = (x1 * x2 + y1 * y2 + z1 * z2);
		double cos_theta = dot / (r * r);

		double theta = Math.acos(cos_theta);

		// Distance in Metres
		return r * theta;
	}

	private double getMedian(double in_array[]) {
		Arrays.sort(in_array);
		int size = in_array.length;
		if (size % 2 == 0) {
			return (in_array[size / 2] + in_array[(size / 2) + 1]) / 2;
		} else {
			return in_array[(size + 1) / 2];
		}
	}

	private void changeActivity(){
		// System.out.println("walking SD: " + walkSdev);
		switch (currentActivity) {
			// can change to walk or vehicle
			case IDLE_INDOOR:
			case IDLE_OUTDOOR:
				if (isLocationGPS) {
					if (isMoving) {
						currentActivity = UserActivities.BUS;
						break;
					}
				} else {
					if (isUnderground) {
						currentActivity = UserActivities.BUS;
						break;
					}
				}
				if (isWalking) {
					currentActivity = UserActivities.WALKING;
				}

				break;

				// can change to idle or vehicle
			case WALKING:
				if (isLocationGPS) {
					if (isMoving) {
						currentActivity = UserActivities.BUS;
						break;
					}
				} else {
					if (isUnderground) {
						currentActivity = UserActivities.BUS;
						break;
					}
				}
				if (!isWalking) {
					if (isUserOutside) {
						currentActivity = UserActivities.IDLE_OUTDOOR;
					} else {
						currentActivity = UserActivities.IDLE_INDOOR;
					}
				}
				break;

				// can change to walking;
			case BUS:
			case TRAIN:
			case CAR:
				if (isLocationGPS) {
					if (!isMoving && isWalking) {
						currentActivity = UserActivities.WALKING;
					}
				} else {
					if (isWalking) {
						currentActivity = UserActivities.WALKING;
					}
				}
				break;

			default:
				break;
		}
		// System.out.println(currentActivity);
		ActivitySimulator.outputDetectedActivity(currentActivity);
	}

	/**
	 * Helper method to convert UNIX millis time into a human-readable string.
	 */
	private static String convertUnixTimeToReadableString(long millisec) {
		return sdf.format(new Date(millisec));
	}

	private Runnable task = new Runnable() {
		public void run() {
			// Logging to the DDMS (in the simulator, the DDMS log is to the
			// console)

			// System.out.println();
			// Log.i("ActivitySim", "Timer " + numberTimers + ": Current simulator time: "
			//     + convertUnixTimeToReadableString(ActivitySimulator.currentTimeMillis()));
			// System.out.println("Timer " + numberTimers + ": Current simulator time: "
			//     + convertUnixTimeToReadableString(ActivitySimulator.currentTimeMillis()));

			// Dummy example of outputting a detected activity
			// (to the file "DetectedActivities.txt" in the trace folder).
			// (here we just alternate between indoor and walking every 10 min)

			// wait till first two GPS readings
			if(!initDone && calculateGPS){
				if (isMoving) { // if speed > to check for vehicle
					currentActivity = UserActivities.BUS;
				}else if(isWalking){// check for walking
					currentActivity = UserActivities.WALKING;
				}else if(isUserOutside){// check proximity sensor for pocket and light sensor values
					currentActivity = UserActivities.IDLE_OUTDOOR;
				}else{
					currentActivity = UserActivities.IDLE_INDOOR;
				}
				initDone = true;
				//System.out.println(currentActivity);
				ActivitySimulator.outputDetectedActivity(currentActivity);
			}else if(initDone){
				changeActivity();
			}
			++numberTimers;
			if (numberTimers <= 8100) {
				SimulatorTimer timer = new SimulatorTimer();
				timer.schedule(task, // Task to be executed
						1 * 1000); // Delay in millisec (10 min)
			}
		}
	};
	}
