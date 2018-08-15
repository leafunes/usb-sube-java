package org.ungs.usb;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.usb4java.BufferUtils;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

/**
 * Hello world!
 *
 */
public class App 
{
	public static byte INTERFACE = 0;
	public static byte ENDPOINT_OUT = 0;
	public static byte ENDPOINT_IN = 1;
	public static int TIMEOUT = 1000;
	
	
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        Context context = new Context();
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb.", result);
        
       
        DeviceHandle handle = LibUsb.openDeviceWithVidPid(context, (short)11885, (short)1);
        if (handle == null) throw new LibUsbException("Unable to open USB device", result);
        
        result = LibUsb.claimInterface(handle, INTERFACE);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to claim interface", result);
        
        ByteBuffer b = read(handle, 64);

        LibUsb.exit(context);
        
        for(int i = 0; i < b.capacity(); i++) {
        	System.out.println(b.get(i));
        }
        
    }
    
    public static ByteBuffer read(DeviceHandle handle, int size)
    {
        ByteBuffer buffer = BufferUtils.allocateByteBuffer(size).order(
            ByteOrder.LITTLE_ENDIAN);
        IntBuffer transferred = BufferUtils.allocateIntBuffer();
        int result = LibUsb.bulkTransfer(handle, ENDPOINT_IN, buffer,
            transferred, TIMEOUT);
        if (result != LibUsb.SUCCESS)
        {
            throw new LibUsbException("Unable to read data", result);
        }
        System.out.println(transferred.get() + " bytes read from device");
        return buffer;
    }
    
    
    public static Device findDevice(short vendorId, short productId)
    {
        // Read the USB device list
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0) throw new LibUsbException("Unable to get device list", result);

        try
        {
            // Iterate over all devices and scan for the right one
            for (Device device: list)
            {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device descriptor", result);
                if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) return device;
            }
        }
        finally
        {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        // Device not found
        return null;
    }
    

    public static void listDevices()
    {
        // Read the USB device list
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0) throw new LibUsbException("Unable to get device list", result);

        try
        {
            // Iterate over all devices and scan for the right one
            for (Device device: list)
            {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                System.out.println(descriptor.idVendor() + "-" + descriptor.idProduct());
            }
        }
        finally
        {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }
    }
}
