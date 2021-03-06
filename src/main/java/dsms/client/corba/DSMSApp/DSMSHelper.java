package dsms.client.corba.DSMSApp;


import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CORBA.portable.OutputStream;

/**
* dsms/client/corba/DSMSApp/DSMSHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from dsms/client/corba/DSMS.idl
* Tuesday, August 9, 2016 2:17:05 AM EDT
*/

abstract public class DSMSHelper
{
  private static String  _id = "IDL:DSMSApp/DSMS:1.0";

  public static void insert (Any a, DSMS that)
  {
    OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static DSMS extract (Any a)
  {
    return read (a.create_input_stream ());
  }

  private static TypeCode __typeCode = null;
  synchronized public static TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = ORB.init ().create_interface_tc (DSMSHelper.id (), "DSMS");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static DSMS read (InputStream istream)
  {
    return narrow (istream.read_Object (_DSMSStub.class));
  }

  public static void write (OutputStream ostream, DSMS value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static DSMS narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof DSMS)
      return (DSMS)obj;
    else if (!obj._is_a (id ()))
      throw new BAD_PARAM ();
    else
    {
      Delegate delegate = ((ObjectImpl)obj)._get_delegate ();
      _DSMSStub stub = new _DSMSStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static DSMS unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof DSMS)
      return (DSMS)obj;
    else
    {
      Delegate delegate = ((ObjectImpl)obj)._get_delegate ();
      _DSMSStub stub = new _DSMSStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
