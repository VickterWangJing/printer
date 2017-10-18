/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\git\\WelcomeActivity\\src\\android\\app\\IAlarmManager.aidl
 */
package android.app;
/**
 * System private API for talking with the alarm manager service.
 *
 */
public interface IAlarmManager extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements android.app.IAlarmManager
{
private static final java.lang.String DESCRIPTOR = "android.app.IAlarmManager";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an android.app.IAlarmManager interface,
 * generating a proxy if needed.
 */
public static android.app.IAlarmManager asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof android.app.IAlarmManager))) {
return ((android.app.IAlarmManager)iin);
}
return new android.app.IAlarmManager.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_HwToSystemClock:
{
data.enforceInterface(DESCRIPTOR);
this.HwToSystemClock();
reply.writeNoException();
return true;
}
case TRANSACTION_SystemClockToHw:
{
data.enforceInterface(DESCRIPTOR);
this.SystemClockToHw();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements android.app.IAlarmManager
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void HwToSystemClock() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_HwToSystemClock, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void SystemClockToHw() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_SystemClockToHw, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_HwToSystemClock = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_SystemClockToHw = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void HwToSystemClock() throws android.os.RemoteException;
public void SystemClockToHw() throws android.os.RemoteException;
}
