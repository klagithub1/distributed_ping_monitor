package dsms.client.corba.DSMSApp;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.Streamable;

/**
* dsms/client/corba/DSMSApp/DSMSHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from dsms/client/corba/DSMS.idl
* Tuesday, August 9, 2016 2:17:05 AM EDT
*/

public final class DSMSHolder implements Streamable
{
  public DSMS value = null;

  public DSMSHolder ()
  {
  }

  public DSMSHolder (DSMS initialValue)
  {
    value = initialValue;
  }

  public void _read (InputStream i)
  {
    value = DSMSHelper.read (i);
  }

  public void _write (OutputStream o)
  {
    DSMSHelper.write (o, value);
  }

  public TypeCode _type ()
  {
    return DSMSHelper.type ();
  }

}