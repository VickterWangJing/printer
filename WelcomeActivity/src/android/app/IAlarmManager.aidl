package android.app;

/**
 * System private API for talking with the alarm manager service.
 *
 */
interface IAlarmManager {
	void HwToSystemClock();
	void SystemClockToHw(); 
}