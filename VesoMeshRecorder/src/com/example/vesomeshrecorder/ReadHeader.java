package com.example.vesomeshrecorder;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class ReadHeader 
{

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static int channels(String filename)  
	{
		InputStream fis;
		String s;
		int channels=0;
		try {
			fis = new FileInputStream(filename);
			
				s = readString(fis);
				int pkglen = readLEint(fis);

			 	s = readString(fis);


			 	s = readString(fis);

			 	int pcm = readLEint(fis);

			 	int lin = readLEshort(fis);

				 channels = readLEshort(fis);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			
		return channels;
	}

	private static String readString(InputStream fis) throws IOException 
	{
		byte[] buf = new byte[4];
		int bytesread = fis.read(buf, 0, 4);
		if (bytesread != 4) throw new IOException();
		return new String(buf);
	}

	private static int readLEint(InputStream fis) throws IOException 
	{
		byte[] buf = new byte[4];
		int bytesread = fis.read(buf, 0, 4);
		if (bytesread != 4) throw new IOException();
		return ( ((buf[3] & 0xff) << 24) |
			     ((buf[2] & 0xff) << 16) |
			     ((buf[1] & 0xff) <<  8) |
			     ((buf[0] & 0xff)      ) );
	}
private static int readLEshort(InputStream fis) throws IOException
{
	byte[] buf = new byte[2];
	int bytesread = fis.read(buf, 0, 2);
	if (bytesread != 2) throw new IOException();
	return ( ((buf[1] & 0xff) <<  8) |
		     ((buf[0] & 0xff)      ) );
}

}