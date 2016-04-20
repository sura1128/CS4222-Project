Activity Tracking Android app (ActivityTrackApp) readme:

Porting your code to this Android app is simple. You basically just need 
to copy paste your ActivityDetection.java file to the app, and recompile 
it, and try running on a phone. If you face any problems, please email me.

This is a slightly modified version of SensorLogApp, which additionally 
displays the most recent 10 detected activities (with timestamp) from 
your algorithm. By default, the algorithm in the code does nothing. 

Steps to port your code: 
 1. Try compiling the app (just to check if your build system is OK with
    the specified android target version, etc).
 2. Clean the project (IMPORTANT!!).
 3. Copy paste your ActivityDetection.java file, UserActivites.java file,
    and any other source files and libraries you may have added, from
    your simulator code (ActivitySim) to the Android app's code (ActivityTrackApp),
    overwriting the dummy algorithm and activity enumeration.
 4. Recompile the android app.
 5. Try running the apk on your phone, and see if your algorithm seems
    to detect your activities in real-time :-)
NOTE: If your code is too slow, or you have done too many calculations,
 you may notice that the GUI is laggy. See if you can do the calculations
 less frequently, or reduce the calculation, to make the GUI more responsive. I 
 tested a couple of groups' stage 1 code on the phone, and they seem to 
 work fine.
