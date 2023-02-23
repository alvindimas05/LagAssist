package com.entryrise.lagassist.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

public class MathUtils {

	public static int toMegaByte(long bytes) {
		return (int) (bytes / 1048576);
	}

	public static boolean isInt(String str) {
		try {
			int d = Integer.parseInt(str);
			d = d + 1;
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
	public static byte[] integersToBytes(List<Integer> values, int length) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream wrt = new DataOutputStream(baos);

		try {
			for (int i : values) {
				wrt.writeInt(i);
			}
			wrt.flush();
			wrt.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] good = new byte[length];

		System.arraycopy(baos.toByteArray(), 0, good, 0, length);

		return good;
	}

	public static int[] bytesToIntegers(byte[] raw) {

		int rst = raw.length % 4;
		byte[] bts = new byte[raw.length + 4 - rst];

		System.arraycopy(raw, 0, bts, 0, raw.length);

		IntBuffer intBuf = ByteBuffer.wrap(bts).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		int[] pixels = new int[intBuf.remaining()];
		intBuf.get(pixels);
		
		return pixels;

	}


}
